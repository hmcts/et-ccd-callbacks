package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.SupportTaskState;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_ADMIN_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_JUDGE_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_LEGAL_OFFICER_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.FLAG_STATUS_ACTIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.FLAG_STATUS_REQUESTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_JUDGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.PartyCaseFlagUtils.allFlagItems;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.PartyCaseFlagUtils.allPartyFlagSections;
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
        retainCreatedTaskState(caseData, caseDataBefore);
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

        if (newFlags.isEmpty()) {
            newFlags = latestCreatedFlag(caseData).map(List::of).orElseGet(List::of);
        }

        prepareTasks(caseData, newFlags);
    }

    private static void retainCreatedTaskState(CaseData caseData, CaseData caseDataBefore) {
        if (caseDataBefore == null || caseDataBefore.getSupportTaskState() == null) {
            return;
        }

        SupportTaskState currentState = taskState(caseData);
        SupportTaskState previousState = caseDataBefore.getSupportTaskState();
        if (currentState.getAdminTaskCreated() == null) {
            currentState.setAdminTaskCreated(previousState.getAdminTaskCreated());
        }
        if (currentState.getLegalOfficerTaskCreated() == null) {
            currentState.setLegalOfficerTaskCreated(previousState.getLegalOfficerTaskCreated());
        }
        if (currentState.getJudgeTaskCreated() == null) {
            currentState.setJudgeTaskCreated(previousState.getJudgeTaskCreated());
        }
    }

    public Set<String> prepareManagedReviewSupportTasks(CaseData caseData, CaseData caseDataBefore) {
        retainCreatedTaskState(caseData, caseDataBefore);
        SupportTaskState taskState = taskState(caseData);
        taskState.setAdminTaskRequired(null);
        taskState.setLegalOfficerTaskRequired(null);
        taskState.setJudgeTaskRequired(null);
        List<FlagDetailType> flags = allFlagItems(caseData.getAllPartyFlags())
                .map(GenericTypeItem::getValue)
                .toList();
        Set<String> taskTypesToComplete = new LinkedHashSet<>();

        if (updateTaskState(flags, configuration.getReview().getAdminFlagCodes(), taskState.getAdminTaskCreated(),
                taskState::setAdminTaskCreated)) {
            taskTypesToComplete.add(TASK_TYPE_REVIEW_SUPPORT_ADMIN);
        }
        if (updateTaskState(flags, configuration.getReview().getLegalOfficerFlagCodes(),
                taskState.getLegalOfficerTaskCreated(),
                taskState::setLegalOfficerTaskCreated)) {
            taskTypesToComplete.add(TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER);
        }
        if (updateTaskState(flags, configuration.getReview().getJudgeFlagCodes(), taskState.getJudgeTaskCreated(),
                taskState::setJudgeTaskCreated)) {
            taskTypesToComplete.add(TASK_TYPE_REVIEW_SUPPORT_JUDGE);
        }
        return taskTypesToComplete;
    }

    public void prepareReviewSupportRequest(CaseData caseData, String eventId) {
        Set<String> eligibleCodes = reviewFlagCodes(eventId);
        ListTypeItem<CaseFlagsType> selectedFlags = allPartyFlagSections(caseData.getAllPartyFlags())
                .map(flags -> requestedFlags(flags, eligibleCodes))
                .filter(flags -> !flags.getDetails().isEmpty())
                .map(GenericTypeItem::from)
                .collect(Collectors.toCollection(ListTypeItem::new));
        caseData.setReviewSupportRequestFlags(selectedFlags);
    }

    public boolean applyReviewSupportRequest(CaseData caseData) {
        if (caseData.getReviewSupportRequestFlags() == null) {
            return false;
        }

        Map<String, GenericTypeItem<FlagDetailType>> originalFlags = new HashMap<>();
        allFlagItems(caseData.getAllPartyFlags())
                .filter(item -> item.getId() != null)
                .forEach(item -> originalFlags.put(item.getId(), item));

        List<GenericTypeItem<FlagDetailType>> updatedFlags = caseData.getReviewSupportRequestFlags().stream()
                .filter(Objects::nonNull)
                .map(GenericTypeItem::getValue)
                .filter(Objects::nonNull)
                .flatMap(flags -> flags.getDetails() == null ? Stream.empty() : flags.getDetails().stream())
                .filter(item -> item != null && item.getId() != null && item.getValue() != null)
                .filter(item -> !isRequested(item.getValue()))
                .filter(item -> originalFlags.containsKey(item.getId()))
                .toList();
        updatedFlags.forEach(item -> originalFlags.get(item.getId()).setValue(item.getValue()));

        if (!updatedFlags.isEmpty()) {
            caseData.setReviewSupportRequestFlags(null);
        }
        return !updatedFlags.isEmpty();
    }

    private Set<String> reviewFlagCodes(String eventId) {
        return switch (eventId) {
            case EVENT_REVIEW_ADMIN_SUPPORT_REQUEST -> configuration.getReview().getAdminFlagCodes();
            case EVENT_REVIEW_LEGAL_OFFICER_SUPPORT_REQUEST ->
                    configuration.getReview().getLegalOfficerFlagCodes();
            case EVENT_REVIEW_JUDGE_SUPPORT_REQUEST -> configuration.getReview().getJudgeFlagCodes();
            default -> Set.of();
        };
    }

    private static CaseFlagsType requestedFlags(CaseFlagsType flags, Set<String> eligibleCodes) {
        ListTypeItem<FlagDetailType> details = flags.getDetails() == null
                ? new ListTypeItem<>()
                : flags.getDetails().stream()
                        .filter(item -> item != null && item.getValue() != null)
                        .filter(item -> isRequested(item.getValue()))
                        .filter(item -> eligibleCodes.contains(item.getValue().getFlagCode()))
                        .collect(Collectors.toCollection(ListTypeItem::new));
        return CaseFlagsType.builder()
                .partyName(flags.getPartyName())
                .roleOnCase(flags.getRoleOnCase())
                .groupId(flags.getGroupId())
                .visibility(flags.getVisibility())
                .details(details)
                .build();
    }

    private void prepareTasks(CaseData caseData, List<FlagDetailType> flags) {
        SupportTaskState taskState = taskState(caseData);
        taskState.setAdminTaskRequired(null);
        taskState.setLegalOfficerTaskRequired(null);
        taskState.setJudgeTaskRequired(null);

        if (isTaskNotCreated(taskState.getAdminTaskCreated())
                && hasRequestedFlag(flags, configuration.getReview().getAdminFlagCodes())) {
            taskState.setAdminTaskCreated(YES);
            taskState.setAdminTaskRequired(YES);
        }
        if (isTaskNotCreated(taskState.getLegalOfficerTaskCreated())
                && hasRequestedFlag(flags, configuration.getReview().getLegalOfficerFlagCodes())) {
            taskState.setLegalOfficerTaskCreated(YES);
            taskState.setLegalOfficerTaskRequired(YES);
        }
        if (isTaskNotCreated(taskState.getJudgeTaskCreated())
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

    private static boolean updateTaskState(List<FlagDetailType> flags,
                                           Set<String> eligibleFlagCodes,
                                           String taskCreated,
                                           Consumer<String> taskCreatedSetter) {
        boolean categoryHasFlags = flags.stream()
                .map(FlagDetailType::getFlagCode)
                .anyMatch(eligibleFlagCodes::contains);
        if (!hasRequestedFlag(flags, eligibleFlagCodes)
                && (isTaskCreated(taskCreated) || categoryHasFlags)) {
            taskCreatedSetter.accept(NO);
            return true;
        }
        return false;
    }

    private static boolean isTaskCreated(String taskCreated) {
        return YES.equalsIgnoreCase(taskCreated) || Boolean.parseBoolean(taskCreated);
    }

    private static boolean isTaskNotCreated(String taskCreated) {
        return !YES.equalsIgnoreCase(taskCreated)
                && !Boolean.parseBoolean(taskCreated);
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

    public void prepareNewFlagArrangeSupportTask(CaseData caseData, CaseData caseDataBefore) {
        prepareArrangeSupportTask(caseData, caseDataBefore);
        if (caseData.getSupportTaskState().getArrangeSupportTaskName() != null) {
            return;
        }

        Map<String, String> flagTitles = configuration.getArrange().getFlagTitles();
        latestCreatedFlag(caseData)
                .filter(flag -> FLAG_STATUS_ACTIVE.equals(flag.getStatus()))
                .map(FlagDetailType::getFlagCode)
                .map(flagTitles::get)
                .ifPresent(caseData.getSupportTaskState()::setArrangeSupportTaskName);
    }

    private static Optional<FlagDetailType> latestCreatedFlag(CaseData caseData) {
        return allFlagItems(caseData.getAllPartyFlags())
                .map(GenericTypeItem::getValue)
                .filter(flag -> flag.getDateTimeCreated() != null)
                .max(Comparator.comparing(FlagDetailType::getDateTimeCreated));
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
