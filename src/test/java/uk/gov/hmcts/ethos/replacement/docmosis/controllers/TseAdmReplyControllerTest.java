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
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

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
class TseAdmReplyControllerTest extends BaseControllerTest {

    @MockBean
    private TseAdmReplyService tseAdmReplyService;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private MockMvc mockMvc;

    private CCDRequest ccdRequest;

    private static final String MID_DETAILS_TABLE = "/tseAdmReply/midDetailsTable";
    private static final String MID_VALIDATE_INPUT = "/tseAdmReply/midValidateInput";
    private static final String ABOUT_TO_SUBMIT_URL = "/tseAdmReply/aboutToSubmit";
    private static final String SUBMITTED_URL = "/tseAdmReply/submitted";

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("1234")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

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
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
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
    void midValidateInput_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_VALIDATE_INPUT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(tseAdmReplyService).validateInput(
                ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void midValidateInput_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_VALIDATE_INPUT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(tseAdmReplyService, never()).validateInput(
                ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void midValidateInput_badRequest() throws Exception {
        mockMvc.perform(post(MID_VALIDATE_INPUT)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(tseAdmReplyService, never()).validateInput(
                ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void aboutToSubmit_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(tseAdmReplyService).saveTseAdmReplyDataFromCaseData(
            ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdmReplyService).sendNotifyEmailsToClaimant(
            ccdRequest.getCaseDetails().getCaseId(), ccdRequest.getCaseDetails().getCaseData(), AUTH_TOKEN);
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
        verify(tseAdmReplyService, never()).sendNotifyEmailsToClaimant(
            "4321", ccdRequest.getCaseDetails().getCaseData(), AUTH_TOKEN);
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
        verify(tseAdmReplyService, never()).sendNotifyEmailsToClaimant(
            "4321", ccdRequest.getCaseDetails().getCaseData(), AUTH_TOKEN);
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
            .andExpect(jsonPath(JsonMapper.DATA, nullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
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
