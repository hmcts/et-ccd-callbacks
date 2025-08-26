package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.CLAIMANT_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.CLAIM_DETAILS_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.ERROR_CASE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.MULTIPLE_OPTION_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.TRIAGE_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS;
import static uk.gov.hmcts.ethos.utils.CaseDataBuilder.createGenericAddress;

@ExtendWith(SpringExtension.class)
@WebMvcTest({Et1ReppedController.class, JsonMapper.class})
class Et1ReppedControllerTest {

    private static final String VALIDATE_POSTCODE = "/et1Repped/createCase/validatePostcode";
    private static final String OFFICE_ERROR = "/et1Repped/createCase/officeError";
    private static final String CREATE_CASE_ABOUT_TO_SUBMIT = "/et1Repped/createCase/aboutToSubmit";
    private static final String CREATE_CASE_SUBMITTED = "/et1Repped/createCase/submitted";
    private static final String VALIDATE_CLAIMANT_SEX = "/et1Repped/sectionOne/validateClaimantSex";
    private static final String VALIDATE_CLAIMANT_SUPPORT = "/et1Repped/sectionOne/validateClaimantSupport";
    private static final String VALIDATE_REPRESENTATIVE_INFORMATION =
            "/et1Repped/sectionOne/validateRepresentativeInformation";
    private static final String ABOUT_TO_SUBMIT_SECTION = "/et1Repped/aboutToSubmitSection";
    private static final String VALIDATE_CLAIMANT_WORKED = "/et1Repped/sectionTwo/validateClaimantWorked";
    private static final String VALIDATE_CLAIMANT_WORKING = "/et1Repped/sectionTwo/validateClaimantWorking";
    private static final String VALIDATE_CLAIMANT_WRITTEN_NOTICE_PERIOD =
            "/et1Repped/sectionTwo/validateClaimantWrittenNoticePeriod";
    private static final String VALIDATE_CLAIMANT_WORKING_NOTICE_PERIOD =
            "/et1Repped/sectionTwo/validateClaimantWorkingNoticePeriod";
    private static final String VALIDATE_CLAIMANT_NO_LONGER_WORKING =
            "/et1Repped/sectionTwo/validateClaimantNoLongerWorking";
    private static final String VALIDATE_CLAIMANT_PAY = "/et1Repped/sectionTwo/validateClaimantPay";
    private static final String VALIDATE_CLAIMANT_PENSION_BENEFITS =
            "/et1Repped/sectionTwo/validateClaimantPensionBenefits";
    private static final String VALIDATE_CLAIMANT_NEW_JOB = "/et1Repped/sectionTwo/validateClaimantNewJob";
    private static final String VALIDATE_CLAIMANT_NEW_JOB_PAY = "/et1Repped/sectionTwo/validateClaimantNewJobPay";
    private static final String GENERATE_RESPONDENT_PREAMBLE = "/et1Repped/sectionTwo/generateRespondentPreamble";
    private static final String GENERATE_WORK_ADDRESS_LABEL = "/et1Repped/sectionTwo/generateWorkAddressLabel";
    private static final String SECTION_COMPLETED = "/et1Repped/sectionCompleted";
    private static final String VALIDATE_WHISTLEBLOWING = "/et1Repped/sectionThree/validateWhistleblowing";
    private static final String VALIDATE_LINKED_CASES = "/et1Repped/sectionThree/validateLinkedCases";
    private static final String SUBMIT_CLAIM = "/et1Repped/submitClaim";
    private static final String SUBMIT_CLAIM_ABOUT_TO_START = "/et1Repped/submitClaim/aboutToStart";
    private static final String CLAIM_SUBMITTED = "/et1Repped/submitted";
    private static final String CREATE_DRAFT_ET1 = "/et1Repped/createDraftEt1";
    private static final String CREATE_DRAFT_ET1_SUBMITTED = "/et1Repped/createDraftEt1Submitted";
    private static final String VALIDATE_GROUNDS = "/et1Repped/validateGrounds";
    private static final String VALIDATE_HEARING_PREFERENCES = "/et1Repped/sectionOne/validateHearingPreferences";
    private static final String GENERATE_DOCUMENTS = "/et1Repped/generateDocuments";
    private static final String ABOUT_TO_START_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT =
            "/et1Repped/aboutToStartAmendClaimantRepresentativeContact";
    private static final String ABOUT_TO_SUBMIT_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT =
            "/et1Repped/aboutToSubmitAmendClaimantRepresentativeContact";
    private static final String MID_EVENT_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT =
            "/et1Repped/midEventAmendClaimantRepresentativeContact";

    private static final String AUTH_TOKEN = "some-token";
    private CCDRequest ccdRequest;
    private CCDRequest ccdRequest2;
    private CaseData caseData;
    private CCDRequest ccdRequestWithClaimantRepresentative;
    private CCDRequest ccdRequestWithoutCaseData;
    private CCDRequest ccdRequestWithoutClaimantRepresentative;

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private Et1ReppedService et1ReppedService;
    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private OrganisationClient organisationClient;
    @MockBean
    private Et1SubmissionService et1SubmissionService;
    @MockBean
    private CaseActionsForCaseWorkerController caseActionsForCaseWorkerController;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    // Below needed as reusing code in caseActionsForCaseWorkerController
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
    private NocRespondentHelper nocRespondentHelper;
    @MockBean
    private CaseManagementLocationService caseManagementLocationService;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        caseData.setEt1ReppedSectionOne(NO);
        caseData.setEt1ReppedSectionTwo(NO);
        caseData.setEt1ReppedSectionThree(NO);
        caseData.setClaimantFirstName("First");
        caseData.setClaimantLastName("Last");
        caseData.setRespondentType(ORGANISATION);
        caseData.setRespondentOrganisationName("Org");
        caseData.setRespondentAddress(createGenericAddress());

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .withCaseData(caseData)
                .build();

        CaseDetails caseDetails = generateCaseDetails();
        ccdRequest2 = CCDRequestBuilder.builder()
                .withCaseData(caseDetails.getCaseData())
                .withState(caseDetails.getState())
                .withCaseTypeId(caseDetails.getCaseTypeId())
                .withCaseId(caseDetails.getCaseId())
                .build();

        CaseData caseDataWithClaimantRepresentative = new CaseData();
        caseDataWithClaimantRepresentative.setEt1ReppedSectionOne(NO);
        caseDataWithClaimantRepresentative.setEt1ReppedSectionTwo(NO);
        caseDataWithClaimantRepresentative.setEt1ReppedSectionThree(NO);
        caseDataWithClaimantRepresentative.setClaimantFirstName("First");
        caseDataWithClaimantRepresentative.setClaimantLastName("Last");
        caseDataWithClaimantRepresentative.setRespondentType(ORGANISATION);
        caseDataWithClaimantRepresentative.setRespondentOrganisationName("Org");
        caseDataWithClaimantRepresentative.setRespondentAddress(createGenericAddress());
        caseDataWithClaimantRepresentative.setClaimantRepresentedQuestion("Yes");
        caseDataWithClaimantRepresentative.setRepresentativeClaimantType(
                RepresentedTypeC.builder().representativeAddress(createGenericAddress())
                        .representativePhoneNumber("07444518903").build());
        ccdRequestWithClaimantRepresentative = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithClaimantRepresentative)
                .withState(caseDetails.getState())
                .withCaseTypeId(caseDetails.getCaseTypeId())
                .withCaseId(caseDetails.getCaseId())
                .build();

        CaseData caseDataWithOutClaimantRepresentative = new CaseData();
        caseDataWithOutClaimantRepresentative.setEt1ReppedSectionOne(NO);
        caseDataWithOutClaimantRepresentative.setEt1ReppedSectionTwo(NO);
        caseDataWithOutClaimantRepresentative.setEt1ReppedSectionThree(NO);
        caseDataWithOutClaimantRepresentative.setClaimantFirstName("First");
        caseDataWithOutClaimantRepresentative.setClaimantLastName("Last");
        caseDataWithOutClaimantRepresentative.setRespondentType(ORGANISATION);
        caseDataWithOutClaimantRepresentative.setRespondentOrganisationName("Org");
        caseDataWithOutClaimantRepresentative.setRespondentAddress(createGenericAddress());
        caseDataWithOutClaimantRepresentative.setClaimantRepresentedQuestion("Yes");
        caseDataWithOutClaimantRepresentative.setRepresentativeClaimantType(null);
        ccdRequestWithoutClaimantRepresentative = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithOutClaimantRepresentative)
                .withState(caseDetails.getState())
                .withCaseTypeId(caseDetails.getCaseTypeId())
                .withCaseId(caseDetails.getCaseId())
                .build();

        ccdRequestWithoutCaseData = CCDRequestBuilder.builder()
                .withCaseData(null)
                .withState(caseDetails.getState())
                .withCaseTypeId(caseDetails.getCaseTypeId())
                .withCaseId(caseDetails.getCaseId())
                .build();

        when(userIdamService.getUserDetails(AUTH_TOKEN)).thenReturn(HelperTest.getUserDetails());
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
                .name("TestOrg")
                .organisationIdentifier("AA11BB")
                .build();
        when(et1ReppedService.getOrganisationDetailsFromUserId(anyString())).thenReturn(organisationsResponse);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(any())).thenReturn(caseData);
    }

    @Test
    @SneakyThrows
    void validatePostcode() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(et1ReppedService.validatePostcode(any(), anyString())).thenReturn(Collections.emptyList());
        mockMvc.perform(post(VALIDATE_POSTCODE)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(et1ReppedService, times(1)).validatePostcode(any(), anyString());
    }

    @Test
    @SneakyThrows
    void validatePostcode_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_POSTCODE)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(et1ReppedService, never()).validatePostcode(any(), anyString());
    }

    @Test
    @SneakyThrows
    void officeError() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(OFFICE_ERROR)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(TRIAGE_ERROR_MESSAGE)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void officeError_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(OFFICE_ERROR)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToSubmit() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(CREATE_CASE_ABOUT_TO_SUBMIT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_Error() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(CREATE_CASE_ABOUT_TO_SUBMIT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantSex() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_SEX)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantSex_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_SEX)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantSupport() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_SUPPORT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantSupport_shouldReturnErrors() {
        caseData.setClaimantSupportQuestion(List.of("Yes", "No"));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_SUPPORT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(MULTIPLE_OPTION_ERROR)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantSupport_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_SUPPORT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateRepresentativeInformation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_REPRESENTATIVE_INFORMATION)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateRepresentativeInformation_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_REPRESENTATIVE_INFORMATION)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({"et1SectionOne", "et1SectionTwo", "et1SectionThree"})
    @SneakyThrows
    void aboutToSubmitSection(String eventId) {
        ccdRequest.setEventId(eventId);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_SECTION)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @ParameterizedTest
    @CsvSource({"et1SectionOne", "et1SectionTwo", "et1SectionThree"})
    @SneakyThrows
    void aboutToSubmitSection_badToken(String eventId) {
        ccdRequest.setEventId(eventId);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_SECTION)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantWorked() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_WORKED)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantWorked_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_WORKED)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantWorking() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_WORKING)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantWorking_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_WORKING)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantWrittenNoticePeriod() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_WRITTEN_NOTICE_PERIOD)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantWrittenNoticePeriod_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_WRITTEN_NOTICE_PERIOD)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantWorkingNoticePeriod() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_WORKING_NOTICE_PERIOD)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantWorkingNoticePeriod_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_WORKING_NOTICE_PERIOD)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantNoLongerWorking() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_NO_LONGER_WORKING)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantNoLongerWorking_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_NO_LONGER_WORKING)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantPay() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_PAY)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantPay_multipleOptions() {
        caseData.setClaimantPayType(List.of("Weekly", "Monthly"));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_PAY)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(MULTIPLE_OPTION_ERROR)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantPay_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_PAY)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantPensionBenefits() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_PENSION_BENEFITS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantPensionBenefits_multipleOptions() {
        caseData.setClaimantPensionContribution(List.of("Yes", "No"));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_PENSION_BENEFITS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(MULTIPLE_OPTION_ERROR)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantPensionBenefits_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_PENSION_BENEFITS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantNewJob() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_NEW_JOB)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantNewJob_multipleOptions() {
        caseData.setClaimantNewJob(List.of("Yes", "No"));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_NEW_JOB)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(MULTIPLE_OPTION_ERROR)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantNewJob_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_NEW_JOB)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateClaimantNewJobPay() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_CLAIMANT_NEW_JOB_PAY)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateClaimantNewJobPay_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_CLAIMANT_NEW_JOB_PAY)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateRespondentPreamble() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(GENERATE_RESPONDENT_PREAMBLE)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateRespondentPreamble_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(GENERATE_RESPONDENT_PREAMBLE)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateWorkAddressLabel() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(GENERATE_WORK_ADDRESS_LABEL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateWorkAddressLabel_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(GENERATE_WORK_ADDRESS_LABEL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void sectionCompleted() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(SECTION_COMPLETED)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void sectionCompleted_badToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(SECTION_COMPLETED)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void validateWhistleblowing() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_WHISTLEBLOWING)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateLinkedCases() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_LINKED_CASES)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void createCaseSubmitted() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(CREATE_CASE_SUBMITTED)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void createDraftEt1Submitted() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(CREATE_DRAFT_ET1_SUBMITTED)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void createDraftEt1() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(CREATE_DRAFT_ET1)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void submitted() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(CLAIM_SUBMITTED)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void submitClaim() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(SUBMIT_CLAIM)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void submitClaim_toggleOn() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(featureToggleService.isEt1DocGenEnabled()).thenReturn(true);
        mockMvc.perform(post(SUBMIT_CLAIM)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @SneakyThrows
    private CaseDetails generateCaseDetails() {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource("et1ReppedDraftStillWorking.json")).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    @SneakyThrows
    void submitClaimAboutToStart() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(SUBMIT_CLAIM_ABOUT_TO_START)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void submitClaimAboutToStartErrors() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ccdRequest2.getCaseDetails().getCaseData().setEt1ReppedSectionTwo(NO);
        mockMvc.perform(post(SUBMIT_CLAIM_ABOUT_TO_START)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is("Please complete all sections before submitting the claim")))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateGrounds() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ccdRequest2.getCaseDetails().getCaseData().setEt1SectionThreeClaimDetails("Grounds");
        mockMvc.perform(post(VALIDATE_GROUNDS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateGroundsError() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ccdRequest2.getCaseDetails().getCaseData().setEt1SectionThreeClaimDetails(null);
        ccdRequest2.getCaseDetails().getCaseData().setEt1SectionThreeDocumentUpload(null);
        mockMvc.perform(post(VALIDATE_GROUNDS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(CLAIM_DETAILS_MISSING)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void validateHearingPreferences() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(VALIDATE_HEARING_PREFERENCES)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateDocuments_toggleFalse() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(GENERATE_DOCUMENTS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateDocuments_toggleOn() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(featureToggleService.isEt1DocGenEnabled()).thenReturn(true);
        mockMvc.perform(post(GENERATE_DOCUMENTS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void theAboutToStartAmendClaimantRepresentativeContact() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_START_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithClaimantRepresentative)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        // When there is no representative should have any claimant representative missing error and have data
        mockMvc.perform(post(ABOUT_TO_START_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithoutClaimantRepresentative)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(CLAIMANT_REPRESENTATIVE_NOT_FOUND)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        // When there is no case data should have any claim missing error and have no data
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_START_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithoutCaseData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, nullValue()))
                .andExpect(jsonPath("$.errors[0]", is(ERROR_CASE_NOT_FOUND)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

    }

    @Test
    @SneakyThrows
    void theMidEventAmendClaimantRepresentativeContact() {
        // when representative contact change option is not set
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_EVENT_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithClaimantRepresentative)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        // when representative contact change option is set to use my hmcts details
        ccdRequestWithClaimantRepresentative.getCaseDetails().getCaseData().setRepresentativeContactChangeOption(
                REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS
        );
        doNothing().when(et1ReppedService).setMyHmctsOrganisationAddress(AUTH_TOKEN,
                ccdRequestWithClaimantRepresentative.getCaseDetails().getCaseData());
        mockMvc.perform(post(MID_EVENT_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithClaimantRepresentative)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        ccdRequestWithClaimantRepresentative.getCaseDetails().getCaseData().setRepresentativeContactChangeOption(null);
    }

    @Test
    @SneakyThrows
    void theMidEventAmendClaimantRepresentativeContact_ThrowsException() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ccdRequestWithClaimantRepresentative.getCaseDetails().getCaseData().setRepresentativeContactChangeOption(
                REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS
        );
        doThrow(new GenericServiceException(ERROR_CASE_DATA_NOT_FOUND,
                new Exception(ERROR_CASE_DATA_NOT_FOUND),
                ERROR_CASE_DATA_NOT_FOUND,
                StringUtils.EMPTY,
                "Et1ReppedService",
                "checkCaseData")).when(et1ReppedService)
                .setMyHmctsOrganisationAddress(AUTH_TOKEN,
                        ccdRequestWithClaimantRepresentative.getCaseDetails().getCaseData());
        mockMvc.perform(post(MID_EVENT_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithClaimantRepresentative)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(1)))
                .andExpect(jsonPath("$.errors[0]", is(ERROR_CASE_DATA_NOT_FOUND)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        ccdRequestWithClaimantRepresentative.getCaseDetails().getCaseData().setRepresentativeContactChangeOption(null);
    }

    @Test
    @SneakyThrows
    void theAboutToSubmitAmendClaimantRepresentativeContact() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doNothing().when(et1ReppedService).setClaimantRepresentativeValues(AUTH_TOKEN,
                ccdRequestWithClaimantRepresentative.getCaseDetails().getCaseData());
        mockMvc.perform(post(ABOUT_TO_SUBMIT_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithClaimantRepresentative)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void theAboutToSubmitAmendClaimantRepresentativeContact_ThrowsException() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doThrow(new GenericServiceException(ERROR_CASE_DATA_NOT_FOUND,
                new Exception(ERROR_CASE_DATA_NOT_FOUND),
                ERROR_CASE_DATA_NOT_FOUND,
                StringUtils.EMPTY,
                "Et3ResponseService",
                "setRespondentRepresentsContactDetails")).when(et1ReppedService)
                .setClaimantRepresentativeValues(AUTH_TOKEN,
                        ccdRequestWithClaimantRepresentative.getCaseDetails().getCaseData());
        mockMvc.perform(post(ABOUT_TO_SUBMIT_AMEND_CLAIMANT_REPRESENTATIVE_CONTACT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithClaimantRepresentative)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(1)))
                .andExpect(jsonPath("$.errors[0]", is(ERROR_CASE_DATA_NOT_FOUND)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }
}