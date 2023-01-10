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
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseAdmReplyService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({TseAdmReplyController.class, JsonMapper.class})
class TseAdmReplyControllerTest {

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private TseAdmReplyService tseAdmReplyService;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private MockMvc mockMvc;

    private CCDRequest ccdRequest;

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String MID_DETAILS_TABLE = "/tseAdmReply/midDetailsTable";
    private static final String ABOUT_TO_SUBMIT_URL = "/tseAdmReply/aboutToSubmit";
    private static final String SUBMITTED_URL = "/tseAdmReply/submitted";

    @BeforeEach
    void setUp() throws Exception {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.getCaseData().setEthosCaseReference("1234");
        caseDetails.setCaseId("4321");

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseDetails.getCaseData())
                .build();
    }

    @Test
    void midDetailsTable_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_DETAILS_TABLE)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(tseAdmReplyService).initialTseAdmReplyTableMarkUp(
                ccdRequest.getCaseDetails().getCaseData(),
                AUTH_TOKEN);
    }

    @Test
    void midDetailsTable_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_DETAILS_TABLE)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(tseAdmReplyService, never()).initialTseAdmReplyTableMarkUp(
                ccdRequest.getCaseDetails().getCaseData(),
                AUTH_TOKEN);
    }

    @Test
    void midDetailsTable_badRequest() throws Exception {
        mockMvc.perform(post(MID_DETAILS_TABLE)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(tseAdmReplyService, never()).initialTseAdmReplyTableMarkUp(
                ccdRequest.getCaseDetails().getCaseData(),
                AUTH_TOKEN);
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
        verify(tseAdmReplyService).saveTseAdmReplyDataFromCaseData(
            ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdmReplyService).sendAdmReplyEmails(
            ccdRequest.getCaseDetails().getCaseId(), ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdmReplyService).clearTseAdmReplyDataFromCaseData(
            ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void aboutToSubmit_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(tseAdmReplyService, never()).saveTseAdmReplyDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdmReplyService, never()).sendAdmReplyEmails(
            "4321", ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdmReplyService, never()).clearTseAdmReplyDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void aboutToSubmit_badRequest() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(tseAdmReplyService, never()).saveTseAdmReplyDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdmReplyService, never()).sendAdmReplyEmails(
            "4321", ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdmReplyService, never()).clearTseAdmReplyDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
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
