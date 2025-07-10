package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesRespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({BundlesRespondentController.class, JsonMapper.class})
class BundlesRespondentControllerTest extends BaseControllerTest {

    private static final String ABOUT_TO_START_URL = "/bundlesRespondent/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/bundlesRespondent/aboutToSubmit";
    private static final String MID_POPULATE_HEARINGS_URL = "/bundlesRespondent/midPopulateHearings";
    private static final String MID_VALIDATE_UPLOAD_URL = "/bundlesRespondent/midValidateUpload";
    private static final String SUBMITTED_URL = "/bundlesRespondent/submitted";
    private static final String REMOVE_HEARING_BUNDLE_URL = "/bundlesRespondent/removeHearingBundle";
    private static final String MID_POPULATE_REMOVE_HEARING_BUNDLES_URL =
            "/bundlesRespondent/midPopulateRemoveHearingBundles";

    @MockBean
    private BundlesRespondentService bundlesRespondentService;

    @MockBean
    private FeatureToggleService featureToggleService;
    
    @MockBean
    private SendNotificationService sendNotificationService;

    @Autowired
    private JsonMapper jsonMapper;

    private CCDRequest ccdRequest;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(CaseDataBuilder.builder().build())
            .build();

        when(featureToggleService.isBundlesEnabled()).thenReturn(true);
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
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(bundlesRespondentService).clearInputData(ccdRequest.getCaseDetails().getCaseData());
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
        verify(bundlesRespondentService, never()).clearInputData(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_badRequest() {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        verify(bundlesRespondentService, never()).clearInputData(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void midPopulateHearings_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_HEARINGS_URL)
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
    void midPopulateHearings_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_POPULATE_HEARINGS_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midPopulateHearings_badRequest() {
        mockMvc.perform(post(MID_POPULATE_HEARINGS_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midValidateUpload_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_VALIDATE_UPLOAD_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", notNullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    @SneakyThrows
    void midValidateUpload_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_VALIDATE_UPLOAD_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midValidateUpload_badRequest() {
        mockMvc.perform(post(MID_VALIDATE_UPLOAD_URL)
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
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
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
    void removeHearingBundle_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(REMOVE_HEARING_BUNDLE_URL)
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
    void removeHearingBundle_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(REMOVE_HEARING_BUNDLE_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void removeHearingBundle_badRequest() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doThrow(new NotFoundException("Bundle not found in the collection"))
                .when(bundlesRespondentService).removeHearingBundles(any());
        mockMvc.perform(post(REMOVE_HEARING_BUNDLE_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", notNullValue()));
    }

    @Test
    @SneakyThrows
    void midPopulateRemoveHearingBundles_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_REMOVE_HEARING_BUNDLES_URL)
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
    void midPopulateRemoveHearingBundles_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_POPULATE_REMOVE_HEARING_BUNDLES_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midPopulateRemoveHearingBundles_badRequest() {
        mockMvc.perform(post(MID_POPULATE_REMOVE_HEARING_BUNDLES_URL)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
