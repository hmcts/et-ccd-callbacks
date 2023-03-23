package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

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
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentTellSomethingElseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseRespondentReplyService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({TseRespondentReplyController.class, JsonMapper.class})
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.ExcessiveImports", "PMD.TooManyMethods"})
class TseRespondentReplyControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ABOUT_TO_START_URL = "/tseResponse/aboutToStart";
    private static final String MID_POPULATE_REPLY_URL = "/tseResponse/midPopulateReply";
    private static final String ABOUT_TO_SUBMIT_URL = "/tseResponse/aboutToSubmit";
    private static final String SUBMITTED_URL = "/tseResponse/submitted";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private TseRespondentReplyService tseRespondentReplyService;
    @MockBean
    private RespondentTellSomethingElseService respondentTellSomethingElseService;
    private CCDRequest ccdRequest;

    @BeforeEach
    void setUp() throws Exception {
        CaseData caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("9876")
            .withClaimantType("person@email.com")
            .withRespondent("respondent", YES, "01-Jan-2003", false)
            .withClaimant("Claimant LastName")
            .build();

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
            .withCaseId("1234")
            .withCaseData(caseData)
            .build();
    }

    @Test
    void aboutToStart_tokenOk() throws Exception {
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
    void aboutToStart_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void aboutToStart_badRequest() throws Exception {
        mockMvc.perform(post(ABOUT_TO_START_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void midPopulateReply_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_REPLY_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void midPopulateReply_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_POPULATE_REPLY_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void midPopulateReply_badRequest() throws Exception {
        mockMvc.perform(post(MID_POPULATE_REPLY_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void aboutToSubmit_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(respondentTellSomethingElseService, times(1)).sendAdminEmail(any());
    }

    @Test
    void aboutToSubmit_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmit_badRequest() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void submitted_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(SUBMITTED_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", nullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void submitted_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(SUBMITTED_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void submitted_badRequest() throws Exception {
        mockMvc.perform(post(SUBMITTED_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
