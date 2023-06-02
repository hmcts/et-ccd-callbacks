package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FUNCTION_NOT_AVAILABLE_ERROR;

@ExtendWith(SpringExtension.class)
@WebMvcTest({PseRespondToTribunalController.class, JsonMapper.class})
class PseRespondToTribunalControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ABOUT_TO_START_URL = "/pseRespondToTribunal/aboutToStart";
    private static final String MID_TABLE_DETAILS = "/pseRespondToTribunal/midDetailsTable";
    private static final String MID_VALIDATE_INPUT = "/pseRespondToTribunal/midValidateInput";
    private static final String ABOUT_TO_SUBMIT_URL = "/pseRespondToTribunal/aboutToSubmit";
    private static final String SUBMITTED_URL = "/pseRespondToTribunal/submitted";
    private static final String SHOW_ERROR_URL = "/pseRespondToTribunal/showError";

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private PseRespondToTribunalService pseRespondToTribunalService;

    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    @Autowired
    private MockMvc mockMvc;
    private MockedStatic mockHelper;

    @BeforeEach
    void setUp() {
        CaseData caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("test")
            .withClaimant("claimant")
            .withRespondent(RespondentSumType.builder()
                .respondentName("Boris Johnson")
                .build())
            .build();

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .withCaseId("123")
            .build();

        mockHelper = mockStatic(Helper.class);
    }

    @AfterEach
    void afterEach() {
        mockHelper.close();
    }

    @Test
    void aboutToStart_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                    .contentType(APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
    }

    @Test
    void aboutToStart_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void showError_returnError() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockHelper.when(() -> Helper.isClaimantNonSystemUser(any()))
                .thenReturn(true);
        mockMvc.perform(post(SHOW_ERROR_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors[0]", equalTo(FUNCTION_NOT_AVAILABLE_ERROR)))
                .andExpect(jsonPath("$.warnings", nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
    }

    @Test
    void showError_noError() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockHelper.when(() -> Helper.isClaimantNonSystemUser(any()))
                .thenReturn(false);
        mockMvc.perform(post(SHOW_ERROR_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", empty()))
                .andExpect(jsonPath("$.warnings", nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
    }

    @Test
    void midDetailsTable_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_TABLE_DETAILS)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(pseRespondToTribunalService).initialOrdReqDetailsTableMarkUp(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void midDetailsTable_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_TABLE_DETAILS)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void midValidateInput_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_VALIDATE_INPUT)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", notNullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(pseRespondToTribunalService).validateInput(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void midValidateInput_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_VALIDATE_INPUT)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmit_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(pseRespondToTribunalService, times(1)).sendAcknowledgeEmail(ccdRequest.getCaseDetails(), AUTH_TOKEN);
        verify(pseRespondToTribunalService, times(1)).sendClaimantEmail(ccdRequest.getCaseDetails());
        verify(pseRespondToTribunalService, times(1)).sendTribunalEmail(ccdRequest.getCaseDetails());
    }

    @Test
    void aboutToSubmit_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void submitted_Success() throws Exception {
        when(pseRespondToTribunalService.getSubmittedBody(ccdRequest.getCaseDetails().getCaseData()))
            .thenReturn("SubmittedBody");
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_body", notNullValue()))
            .andExpect(jsonPath("$.data", nullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void submitted_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

}
