package uk.gov.hmcts.ethos.replacement.docmosis.service;

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

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class SupportTaskServiceTest {
    private final SupportTaskService service = new SupportTaskService(configuration());

    private static SupportTaskConfiguration configuration() {
        SupportTaskConfiguration configuration = new SupportTaskConfiguration();
        configuration.getReview().setAdminFlagCodes(Set.of(
                "RA0002", "RA0003", "RA0004", "RA0005", "RA0006", "RA0008", "RA0009", "RA0021", "RA0033",
                "RA0039", "RA0041"));
        configuration.getReview().setLegalOfficerFlagCodes(Set.of("RA0034", "RA0035", "RA0036"));
        configuration.getReview().setJudgeFlagCodes(Set.of("RA0029", "RA0031", "RA0032", "RA0037", "RA0038"));
        configuration.getArrange().setFlagTitles(Map.ofEntries(
                Map.entry("RA0003", "I need help with forms"),
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
        return configuration;
    }

    static Stream<Arguments> eligibleFlagScenarios() {
        return Stream.of(
            Stream.of("RA0002", "RA0003", "RA0004", "RA0005", "RA0006", "RA0008", "RA0009", "RA0021",
                    "RA0033", "RA0039", "RA0041")
                .map(code -> Arguments.of(code, "Admin")),
            Stream.of("RA0034", "RA0035", "RA0036")
                .map(code -> Arguments.of(code, "LegalOfficer")),
            Stream.of("RA0029", "RA0031", "RA0032", "RA0037", "RA0038")
                .map(code -> Arguments.of(code, "Judge"))
        ).flatMap(stream -> stream);
    }

    @ParameterizedTest
    @MethodSource("eligibleFlagScenarios")
    void prepares_each_eligible_task_type_once_per_case(String flagCode, String taskType) {
        CaseData caseData = caseDataWithClaimantFlag(flagCode, "Requested", false);

        service.prepareReviewSupportTasks(caseData);

        assertEquals("Admin".equals(taskType) ? YES : null,
                taskState(caseData).getAdminTaskRequired());
        assertEquals("LegalOfficer".equals(taskType) ? YES : null,
                taskState(caseData).getLegalOfficerTaskRequired());
        assertEquals("Judge".equals(taskType) ? YES : null,
                taskState(caseData).getJudgeTaskRequired());

        service.prepareReviewSupportTasks(caseData);

        assertNull(taskState(caseData).getAdminTaskRequired());
        assertNull(taskState(caseData).getLegalOfficerTaskRequired());
        assertNull(taskState(caseData).getJudgeTaskRequired());
    }

    @Test
    void prepares_task_for_requested_external_claimant_flag() {
        CaseData caseData = caseDataWithClaimantFlag("RA0029", "Requested", true);

        service.prepareReviewSupportTasks(caseData);

        assertEquals(YES, taskState(caseData).getJudgeTaskCreated());
        assertEquals(YES, taskState(caseData).getJudgeTaskRequired());
    }

    @Test
    void prepares_task_for_requested_flag_from_any_respondent() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .respondent3ExternalFlags(caseFlags("respondent-3-flag", "RA0034", "Requested"))
                .build());

        service.prepareRespondentReviewSupportTasks(caseData);

        assertEquals(YES, taskState(caseData).getLegalOfficerTaskCreated());
        assertEquals(YES, taskState(caseData).getLegalOfficerTaskRequired());
    }

    @Test
    void respondent_event_does_not_use_a_claimant_flag() {
        CaseData caseData = caseDataWithClaimantFlag("RA0002", "Requested", false);

        service.prepareRespondentReviewSupportTasks(caseData);

        assertNull(taskState(caseData).getAdminTaskCreated());
    }

    @Test
    void prepares_task_only_for_a_newly_created_flag() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("existing-flag", "RA0002", "Requested"))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("existing-flag", "RA0002", "Requested"))
                .respondent2Flags(caseFlags("new-flag", "RA0029", "Requested"))
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
                .representative2ExternalFlags(caseFlags("new-flag", "RA0002", "Requested"))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, new CaseData());

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertEquals(YES, taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void does_not_treat_an_unchanged_flag_without_an_id_as_new() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags(null, "RA0002", "Requested"))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags(null, "RA0002", "Requested"))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertNull(taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void does_not_treat_an_updated_existing_flag_as_new() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("existing-flag", "RA0002", "Active"))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("existing-flag", "RA0002", "Requested"))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertNull(taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void does_not_prepare_task_for_a_new_case_level_flag() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .caseFlags(caseFlags("new-case-flag", "RA0002", "Requested"))
                .build());

        service.prepareNewFlagReviewSupportTasks(caseData, new CaseData());

        assertNull(taskState(caseData).getAdminTaskCreated());
    }

    @Test
    void prepares_tasks_for_new_claimant_and_representative_flags() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantExternalFlags(caseFlags("claimant-flag", "RA0002", "Requested"))
                .claimantRepresentativeFlags(caseFlags("claimant-rep-flag", "RA0034", "Requested"))
                .representative4ExternalFlags(caseFlags("respondent-rep-flag", "RA0029", "Requested"))
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
                .claimantFlags(caseFlags("admin-flag", "RA0002", "Active"))
                .respondent2Flags(caseFlags("judge-flag", "RA0029", "Requested"))
                .build());
        caseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(YES)
                .judgeTaskCreated(YES)
                .build());

        service.prepareManagedReviewSupportTasks(caseData);

        assertNull(taskState(caseData).getAdminTaskCreated());
        assertEquals(NO, taskState(caseData).getAdminTaskRequired());
        assertEquals(YES, taskState(caseData).getJudgeTaskCreated());
        assertNull(taskState(caseData).getJudgeTaskRequired());
        assertNull(taskState(caseData).getLegalOfficerTaskRequired());
    }

    @Test
    void keeps_task_active_when_another_party_has_an_eligible_requested_flag() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("managed-flag", "RA0002", "Active"))
                .representative2ExternalFlags(caseFlags("requested-flag", "RA0003", "Requested"))
                .build());
        caseData.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(YES).build());

        service.prepareManagedReviewSupportTasks(caseData);

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void keeps_task_active_when_an_eligible_requested_flag_remains() {
        CaseData caseData = caseDataWithClaimantFlag("RA0002", "Requested", false);
        caseData.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(YES).build());

        service.prepareManagedReviewSupportTasks(caseData);

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getAdminTaskRequired());
    }

    @Test
    void completes_created_tasks_when_party_flags_are_missing() {
        CaseData caseData = new CaseData();
        caseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(YES)
                .legalOfficerTaskCreated(YES)
                .judgeTaskCreated(YES)
                .build());

        service.prepareManagedReviewSupportTasks(caseData);

        assertNull(taskState(caseData).getAdminTaskCreated());
        assertNull(taskState(caseData).getLegalOfficerTaskCreated());
        assertNull(taskState(caseData).getJudgeTaskCreated());
        assertEquals(NO, taskState(caseData).getAdminTaskRequired());
        assertEquals(NO, taskState(caseData).getLegalOfficerTaskRequired());
        assertEquals(NO, taskState(caseData).getJudgeTaskRequired());
    }

    @Test
    void creates_a_new_task_after_the_previous_task_was_completed() {
        CaseData caseDataBefore = caseDataWithClaimantFlag("RA0002", "Active", false);
        caseDataBefore.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(YES).build());
        service.prepareManagedReviewSupportTasks(caseDataBefore);

        CaseData caseData = caseDataWithClaimantFlag("RA0002", "Active", false);
        caseData.getAllPartyFlags().setRespondent1Flags(
                caseFlags("new-requested-flag", "RA0003", "Requested"));

        service.prepareNewFlagReviewSupportTasks(caseData, caseDataBefore);

        assertEquals(YES, taskState(caseData).getAdminTaskCreated());
        assertEquals(YES, taskState(caseData).getAdminTaskRequired());
    }

    static Stream<Arguments> ineligibleFlagScenarios() {
        return Stream.of(
            Arguments.of("RA0002", "Active"),
            Arguments.of("RA0002", null),
            Arguments.of("RA9999", "Requested")
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
                .representative4ExternalFlags(caseFlags("new-flag", flagCode, "Active"))
                .build());

        service.prepareArrangeSupportTask(caseData, new CaseData());

        assertEquals(taskName, taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void prepares_separate_arrange_support_tasks_for_the_same_code_on_separate_events() {
        CaseData firstEvent = new CaseData();
        firstEvent.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("claimant-flag", "RA0017", "Active"))
                .build());
        service.prepareArrangeSupportTask(firstEvent, new CaseData());

        CaseData secondEvent = new CaseData();
        secondEvent.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("claimant-flag", "RA0017", "Active"))
                .respondent2Flags(caseFlags("respondent-flag", "RA0017", "Active"))
                .build());
        service.prepareArrangeSupportTask(secondEvent, firstEvent);

        assertEquals("Guidance on how to complete forms", taskState(firstEvent).getArrangeSupportTaskName());
        assertEquals("Guidance on how to complete forms", taskState(secondEvent).getArrangeSupportTaskName());
    }

    @Test
    void prepares_arrange_support_task_when_existing_flag_becomes_active() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantRepresentativeFlags(caseFlags("managed-flag", "RA0042", "Requested"))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantRepresentativeFlags(caseFlags("managed-flag", "RA0042", "Active"))
                .build());

        service.prepareArrangeSupportTask(caseData, caseDataBefore);

        assertEquals("Sign language interpreter", taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void does_not_prepare_arrange_support_task_for_unchanged_active_flag() {
        CaseData caseDataBefore = new CaseData();
        caseDataBefore.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("unchanged-flag", "RA0003", "Active"))
                .build());
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("unchanged-flag", "RA0003", "Active"))
                .build());

        service.prepareArrangeSupportTask(caseData, caseDataBefore);

        assertNull(taskState(caseData).getArrangeSupportTaskName());
    }

    @Test
    void does_not_prepare_arrange_support_task_for_ineligible_or_non_active_flag() {
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(caseFlags("requested-flag", "RA0003", "Requested"))
                .respondentFlags(caseFlags("ineligible-flag", "RA9999", "Active"))
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
        FlagDetailType detail = FlagDetailType.builder()
                .flagCode(flagCode)
                .status(status)
                .build();
        CaseFlagsType flags = CaseFlagsType.builder().details(ListTypeItem.from(detail)).build();
        AllPartyFlags allPartyFlags = external
                ? AllPartyFlags.builder().claimantExternalFlags(flags).build()
                : AllPartyFlags.builder().claimantFlags(flags).build();
        CaseData caseData = new CaseData();
        caseData.setAllPartyFlags(allPartyFlags);
        return caseData;
    }

    private static CaseFlagsType caseFlags(String id, String flagCode, String status) {
        FlagDetailType detail = FlagDetailType.builder()
                .flagCode(flagCode)
                .status(status)
                .build();
        ListTypeItem<FlagDetailType> details = ListTypeItem.from(GenericTypeItem.from(id, detail));
        return CaseFlagsType.builder().details(details).build();
    }

    private static SupportTaskState taskState(CaseData caseData) {
        return caseData.getSupportTaskState();
    }
}
