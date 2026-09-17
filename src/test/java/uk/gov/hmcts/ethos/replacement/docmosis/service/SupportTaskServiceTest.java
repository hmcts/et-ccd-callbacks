package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_ADMIN_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_JUDGE_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_REVIEW_LEGAL_OFFICER_SUPPORT_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_JUDGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER;

class SupportTaskServiceTest {
    private static final String TASK_TYPE_ADMIN = "Admin";
    private static final String TASK_TYPE_LEGAL_OFFICER = "LegalOfficer";
    private static final String TASK_TYPE_JUDGE = "Judge";
    private static final String STATUS_REQUESTED = "Requested";
    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_NOT_APPROVED = "Not Approved";
    private static final String STATUS_INACTIVE = "Inactive";
    private static final String CCD_TRUE = "true";
    private static final String CCD_FALSE = "false";
    private static final String OTHER_FLAG_CODE = "OT0001";
    private static final List<String> HELP_WITH_FORMS_PATH = List.of(
            "Party", "Reasonable adjustment", "I need help with forms");
    private static final List<List<String>> OTHER_REVIEW_PATHS = List.of(
            List.of("Party", "Reasonable adjustment", "I need help communicating and understanding",
                    "Hearing Enhancement System (Hearing", "Induction Loop, Infrared Receiver)"),
            List.of("Party", "Reasonable adjustment", "I need documents in an alternative format"),
            HELP_WITH_FORMS_PATH,
            List.of("Party", "Reasonable adjustment",
                    "I need adjustments to get to, into and around our buildings"),
            List.of("Party", "Reasonable adjustment", "I need to bring support with me to a hearing"),
            List.of("Party", "Reasonable adjustment", "I need something to feel comfortable during my hearing"),
            List.of("Party", "Reasonable adjustment", "I need help communicating and understanding"));

    private final SupportTaskService service = new SupportTaskService(configuration());

    private static SupportTaskConfiguration configuration() {
        SupportTaskConfiguration configuration = new SupportTaskConfiguration();
        configuration.getReview().setAdminFlagCodes(Set.of("RA0021", "RA0033", "RA0039", "RA0041"));
        configuration.getReview().setLegalOfficerFlagCodes(Set.of("RA0034", "RA0035", "RA0036"));
        configuration.getReview().setJudgeFlagCodes(Set.of("RA0029", "RA0031", "RA0032", "RA0037", "RA0038"));
        configuration.getReview().setAdminPathFlags(OTHER_REVIEW_PATHS.stream()
                .map(path -> pathFlag(OTHER_FLAG_CODE, path))
                .toList());
        configuration.getArrange().setFlagTitles(Map.ofEntries(
                Map.entry("RA0017", "Guidance on how to complete forms"),
                Map.entry("RA0018", "Support filling in forms"),
                Map.entry("RA0019", "Step free / wheelchair access"),
                Map.entry("RA0020", "Use of venue wheelchair"),
                Map.entry("RA0021", "Parking space close to the venue"),
                Map.entry("RA0022", "Accessible toilet"),
                Map.entry("RA0024", "A different type of chair"),
                Map.entry("RA0030", "Appropriate lighting"),
                Map.entry("RA0038", "Intermediary"),
                Map.entry("RA0039", "Speech to text reporter (palantypist)"),
                Map.entry("RA0041", "Lip speaker"),
                Map.entry("RA0042", "Sign language interpreter"),
                Map.entry("RA0043", "Hearing loop (hearing enhancement system)"),
                Map.entry("RA0044", "Infrared receiver (hearing enhancement system)"),
                Map.entry("RA0045", "Induction loop (hearing enhancement system)"),
                Map.entry("RA0046", "Visit to court or tribunal before the hearing")
        ));
        ArrangePathFlag arrangePathFlag = new ArrangePathFlag();
        arrangePathFlag.setFlagCode(OTHER_FLAG_CODE);
        arrangePathFlag.setPath(HELP_WITH_FORMS_PATH);
        arrangePathFlag.setTaskTitle("I need help with forms");
        configuration.getArrange().setPathFlags(List.of(arrangePathFlag));
        return configuration;
    }

    private static PathFlag pathFlag(String flagCode, List<String> path) {
        PathFlag pathFlag = new PathFlag();
        pathFlag.setFlagCode(flagCode);
        pathFlag.setPath(path);
        return pathFlag;
    }

    static Stream<Arguments> eligibleFlagScenarios() {
        return Stream.of(
            Stream.of("RA0021", "RA0033", "RA0039", "RA0041")
                .map(code -> Arguments.of(code, TASK_TYPE_ADMIN)),
            Stream.of("RA0034", "RA0035", "RA0036")
                .map(code -> Arguments.of(code, TASK_TYPE_LEGAL_OFFICER)),
            Stream.of("RA0029", "RA0031", "RA0032", "RA0037", "RA0038")
                .map(code -> Arguments.of(code, TASK_TYPE_JUDGE))
        ).flatMap(stream -> stream);
    }

    @ParameterizedTest
    @MethodSource("eligibleFlagScenarios")
    void prepares_each_eligible_task_type_once_per_case(String flagCode, String taskType) {
        CaseData caseData = caseDataWithClaimantFlag(flagCode, STATUS_REQUESTED, false);

        service.prepareReviewSupportTasks(caseData);

        assertEquals(TASK_TYPE_ADMIN.equals(taskType) ? YES : null,
                taskState(caseData).getAdminTaskRequired());
        assertEquals(TASK_TYPE_LEGAL_OFFICER.equals(taskType) ? YES : null,
                taskState(caseData).getLegalOfficerTaskRequired());
        assertEquals(TASK_TYPE_JUDGE.equals(taskType) ? YES : null,
                taskState(caseData).getJudgeTaskRequired());
        service.prepareReviewSupportTasks(caseData);

        assertNull(taskState(caseData).getAdminTaskRequired());
        assertNull(taskState(caseData).getLegalOfficerTaskRequired());
        assertNull(taskState(caseData).getJudgeTaskRequired());
    }

    @Test
    void prepares_task_for_requested_external_claimant_flag() {
        CaseData caseData = caseDataWithClaimantFlag("RA0029", STATUS_REQUESTED, true);

        service.prepareReviewSupportTasks(caseData);

        assertEquals(YES, taskState(caseData).getJudgeTaskCreated());
        assertEquals(YES, taskState(caseData).getJudgeTaskRequired());
    }

    static Stream<Arguments> otherReviewPathScenarios() {
        return OTHER_REVIEW_PATHS.stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("otherReviewPathScenarios")
    void prepares_admin_review_task_for_each_configured_other_flag_path(List<String> path) {
        CaseData caseData = caseDataWithClaimantFlag(OTHER_FLAG_CODE, STATUS_REQUESTED, false, path);

        service.prepareReviewSupportTasks(caseData);

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertEquals(YES, taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void does_not_prepare_review_task_for_other_flag_with_an_unconfigured_or_missing_path() {
        CaseData unconfiguredPath = caseDataWithClaimantFlag(OTHER_FLAG_CODE, STATUS_REQUESTED, false,
                List.of("Party", "Other"));
        CaseData missingPath = caseDataWithClaimantFlag(OTHER_FLAG_CODE, STATUS_REQUESTED, false);

        service.prepareReviewSupportTasks(unconfiguredPath);
        service.prepareReviewSupportTasks(missingPath);

        assertNull(taskState(unconfiguredPath).getAdminTaskCreated());
        assertNull(taskState(missingPath).getAdminTaskCreated());
    }

    @Test
    void keeps_admin_task_active_while_requested_other_flag_has_a_configured_path() {
        CaseData caseData = caseDataWithClaimantFlag(
                OTHER_FLAG_CODE, STATUS_REQUESTED, false, HELP_WITH_FORMS_PATH);
        caseData.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(YES).build());

        Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertTrue(taskTypesToComplete.isEmpty());
    }

    @Test
    void admin_review_event_contains_requested_other_flag_only_for_a_configured_path() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("matching", OTHER_FLAG_CODE, STATUS_REQUESTED, null,
                        HELP_WITH_FORMS_PATH))
                .respondentFlags(caseFlags("not-matching", OTHER_FLAG_CODE, STATUS_REQUESTED, null,
                        List.of("Party", "Other")))
                .build());

        service.prepareReviewSupportRequest(caseData, EVENT_REVIEW_ADMIN_SUPPORT_REQUEST);

        assertEquals(1, caseData.getReviewSupportRequestFlags().size());
        assertEquals("matching", caseData.getReviewSupportRequestFlags().getFirst().getValue()
                .getDetails().getFirst().getId());
    }

    @Test
    void prepares_task_for_requested_flag_from_any_respondent() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .respondent3ExternalFlags(caseFlags("respondent-3-flag", "RA0034", STATUS_REQUESTED))
                .build());

        service.prepareRespondentReviewSupportTasks(caseData);

        assertEquals(YES, taskState(caseData).getLegalOfficerTaskCreated());
        assertEquals(YES, taskState(caseData).getLegalOfficerTaskRequired());
    }

    @Test
    void respondent_event_does_not_use_a_claimant_flag() {
        CaseData caseData = caseDataWithClaimantFlag("RA0033", STATUS_REQUESTED, false);

        service.prepareRespondentReviewSupportTasks(caseData);

        assertNull(taskState(caseData).getAdminTaskCreated());
    }

    @Test
    void prepares_task_only_for_a_newly_created_flag() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("existing-flag", "RA0033", STATUS_REQUESTED))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("existing-flag", "RA0033", STATUS_REQUESTED))
                .respondent2Flags(caseFlags("new-flag", "RA0029", STATUS_REQUESTED))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertNull(taskState(caseData).getAdminTaskCreated());
        assertEquals(YES, taskState(caseData).getJudgeTaskCreated());
        assertEquals(YES, taskState(caseData).getJudgeTaskRequired());
    }

    @Test
    void prepares_task_for_a_new_requested_flag() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .representative2ExternalFlags(caseFlags("new-flag", "RA0033", STATUS_REQUESTED))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, new CaseData());

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertEquals(YES, taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void does_not_prepare_duplicate_judge_task_for_requested_flag_on_another_party() {
        CaseData caseDataBefore = caseDataWithClaimantFlag("RA0038", STATUS_REQUESTED, false);
        caseDataBefore.setSupportTaskState(SupportTaskState.builder().judgeTaskCreated(YES).build());

        CaseData caseData = caseDataWithClaimantFlag("RA0038", STATUS_REQUESTED, false);
        caseData.getAllPartyFlags().setRespondent1Flags(
                caseFlags("respondent-flag", "RA0038", STATUS_REQUESTED));

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertEquals(YES, taskState(caseData).getJudgeTaskCreated());
        assertNull(taskState(caseData).getJudgeTaskRequired());
    }

    @Test
    void prepares_review_task_when_create_flag_before_data_already_contains_the_new_flag() {
        CaseFlagsType requestedFlag = caseFlags("new-flag", "RA0038", STATUS_REQUESTED, "2026-09-11T10:00:00.000Z");
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder().claimantFlags(requestedFlag).build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder().claimantFlags(requestedFlag).build());

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertEquals(YES, taskState(caseData).getJudgeTaskCreated());
        assertEquals(YES, taskState(caseData).getJudgeTaskRequired());
    }

    @Test
    void does_not_treat_an_unchanged_flag_without_an_id_as_new() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags(null, "RA0033", STATUS_REQUESTED))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags(null, "RA0033", STATUS_REQUESTED))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertNull(taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void does_not_treat_an_updated_existing_flag_as_new() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("existing-flag", "RA0033", STATUS_ACTIVE))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("existing-flag", "RA0033", STATUS_REQUESTED))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertNull(taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void does_not_prepare_task_for_a_new_case_level_flag() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .caseFlags(caseFlags("new-case-flag", "RA0033", STATUS_REQUESTED))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, new CaseData());

        assertNull(taskState(caseData).getAdminTaskCreated());
    }

    @Test
    void prepares_tasks_for_new_claimant_and_representative_flags() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantExternalFlags(caseFlags("claimant-flag", "RA0033", STATUS_REQUESTED))
                .claimantRepresentativeFlags(caseFlags("claimant-rep-flag", "RA0034", STATUS_REQUESTED))
                .representative4ExternalFlags(caseFlags("respondent-rep-flag", "RA0029", STATUS_REQUESTED))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, new CaseData());

        assertEquals(YES, taskState(caseData).getAdminTaskRequired());
        assertEquals(YES, taskState(caseData).getLegalOfficerTaskRequired());
        assertEquals(YES, taskState(caseData).getJudgeTaskRequired());
    }

    @Test
    void completes_only_task_type_without_an_eligible_requested_flag() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("admin-flag", "RA0033", STATUS_ACTIVE))
                .respondent2Flags(caseFlags("judge-flag", "RA0029", STATUS_REQUESTED))
                .build());
        caseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(YES)
                .judgeTaskCreated(YES)
                .build());

        final Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(NO, taskState(caseData).getAdminTaskCreated());
        assertEquals(YES, taskState(caseData).getJudgeTaskCreated());
        assertNull(taskState(caseData).getLegalOfficerTaskCreated());
        assertEquals(Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN), taskTypesToComplete);
    }

    @Test
    void keeps_task_active_when_another_party_has_an_eligible_requested_flag() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("managed-flag", "RA0033", STATUS_ACTIVE))
                .representative2ExternalFlags(caseFlags("requested-flag", "RA0033", STATUS_REQUESTED))
                .build());
        caseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(YES)
                .adminTaskRequired(YES)
                .build());

        final Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getAdminTaskRequired());
        assertTrue(taskTypesToComplete.isEmpty());
    }

    static Stream<Arguments> reviewTaskCategoryScenarios() {
        return Stream.of(
            Arguments.of(TASK_TYPE_ADMIN, "RA0041", "RA0039"),
            Arguments.of(TASK_TYPE_LEGAL_OFFICER, "RA0034", "RA0035"),
            Arguments.of(TASK_TYPE_JUDGE, "RA0038", "RA0029")
        );
    }

    static Stream<Arguments> reviewSupportRequestScenarios() {
        return Stream.of(
            Arguments.of(EVENT_REVIEW_ADMIN_SUPPORT_REQUEST, "RA0041", "RA0038"),
            Arguments.of(EVENT_REVIEW_LEGAL_OFFICER_SUPPORT_REQUEST, "RA0034", "RA0041"),
            Arguments.of(EVENT_REVIEW_JUDGE_SUPPORT_REQUEST, "RA0038", "RA0034")
        );
    }

    @ParameterizedTest
    @MethodSource("reviewSupportRequestScenarios")
    void review_event_contains_only_requested_flags_for_its_task_category(
            String eventId, String flagCode, String differentCategoryFlagCode) {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("matching-requested", flagCode, STATUS_REQUESTED))
                .respondentFlags(caseFlags("matching-active", flagCode, STATUS_ACTIVE))
                .representativeFlags(caseFlags("different-category", differentCategoryFlagCode, STATUS_REQUESTED))
                .build());

        service.prepareReviewSupportRequest(caseData, eventId);

        assertEquals(1, caseData.getReviewSupportRequestFlags().size());
        CaseFlagsType selectedSection = caseData.getReviewSupportRequestFlags().getFirst().getValue();
        assertEquals(1, selectedSection.getDetails().size());
        assertEquals("matching-requested", selectedSection.getDetails().getFirst().getId());
    }

    @Test
    void review_event_includes_matching_flags_for_every_party_type() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("claimant", "RA0041", STATUS_REQUESTED))
                .respondent1ExternalFlags(caseFlags("respondent", "RA0041", STATUS_REQUESTED))
                .claimantRepresentativeFlags(caseFlags("claimant-representative", "RA0041", STATUS_REQUESTED))
                .representative2ExternalFlags(caseFlags("respondent-representative", "RA0041", STATUS_REQUESTED))
                .build());

        service.prepareReviewSupportRequest(caseData, EVENT_REVIEW_ADMIN_SUPPORT_REQUEST);

        List<String> selectedFlagIds = caseData.getReviewSupportRequestFlags().stream()
                .map(GenericTypeItem::getValue)
                .flatMap(flags -> flags.getDetails().stream())
                .map(GenericTypeItem::getId)
                .toList();
        assertEquals(List.of("claimant", "respondent", "claimant-representative", "respondent-representative"),
                selectedFlagIds);
    }

    @Test
    void reviewed_flag_is_updated_in_its_original_party_section() {
        CaseData caseData = caseDataWithClaimantFlag("RA0041", STATUS_REQUESTED, false);
        GenericTypeItem<FlagDetailType> originalFlag = caseData.getAllPartyFlags()
                .getClaimantFlags().getDetails().getFirst();
        originalFlag.setId("reviewed-flag");
        CaseFlagsType selectedFlags = caseFlags("reviewed-flag", "RA0041", STATUS_ACTIVE);
        caseData.setReviewSupportRequestFlags(ListTypeItem.from(selectedFlags));

        boolean updated = service.applyReviewSupportRequest(caseData);

        assertTrue(updated);
        assertEquals(STATUS_ACTIVE, originalFlag.getValue().getStatus());
        assertNull(caseData.getReviewSupportRequestFlags());
    }

    @Test
    void review_event_rejects_a_flag_that_is_still_requested() {
        CaseData caseData = caseDataWithClaimantFlag("RA0041", STATUS_REQUESTED, false);
        CaseFlagsType selectedFlags = caseFlags("selected-flag", "RA0041", STATUS_REQUESTED);
        caseData.setReviewSupportRequestFlags(ListTypeItem.from(selectedFlags));

        boolean updated = service.applyReviewSupportRequest(caseData);

        assertFalse(updated);
        assertEquals(STATUS_REQUESTED, caseData.getAllPartyFlags()
                .getClaimantFlags().getDetails().getFirst().getValue().getStatus());
    }

    static Stream<Arguments> nonRequestedReviewTaskScenarios() {
        return Stream.of(STATUS_NOT_APPROVED, STATUS_INACTIVE)
                .flatMap(status -> Stream.of(
                    Arguments.of(TASK_TYPE_ADMIN, "RA0041", status),
                    Arguments.of(TASK_TYPE_LEGAL_OFFICER, "RA0034", status),
                    Arguments.of(TASK_TYPE_JUDGE, "RA0038", status)
                ));
    }

    @ParameterizedTest
    @MethodSource("reviewTaskCategoryScenarios")
    void completes_each_task_category_only_after_its_last_requested_flag_is_activated(
            String taskType, String activeFlagCode, String requestedFlagCode) {
        CaseData caseData = caseDataWithFlags(activeFlagCode, STATUS_ACTIVE, requestedFlagCode, STATUS_REQUESTED);
        setTaskCreated(taskState(caseData), taskType, CCD_TRUE);

        Set<String> firstCompletion = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(CCD_TRUE, getTaskCreated(taskState(caseData), taskType));
        assertTrue(firstCompletion.isEmpty());

        activateRespondentFlag(caseData);
        Set<String> secondCompletion = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(NO, getTaskCreated(taskState(caseData), taskType));
        assertNull(getTaskRequired(taskState(caseData), taskType));
        assertEquals(Set.of(reviewTaskType(taskType)), secondCompletion);
    }

    @ParameterizedTest
    @MethodSource("reviewTaskCategoryScenarios")
    void boolean_created_state_prevents_duplicate_tasks_for_each_category(
            String taskType, String existingFlagCode, String newFlagCode) {
        CaseData caseDataBefore = caseDataWithClaimantFlag(existingFlagCode, STATUS_REQUESTED, false);
        caseDataBefore.setSupportTaskState(new SupportTaskState());
        setTaskCreated(taskState(caseDataBefore), taskType, CCD_TRUE);
        CaseData caseData = caseDataWithFlags(existingFlagCode, STATUS_REQUESTED, newFlagCode, STATUS_REQUESTED);

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertEquals(CCD_TRUE, getTaskCreated(taskState(caseData), taskType));
        assertNull(getTaskRequired(taskState(caseData), taskType));
    }

    @ParameterizedTest
    @MethodSource("reviewTaskCategoryScenarios")
    void restores_boolean_created_state_from_before_data_and_completes_each_category(
            String taskType, String flagCode, String secondFlagCode) {
        CaseData caseDataBefore = caseDataWithClaimantFlag(flagCode, STATUS_REQUESTED, false);
        caseDataBefore.setSupportTaskState(new SupportTaskState());
        setTaskCreated(taskState(caseDataBefore), taskType, CCD_TRUE);
        CaseData caseData = caseDataWithFlags(flagCode, STATUS_ACTIVE, secondFlagCode, STATUS_ACTIVE);

        Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, caseDataBefore);

        assertEquals(NO, getTaskCreated(taskState(caseData), taskType));
        assertEquals(Set.of(reviewTaskType(taskType)), taskTypesToComplete);
    }

    @ParameterizedTest
    @MethodSource("reviewTaskCategoryScenarios")
    void false_created_state_allows_a_new_task_for_each_category(
            String taskType, String flagCode, String secondFlagCode) {
        CaseData caseData = caseDataWithFlags(flagCode, STATUS_REQUESTED, secondFlagCode, STATUS_ACTIVE);
        setTaskCreated(taskState(caseData), taskType, CCD_FALSE);

        service.prepareReviewSupportTasks(caseData);

        assertEquals(YES, getTaskCreated(taskState(caseData), taskType));
        assertEquals(YES, getTaskRequired(taskState(caseData), taskType));
    }

    @ParameterizedTest
    @MethodSource("nonRequestedReviewTaskScenarios")
    void completes_each_task_category_for_every_non_requested_status(
            String taskType, String flagCode, String status) {
        CaseData caseData = caseDataWithClaimantFlag(flagCode, status, false);
        caseData.setSupportTaskState(new SupportTaskState());
        setTaskCreated(taskState(caseData), taskType, CCD_TRUE);

        final Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(NO, getTaskCreated(taskState(caseData), taskType));
        assertEquals(Set.of(reviewTaskType(taskType)), taskTypesToComplete);
    }

    @Test
    void completes_only_categories_without_requested_flags_when_boolean_state_is_used() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("admin-flag", "RA0041", STATUS_ACTIVE))
                .respondentFlags(caseFlags("legal-flag", "RA0034", STATUS_REQUESTED))
                .respondent1Flags(caseFlags("judge-flag", "RA0038", STATUS_REQUESTED))
                .build());
        caseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(CCD_TRUE)
                .legalOfficerTaskCreated(CCD_TRUE)
                .judgeTaskCreated(CCD_TRUE)
                .build());

        final Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(NO, taskState(caseData).getAdminTaskCreated());
        assertEquals(CCD_TRUE, taskState(caseData).getLegalOfficerTaskCreated());
        assertEquals(CCD_TRUE, taskState(caseData).getJudgeTaskCreated());
        assertEquals(Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN), taskTypesToComplete);
    }

    @Test
    void ccd_boolean_task_state_is_deserialized_to_boolean_strings() throws JsonProcessingException {
        SupportTaskState state = new ObjectMapper().readValue("""
                {
                  "adminTaskCreated": true,
                  "legalOfficerTaskCreated": false,
                  "judgeTaskCreated": true
                }
                """, SupportTaskState.class);

        assertEquals(CCD_TRUE, state.getAdminTaskCreated());
        assertEquals(CCD_FALSE, state.getLegalOfficerTaskCreated());
        assertEquals(CCD_TRUE, state.getJudgeTaskCreated());
    }

    @Test
    void keeps_task_active_when_an_eligible_requested_flag_remains() {
        CaseData caseData = caseDataWithClaimantFlag("RA0033", STATUS_REQUESTED, false);
        caseData.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(YES).build());

        Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertTrue(taskTypesToComplete.isEmpty());
    }

    @Test
    void completes_created_tasks_when_party_flags_are_missing() {
        CaseData caseData = new CaseData();
        caseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(YES)
                .legalOfficerTaskCreated(YES)
                .judgeTaskCreated(YES)
                .build());

        final Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(NO, taskState(caseData).getAdminTaskCreated());
        assertEquals(NO, taskState(caseData).getLegalOfficerTaskCreated());
        assertEquals(NO, taskState(caseData).getJudgeTaskCreated());
        assertEquals(Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN, TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER,
                TASK_TYPE_REVIEW_SUPPORT_JUDGE), taskTypesToComplete);
    }

    @Test
    void completes_judge_task_when_last_requested_flag_is_managed_and_current_state_is_missing() {
        CaseData caseDataBefore = caseDataWithClaimantFlag("RA0038", STATUS_REQUESTED, false);
        caseDataBefore.setSupportTaskState(SupportTaskState.builder().judgeTaskCreated(YES).build());
        CaseData caseData = caseDataWithClaimantFlag("RA0038", STATUS_ACTIVE, false);

        Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, caseDataBefore);

        assertEquals(NO, taskState(caseData).getJudgeTaskCreated());
        assertEquals(Set.of(TASK_TYPE_REVIEW_SUPPORT_JUDGE), taskTypesToComplete);
    }

    static Stream<Arguments> reviewAndArrangeTaskScenarios() {
        return Stream.of(
            Arguments.of("RA0041", TASK_TYPE_ADMIN, "Lip speaker"),
            Arguments.of("RA0038", TASK_TYPE_JUDGE, "Intermediary")
        );
    }

    @ParameterizedTest
    @MethodSource("reviewAndArrangeTaskScenarios")
    void preservesReviewCompletionAndArrangeCreationInTheSameUpdate(String flagCode,
                                                                     String taskType,
                                                                     String arrangeTaskName) {
        CaseData caseDataBefore = caseDataWithClaimantFlag(flagCode, STATUS_REQUESTED, false);
        CaseData caseData = caseDataWithClaimantFlag(flagCode, STATUS_ACTIVE, false);
        caseData.setSupportTaskState(new SupportTaskState());
        setTaskCreated(taskState(caseData), taskType, YES);

        Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, caseDataBefore);
        service.prepareArrangeSupportTask(caseData, caseDataBefore);

        assertEquals(Set.of(reviewTaskType(taskType)), taskTypesToComplete);
        assertEquals(arrangeTaskName, taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void completes_admin_task_when_created_marker_is_missing() {
        CaseData caseData = caseDataWithClaimantFlag("RA0041", STATUS_ACTIVE, false);

        Set<String> taskTypesToComplete = service.prepareManagedReviewSupportTasks(caseData, null);

        assertEquals(NO, taskState(caseData).getAdminTaskCreated());
        assertEquals(Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN), taskTypesToComplete);
    }

    @Test
    void creates_a_new_task_after_the_previous_task_was_completed() {
        CaseData caseDataBefore = caseDataWithClaimantFlag("RA0033", STATUS_ACTIVE, false);
        caseDataBefore.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(YES).build());
        service.prepareManagedReviewSupportTasks(caseDataBefore, null);

        CaseData caseData = caseDataWithClaimantFlag("RA0033", STATUS_ACTIVE, false);
        caseData.getAllPartyFlags().setRespondent1Flags(
                caseFlags("new-requested-flag", "RA0033", STATUS_REQUESTED));

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertEquals(YES, taskState(caseData).getAdminTaskRequired());
    }

    static Stream<Arguments> ineligibleFlagScenarios() {
        return Stream.of(
            Arguments.of("RA0033", STATUS_ACTIVE),
            Arguments.of("RA0033", null),
            Arguments.of("RA9999", STATUS_REQUESTED)
        );
    }

    @ParameterizedTest
    @MethodSource("ineligibleFlagScenarios")
    void does_not_prepare_tasks_for_ineligible_flags(String flagCode, String status) {
        CaseData caseData = caseDataWithClaimantFlag(flagCode, status, false);

        service.prepareReviewSupportTasks(caseData);

        assertNull(taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getLegalOfficerTaskCreated());
        assertNull(taskState(caseData).getJudgeTaskCreated());
    }

    static Stream<Arguments> arrangeSupportFlagScenarios() {
        return configuration().getArrange().getFlagTitles().entrySet().stream()
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    @ParameterizedTest
    @MethodSource("arrangeSupportFlagScenarios")
    void prepares_arrange_support_task_for_each_new_active_flag(String flagCode, String taskName) {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .representative4ExternalFlags(caseFlags("new-flag", flagCode, STATUS_ACTIVE))
                .build());

        service.prepareArrangeSupportTask(caseData, new CaseData());

        assertEquals(taskName, taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void prepares_arrange_support_task_for_active_other_flag_with_help_with_forms_path() {
        CaseData caseData = caseDataWithClaimantFlag(
                OTHER_FLAG_CODE, STATUS_ACTIVE, false, HELP_WITH_FORMS_PATH);

        service.prepareArrangeSupportTask(caseData, new CaseData());

        assertEquals("I need help with forms", taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void does_not_prepare_arrange_support_task_for_other_flag_with_a_different_path() {
        CaseData caseData = caseDataWithClaimantFlag(
                OTHER_FLAG_CODE, STATUS_ACTIVE, false, OTHER_REVIEW_PATHS.get(1));

        service.prepareArrangeSupportTask(caseData, new CaseData());

        assertNull(taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void prepares_separate_arrange_support_tasks_for_the_same_code_on_separate_events() {
        CaseData firstEvent = new CaseData();
        firstEvent.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("claimant-flag", "RA0017", STATUS_ACTIVE))
                .build());
        service.prepareArrangeSupportTask(firstEvent, new CaseData());

        CaseData secondEvent = new CaseData();
        secondEvent.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("claimant-flag", "RA0017", STATUS_ACTIVE))
                .respondent2Flags(caseFlags("respondent-flag", "RA0017", STATUS_ACTIVE))
                .build());
        service.prepareArrangeSupportTask(secondEvent, firstEvent);

        assertEquals("Guidance on how to complete forms", taskState(firstEvent).getArrangeSupportTaskName());
        assertEquals("Guidance on how to complete forms", taskState(secondEvent).getArrangeSupportTaskName());
    }

    @Test
    void prepares_arrange_support_task_when_existing_flag_becomes_active() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantRepresentativeFlags(caseFlags("managed-flag", "RA0042", STATUS_REQUESTED))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantRepresentativeFlags(caseFlags("managed-flag", "RA0042", STATUS_ACTIVE))
                .build());

        service.prepareArrangeSupportTask(caseData, caseDataBefore);

        assertEquals("Sign language interpreter", taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void prepares_arrange_task_when_create_flag_before_data_already_contains_the_new_active_flag() {
        CaseFlagsType activeFlag = caseFlags("new-flag", "RA0038", STATUS_ACTIVE, "2026-09-11T10:00:00.000Z");
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder().claimantFlags(activeFlag).build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder().claimantFlags(activeFlag).build());

        service.prepareNewFlagArrangeSupportTask(caseData, caseDataBefore);

        assertEquals("Intermediary", taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void does_not_fall_back_to_an_older_eligible_flag_when_the_newest_flag_is_ineligible() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("old-flag", "RA0038", STATUS_ACTIVE, "2026-09-11T09:00:00.000Z"))
                .respondentFlags(caseFlags("new-flag", "RA9999", STATUS_ACTIVE, "2026-09-11T10:00:00.000Z"))
                .build());

        service.prepareNewFlagArrangeSupportTask(caseData, caseData);

        assertNull(taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void does_not_prepare_arrange_support_task_for_unchanged_active_flag() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("unchanged-flag", "RA0017", STATUS_ACTIVE))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("unchanged-flag", "RA0017", STATUS_ACTIVE))
                .build());

        service.prepareArrangeSupportTask(caseData, caseDataBefore);

        assertNull(taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void does_not_prepare_arrange_support_task_for_ineligible_or_non_active_flag() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("requested-flag", "RA0017", STATUS_REQUESTED))
                .respondentFlags(caseFlags("ineligible-flag", "RA9999", STATUS_ACTIVE))
                .build());
        caseData.setSupportTaskState(SupportTaskState.builder()
                .arrangeSupportTaskName("Previous task")
                .build());

        service.prepareArrangeSupportTask(caseData, new CaseData());

        assertNull(taskState(caseData).getArrangeSupportTaskName());
    }

    private static CaseData caseDataWithClaimantFlag(String flagCode,
                                                      String status,
                                                      boolean external) {
        return caseDataWithClaimantFlag(flagCode, status, external, null);
    }

    private static CaseData caseDataWithClaimantFlag(String flagCode,
                                                      String status,
                                                      boolean external,
                                                      List<String> path) {
        FlagDetailType detail = FlagDetailType.builder()
                .flagCode(flagCode)
                .status(status)
                .path(pathItems(path))
                .build();
        CaseFlagsType flags = CaseFlagsType.builder().details(ListTypeItem.from(detail)).build();
        AllPartyFlags allPartyFlags = external
                ? AllPartyFlags.builder().claimantExternalFlags(flags).build()
                : AllPartyFlags.builder().claimantFlags(flags).build();
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(allPartyFlags);
        return caseData;
    }

    private static CaseData caseDataWithFlags(String claimantFlagCode,
                                              String claimantFlagStatus,
                                              String respondentFlagCode,
                                              String respondentFlagStatus) {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("claimant-flag", claimantFlagCode, claimantFlagStatus))
                .respondentFlags(caseFlags("respondent-flag", respondentFlagCode, respondentFlagStatus))
                .build());
        caseData.setSupportTaskState(new SupportTaskState());
        return caseData;
    }

    private static void activateRespondentFlag(CaseData caseData) {
        caseData.getAllPartyFlags()
                .getRespondentFlags()
                .getDetails()
                .getFirst()
                .getValue()
                .setStatus(STATUS_ACTIVE);
    }

    private static void setTaskCreated(SupportTaskState state, String taskType, String value) {
        switch (taskType) {
            case TASK_TYPE_ADMIN -> state.setAdminTaskCreated(value);
            case TASK_TYPE_LEGAL_OFFICER -> state.setLegalOfficerTaskCreated(value);
            case TASK_TYPE_JUDGE -> state.setJudgeTaskCreated(value);
            default -> throw new IllegalArgumentException("Unknown task type: " + taskType);
        }
    }

    private static String getTaskCreated(SupportTaskState state, String taskType) {
        return switch (taskType) {
            case TASK_TYPE_ADMIN -> state.getAdminTaskCreated();
            case TASK_TYPE_LEGAL_OFFICER -> state.getLegalOfficerTaskCreated();
            case TASK_TYPE_JUDGE -> state.getJudgeTaskCreated();
            default -> throw new IllegalArgumentException("Unknown task type: " + taskType);
        };
    }

    private static String getTaskRequired(SupportTaskState state, String taskType) {
        return switch (taskType) {
            case TASK_TYPE_ADMIN -> state.getAdminTaskRequired();
            case TASK_TYPE_LEGAL_OFFICER -> state.getLegalOfficerTaskRequired();
            case TASK_TYPE_JUDGE -> state.getJudgeTaskRequired();
            default -> throw new IllegalArgumentException("Unknown task type: " + taskType);
        };
    }

    private static String reviewTaskType(String taskType) {
        return switch (taskType) {
            case TASK_TYPE_ADMIN -> TASK_TYPE_REVIEW_SUPPORT_ADMIN;
            case TASK_TYPE_LEGAL_OFFICER -> TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER;
            case TASK_TYPE_JUDGE -> TASK_TYPE_REVIEW_SUPPORT_JUDGE;
            default -> throw new IllegalArgumentException("Unknown task type: " + taskType);
        };
    }

    private static CaseFlagsType caseFlags(String id, String flagCode, String status) {
        return caseFlags(id, flagCode, status, null);
    }

    private static CaseFlagsType caseFlags(String id, String flagCode, String status, String dateTimeCreated) {
        return caseFlags(id, flagCode, status, dateTimeCreated, null);
    }

    private static CaseFlagsType caseFlags(String id,
                                           String flagCode,
                                           String status,
                                           String dateTimeCreated,
                                           List<String> path) {
        FlagDetailType detail = FlagDetailType.builder()
                .flagCode(flagCode)
                .status(status)
                .dateTimeCreated(dateTimeCreated)
                .path(pathItems(path))
                .build();
        ListTypeItem<FlagDetailType> details = ListTypeItem.from(GenericTypeItem.from(id, detail));
        return CaseFlagsType.builder().details(details).build();
    }

    private static ListTypeItem<String> pathItems(List<String> path) {
        if (path == null) {
            return null;
        }
        ListTypeItem<String> pathItems = new ListTypeItem<>();
        path.forEach(pathItems::addAsItem);
        return pathItems;
    }

    private static SupportTaskState taskState(CaseData caseData) {
        return caseData.getSupportTaskState();
    }
}
