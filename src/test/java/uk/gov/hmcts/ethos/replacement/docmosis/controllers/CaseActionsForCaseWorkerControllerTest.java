package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.DraftAndSignJudgement;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AddSingleCaseToMultipleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseCloseValidator;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseCreationForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseRetrievalForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseUpdateForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClerkService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DepositOrderValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FixCaseApiService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.JudgmentValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScotlandFileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SingleCaseMultipleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SingleReferenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INDIVIDUAL_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CaseActionsForCaseWorkerController.class, JsonMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
class CaseActionsForCaseWorkerControllerTest extends BaseControllerTest {

    private static final String CREATION_CASE_URL = "/createCase";
    private static final String RETRIEVE_CASE_URL = "/retrieveCase";
    private static final String RETRIEVE_CASES_URL = "/retrieveCases";
    private static final String UPDATE_CASE_URL = "/updateCase";
    private static final String PRE_DEFAULT_VALUES_URL = "/preDefaultValues";
    private static final String POST_DEFAULT_VALUES_URL = "/postDefaultValues";
    private static final String AMEND_CASE_DETAILS_URL = "/amendCaseDetails";
    private static final String AMEND_CLAIMANT_DETAILS_URL = "/amendClaimantDetails";
    private static final String AMEND_RESPONDENT_DETAILS_URL = "/amendRespondentDetails";
    private static final String AMEND_RESPONDENT_REPRESENTATIVE_URL = "/amendRespondentRepresentative";
    private static final String UPDATE_HEARING_URL = "/updateHearing";
    private static final String ALLOCATE_HEARING_URL = "/allocateHearing";
    private static final String RESTRICTED_CASES_URL = "/restrictedCases";
    private static final String AMEND_HEARING_URL = "/amendHearing";
    private static final String MID_EVENT_AMEND_HEARING_URL = "/midEventAmendHearing";
    private static final String AMEND_CASE_STATE_URL = "/amendCaseState";
    private static final String AMEND_FIX_CASE_API_URL = "/amendFixCaseAPI";
    private static final String MID_RESPONDENT_ADDRESS_URL = "/midRespondentAddress";
    private static final String JURISDICTION_VALIDATION_URL = "/jurisdictionValidation";
    private static final String JUDGEMENT_VALIDATION_URL = "/judgmentValidation";
    private static final String DEPOSIT_VALIDATION_URL = "/depositValidation";
    private static final String MID_RESPONDENT_ECC_URL = "/midRespondentECC";
    private static final String CREATE_ECC_URL = "/createECC";
    private static final String LINK_ORIGINAL_CASE_ECC_URL = "/linkOriginalCaseECC";
    private static final String SINGLE_CASE_MULTIPLE_MID_EVENT_VALIDATION_URL = "/singleCaseMultipleMidEventValidation";
    private static final String HEARING_MID_EVENT_VALIDATION_URL = "/hearingMidEventValidation";
    private static final String BF_ACTIONS_URL = "/bfActions";
    private static final String DYNAMIC_LIST_BF_ACTIONS_URL = "/dynamicListBfActions";
    private static final String ABOUT_TO_START_DISPOSAL_URL = "/aboutToStartDisposal";
    private static final String INITIALISE_AMEND_CASE_DETAILS_URL = "/initialiseAmendCaseDetails";
    private static final String DYNAMIC_RESPONDENT_REPRESENTATIVE_NAMES_URL = "/dynamicRespondentRepresentativeNames";
    private static final String DYNAMIC_RESTRICTED_REPORTING_URL = "/dynamicRestrictedReporting";
    private static final String DYNAMIC_DEPOSIT_ORDER_URL = "/dynamicDepositOrder";
    private static final String DYNAMIC_JUDGMENT_URL = "/dynamicJudgments";
    private static final String JUDGEMENT_SUBMITTED_URL = "/judgementSubmitted";
    private static final String REINSTATE_CLOSED_CASE_MID_EVENT_VALIDATION_URL =
            "/reinstateClosedCaseMidEventValidation";

    private static final String ADD_SERVICE_ID_URL = "/addServiceId";
    private static final String AUTHORIZATION = "Authorization";
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private CaseCloseValidator caseCloseValidator;

    @MockBean
    private CaseCreationForCaseWorkerService caseCreationForCaseWorkerService;

    @MockBean
    private CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;

    @MockBean
    private CaseUpdateForCaseWorkerService caseUpdateForCaseWorkerService;

    @MockBean
    private DefaultValuesReaderService defaultValuesReaderService;

    @MockBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @MockBean
    private SingleReferenceService singleReferenceService;

    @MockBean
    private EventValidationService eventValidationService;

    @MockBean
    private DepositOrderValidationService depositOrderValidationService;

    @MockBean
    private JudgmentValidationService judgmentValidationService;

    @MockBean
    private ConciliationTrackService conciliationTrackService;

    @MockBean
    private SingleCaseMultipleMidEventValidationService singleCaseMultipleMidEventValidationService;

    @MockBean
    private AddSingleCaseToMultipleService addSingleCaseToMultipleService;

    @MockBean
    private ClerkService clerkService;

    @MockBean
    private FileLocationSelectionService fileLocationSelectionService;

    @MockBean
    private ScotlandFileLocationSelectionService scotlandFileLocationSelectionService;

    @MockBean
    private FixCaseApiService fixCaseApiService;
    @MockBean
    private Et1VettingService et1VettingService;

    @MockBean
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @MockBean
    private CaseFlagsService caseFlagsService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private Et1SubmissionService et1SubmissionService;

    @MockBean
    private NocRespondentHelper nocRespondentHelper;
    @MockBean
    private CaseManagementLocationService caseManagementLocationService;
    private MockMvc mvc;
    private JsonNode requestContent;
    private JsonNode requestContent2;
    private JsonNode requestContent3;
    private SubmitEvent submitEvent;
    private DefaultValues defaultValues;

    private CCDRequest ccdRequest;

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleV1.json")).toURI()));
        requestContent2 = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleV2.json")).toURI()));
        requestContent3 = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleV3.json")).toURI()));

        objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/CaseCloseEvent_ValidHearingStatusCaseDetails.json")).toURI()));

        ccdRequest = objectMapper.readValue(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleV1.json")).toURI()), CCDRequest.class);
    }

    @BeforeEach
    @Override
    @SneakyThrows
    public void setUp() {
        super.setUp();
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        when(featureToggleService.isHmcEnabled()).thenReturn(true);

        doRequestSetUp();
        submitEvent = new SubmitEvent();
        submitEvent.setCaseData(new CaseData());
        defaultValues = DefaultValues.builder()
                .positionType("Awaiting ET3")
                .claimantTypeOfClaimant(INDIVIDUAL_TYPE_CLAIMANT)
                .managingOffice(TribunalOffice.GLASGOW.getOfficeName())
                .caseType(SINGLE_CASE_TYPE)
                .tribunalCorrespondenceAddressLine1("")
                .tribunalCorrespondenceAddressLine2("")
                .tribunalCorrespondenceAddressLine3("")
                .tribunalCorrespondenceTown("")
                .tribunalCorrespondencePostCode("")
                .tribunalCorrespondenceTelephone("3577131270")
                .tribunalCorrespondenceFax("7577126570")
                .tribunalCorrespondenceDX("123456")
                .tribunalCorrespondenceEmail("manchester@gmail.com")
                .build();
    }

    @Test
    @SneakyThrows
    void createCase() {
        when(caseCreationForCaseWorkerService.caseCreationRequest(any(CCDRequest.class),
                eq(AUTH_TOKEN))).thenReturn(submitEvent);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATION_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void retrieveCase() {
        when(caseRetrievalForCaseWorkerService.caseRetrievalRequest(eq(AUTH_TOKEN),
                anyString(), anyString(), anyString()))
                .thenReturn(submitEvent);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RETRIEVE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void retrieveCases() {
        List<SubmitEvent> submitEventList = Collections.singletonList(submitEvent);
        when(caseRetrievalForCaseWorkerService.casesRetrievalRequest(any(CCDRequest.class),
                eq(AUTH_TOKEN))).thenReturn(submitEventList);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RETRIEVE_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void updateCase() {
        when(caseUpdateForCaseWorkerService.caseUpdateRequest(any(CCDRequest.class),
                eq(AUTH_TOKEN))).thenReturn(submitEvent);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void preDefaultValues() {
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(defaultValues);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_DEFAULT_VALUES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void postDefaultValuesFromET1WithPositionTypeDefined() {
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(defaultValues);
        when(singleReferenceService.createReference(anyString())).thenReturn("5100001/2019");
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
            .thenReturn(ccdRequest.getCaseDetails().getCaseData());
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void postDefaultValues() {
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(defaultValues);
        when(singleReferenceService.createReference(anyString())).thenReturn("5100001/2019");
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
            .thenReturn(ccdRequest.getCaseDetails().getCaseData());
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendCaseDetails() {
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(defaultValues);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseState(any(CaseDetails.class))).thenReturn(true);
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendCaseDetails_noErrors() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateReceiptDate(any(CaseDetails.class))).thenReturn(new ArrayList<>());
        when(eventValidationService.validateCaseState(any(CaseDetails.class))).thenReturn(true);
        when(eventValidationService.validateCurrentPosition(any(CaseDetails.class))).thenReturn(true);
        when(defaultValuesReaderService.getDefaultValues(any(String.class))).thenReturn(defaultValues);
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                        .content(requestContent2.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendCaseDetailsWithErrors() {
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(defaultValues);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseState(any(CaseDetails.class))).thenReturn(false);
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is("null Case has not been Accepted.")))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendClaimantDetails() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_CLAIMANT_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentDetails() {
        when(caseManagementForCaseWorkerService.struckOutRespondents(any(CCDRequest.class)))
                .thenReturn(submitEvent.getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doNothing().when(nocRespondentHelper).amendRespondentNameRepresentativeNames(any(CaseData.class));
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                        .content(requestContent2.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentDetails_moreThan10Respondents() {
        when(caseManagementForCaseWorkerService.struckOutRespondents(any(CCDRequest.class)))
                .thenReturn(submitEvent.getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateMaximumSize(any(CaseData.class)))
                .thenReturn(Optional.of("Max respondents exceeded"));

        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                        .content(requestContent2.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(nocRespondentHelper, times(0)).amendRespondentNameRepresentativeNames(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void amendRespondentDetails_noRepCollection() {
        when(caseManagementForCaseWorkerService.struckOutRespondents(any(CCDRequest.class)))
                .thenReturn(submitEvent.getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateMaximumSize(any(CaseData.class)))
                .thenReturn(Optional.empty());
        ccdRequest.getCaseDetails().getCaseData().setRepCollection(null);
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(nocRespondentHelper, times(0)).amendRespondentNameRepresentativeNames(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void amendRespondentDetails_UpdateCounter() {
        when(caseManagementForCaseWorkerService.struckOutRespondents(any(CCDRequest.class)))
                .thenReturn(submitEvent.getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doNothing().when(nocRespondentHelper).amendRespondentNameRepresentativeNames(any(CaseData.class));

        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);

        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                        .content(requestContent2.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseManagementForCaseWorkerService, times(1))
                .updateWorkAllocationField(anyList(), any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void amendRespondentDetailsContinuingClaim() {
        when(caseManagementForCaseWorkerService.continuingRespondent(any(CCDRequest.class)))
                .thenReturn(submitEvent.getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
            .thenReturn(ccdRequest.getCaseDetails().getCaseData());
        doNothing().when(nocRespondentHelper).amendRespondentNameRepresentativeNames(any(CaseData.class));
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentative() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
                .thenReturn(ccdRequest.getCaseDetails().getCaseData());
        when(nocRespondentRepresentativeService.prepopulateOrgAddress(any(CaseData.class), anyString()))
                .thenReturn(ccdRequest.getCaseDetails().getCaseData());

        mvc.perform(post(AMEND_RESPONDENT_REPRESENTATIVE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(nocRespondentRepresentativeService, times(1)).updateNonMyHmctsOrgIds(anyList());
    }

    @Test
    @SneakyThrows
    void updateHearing() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_HEARING_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void allocateHearing()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(ALLOCATE_HEARING_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void restrictedCases() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RESTRICTED_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(caseFlagsService, times(1)).setPrivateHearingFlag(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void dynamicRestrictedCases() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_RESTRICTED_REPORTING_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendHearing() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_HEARING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midEventAmendHearing() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_EVENT_AMEND_HEARING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendCaseState() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_CASE_STATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendCaseStateValidationErrors() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doCallRealMethod().when(eventValidationService).validateJurisdictionOutcome(any(CaseData.class),
                eq(false), eq(false), eq(new ArrayList<>()));
        mvc.perform(post(AMEND_CASE_STATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midRespondentAddress() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_RESPONDENT_ADDRESS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void jurisdictionValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(JURISDICTION_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void judgementValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(JUDGEMENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void depositValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DEPOSIT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midRespondentAddressPopulated() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_RESPONDENT_ADDRESS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midRespondentECC() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseManagementForCaseWorkerService.createECC(any(CaseDetails.class),
                eq(AUTH_TOKEN), anyList(), anyString()))
                .thenReturn(new CaseData());
        mvc.perform(post(MID_RESPONDENT_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void createECC() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseManagementForCaseWorkerService.createECC(any(CaseDetails.class),
                eq(AUTH_TOKEN), anyList(), anyString()))
                .thenReturn(new CaseData());
        mvc.perform(post(CREATE_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void linkOriginalCaseECC() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseManagementForCaseWorkerService.createECC(any(CaseDetails.class),
                eq(AUTH_TOKEN), anyList(), anyString()))
                .thenReturn(new CaseData());
        mvc.perform(post(LINK_ORIGINAL_CASE_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void singleCaseMultipleMidEventValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SINGLE_CASE_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void hearingMidEventValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(HEARING_MID_EVENT_VALIDATION_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void bfActions()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(BF_ACTIONS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void dynamicListBfActions()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_LIST_BF_ACTIONS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToStartDisposal()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseBeforeCloseEvent(any(CaseData.class),
                        eq(false), eq(false), anyList())).thenReturn(anyList());

        mvc.perform(post(ABOUT_TO_START_DISPOSAL_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(clerkService, times(1)).initialiseClerkResponsible(any(CaseData.class));
        verify(fileLocationSelectionService, times(1)).initialiseFileLocation(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void aboutToStartDisposalCaseCloseEventValidationErrors()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseBeforeCloseEvent(any(CaseData.class),
                eq(false), eq(false), anyList())).thenReturn(List.of("test error"));

        mvc.perform(post(ABOUT_TO_START_DISPOSAL_URL)
                .content(requestContent3.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void dynamicRespondentRepresentativeNamesErrors()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_RESPONDENT_REPRESENTATIVE_NAMES_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void dynamicDepositOrderErrors()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_DEPOSIT_ORDER_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void dynamicRestrictedReportingErrors()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_RESTRICTED_REPORTING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void initialiseAmendCaseDetails()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INITIALISE_AMEND_CASE_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(clerkService, times(1)).initialiseClerkResponsible(any(CaseData.class));
        verify(fileLocationSelectionService, times(1)).initialiseFileLocation(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void reinstateClosedCaseMidEventValidation()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseCloseValidator.validateReinstateClosedCaseMidEvent(any(CaseData.class))).thenReturn(anyList());
        mvc.perform(post(REINSTATE_CLOSED_CASE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void reinstateClosedCaseMidEventValidationErrors()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseCloseValidator.validateReinstateClosedCaseMidEvent(any(CaseData.class)))
                .thenReturn(List.of("test error"));
        mvc.perform(post(REINSTATE_CLOSED_CASE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void createCaseError400()  {
        mvc.perform(post(CREATION_CASE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void retrieveCaseError400()  {
        mvc.perform(post(RETRIEVE_CASE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void retrieveCasesError400()  {
        mvc.perform(post(RETRIEVE_CASES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updateCaseError400()  {
        mvc.perform(post(UPDATE_CASE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void preDefaultValuesError400()  {
        mvc.perform(post(PRE_DEFAULT_VALUES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void postDefaultValuesError400()  {
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void amendCaseDetailsError400()  {
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void amendClaimantDetailsError400()  {
        mvc.perform(post(AMEND_CLAIMANT_DETAILS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void amendRespondentDetailsError400()  {
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeError400()  {
        mvc.perform(post(AMEND_RESPONDENT_REPRESENTATIVE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updateHearingError400()  {
        mvc.perform(post(UPDATE_HEARING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void allocateHearingError400()  {
        mvc.perform(post(ALLOCATE_HEARING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void restrictedCasesError400()  {
        mvc.perform(post(RESTRICTED_CASES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void amendHearingError400()  {
        mvc.perform(post(AMEND_HEARING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midEventAmendHearingError400()  {
        mvc.perform(post(MID_EVENT_AMEND_HEARING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void amendCaseStateError400()  {
        mvc.perform(post(AMEND_CASE_STATE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midRespondentAddressError400()  {
        mvc.perform(post(MID_RESPONDENT_ADDRESS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void jurisdictionValidationError400()  {
        mvc.perform(post(JURISDICTION_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void judgementValidationError400()  {
        mvc.perform(post(JUDGEMENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void depositValidationError400()  {
        mvc.perform(post(DEPOSIT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midRespondentECCError400()  {
        mvc.perform(post(MID_RESPONDENT_ECC_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createECCError400()  {
        mvc.perform(post(CREATE_ECC_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void linkOriginalCaseECCError400()  {
        mvc.perform(post(LINK_ORIGINAL_CASE_ECC_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void singleCaseMultipleMidEventValidationError400()  {
        mvc.perform(post(SINGLE_CASE_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void hearingMidEventValidationError400()  {
        mvc.perform(post(HEARING_MID_EVENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void bfActionsError400()  {
        mvc.perform(post(BF_ACTIONS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dynamicListBfActionsError400()  {
        mvc.perform(post(DYNAMIC_LIST_BF_ACTIONS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void aboutToStartDisposalError400()  {
        mvc.perform(post(ABOUT_TO_START_DISPOSAL_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dynamicRespondentRepresentativeNamesUrlError400()  {
        mvc.perform(post(DYNAMIC_RESPONDENT_REPRESENTATIVE_NAMES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dynamicDepositOrderError400()  {
        mvc.perform(post(DYNAMIC_DEPOSIT_ORDER_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dynamicRestrictedReportingError400()  {
        mvc.perform(post(DYNAMIC_RESTRICTED_REPORTING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dynamicJudgmentError400()  {
        mvc.perform(post(DYNAMIC_JUDGMENT_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void judgmentSubmittedError400()  {
        mvc.perform(post(JUDGEMENT_SUBMITTED_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dynamicFixCaseAPIError400()  {
        mvc.perform(post(AMEND_FIX_CASE_API_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(fixCaseApiService, never()).checkUpdateMultipleReference(any(CaseDetails.class), anyString());
    }

    @Test
    @SneakyThrows
    void reinstateClosedCaseError400()  {
        mvc.perform(post(REINSTATE_CLOSED_CASE_MID_EVENT_VALIDATION_URL)
                        .content("error")
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(caseCloseValidator, never()).validateReinstateClosedCaseMidEvent(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void createCaseError500()  {
        when(caseCreationForCaseWorkerService.caseCreationRequest(any(CCDRequest.class),
                eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATION_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void retrieveCaseError500()  {
        when(caseRetrievalForCaseWorkerService.caseRetrievalRequest(eq(AUTH_TOKEN), anyString(),
                anyString(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RETRIEVE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void retrieveCasesError500()  {
        when(caseRetrievalForCaseWorkerService.casesRetrievalRequest(any(CCDRequest.class),
                eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RETRIEVE_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void updateCaseError500()  {
        when(caseUpdateForCaseWorkerService.caseUpdateRequest(any(CCDRequest.class),
                eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void preDefaultValuesError500()  {
        when(defaultValuesReaderService.getClaimantTypeOfClaimant()).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_DEFAULT_VALUES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void postDefaultValuesError500()  {
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenThrow(
                new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateReceiptDate(any(CaseDetails.class))).thenThrow(
                new InternalException(ERROR_MESSAGE));
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void amendCaseDetailsError500()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseState(any(CaseDetails.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(eventValidationService.validateCurrentPosition(any(CaseDetails.class))).thenReturn(true);
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void amendRespondentDetailsError500()  {
        when(caseManagementForCaseWorkerService.struckOutRespondents(any(CCDRequest.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void amendRespondentDetailsClaimContinuingError500()  {
        when(caseManagementForCaseWorkerService.continuingRespondent(any(CCDRequest.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void singleCaseMultipleMidEventValidationError500()  {
        doThrow(new InternalException(ERROR_MESSAGE)).when(singleCaseMultipleMidEventValidationService)
                .singleCaseMultipleValidationLogic(
                eq(AUTH_TOKEN), any(CaseDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SINGLE_CASE_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void createCaseErrorForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CREATION_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void retrieveCaseForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(RETRIEVE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void retrieveCasesForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(RETRIEVE_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void updateCaseForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void preDefaultValuesForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(PRE_DEFAULT_VALUES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void postDefaultValuesForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void amendCaseDetailsForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void amendClaimantDetailsForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_CLAIMANT_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void amendRespondentDetailsForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_RESPONDENT_REPRESENTATIVE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void updateHearingForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_HEARING_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void allocateHearingForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ALLOCATE_HEARING_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void restrictedCasesForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(RESTRICTED_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void amendHearingForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_HEARING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midEventAmendHearingForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_EVENT_AMEND_HEARING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void amendCaseStateForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_CASE_STATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midRespondentAddressForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_RESPONDENT_ADDRESS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void jurisdictionValidationForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(JURISDICTION_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void judgementValidationForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(JUDGEMENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void depositValidationForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DEPOSIT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midRespondentAddressPopulatedForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_RESPONDENT_ADDRESS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midRespondentECCForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_RESPONDENT_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void createECCForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CREATE_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void linkOriginalCaseECCForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(LINK_ORIGINAL_CASE_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void singleCaseMultipleMidEventValidationForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SINGLE_CASE_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void hearingMidEventValidationForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(HEARING_MID_EVENT_VALIDATION_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void bfActionsForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(BF_ACTIONS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicListBfActionsForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_LIST_BF_ACTIONS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToStartDisposalForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ABOUT_TO_START_DISPOSAL_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(clerkService, never()).initialiseClerkResponsible(any(CaseData.class));
        verify(fileLocationSelectionService, never()).initialiseFileLocation(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void dynamicRespondentRepresentativeNamesUrlForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_RESPONDENT_REPRESENTATIVE_NAMES_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicDepositOrderUrlForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_DEPOSIT_ORDER_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicRestrictedReportingUrlForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_RESTRICTED_REPORTING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicJudgmentUrlForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_JUDGMENT_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void judgmentSubmittedUrlForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(JUDGEMENT_SUBMITTED_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicFixCaseAPIForbidden()  {
        CaseDetails caseDetails = new CaseDetails();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_FIX_CASE_API_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(fixCaseApiService, never()).checkUpdateMultipleReference(caseDetails, AUTH_TOKEN);
    }

    @Test
    @SneakyThrows
    void reinstateClosedCaseForbidden()  {
        CaseData caseData = new CaseData();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(REINSTATE_CLOSED_CASE_MID_EVENT_VALIDATION_URL)
                        .content(requestContent2.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(caseCloseValidator, never()).validateReinstateClosedCaseMidEvent(caseData);
    }

    @Test
    @SneakyThrows
    void initialiseAmendCaseDetailsForbidden()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(INITIALISE_AMEND_CASE_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(clerkService, never()).initialiseClerkResponsible(any(CaseData.class));
        verify(fileLocationSelectionService, never()).initialiseFileLocation(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void addServiceIdUrl_tokenOk()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(ADD_SERVICE_ID_URL)
                .content(requestContent2.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(caseManagementForCaseWorkerService, times(1))
                .setHmctsServiceIdSupplementary(any(CaseDetails.class));
    }

    @Test
    @SneakyThrows
    void addServiceIdUrl_tokenFail()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ADD_SERVICE_ID_URL)
                .content(requestContent2.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void addServiceIdUrl_badRequest()  {
        mvc.perform(post(ADD_SERVICE_ID_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void setPostDefaultValuesAddsCaseManagementLocationCode()  {
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(defaultValues);
        when(singleReferenceService.createReference(anyString())).thenReturn("5100001/2019");
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
                .thenReturn(ccdRequest.getCaseDetails().getCaseData());
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(caseManagementLocationService, times(1))
                .setCaseManagementLocationCode(any(CaseData.class));
    }

    @ParameterizedTest
    @CsvSource({
        "initiateCase, 1",
        "SUBMIT_CASE_DRAFT, 1",
        "submitEt1Draft, 1",
        "randomCCDEvent, 0"
    })
    @SneakyThrows
    void setNoCAnswersWhenSubmittingACase(String eventId, int expectedInvocations)  {
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(defaultValues);
        when(singleReferenceService.createReference(anyString())).thenReturn("5100001/2019");
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any(CaseData.class)))
                .thenReturn(ccdRequest.getCaseDetails().getCaseData());

        ccdRequest.setEventId(eventId);

        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(nocRespondentRepresentativeService, times(expectedInvocations))
                .prepopulateOrgPolicyAndNoc(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void judgmentSubmitted_tokenOk()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        DraftAndSignJudgement draftAndSignJudgement = DraftAndSignJudgement.builder()
                .isJudgement(YES)
                .furtherDirections("Dummy directions")
                .build();
        ccdRequest.getCaseDetails().getCaseData().setDraftAndSignJudgement(draftAndSignJudgement);
        mvc.perform(post(JUDGEMENT_SUBMITTED_URL)
                        .content(requestContent2.toString())
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()))
                .andExpect(jsonPath("$.data.draftAndSignJudgement", nullValue()));
    }
}
