package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmploymentRightsActService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PostDefaultValuesService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SingleReferenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/**
 * Exercises the real default-value logic behind the ET1 repped submitClaim callback, rather than mocking it out.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Et1ReppedSubmitClaimDefaultValuesTest {

    private static final String USER_TOKEN = "Bearer token";
    private static final String EPIMMS_ID = "123456";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private DefaultValuesReaderService defaultValuesReaderService;
    @Mock
    private EventValidationService eventValidationService;
    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Spy
    private CaseFlagsService caseFlagsService;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    private Et1SubmissionService et1SubmissionService;
    @Mock
    private Et1ReppedService et1ReppedService;
    @Mock
    private EmploymentRightsActService employmentRightsActService;
    @Mock
    private SingleReferenceService singleReferenceService;

    @InjectMocks
    private PostDefaultValuesService postDefaultValuesService;

    private Et1ReppedController et1ReppedController;

    @BeforeEach
    void setUp() {
        et1ReppedController = new Et1ReppedController(postDefaultValuesService,
            caseManagementForCaseWorkerService, et1ReppedService, featureToggleService, et1SubmissionService,
            nocRespondentRepresentativeService);

        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        when(featureToggleService.isEt1DocGenEnabled()).thenReturn(true);
        when(eventValidationService.validateReceiptDate(any())).thenReturn(List.of());
        doCallRealMethod().when(caseManagementForCaseWorkerService).setPublicCaseName(any());
        doAnswer(invocation -> {
            invocation.<CaseData>getArgument(0).setCaseManagementLocationCode(EPIMMS_ID);
            return null;
        }).when(caseManagementLocationService).setCaseManagementLocationCode(any());
        // Like the real implementation, NoC prepopulation returns a new CaseData rather than mutating its argument.
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any()))
            .thenAnswer(invocation -> OBJECT_MAPPER.convertValue(invocation.getArgument(0), CaseData.class));
    }

    @Test
    void submitClaimReturnsDefaultsSetAfterNocPrepopulation() {
        CCDRequest ccdRequest = submitEt1DraftRequest();

        ResponseEntity<CCDCallbackResponse> response = et1ReppedController.submitClaim(ccdRequest, USER_TOKEN);

        CaseData data = Objects.requireNonNull(response.getBody()).getData();
        assertSoftly(softly -> {
            softly.assertThat(data.getClaimantFlags()).as("claimant case flags").isNotNull();
            softly.assertThat(data.getRespondentFlags()).as("respondent case flags").isNotNull();
            softly.assertThat(data.getPublicCaseName()).as("public case name").isEqualTo("John Doe vs Claimant Org");
            softly.assertThat(data.getCaseManagementLocationCode()).as("case management location code")
                .isEqualTo(EPIMMS_ID);
        });
    }

    @Test
    void submitClaimReturnsReceiptDateValidationErrors() {
        when(eventValidationService.validateReceiptDate(any())).thenReturn(List.of("Receipt date error"));
        CCDRequest ccdRequest = submitEt1DraftRequest();

        ResponseEntity<CCDCallbackResponse> response = et1ReppedController.submitClaim(ccdRequest, USER_TOKEN);

        assertThat(Objects.requireNonNull(response.getBody()).getErrors()).containsExactly("Receipt date error");
    }

    @SneakyThrows
    private CCDRequest submitEt1DraftRequest() {
        String json = Files.readString(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource("et1ReppedDraftStillWorking.json")).toURI()));
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(new ObjectMapper().readValue(json, CaseDetails.class));
        ccdRequest.setEventId("submitEt1Draft");
        return ccdRequest;
    }
}
