package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesClaimantService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({BundlesClaimantController.class, JsonMapper.class})
class BundlesClaimantControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ABOUT_TO_START_URL = "/bundlesClaimant/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/bundlesClaimant/aboutToSubmit";
    private static final String MID_POPULATE_HEARINGS_URL = "/bundlesClaimant/midPopulateHearings";
    private static final String MID_VALIDATE_UPLOAD_URL = "/bundlesClaimant/midValidateUpload";
    private static final String SUBMITTED_URL = "/bundlesClaimant/submitted";

    @MockitoBean
    private BundlesClaimantService bundlesClaimantService;

    @MockitoBean
    private SendNotificationService sendNotificationService;

    @MockitoBean
    private VerifyTokenService verifyTokenService;

    @Autowired
    private JsonMapper jsonMapper;

    private CCDRequest ccdRequest;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(CaseDataBuilder.builder().build())
            .build();
    }

    @Test
    void aboutToStart_success() throws Exception {
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
    void aboutToStart_badRequest() throws Exception {
        mockMvc.perform(post(ABOUT_TO_START_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void aboutToSubmit_success() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(bundlesClaimantService).addToBundlesCollection(ccdRequest.getCaseDetails().getCaseData());
        verify(bundlesClaimantService).clearInputData(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void aboutToSubmit_badRequest() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        verify(bundlesClaimantService, never()).clearInputData(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void midPopulateHearings_success() throws Exception {
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
    void midPopulateHearings_badRequest() throws Exception {
        mockMvc.perform(post(MID_POPULATE_HEARINGS_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void midValidateUpload_success() throws Exception {
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
    void midValidateUpload_badRequest() throws Exception {
        mockMvc.perform(post(MID_VALIDATE_UPLOAD_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitted_success() throws Exception {
        mockMvc.perform(post(SUBMITTED_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()))
            .andExpect(jsonPath("$.confirmation_header", notNullValue()))
            .andExpect(jsonPath("$.confirmation_body", notNullValue()));
        verify(sendNotificationService).notifyClaimantBundlesSubmitted(ccdRequest.getCaseDetails());
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
