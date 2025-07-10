package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseRespondentReplyService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FUNCTION_NOT_AVAILABLE_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({TseRespondentReplyController.class, JsonMapper.class})
class TseRespondentReplyControllerTest extends BaseControllerTest {

    private static final String ABOUT_TO_START_URL = "/tseResponse/aboutToStart";
    private static final String MID_POPULATE_REPLY_URL = "/tseResponse/midPopulateReply";
    private static final String ABOUT_TO_SUBMIT_URL = "/tseResponse/aboutToSubmit";
    private static final String SUBMITTED_URL = "/tseResponse/submitted";
    private static final String SHOW_ERROR_URL = "/tseResponse/showError";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @MockBean
    private TseRespondentReplyService tseRespondentReplyService;
    @MockBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private MockedStatic<Helper> mockHelper;
    private CCDRequest ccdRequest;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
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

        mockHelper = mockStatic(Helper.class);
    }

    @AfterEach
    void afterEach() {
        mockHelper.close();
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
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenOk_isClaimantNonSystemUser_isRepresentedClaimantWithMyHmctsCase_true() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(Helper.isClaimantNonSystemUser(any())).thenReturn(true);
        when(Helper.isRepresentedClaimantWithMyHmctsCase(any())).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenOk_isRepresentedClaimantWithMyHmctsCase_false() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(Helper.isClaimantNonSystemUser(any())).thenReturn(true);
        when(Helper.isRepresentedClaimantWithMyHmctsCase(any())).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenOk_isClaimantNonSystemUser_false() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(Helper.isClaimantNonSystemUser(any())).thenReturn(false);
        when(Helper.isRepresentedClaimantWithMyHmctsCase(any())).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
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
    void showError_returnError() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockHelper.when(() -> Helper.isClaimantNonSystemUser(any()))
                .thenReturn(true);
        mockMvc.perform(post(SHOW_ERROR_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", equalTo(FUNCTION_NOT_AVAILABLE_ERROR)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
    }

    @Test
    @SneakyThrows
    void showError_noError() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockHelper.when(() -> Helper.isClaimantNonSystemUser(any()))
                .thenReturn(false);
        mockHelper.when(() -> Helper.isRepresentedClaimantWithMyHmctsCase(any()))
                .thenReturn(true);
        mockMvc.perform(post(SHOW_ERROR_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, empty()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        mockHelper.verify(() -> Helper.isClaimantNonSystemUser(any()), times(1));
    }

    @Test
    @SneakyThrows
    void midPopulateReply_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_REPLY_URL)
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
    void midPopulateReply_isRespondingToTribunal() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(tseRespondentReplyService.isRespondingToTribunal(any())).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_REPLY_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(tseRespondentReplyService, times(1))
                .initialResReplyToTribunalTableMarkUp(any(), any());
    }

    @Test
    @SneakyThrows
    void midPopulateReply_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_POPULATE_REPLY_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midPopulateReply_badRequest() {
        mockMvc.perform(post(MID_POPULATE_REPLY_URL)
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
        verify(tseRespondentReplyService, times(1)).respondentReplyToTse(any(), any(), any());
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
    
    @Test
    @SneakyThrows
    void submitted_tokenOk() {
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
    @SneakyThrows
    void submitted_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(SUBMITTED_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void submitted_badRequest() {
        mockMvc.perform(post(SUBMITTED_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
