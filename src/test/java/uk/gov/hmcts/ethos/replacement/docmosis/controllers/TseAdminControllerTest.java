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
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseAdmCloseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseAdminService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({TseAdminController.class, JsonMapper.class})
class TseAdminControllerTest extends BaseControllerTest {

    private static final String ABOUT_TO_START_URL = "/tseAdmin/aboutToStart";
    private static final String MID_DETAILS_TABLE = "/tseAdmin/midDetailsTable";
    private static final String ABOUT_TO_SUBMIT_URL = "/tseAdmin/aboutToSubmit";
    private static final String SUBMITTED_URL = "/tseAdmin/submitted";
    private static final String ABOUT_TO_SUBMIT_CLOSE_APP_URL = "/tseAdmin/aboutToSubmitCloseApplication";
    private static final String SUBMITTED_CLOSE_APP_URL = "/tseAdmin/submittedCloseApplication";

    @MockBean
    private TseAdminService tseAdminService;

    @MockBean
    private TseAdmCloseService tseAdmCloseService;

    @MockBean
    private CaseFlagsService caseFlagsService;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("1234")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.setCaseId("4321");
        CaseData caseData = caseDetails.getCaseData();
        caseData.setEt1OnlineSubmission(YES);
        caseData.setGenericTseApplicationCollection(
            List.of(GenericTseApplicationTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(TseApplicationBuilder.builder()
                .withNumber("2")
                .withType(TSE_APP_CHANGE_PERSONAL_DETAILS)
                .build())
            .build()));

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .build();
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
    void midDetailsTable_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_DETAILS_TABLE)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(tseAdminService).initialTseAdminTableMarkUp(
                ccdRequest.getCaseDetails().getCaseData(),
                AUTH_TOKEN);
    }

    @Test
    @SneakyThrows
    void midDetailsTable_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_DETAILS_TABLE)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(tseAdminService, never()).initialTseAdminTableMarkUp(
                ccdRequest.getCaseDetails().getCaseData(),
                AUTH_TOKEN);
    }

    @Test
    @SneakyThrows
    void midDetailsTable_badRequest() {
        mockMvc.perform(post(MID_DETAILS_TABLE)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(tseAdminService, never()).initialTseAdminTableMarkUp(
                ccdRequest.getCaseDetails().getCaseData(),
                AUTH_TOKEN);
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
        verify(tseAdminService).saveTseAdminDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdminService).sendEmailToClaimant(
            ccdRequest.getCaseDetails().getCaseId(),
            ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdminService).clearTseAdminDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
        verify(caseFlagsService, times(1)).setPrivateHearingFlag(any());
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
        verify(tseAdminService, never()).saveTseAdminDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdminService, never()).sendEmailToClaimant(
            ccdRequest.getCaseDetails().getCaseId(),
            ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdminService, never()).clearTseAdminDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_badRequest() {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        verify(tseAdminService, never()).saveTseAdminDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdminService, never()).sendEmailToClaimant(
            ccdRequest.getCaseDetails().getCaseId(),
            ccdRequest.getCaseDetails().getCaseData());
        verify(tseAdminService, never()).clearTseAdminDataFromCaseData(
                ccdRequest.getCaseDetails().getCaseData());
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
            .andExpect(jsonPath("$.confirmation_body", notNullValue()))
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

    @Test
    @SneakyThrows
    void aboutToSubmitCloseApplication_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_CLOSE_APP_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(tseAdmCloseService).aboutToSubmitCloseApplication(
            ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void aboutToSubmitCloseApplication_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_CLOSE_APP_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
        verify(tseAdmCloseService, never()).aboutToSubmitCloseApplication(
            ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void aboutToSubmitCloseApplication_badRequest() {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_CLOSE_APP_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        verify(tseAdmCloseService, never()).aboutToSubmitCloseApplication(
            ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void submittedCloseApplication_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(SUBMITTED_CLOSE_APP_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_body", notNullValue()))
                .andExpect(jsonPath(JsonMapper.DATA, nullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void submittedCloseApplication_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(SUBMITTED_CLOSE_APP_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void submittedCloseApplication_badRequest() {
        mockMvc.perform(post(SUBMITTED_CLOSE_APP_URL)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
