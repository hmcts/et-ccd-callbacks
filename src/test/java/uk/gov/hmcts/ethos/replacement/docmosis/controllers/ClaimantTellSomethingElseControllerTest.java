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
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClaimantTellSomethingElseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;

@ExtendWith(SpringExtension.class)
@WebMvcTest({ClaimantTellSomethingElseController.class, JsonMapper.class})
class ClaimantTellSomethingElseControllerTest extends BaseControllerTest {
    private static final String ABOUT_TO_START_URL = "/claimantTSE/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/claimantTSE/aboutToSubmit";
    private static final String COMPLETE_APPLICATION_URL = "/claimantTSE/completeApplication";
    private static final String VALIDATE_DETAILS_URL = "/claimantTSE/validateGiveDetails";
    private static final String DISPLAY_TABLE_URL = "/claimantTSE/displayTable";
    private static final String VIEW_APPLICATION_ABOUT_TO_START_URL =
            "/claimantTSE/viewApplicationsAboutToStart";
    private static final String MID_POPULATE_CHOOSE_APPLICATION_URL =
            "/claimantTSE/midPopulateChooseApplication";
    private static final String MID_POPULATE_SELECTED_APPLICATION =
            "/claimantTSE/midPopulateSelectedApplicationData";

    @MockBean
    ClaimantTellSomethingElseService claimantTseService;
    @MockBean
    private TseService tseService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    private MockedStatic mockHelper;

    @BeforeEach
    @Override
    protected void setUp()  {
        super.setUp();
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("test")
                .withClaimant("claimant")
                .build();
        caseData.setClaimantTseSelectApplication("caseRef");
        caseData.setClaimantTseRule92(NO);
        caseData.setGenericTseApplicationCollection(createApplicationCollection());

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
    @SneakyThrows
    void aboutToStartClaimantTSE_SystemUser_Success()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(Helper.isRespondentSystemUser(any())).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        mockHelper.verify(() -> Helper.isRespondentSystemUser(any()), times(1));
    }

    @Test
    @SneakyThrows
    void aboutToStartClaimantTSE_NonSystemUser_Success()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(Helper.isRespondentSystemUser(any())).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        mockHelper.verify(() -> Helper.isRespondentSystemUser(any()), times(1));
    }

    @Test
    @SneakyThrows
    void aboutToStartClaimantTSE_badRequest()  {
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void aboutToSubmitClaimantTSE_Success()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(Helper.isRespondentSystemUser(any())).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(tseService).createApplication(ccdRequest.getCaseDetails().getCaseData(), CLAIMANT_REP_TITLE);
        verify(claimantTseService).sendAcknowledgementEmail(ccdRequest.getCaseDetails(), AUTH_TOKEN);
        verify(claimantTseService).sendAdminEmail(ccdRequest.getCaseDetails());
    }

    @Test
    @SneakyThrows
    void aboutToSubmitClaimantTSE_NonSystemRespondent_Success()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(Helper.isRespondentSystemUser(any())).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(tseService).createApplication(ccdRequest.getCaseDetails().getCaseData(), CLAIMANT_REP_TITLE);
        verify(claimantTseService).sendRespondentsEmail(ccdRequest.getCaseDetails());
        verify(claimantTseService).sendAcknowledgementEmail(ccdRequest.getCaseDetails(), AUTH_TOKEN);
        verify(claimantTseService).sendAdminEmail(ccdRequest.getCaseDetails());
    }

    @Test
    @SneakyThrows
    void completeApplication_Success()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(claimantTseService.buildApplicationCompleteResponse(any())).thenReturn("body");
        mockMvc.perform(post(COMPLETE_APPLICATION_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_body", notNullValue()))
                .andExpect(jsonPath(JsonMapper.DATA, nullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(claimantTseService).buildApplicationCompleteResponse(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void validateGiveDetails_Success()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_DETAILS_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(claimantTseService).validateGiveDetails(ccdRequest.getCaseDetails().getCaseData());
    }

    private List<GenericTseApplicationTypeItem> createApplicationCollection() {
        GenericTseApplicationType claimantTseType = new GenericTseApplicationType();
        claimantTseType.setCopyToOtherPartyYesOrNo(NO);

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(claimantTseType);

        return new ArrayList<>(Collections.singletonList(tseApplicationTypeItem));
    }

    @Test
    @SneakyThrows
    void displayClaimantApplicationsTable_Success()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(DISPLAY_TABLE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(claimantTseService).generateClaimantApplicationTableMarkdown(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void displayClaimantApplicationsTable_invalidToken()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(DISPLAY_TABLE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenOk()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VIEW_APPLICATION_ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenFail()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VIEW_APPLICATION_ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToStart_badRequest()  {
        mockMvc.perform(post(VIEW_APPLICATION_ABOUT_TO_START_URL)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midPopulateChooseApplication_tokenOk()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_CHOOSE_APPLICATION_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midPopulateChooseApplication_tokenFail()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_POPULATE_CHOOSE_APPLICATION_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midPopulateChooseApplication_badRequest()  {
        mockMvc.perform(post(MID_POPULATE_CHOOSE_APPLICATION_URL)
                        .content("test content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midPopulateReply_tokenOk()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_SELECTED_APPLICATION)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midPopulateReply_tokenFail()  {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_POPULATE_SELECTED_APPLICATION)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midPopulateReply_badRequest()  {
        mockMvc.perform(post(MID_POPULATE_SELECTED_APPLICATION)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}