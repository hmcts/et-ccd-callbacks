package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.SyncTaskExecutor;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.client.CcdSubmitEventParams;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.AiGatewayProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiVettingPendingResult;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.Et1AiVettingResult;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ai.AiVettingPendingResultRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice.JpaVenueService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(MockitoExtension.class)
class Et1AiVettingServiceTest {

    private static final String CASE_ID = "1756969501994958";
    private static final long CASE_REFERENCE = 1_756_969_501_994_958L;
    private static final String ADMIN_TOKEN = "Bearer admin-token";

    @Mock
    private AiGatewayClient aiGatewayClient;
    @Mock
    private Et1DocumentReaderService documentReaderService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private JpaVenueService jpaVenueService;
    @Mock
    private AiVettingPendingResultRepository pendingResultRepository;
    @Mock
    private FeatureToggleService featureToggleService;

    private ObjectMapper objectMapper;
    private Et1AiVettingService aiVettingService;

    @BeforeEach
    void setUp() {
        AiGatewayProperties aiGatewayProperties = new AiGatewayProperties();
        aiGatewayProperties.setEnabled(true);
        aiGatewayProperties.setSubscriptionKey("test-key");

        objectMapper = new ObjectMapper();
        Et1VettingPromptBuilder promptBuilder = new Et1VettingPromptBuilder();

        when(featureToggleService.isEt1VettingAiEnabled(any())).thenReturn(true);

        aiVettingService = new Et1AiVettingService(
                aiGatewayProperties,
                aiGatewayClient,
                promptBuilder,
                documentReaderService,
                objectMapper,
                ccdClient,
                adminUserService,
                jpaVenueService,
                new SyncTaskExecutor(),
                pendingResultRepository,
                featureToggleService
        );
    }

    @Test
    void vetClaimInBackground_persistsResultInDbAndConsumesDuringStartEvent() throws Exception {
        CaseDetails submittedCase = CaseDataBuilder.builder()
                .withEthosCaseReference("6000001/2026")
                .withManagingOffice("Leeds")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        submittedCase.setCaseId(CASE_ID);

        String aiJson = """
                {
                  "canServeClaim": {"canServe": "Yes"},
                  "confidence": "HIGH",
                  "executiveSummaryMarkdown": "### ET1 Vetting Summary\\n- **Claimant**: Test"
                }
                """;
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(documentReaderService.findClaimDocumentAttachments(any(CaseData.class), anyString()))
                .thenReturn(List.of());
        when(aiGatewayClient.generateContent(anyString(), anyString(), anyList())).thenReturn(aiJson);

        CaseDetails eventCaseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("6000001/2026")
                .withManagingOffice("Leeds")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        eventCaseDetails.setCaseId(CASE_ID);

        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(eventCaseDetails);

        when(pendingResultRepository.save(any(AiVettingPendingResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(ccdClient.startEventForCase(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID),
                anyString(), eq(CASE_ID), eq("et1Vetting")))
                .thenAnswer(invocation -> {
                    // Simulate another AKS replica handling /initialiseEt1Vetting by reading from DB
                    ArgumentCaptor<AiVettingPendingResult> savedCaptor =
                            ArgumentCaptor.forClass(AiVettingPendingResult.class);
                    verify(pendingResultRepository).save(savedCaptor.capture());
                    when(pendingResultRepository.findById(CASE_REFERENCE))
                            .thenReturn(Optional.of(savedCaptor.getValue()), Optional.empty());

                    aiVettingService.vetClaimAndPrepopulate(eventCaseDetails, null);
                    return ccdRequest;
                });

        aiVettingService.vetClaimInBackground(submittedCase, "Bearer user-token");

        verify(pendingResultRepository).deleteByExpiresAtBefore(any(Instant.class));
        verify(aiGatewayClient, times(1)).generateContent(anyString(), anyString(), anyList());
        verify(ccdClient).submitEventForCase(any(CcdSubmitEventParams.class));
        assertThat(eventCaseDetails.getCaseData().getEt1VettingCanServeClaimYesOrNo()).isEqualTo("Yes");
    }

    @Test
    void vetClaimAndPrepopulate_ignoresExpiredDbResultAndCallsAiGateway() throws Exception {
        Et1AiVettingResult stale = new Et1AiVettingResult();
        stale.setConfidence("LOW");
        AiVettingPendingResult expiredRow = AiVettingPendingResult.create(
                CASE_REFERENCE,
                objectMapper.writeValueAsString(stale),
                Instant.now().minusSeconds(60)
        );
        when(pendingResultRepository.findById(CASE_REFERENCE)).thenReturn(Optional.of(expiredRow));
        when(documentReaderService.findClaimDocumentAttachments(any(CaseData.class), any()))
                .thenReturn(List.of());
        when(aiGatewayClient.generateContent(anyString(), anyString(), anyList())).thenReturn("""
                {
                  "canServeClaim": {"canServe": "Yes"},
                  "confidence": "HIGH",
                  "executiveSummaryMarkdown": "### ET1 Vetting Summary\\n- **Claimant**: Fresh"
                }
                """);

        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("6000002/2026")
                .withManagingOffice("Leeds")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(CASE_ID);

        aiVettingService.vetClaimAndPrepopulate(caseDetails, "Bearer user-token");

        verify(pendingResultRepository).delete(expiredRow);
        verify(aiGatewayClient, times(1)).generateContent(anyString(), anyString(), anyList());
        assertThat(caseDetails.getCaseData().getEt1VettingCanServeClaimYesOrNo()).isEqualTo("Yes");
        assertThat(caseDetails.getCaseData().getEt1VettingAdditionalInformationTextArea())
                .contains("Confidence: HIGH");
    }

    @Test
    void skipsAiVetting_whenLaunchDarklyDisabledForManagingOffice() throws Exception {
        when(featureToggleService.isEt1VettingAiEnabled("Manchester")).thenReturn(false);

        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("6000003/2026")
                .withManagingOffice("Manchester")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(CASE_ID);

        aiVettingService.vetClaimInBackground(caseDetails, "Bearer user-token");
        aiVettingService.vetClaimAndPrepopulate(caseDetails, "Bearer user-token");

        verify(aiGatewayClient, never()).generateContent(anyString(), anyString(), anyList());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void vetClaimAndPrepopulate_appliesFullVettingResultFieldsAndBanner() throws Exception {
        Et1AiVettingResult fullResult = Et1AiVettingResult.builder()
                .canServeClaim(Et1AiVettingResult.CanServeClaimAssessment.builder()
                        .canServe("No")
                        .reason("Missing postcode")
                        .notes("Rule 12(1)(b) incomplete respondent address")
                        .build())
                .acasAssessments(List.of(
                        Et1AiVettingResult.AcasAssessment.builder()
                                .respondentNumber(1).hasCertificate("Yes").isExempt("No").build(),
                        Et1AiVettingResult.AcasAssessment.builder()
                                .respondentNumber(2).hasCertificate("No").isExempt("Yes").build(),
                        Et1AiVettingResult.AcasAssessment.builder()
                                .respondentNumber(3).hasCertificate("Yes").isExempt("No").build(),
                        Et1AiVettingResult.AcasAssessment.builder()
                                .respondentNumber(4).hasCertificate("No").isExempt("No").build(),
                        Et1AiVettingResult.AcasAssessment.builder()
                                .respondentNumber(5).hasCertificate("Yes").isExempt("No").build(),
                        Et1AiVettingResult.AcasAssessment.builder()
                                .respondentNumber(6).hasCertificate("No").isExempt("Yes").build()
                ))
                .et1VettingEra("Yes")
                .substantiveDefects(Et1AiVettingResult.SubstantiveDefectsAssessment.builder()
                        .defectsList(List.of("rule121a", "rule121b", "rule121c", "rule121d",
                                "rule121da", "rule121e", "rule121f"))
                        .rule121aTextArea("No jurisdiction note")
                        .rule121bTextArea("Abuse of process note")
                        .rule121cTextArea("No EC number note")
                        .rule121dTextArea("Exemption may not apply")
                        .rule121daTextArea("Rule 12(1)(da) EC mismatch")
                        .rule121eTextArea("Rule 12(1)(e) claimant mismatch")
                        .rule121fTextArea("Rule 12(1)(f) respondent mismatch")
                        .generalNotes("Substantive defects summary")
                        .build())
                .jurisdictionAssessment(Et1AiVettingResult.JurisdictionAssessment.builder()
                        .areCodesCorrect("No")
                        .suggestedJurisdictionCodes(List.of("UDL", "ADT(ST)", "INVALID_CODE"))
                        .details("Add UDL and ADT(ST)")
                        .generalNotes("Jurisdiction notes")
                        .build())
                .trackAllocation(Et1AiVettingResult.TrackAllocationAssessment.builder()
                        .isTrackCorrect("No")
                        .suggestedTrack("Open")
                        .whyChangeTrackAllocation("Discrimination claim requires Open track")
                        .generalNotes("Track notes")
                        .build())
                .locationAndVenue(Et1AiVettingResult.LocationAndVenueAssessment.builder()
                        .isLocationCorrect("No")
                        .suggestedOffice("Leeds")
                        .whyChangeOffice("Closer to claimant")
                        .generalNotes("Location notes")
                        .suggestHearingVenue("Yes")
                        .suggestedHearingVenue("Leeds")
                        .hearingVenueGeneralNotes("Venue notes")
                        .build())
                .furtherQuestions(Et1AiVettingResult.FurtherQuestionsAssessment.builder()
                        .isGovOrMajorEmployer("Yes")
                        .hasReasonableAdjustments("Yes")
                        .reasonableAdjustmentsDetails("Hearing loop")
                        .videoHearingPreference("No")
                        .videoHearingDetails("Prefers in-person")
                        .generalNotes("Further questions notes")
                        .build())
                .judicialReferrals(Et1AiVettingResult.JudicialReferralAssessment.builder()
                        .referralsToJudge(List.of("aClaimOfInterimRelief", "aStatutoryAppeal",
                                "anAllegationOfCommissionOfSexualOffence", "insolvency",
                                "jurisdictionsUnclear", "lengthOfService",
                                "potentiallyLinkedCasesInTheEcm", "rule50Issues",
                                "anotherReasonForJudicialReferral"))
                        .claimOfInterimReliefNotes("Interim relief")
                        .statutoryAppealNotes("Statutory appeal")
                        .sexualOffenceNotes("Sexual offence")
                        .insolvencyNotes("Insolvency")
                        .jurisdictionsUnclearNotes("Unclear")
                        .lengthOfServiceNotes("Under 2 years")
                        .potentiallyLinkedCasesNotes("Linked")
                        .rule50IssuesNotes("Rule 50")
                        .anotherReasonNotes("Another")
                        .generalNotes("Judge notes")
                        .build())
                .rejOrVpReferrals(Et1AiVettingResult.RejOrVpReferralAssessment.builder()
                        .referralsToRejOrVp(List.of("vexatiousLitigantOrder", "aNationalSecurityIssue",
                                "nationalMultipleOrPresidentialOrder", "transferToOtherRegion",
                                "serviceAbroad", "aSensitiveIssue", "anyPotentialConflict",
                                "anotherReasonREJOrVP"))
                        .vexatiousLitigantOrderNotes("Vexatious")
                        .nationalSecurityNotes("Security")
                        .nationalMultipleNotes("Multiple")
                        .transferToOtherRegionNotes("Transfer")
                        .serviceAbroadNotes("Abroad")
                        .sensitiveIssueNotes("Sensitive")
                        .potentialConflictNotes("Conflict")
                        .anotherReasonNotes("Another REJ")
                        .generalNotes("REJ notes")
                        .build())
                .otherReferrals(Et1AiVettingResult.OtherReferralAssessment.builder()
                        .otherReferrals(List.of("claimOutOfTime", "multipleClaim",
                                "employmentStatusIssues", "pidJurisdictionRegulator",
                                "videoHearingPreference", "rule50IssuesOtherFactors",
                                "otherRelevantFactors"))
                        .claimOutOfTimeNotes("Out of time")
                        .multipleClaimNotes("Multiple claim")
                        .employmentStatusIssuesNotes("Worker status")
                        .pidRegulatorNotes("PID")
                        .videoHearingNotes("Video")
                        .rule50Notes("Rule 50")
                        .anotherReasonNotes("Other")
                        .generalNotes("Other referral notes")
                        .build())
                .confidence("HIGH")
                .executiveSummaryMarkdown("### ET1 Vetting Summary\n"
                        + "- **Claimant**: Jane Doe with `UDL`\n"
                        + "**Key Issues:**\n"
                        + "* Check Rule 12\n\n"
                        + "Paragraph text.")
                .itemsRequiringReview(List.of("Verify **ACAS** certificate manually"))
                .build();

        when(pendingResultRepository.findById(CASE_REFERENCE)).thenReturn(Optional.empty());
        when(documentReaderService.findClaimDocumentAttachments(any(CaseData.class), any()))
                .thenReturn(List.of());
        when(aiGatewayClient.generateContent(anyString(), anyString(), anyList()))
                .thenReturn("[" + objectMapper.writeValueAsString(fullResult) + "]");

        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("6000004/2026")
                .withManagingOffice("Leeds")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(CASE_ID);
        caseDetails.getCaseData().setEt1VettingBeforeYouStart("<p>Existing banner</p>");

        aiVettingService.vetClaimAndPrepopulate(caseDetails, "Bearer user-token");

        CaseData data = caseDetails.getCaseData();
        assertThat(data.getEt1VettingCanServeClaimYesOrNo()).isEqualTo("No");
        assertThat(data.getEt1VettingAcasCertIsYesOrNo1()).isEqualTo("Yes");
        assertThat(data.getEt1VettingAcasCertExemptYesOrNo2()).isEqualTo("Yes");
        assertThat(data.getEt1VettingAcasCertIsYesOrNo6()).isEqualTo("No");
        assertThat(data.getSubstantiveDefectsList()).contains("rule121a", "rule121 da");
        assertThat(data.getRule121daTextArea()).contains("Rule 13(1)(e)");
        assertThat(data.getSuggestAnotherTrack()).isEqualTo("Open");
        assertThat(data.getEt1VettingBeforeYouStart())
                .contains("ET1 Vetting Summary</h4>")
                .contains("<strong>Claimant</strong>")
                .contains("<code>UDL</code>")
                .contains("<p>Existing banner</p>");
        assertThat(data.getEt1VettingAdditionalInformationTextArea())
                .contains("Items Requiring Double-Checking / Unable to Answer:")
                .contains("Verify ACAS certificate manually");
    }

    @Test
    void vetClaimAndPrepopulate_handlesBlankArrayAndMalformedJsonGracefully() {
        when(pendingResultRepository.findById(CASE_REFERENCE)).thenReturn(Optional.empty());
        when(documentReaderService.findClaimDocumentAttachments(any(CaseData.class), any()))
                .thenReturn(List.of());
        when(aiGatewayClient.generateContent(anyString(), anyString(), anyList()))
                .thenReturn("[]")
                .thenReturn("\"not-an-object\"")
                .thenReturn("{invalid-json");

        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("6000005/2026")
                .withManagingOffice("Leeds")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(CASE_ID);

        aiVettingService.vetClaimAndPrepopulate(caseDetails, "Bearer user-token");
        aiVettingService.vetClaimAndPrepopulate(caseDetails, "Bearer user-token");
        aiVettingService.vetClaimAndPrepopulate(caseDetails, "Bearer user-token");

        assertThat(caseDetails.getCaseData().getEt1VettingCanServeClaimYesOrNo()).isNull();
    }

    @Test
    void vetClaimInBackground_skipsUnassignedOffice() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("6000006/2026")
                .withManagingOffice("Unassigned")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(CASE_ID);

        aiVettingService.vetClaimInBackground(caseDetails, "Bearer user-token");

        verify(aiGatewayClient, never()).generateContent(anyString(), anyString(), anyList());
    }
}
