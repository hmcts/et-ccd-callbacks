package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.SupportTaskState;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.FLAG_STATUS_ACTIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.FLAG_STATUS_REQUESTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.PartyCaseFlagUtils.allFlagItems;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.PartyCaseFlagUtils.flagDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.PartyCaseFlagUtils.respondentFlagDetails;

@Service
@RequiredArgsConstructor
public class SupportTaskService {
    private final SupportTaskConfiguration configuration;

    public void prepareReviewSupportTasks(CaseData caseData) {
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        prepareTasks(caseData, allPartyFlags == null
                ? List.of()
                : flagDetails(allPartyFlags.getClaimantFlags(), allPartyFlags.getClaimantExternalFlags()).toList());
    }

    public void prepareRespondentReviewSupportTasks(CaseData caseData) {
        prepareTasks(caseData, respondentFlagDetails(caseData.getAllPartyFlags()).toList());
    }

    public void prepareNewFlagReviewSupportTasks(CaseData caseData, CaseData caseDataBefore) {
        List<GenericTypeItem<FlagDetailType>> previousFlags = allFlagItems(
                caseDataBefore == null ? null : caseDataBefore.getAllPartyFlags()).toList();
        Set<String> previousFlagIds = previousFlags.stream()
                .map(GenericTypeItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Predicate<GenericTypeItem<FlagDetailType>> isNewFlag = item -> item.getId() == null
                ? previousFlags.stream().noneMatch(previous -> previous.getValue().equals(item.getValue()))
                : !previousFlagIds.contains(item.getId());
        List<FlagDetailType> newFlags = allFlagItems(caseData.getAllPartyFlags())
                .filter(isNewFlag)
                .map(GenericTypeItem::getValue)
                .toList();

        prepareTasks(caseData, newFlags);
    }

    public void prepareManagedReviewSupportTasks(CaseData caseData) {
        SupportTaskState taskState = taskState(caseData);
        List<FlagDetailType> flags = allFlagItems(caseData.getAllPartyFlags())
                .map(GenericTypeItem::getValue)
                .toList();

        updateTaskState(flags, configuration.getReview().getAdminFlagCodes(),
                taskState.getAdminTaskCreated(),
                taskState::setAdminTaskCreated,
                taskState::setAdminTaskRequired);
        updateTaskState(flags, configuration.getReview().getLegalOfficerFlagCodes(),
                taskState.getLegalOfficerTaskCreated(),
                taskState::setLegalOfficerTaskCreated,
                taskState::setLegalOfficerTaskRequired);
        updateTaskState(flags, configuration.getReview().getJudgeFlagCodes(),
                taskState.getJudgeTaskCreated(),
                taskState::setJudgeTaskCreated,
                taskState::setJudgeTaskRequired);
    }

    private void prepareTasks(CaseData caseData, List<FlagDetailType> flags) {
        SupportTaskState taskState = taskState(caseData);
        taskState.setAdminTaskRequired(null);
        taskState.setLegalOfficerTaskRequired(null);
        taskState.setJudgeTaskRequired(null);

        if (!YES.equals(taskState.getAdminTaskCreated())
                && hasRequestedFlag(flags, configuration.getReview().getAdminFlagCodes())) {
            taskState.setAdminTaskCreated(YES);
            taskState.setAdminTaskRequired(YES);
        }
        if (!YES.equals(taskState.getLegalOfficerTaskCreated())
                && hasRequestedFlag(flags, configuration.getReview().getLegalOfficerFlagCodes())) {
            taskState.setLegalOfficerTaskCreated(YES);
            taskState.setLegalOfficerTaskRequired(YES);
        }
        if (!YES.equals(taskState.getJudgeTaskCreated())
                && hasRequestedFlag(flags, configuration.getReview().getJudgeFlagCodes())) {
            taskState.setJudgeTaskCreated(YES);
            taskState.setJudgeTaskRequired(YES);
        }
    }

    private static SupportTaskState taskState(CaseData caseData) {
        if (caseData.getSupportTaskState() == null) {
            caseData.setSupportTaskState(new SupportTaskState());
        }
        return caseData.getSupportTaskState();
    }

    private static boolean hasRequestedFlag(List<FlagDetailType> flags, Set<String> eligibleFlagCodes) {
        return flags.stream().anyMatch(flag -> isRequested(flag) && eligibleFlagCodes.contains(flag.getFlagCode()));
    }

    private static void updateTaskState(List<FlagDetailType> flags,
                                        Set<String> eligibleFlagCodes,
                                        String taskCreated,
                                        Consumer<String> taskCreatedSetter,
                                        Consumer<String> taskRequiredSetter) {
        taskRequiredSetter.accept(null);
        if (YES.equals(taskCreated) && !hasRequestedFlag(flags, eligibleFlagCodes)) {
            taskCreatedSetter.accept(null);
            taskRequiredSetter.accept(NO);
        }
    }

    private static boolean isRequested(FlagDetailType flag) {
        return FLAG_STATUS_REQUESTED.equals(flag.getStatus());
    }

    public void prepareArrangeSupportTask(CaseData caseData, CaseData caseDataBefore) {
        SupportTaskState taskState = taskState(caseData);
        taskState.setArrangeSupportTaskName(null);

        Map<String, String> flagTitles = configuration.getArrange().getFlagTitles();
        if (flagTitles.isEmpty()) {
            return;
        }

        List<GenericTypeItem<FlagDetailType>> previousFlags = allFlagItems(
                caseDataBefore == null ? null : caseDataBefore.getAllPartyFlags()).toList();
        allFlagItems(caseData.getAllPartyFlags())
                .filter(item -> FLAG_STATUS_ACTIVE.equals(item.getValue().getStatus()))
                .filter(item -> flagTitles.containsKey(item.getValue().getFlagCode()))
                .filter(item -> wasNotPreviouslyActive(item, previousFlags))
                .findFirst()
                .map(item -> flagTitles.get(item.getValue().getFlagCode()))
                .ifPresent(taskState::setArrangeSupportTaskName);
    }

    private static boolean wasNotPreviouslyActive(GenericTypeItem<FlagDetailType> current,
                                                   List<GenericTypeItem<FlagDetailType>> previousFlags) {
        return previousFlags.stream()
                .filter(previous -> isSameFlag(current, previous))
                .noneMatch(previous -> FLAG_STATUS_ACTIVE.equals(previous.getValue().getStatus()));
    }

    private static boolean isSameFlag(GenericTypeItem<FlagDetailType> first,
                                      GenericTypeItem<FlagDetailType> second) {
        if (first.getId() != null || second.getId() != null) {
            return Objects.equals(first.getId(), second.getId());
        }

        FlagDetailType firstValue = first.getValue();
        FlagDetailType secondValue = second.getValue();
        return Objects.equals(firstValue.getFlagCode(), secondValue.getFlagCode())
                && Objects.equals(firstValue.getName(), secondValue.getName())
                && Objects.equals(firstValue.getSubTypeKey(), secondValue.getSubTypeKey())
                && Objects.equals(firstValue.getSubTypeValue(), secondValue.getSubTypeValue())
                && Objects.equals(firstValue.getDateTimeCreated(), secondValue.getDateTimeCreated())
                && Objects.equals(firstValue.getPath(), secondValue.getPath());
    }

}
