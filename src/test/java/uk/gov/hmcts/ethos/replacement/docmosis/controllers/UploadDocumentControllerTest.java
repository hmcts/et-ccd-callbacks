package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REJECTION_OF_CLAIM;

@ExtendWith(SpringExtension.class)
@WebMvcTest({UploadDocumentController.class, JsonMapper.class})
class UploadDocumentControllerTest extends BaseControllerTest {

    private static final String ABOUT_TO_START_URL = "/uploadDocument/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/uploadDocument/aboutToSubmit";

    @MockBean
    private EmailService emailService;
    @MockBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        when(emailService.getCitizenCaseLink(any())).thenReturn("");

        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        CaseData caseData = caseDetails.getCaseData();
        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .withState(REJECTED_STATE)
            .withCaseId("1234")
            .build();

        ClaimantType claimant = new ClaimantType();
        claimant.setClaimantEmailAddress("person@email.com");
        caseData.setClaimantType(claimant);
        caseData.setClaimant("First Last");
        caseData.setEthosCaseReference("1234");
        UploadDocumentHelperTest.attachDocumentToCollection(caseData, REJECTION_OF_CLAIM);
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToStart_badRequest() {
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_badRequest() {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
}
