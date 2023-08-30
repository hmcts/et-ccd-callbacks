package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AddSingleCaseToMultipleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseCloseValidator;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseCreationForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseRetrievalForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseUpdateForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClerkService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DepositOrderValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FixCaseApiService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.JudgmentValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScotlandFileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SingleCaseMultipleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SingleReferenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CaseActionsForCaseWorkerController.class)
@ContextConfiguration(classes = DocmosisApplication.class)
class CaseActionsForCaseWorkerControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
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
    private VerifyTokenService verifyTokenService;

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
    private NocRespondentHelper nocRespondentHelper;

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
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();

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
    void createCase() throws Exception {
        when(caseCreationForCaseWorkerService.caseCreationRequest(isA(CCDRequest.class),
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
    void retrieveCase() throws Exception {
        when(caseRetrievalForCaseWorkerService.caseRetrievalRequest(eq(AUTH_TOKEN),
                isA(String.class), isA(String.class), isA(String.class)))
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
    void retrieveCases() throws Exception {
        List<SubmitEvent> submitEventList = Collections.singletonList(submitEvent);
        when(caseRetrievalForCaseWorkerService.casesRetrievalRequest(isA(CCDRequest.class),
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
    void updateCase() throws Exception {
        when(caseUpdateForCaseWorkerService.caseUpdateRequest(isA(CCDRequest.class),
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
    void preDefaultValues() throws Exception {
        when(defaultValuesReaderService.getDefaultValues(isA(String.class))).thenReturn(defaultValues);
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
    void postDefaultValuesFromET1WithPositionTypeDefined() throws Exception {
        when(defaultValuesReaderService.getDefaultValues(isA(String.class))).thenReturn(defaultValues);
        when(singleReferenceService.createReference(isA(String.class))).thenReturn("5100001/2019");
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any()))
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
    void postDefaultValues() throws Exception {
        when(defaultValuesReaderService.getDefaultValues(isA(String.class))).thenReturn(defaultValues);
        when(singleReferenceService.createReference(isA(String.class))).thenReturn("5100001/2019");
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any()))
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
    void amendCaseDetails() throws Exception {
        when(defaultValuesReaderService.getDefaultValues(isA(String.class))).thenReturn(defaultValues);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseState(isA(CaseDetails.class))).thenReturn(true);
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
    void amendCaseDetailsWithErrors() throws Exception {
        when(defaultValuesReaderService.getDefaultValues(isA(String.class))).thenReturn(defaultValues);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseState(isA(CaseDetails.class))).thenReturn(false);
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
    void amendClaimantDetails() throws Exception {
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
    void amendRespondentDetails() throws Exception {
        when(caseManagementForCaseWorkerService.struckOutRespondents(isA(CCDRequest.class)))
                .thenReturn(submitEvent.getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any()))
            .thenReturn(ccdRequest.getCaseDetails().getCaseData());
        doNothing().when(nocRespondentHelper).amendRespondentNameRepresentativeNames(any());
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
    void amendRespondentDetailsContinuingClaim() throws Exception {
        when(caseManagementForCaseWorkerService.continuingRespondent(isA(CCDRequest.class)))
                .thenReturn(submitEvent.getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any()))
            .thenReturn(ccdRequest.getCaseDetails().getCaseData());
        doNothing().when(nocRespondentHelper).amendRespondentNameRepresentativeNames(any());
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
    void amendRespondentRepresentative() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any()))
                .thenReturn(ccdRequest.getCaseDetails().getCaseData());
        when(nocRespondentRepresentativeService.prepopulateOrgAddress(any(), anyString()))
                .thenReturn(ccdRequest.getCaseDetails().getCaseData());

        mvc.perform(post(AMEND_RESPONDENT_REPRESENTATIVE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void updateHearing() throws Exception {
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
    void allocateHearing() throws Exception {
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
    void restrictedCases() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RESTRICTED_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void dynamicRestrictedCases() throws Exception {
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
    void amendHearing() throws Exception {
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
    void midEventAmendHearing() throws Exception {
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
    void amendCaseState() throws Exception {
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
    void amendCaseStateValidationErrors() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doCallRealMethod().when(eventValidationService).validateJurisdictionOutcome(isA(CaseData.class),
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
    void midRespondentAddress() throws Exception {
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
    void jurisdictionValidation() throws Exception {
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
    void judgementValidation() throws Exception {
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
    void depositValidation() throws Exception {
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
    void midRespondentAddressPopulated() throws Exception {
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
    void midRespondentECC() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseManagementForCaseWorkerService.createECC(isA(CaseDetails.class),
                eq(AUTH_TOKEN), isA(List.class), isA(String.class)))
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
    void createECC() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseManagementForCaseWorkerService.createECC(isA(CaseDetails.class),
                eq(AUTH_TOKEN), isA(List.class), isA(String.class)))
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
    void linkOriginalCaseECC() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseManagementForCaseWorkerService.createECC(isA(CaseDetails.class),
                eq(AUTH_TOKEN), isA(List.class), isA(String.class)))
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
    void singleCaseMultipleMidEventValidation() throws Exception {
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
    void hearingMidEventValidation() throws Exception {
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
    void bfActions() throws Exception {
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
    void dynamicListBfActions() throws Exception {
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
    void aboutToStartDisposal() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseBeforeCloseEvent(isA(CaseData.class),
                        eq(false), eq(false), anyList())).thenReturn(anyList());

        mvc.perform(post(ABOUT_TO_START_DISPOSAL_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(clerkService, times(1)).initialiseClerkResponsible(isA(CaseData.class));
        verify(fileLocationSelectionService, times(1)).initialiseFileLocation(isA(CaseData.class));
    }

    @Test
    void aboutToStartDisposalCaseCloseEventValidationErrors() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseBeforeCloseEvent(isA(CaseData.class),
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
    void dynamicRespondentRepresentativeNamesErrors() throws Exception {
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
    void dynamicDepositOrderErrors() throws Exception {
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
    void dynamicRestrictedReportingErrors() throws Exception {
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
    void initialiseAmendCaseDetails() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INITIALISE_AMEND_CASE_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(clerkService, times(1)).initialiseClerkResponsible(isA(CaseData.class));
        verify(fileLocationSelectionService, times(1)).initialiseFileLocation(isA(CaseData.class));
    }

    @Test
    void reinstateClosedCaseMidEventValidation() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseCloseValidator.validateReinstateClosedCaseMidEvent(isA(CaseData.class))).thenReturn(anyList());
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
    void reinstateClosedCaseMidEventValidationErrors() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(caseCloseValidator.validateReinstateClosedCaseMidEvent(isA(CaseData.class)))
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
    void createCaseError400() throws Exception {
        mvc.perform(post(CREATION_CASE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retrieveCaseError400() throws Exception {
        mvc.perform(post(RETRIEVE_CASE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retrieveCasesError400() throws Exception {
        mvc.perform(post(RETRIEVE_CASES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCaseError400() throws Exception {
        mvc.perform(post(UPDATE_CASE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void preDefaultValuesError400() throws Exception {
        mvc.perform(post(PRE_DEFAULT_VALUES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postDefaultValuesError400() throws Exception {
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void amendCaseDetailsError400() throws Exception {
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void amendClaimantDetailsError400() throws Exception {
        mvc.perform(post(AMEND_CLAIMANT_DETAILS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void amendRespondentDetailsError400() throws Exception {
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void amendRespondentRepresentativeError400() throws Exception {
        mvc.perform(post(AMEND_RESPONDENT_REPRESENTATIVE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateHearingError400() throws Exception {
        mvc.perform(post(UPDATE_HEARING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allocateHearingError400() throws Exception {
        mvc.perform(post(ALLOCATE_HEARING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void restrictedCasesError400() throws Exception {
        mvc.perform(post(RESTRICTED_CASES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void amendHearingError400() throws Exception {
        mvc.perform(post(AMEND_HEARING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void midEventAmendHearingError400() throws Exception {
        mvc.perform(post(MID_EVENT_AMEND_HEARING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void amendCaseStateError400() throws Exception {
        mvc.perform(post(AMEND_CASE_STATE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void midRespondentAddressError400() throws Exception {
        mvc.perform(post(MID_RESPONDENT_ADDRESS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void jurisdictionValidationError400() throws Exception {
        mvc.perform(post(JURISDICTION_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void judgementValidationError400() throws Exception {
        mvc.perform(post(JUDGEMENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void depositValidationError400() throws Exception {
        mvc.perform(post(DEPOSIT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void midRespondentECCError400() throws Exception {
        mvc.perform(post(MID_RESPONDENT_ECC_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createECCError400() throws Exception {
        mvc.perform(post(CREATE_ECC_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void linkOriginalCaseECCError400() throws Exception {
        mvc.perform(post(LINK_ORIGINAL_CASE_ECC_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void singleCaseMultipleMidEventValidationError400() throws Exception {
        mvc.perform(post(SINGLE_CASE_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hearingMidEventValidationError400() throws Exception {
        mvc.perform(post(HEARING_MID_EVENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bfActionsError400() throws Exception {
        mvc.perform(post(BF_ACTIONS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dynamicListBfActionsError400() throws Exception {
        mvc.perform(post(DYNAMIC_LIST_BF_ACTIONS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aboutToStartDisposalError400() throws Exception {
        mvc.perform(post(ABOUT_TO_START_DISPOSAL_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dynamicRespondentRepresentativeNamesUrlError400() throws Exception {
        mvc.perform(post(DYNAMIC_RESPONDENT_REPRESENTATIVE_NAMES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dynamicDepositOrderError400() throws Exception {
        mvc.perform(post(DYNAMIC_DEPOSIT_ORDER_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dynamicRestrictedReportingError400() throws Exception {
        mvc.perform(post(DYNAMIC_RESTRICTED_REPORTING_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dynamicJudgmentError400() throws Exception {
        mvc.perform(post(DYNAMIC_JUDGMENT_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void judgmentSubmittedError400() throws Exception {
        mvc.perform(post(JUDGEMENT_SUBMITTED_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dynamicFixCaseAPIError400() throws Exception {
        mvc.perform(post(AMEND_FIX_CASE_API_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(fixCaseApiService, never()).checkUpdateMultipleReference(any(CaseDetails.class), anyString());
    }

    @Test
    void reinstateClosedCaseError400() throws Exception {
        mvc.perform(post(REINSTATE_CLOSED_CASE_MID_EVENT_VALIDATION_URL)
                        .content("error")
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(caseCloseValidator, never()).validateReinstateClosedCaseMidEvent(any(CaseData.class));
    }

    @Test
    void createCaseError500() throws Exception {
        when(caseCreationForCaseWorkerService.caseCreationRequest(isA(CCDRequest.class),
                eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATION_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void retrieveCaseError500() throws Exception {
        when(caseRetrievalForCaseWorkerService.caseRetrievalRequest(eq(AUTH_TOKEN), isA(String.class),
                isA(String.class), isA(String.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RETRIEVE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void retrieveCasesError500() throws Exception {
        when(caseRetrievalForCaseWorkerService.casesRetrievalRequest(isA(CCDRequest.class),
                eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RETRIEVE_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateCaseError500() throws Exception {
        when(caseUpdateForCaseWorkerService.caseUpdateRequest(isA(CCDRequest.class),
                eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void preDefaultValuesError500() throws Exception {
        when(defaultValuesReaderService.getClaimantTypeOfClaimant()).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_DEFAULT_VALUES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postDefaultValuesError500() throws Exception {
        when(defaultValuesReaderService.getDefaultValues(isA(String.class))).thenThrow(
                new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateReceiptDate(isA(CaseDetails.class))).thenThrow(
                new InternalException(ERROR_MESSAGE));
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void amendCaseDetailsError500() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(eventValidationService.validateCaseState(isA(CaseDetails.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(eventValidationService.validateCurrentPosition(isA(CaseDetails.class))).thenReturn(true);
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void amendRespondentDetailsError500() throws Exception {
        when(caseManagementForCaseWorkerService.struckOutRespondents(isA(CCDRequest.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void amendRespondentDetailsClaimContinuingError500() throws Exception {
        when(caseManagementForCaseWorkerService.continuingRespondent(isA(CCDRequest.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void singleCaseMultipleMidEventValidationError500() throws Exception {
        doThrow(new InternalException(ERROR_MESSAGE)).when(singleCaseMultipleMidEventValidationService)
                .singleCaseMultipleValidationLogic(
                eq(AUTH_TOKEN), isA(CaseDetails.class), isA(List.class));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SINGLE_CASE_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void createCaseErrorForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CREATION_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void retrieveCaseForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(RETRIEVE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void retrieveCasesForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(RETRIEVE_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCaseForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void preDefaultValuesForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(PRE_DEFAULT_VALUES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void postDefaultValuesForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(POST_DEFAULT_VALUES_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void amendCaseDetailsForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_CASE_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void amendClaimantDetailsForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_CLAIMANT_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void amendRespondentDetailsForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_RESPONDENT_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void amendRespondentRepresentativeForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_RESPONDENT_REPRESENTATIVE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateHearingForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_HEARING_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void allocateHearingForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ALLOCATE_HEARING_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void restrictedCasesForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(RESTRICTED_CASES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void amendHearingForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_HEARING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void midEventAmendHearingForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_EVENT_AMEND_HEARING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void amendCaseStateForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_CASE_STATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void midRespondentAddressForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_RESPONDENT_ADDRESS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void jurisdictionValidationForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(JURISDICTION_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void judgementValidationForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(JUDGEMENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void depositValidationForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DEPOSIT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void midRespondentAddressPopulatedForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_RESPONDENT_ADDRESS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void midRespondentECCForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_RESPONDENT_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void createECCForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CREATE_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void linkOriginalCaseECCForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(LINK_ORIGINAL_CASE_ECC_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void singleCaseMultipleMidEventValidationForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SINGLE_CASE_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void hearingMidEventValidationForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(HEARING_MID_EVENT_VALIDATION_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void bfActionsForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(BF_ACTIONS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void dynamicListBfActionsForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_LIST_BF_ACTIONS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void aboutToStartDisposalForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ABOUT_TO_START_DISPOSAL_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(clerkService, never()).initialiseClerkResponsible(isA(CaseData.class));
        verify(fileLocationSelectionService, never()).initialiseFileLocation(isA(CaseData.class));
    }

    @Test
    void dynamicRespondentRepresentativeNamesUrlForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_RESPONDENT_REPRESENTATIVE_NAMES_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void dynamicDepositOrderUrlForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_DEPOSIT_ORDER_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void dynamicRestrictedReportingUrlForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_RESTRICTED_REPORTING_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void dynamicJudgmentUrlForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_JUDGMENT_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void judgmentSubmittedUrlForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(JUDGEMENT_SUBMITTED_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void dynamicFixCaseAPIForbidden() throws Exception {
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
    void reinstateClosedCaseForbidden() throws Exception {
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
    void initialiseAmendCaseDetailsForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(INITIALISE_AMEND_CASE_DETAILS_URL)
                .content(requestContent2.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(clerkService, never()).initialiseClerkResponsible(isA(CaseData.class));
        verify(fileLocationSelectionService, never()).initialiseFileLocation(isA(CaseData.class));
    }

    @Test
    public void addServiceIdUrl_tokenOk() throws Exception {
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
                .setHmctsServiceIdSupplementary(any(), any());
    }

    @Test
    public void addServiceIdUrl_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ADD_SERVICE_ID_URL)
                .content(requestContent2.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void addServiceIdUrl_badRequest() throws Exception {
        mvc.perform(post(ADD_SERVICE_ID_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}