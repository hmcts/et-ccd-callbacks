package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.BaseControllerTest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CreateReferralMultiplesController.class, JsonMapper.class})
@ActiveProfiles("test")
class CreateReferralMultiplesControllerTest extends BaseControllerTest {
    private static final String START_CREATE_REFERRAL_URL = "/multiples/createReferral/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/multiples/createReferral/aboutToSubmit";
    private static final String SUBMITTED_REFERRAL_URL = "/multiples/createReferral/completeCreateReferral";
    private static final String VALIDATE_EMAIL_URL = "/multiples/createReferral/validateReferentEmail";

    @MockitoBean
    private UserIdamService userIdamService;
    @MockitoBean
    private ReferralService referralService;
    @MockitoBean
    private DocumentManagementService documentManagementService;
    @MockitoBean
    private EmailService emailService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private MultipleRequest request;
    @MockitoBean
    private CaseLookupService caseLookupService;
    private CCDRequest ccdRequest;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        when(emailService.getExuiCaseLink(any())).thenReturn("exui");
        CaseData caseData = CaseDataBuilder.builder()
                .withHearingScotland("hearingNumber", HEARING_TYPE_JUDICIAL_HEARING, "Judge",
                        TribunalOffice.ABERDEEN, "venue")
                .withHearingSession(
                        0, "2019-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_HEARD,
                        true)
                .build();
        caseData.setEthosCaseReference("caseRef");
        MultipleData multipleData = MultipleData.builder().build();
        caseData.setClaimant("claimant");
        multipleData.setIsUrgent("Yes");
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));
        multipleData.setReferentEmail("test@gmail.com");
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .build();
        request = new MultipleRequest();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        request.setCaseDetails(multipleDetails);

        UserDetails userDetails = new UserDetails();
        userDetails.setRoles(List.of("role1"));
        when(userIdamService.getUserDetails(any())).thenReturn(userDetails);
        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(any())).thenReturn(caseData);
        super.setUp();
    }

    @Test
    void initReferralHearingDetails_Success() throws Exception {
        mockMvc.perform(post(START_CREATE_REFERRAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void initReferralHearingDetails_tokenOk() throws Exception {
        mockMvc.perform(post(START_CREATE_REFERRAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createReferral_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(START_CREATE_REFERRAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void initAboutToSubmit_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmit_tokenOk() throws Exception {
        UserDetails details = new UserDetails();
        details.setName("First Last");
        when(userIdamService.getUserDetails(any())).thenReturn(details);
        when(referralService.generateCRDocument(any(CaseData.class), anyString(), anyString()))
                .thenReturn(new DocumentInfo());
        request.getCaseDetails().getCaseData().setMultipleReference("012345");
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(emailService, times(0)).sendEmail(any(), any(), any());
    }

    @Test
    void aboutToSubmit_NoReferentEmail_tokenOk() throws Exception {
        UserDetails details = new UserDetails();
        details.setName("First Last");
        when(userIdamService.getUserDetails(any())).thenReturn(details);
        when(referralService.generateCRDocument(any(CaseData.class), anyString(), anyString()))
                .thenReturn(new DocumentInfo());
        MultipleRequest noReferentEmailCCDRequest = request;
        noReferentEmailCCDRequest.getCaseDetails().getCaseData().setReferentEmail("");
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(noReferentEmailCCDRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void aboutToSubmit_ReferentEmail_tokenOk() throws Exception {
        UserDetails details = new UserDetails();
        details.setName("First Last");
        when(userIdamService.getUserDetails(any())).thenReturn(details);
        when(referralService.generateCRDocument(any(CaseData.class), anyString(), anyString()))
                .thenReturn(new DocumentInfo());
        MultipleRequest referentEmailCCDRequest = request;
        referentEmailCCDRequest.getCaseDetails().getCaseData().setReferentEmail("Tester@testco.com");
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(referentEmailCCDRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void aboutToSubmit_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void completeCreateReferral_tokenOk() throws Exception {
        mockMvc.perform(post(SUBMITTED_REFERRAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_body", notNullValue()));
    }

    @Test
    void completeCreateReferral_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(SUBMITTED_REFERRAL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateNoReferentEmail_tokenOk() throws Exception {
        CCDRequest noReferentEmailCCDRequest = ccdRequest;
        noReferentEmailCCDRequest.getCaseDetails().getCaseData().setReferentEmail("");
        mockMvc.perform(post(VALIDATE_EMAIL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(noReferentEmailCCDRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)));
    }

    @Test
    void validateReferentEmail_tokenOk() throws Exception {
        mockMvc.perform(post(VALIDATE_EMAIL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)));
    }

    @Test
    void validateReferentEmail_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_EMAIL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateReferentEmail_WithDocumentUploadErrors() throws Exception {
        MultipleData caseData = new MultipleData();
        caseData.setReferentEmail("test@example.com");
        DocumentType documentTypeWithError = new DocumentType();
        documentTypeWithError.setShortDescription("Test description");
        documentTypeWithError.setUploadedDocument(null);
        DocumentTypeItem documentTypeItemWithError = new DocumentTypeItem();
        documentTypeItemWithError.setValue(documentTypeWithError);
        List<DocumentTypeItem> referralDocumentWithError = List.of(documentTypeItemWithError);
        caseData.setReferralDocument(referralDocumentWithError);

        MultipleRequest ccdRequestWithError = new MultipleRequest();
        ccdRequestWithError.setCaseDetails(new MultipleDetails());
        ccdRequestWithError.getCaseDetails().setCaseData(caseData);

        mockMvc.perform(post(VALIDATE_EMAIL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequestWithError)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0]", is(
                        "Short description is added but document is not uploaded.")));
    }

    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Andrew Smith");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
    }
}
