package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.hearingdetails.HearingDetailsService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({HearingDetailsController.class, JsonMapper.class})
class HearingDetailsControllerTest {

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private HearingDetailsService hearingDetailsService;
    @MockBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @SneakyThrows
    void testInitialiseHearingDynamicList() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/hearingdetails/initialiseHearings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(hearingDetailsService, times(1)).initialiseHearingDetails(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void testInitialiseHearingDynamicListInvalidToken() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/hearingdetails/initialiseHearings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());
        verify(hearingDetailsService, never()).initialiseHearingDetails(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void testHandleListingSelected() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/hearingdetails/handleListingSelected")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(hearingDetailsService, times(1)).handleListingSelected(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void testHandleListingSelectedInvalidToken() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/hearingdetails/handleListingSelected")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());
        verify(hearingDetailsService, never()).handleListingSelected(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void testhearingMidEventValidation() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/hearingdetails/hearingMidEventValidation")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void testhearingMidEventValidationInvalidToken() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/hearingdetails/hearingMidEventValidation")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());
        verify(hearingDetailsService, never()).handleListingSelected(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void testAboutToSubmit() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/hearingdetails/aboutToSubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(hearingDetailsService, times(1)).updateCase(ccdRequest.getCaseDetails());
    }

    @Test
    @SneakyThrows
    void testAboutToSubmitInvalidToken() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/hearingdetails/aboutToSubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());
        verify(hearingDetailsService, never()).updateCase(ccdRequest.getCaseDetails());
    }
}
