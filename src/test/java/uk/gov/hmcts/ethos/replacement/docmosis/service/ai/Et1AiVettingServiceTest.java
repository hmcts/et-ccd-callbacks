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
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.GeminiProperties;
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
    private GeminiClient geminiClient;
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
        GeminiProperties geminiProperties = new GeminiProperties();
        geminiProperties.setEnabled(true);
        geminiProperties.setApiKey("test-key");

        objectMapper = new ObjectMapper();
        Et1VettingPromptBuilder promptBuilder = new Et1VettingPromptBuilder();

        when(featureToggleService.isEt1VettingAiEnabled(any())).thenReturn(true);

        aiVettingService = new Et1AiVettingService(
                geminiProperties,
                geminiClient,
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
        when(geminiClient.generateContent(anyString(), anyString(), anyList())).thenReturn(aiJson);

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
        verify(geminiClient, times(1)).generateContent(anyString(), anyString(), anyList());
        verify(ccdClient).submitEventForCase(any(CcdSubmitEventParams.class));
        assertThat(eventCaseDetails.getCaseData().getEt1VettingCanServeClaimYesOrNo()).isEqualTo("Yes");
    }

    @Test
    void vetClaimAndPrepopulate_ignoresExpiredDbResultAndCallsGemini() throws Exception {
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
        when(geminiClient.generateContent(anyString(), anyString(), anyList())).thenReturn("""
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
        verify(geminiClient, times(1)).generateContent(anyString(), anyString(), anyList());
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

        verify(geminiClient, never()).generateContent(anyString(), anyString(), anyList());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }
}
