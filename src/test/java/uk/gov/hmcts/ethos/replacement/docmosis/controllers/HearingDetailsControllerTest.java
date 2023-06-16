package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void testInitialiseHearingDynamicList() throws Exception {
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
    void testInitialiseHearingDynamicListInvalidToken() throws Exception {
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
    void testHandleListingSelected() throws Exception {
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
    void testHandleListingSelectedInvalidToken() throws Exception {
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
    void testhearingMidEventValidation() throws Exception {
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
    void testhearingMidEventValidationInvalidToken() throws Exception {
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
    void testAboutToSubmit() throws Exception {
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
    void testAboutToSubmitInvalidToken() throws Exception {
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
