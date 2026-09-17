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
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration.ArrangePathFlag;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration.PathFlag;

import java.util.ArrayList;
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

        if (updateTaskState(flags, adminReviewFlag(), taskState.getAdminTaskCreated(),
                taskState::setAdminTaskCreated)) {
            taskTypesToComplete.add(TASK_TYPE_REVIEW_SUPPORT_ADMIN);
        }
        if (updateTaskState(flags, legalOfficerReviewFlag(),
                taskState.getLegalOfficerTaskCreated(),
                taskState::setLegalOfficerTaskCreated)) {
            taskTypesToComplete.add(TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER);
        }
        if (updateTaskState(flags, judgeReviewFlag(), taskState.getJudgeTaskCreated(),
                taskState::setJudgeTaskCreated)) {
            taskTypesToComplete.add(TASK_TYPE_REVIEW_SUPPORT_JUDGE);
        }
        return taskTypesToComplete;
    }

    public void retainReviewTasksForSubmittedCallback(CaseData caseData, Set<String> taskTypes) {
        SupportTaskState state = taskState(caseData);
        if (taskTypes.contains(TASK_TYPE_REVIEW_SUPPORT_ADMIN)) {
            state.setAdminTaskCreated(YES);
        }
        if (taskTypes.contains(TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER)) {
            state.setLegalOfficerTaskCreated(YES);
        }
        if (taskTypes.contains(TASK_TYPE_REVIEW_SUPPORT_JUDGE)) {
            state.setJudgeTaskCreated(YES);
        }
    }

    public Set<String> reviewTaskTypesToClose(CaseData caseData) {
        SupportTaskState state = caseData.getSupportTaskState();
        if (state == null) {
            return Set.of();
        }

        List<FlagDetailType> flags = allFlagItems(caseData.getAllPartyFlags())
                .map(GenericTypeItem::getValue)
                .toList();
        Set<String> taskTypes = new LinkedHashSet<>();
        addTaskTypeToClose(flags, adminReviewFlag(), state.getAdminTaskCreated(),
                TASK_TYPE_REVIEW_SUPPORT_ADMIN, taskTypes);
        addTaskTypeToClose(flags, legalOfficerReviewFlag(), state.getLegalOfficerTaskCreated(),
                TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER, taskTypes);
        addTaskTypeToClose(flags, judgeReviewFlag(), state.getJudgeTaskCreated(),
                TASK_TYPE_REVIEW_SUPPORT_JUDGE, taskTypes);
        return taskTypes;
    }

    private static void addTaskTypeToClose(List<FlagDetailType> flags,
                                           Predicate<FlagDetailType> eligibleFlag,
                                           String taskCreated,
                                           String taskType,
                                           Set<String> taskTypes) {
        if (isTaskCreated(taskCreated) && !hasRequestedFlag(flags, eligibleFlag)) {
            taskTypes.add(taskType);
        }
    }

    public void prepareReviewSupportRequest(CaseData caseData, String eventId) {
        Predicate<FlagDetailType> eligibleFlag = reviewFlag(eventId);
        ListTypeItem<CaseFlagsType> selectedFlags = allPartyFlagSections(caseData.getAllPartyFlags())
                .map(flags -> requestedFlags(flags, eligibleFlag))
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

    private Predicate<FlagDetailType> reviewFlag(String eventId) {
        return switch (eventId) {
            case EVENT_REVIEW_ADMIN_SUPPORT_REQUEST -> adminReviewFlag();
            case EVENT_REVIEW_LEGAL_OFFICER_SUPPORT_REQUEST -> legalOfficerReviewFlag();
            case EVENT_REVIEW_JUDGE_SUPPORT_REQUEST -> judgeReviewFlag();
            default -> flag -> false;
        };
    }

    private static CaseFlagsType requestedFlags(CaseFlagsType flags, Predicate<FlagDetailType> eligibleFlag) {
        ListTypeItem<FlagDetailType> details = flags.getDetails() == null
                ? new ListTypeItem<>()
                : flags.getDetails().stream()
                        .filter(item -> item != null && item.getValue() != null)
                        .filter(item -> isRequested(item.getValue()))
                        .filter(item -> eligibleFlag.test(item.getValue()))
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
                && hasRequestedFlag(flags, adminReviewFlag())) {
            taskState.setAdminTaskCreated(YES);
            taskState.setAdminTaskRequired(YES);
        }
        if (isTaskNotCreated(taskState.getLegalOfficerTaskCreated())
                && hasRequestedFlag(flags, legalOfficerReviewFlag())) {
            taskState.setLegalOfficerTaskCreated(YES);
            taskState.setLegalOfficerTaskRequired(YES);
        }
        if (isTaskNotCreated(taskState.getJudgeTaskCreated())
                && hasRequestedFlag(flags, judgeReviewFlag())) {
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

    private static boolean hasRequestedFlag(List<FlagDetailType> flags, Predicate<FlagDetailType> eligibleFlag) {
        return flags.stream().anyMatch(flag -> isRequested(flag) && eligibleFlag.test(flag));
    }

    private static boolean updateTaskState(List<FlagDetailType> flags,
                                           Predicate<FlagDetailType> eligibleFlag,
                                           String taskCreated,
                                           Consumer<String> taskCreatedSetter) {
        boolean categoryHasFlags = flags.stream().anyMatch(eligibleFlag);
        if (!hasRequestedFlag(flags, eligibleFlag)
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
        arrangeSupportTasks(caseData, caseDataBefore).stream()
                .findFirst()
                .map(ArrangeSupportTask::taskName)
                .ifPresent(taskState::setArrangeSupportTaskName);
    }

    public List<ArrangeSupportTask> additionalArrangeSupportTasks(CaseData caseData, CaseData caseDataBefore) {
        return arrangeSupportTasks(caseData, caseDataBefore).stream().skip(1).toList();
    }

    private List<ArrangeSupportTask> arrangeSupportTasks(CaseData caseData, CaseData caseDataBefore) {
        Map<String, String> flagTitles = configuration.getArrange().getFlagTitles();
        if (flagTitles.isEmpty() && configuration.getArrange().getPathFlags().isEmpty()) {
            return List.of();
        }

        List<GenericTypeItem<FlagDetailType>> previousFlags = allFlagItems(
                caseDataBefore == null ? null : caseDataBefore.getAllPartyFlags()).toList();
        List<GenericTypeItem<FlagDetailType>> currentFlags = allFlagItems(caseData.getAllPartyFlags()).toList();
        List<ArrangeSupportTask> tasks = new ArrayList<>();
        for (int index = 0; index < currentFlags.size(); index++) {
            GenericTypeItem<FlagDetailType> item = currentFlags.get(index);
            if (!FLAG_STATUS_ACTIVE.equals(item.getValue().getStatus())
                    || !wasNotPreviouslyActive(item, previousFlags)) {
                continue;
            }
            int flagIndex = index;
            arrangeTaskName(item.getValue()).ifPresent(taskName -> tasks.add(new ArrangeSupportTask(
                    item.getId() == null ? "flag-" + flagIndex : item.getId(), taskName)));
        }
        return tasks;
    }

    public void prepareNewFlagArrangeSupportTask(CaseData caseData, CaseData caseDataBefore) {
        prepareArrangeSupportTask(caseData, caseDataBefore);
        if (caseData.getSupportTaskState().getArrangeSupportTaskName() != null) {
            return;
        }

        latestCreatedFlag(caseData)
                .filter(flag -> FLAG_STATUS_ACTIVE.equals(flag.getStatus()))
                .flatMap(this::arrangeTaskName)
                .ifPresent(caseData.getSupportTaskState()::setArrangeSupportTaskName);
    }

    private Predicate<FlagDetailType> adminReviewFlag() {
        return eligibleFlag(configuration.getReview().getAdminFlagCodes(),
                configuration.getReview().getAdminPathFlags());
    }

    public record ArrangeSupportTask(String flagId, String taskName) {
    }

    private Predicate<FlagDetailType> legalOfficerReviewFlag() {
        return eligibleFlag(configuration.getReview().getLegalOfficerFlagCodes(),
                configuration.getReview().getLegalOfficerPathFlags());
    }

    private Predicate<FlagDetailType> judgeReviewFlag() {
        return eligibleFlag(configuration.getReview().getJudgeFlagCodes(),
                configuration.getReview().getJudgePathFlags());
    }

    private static Predicate<FlagDetailType> eligibleFlag(Set<String> flagCodes, List<PathFlag> pathFlags) {
        return flag -> flagCodes.contains(flag.getFlagCode())
                || pathFlags.stream().anyMatch(pathFlag -> matchesPath(flag, pathFlag));
    }

    private Optional<String> arrangeTaskName(FlagDetailType flag) {
        String title = configuration.getArrange().getFlagTitles().get(flag.getFlagCode());
        if (title != null) {
            return Optional.of(title);
        }
        return configuration.getArrange().getPathFlags().stream()
                .filter(pathFlag -> matchesPath(flag, pathFlag))
                .map(ArrangePathFlag::getTaskTitle)
                .findFirst();
    }

    private static boolean matchesPath(FlagDetailType flag, PathFlag pathFlag) {
        return Objects.equals(flag.getFlagCode(), pathFlag.getFlagCode())
                && hasPath(flag, pathFlag.getPath());
    }

    private static boolean matchesPath(FlagDetailType flag, ArrangePathFlag pathFlag) {
        return Objects.equals(flag.getFlagCode(), pathFlag.getFlagCode())
                && hasPath(flag, pathFlag.getPath());
    }

    private static boolean hasPath(FlagDetailType flag, List<String> expectedPath) {
        if (flag.getPath() == null) {
            return false;
        }
        List<String> actualPath = flag.getPath().stream()
                .map(item -> item == null ? null : item.getValue())
                .toList();
        return expectedPath.equals(actualPath);
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
