package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({PseRespondToTribunalController.class, JsonMapper.class})
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class PseRespondToTribunalControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ABOUT_TO_START_URL = "/pseRespondToTribunal/aboutToStart";
    private static final String MID_TABLE_DETAILS = "/pseRespondToTribunal/midDetailsTable";
    private static final String MID_VALIDATE_INPUT = "/pseRespondToTribunal/midValidateInput";
    private static final String ABOUT_TO_SUBMIT_URL = "/pseRespondToTribunal/aboutToSubmit";
    private static final String SUBMITTED_URL = "/pseRespondToTribunal/submitted";

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private PseRespondToTribunalService pseRespondToTribunalService;

    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    @Autowired
    private MockMvc mockMvc;

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

    // TODO: submitted_Success
    /*
    @Test
    void submitted_Success() throws Exception {
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
     */

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
