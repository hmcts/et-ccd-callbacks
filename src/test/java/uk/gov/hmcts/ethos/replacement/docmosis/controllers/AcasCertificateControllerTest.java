package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AcasService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.util.ArrayList;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@WebMvcTest(controllers = AcasCertificateController.class,
            excludeAutoConfiguration = {
                org.springframework.cloud.openfeign.FeignAutoConfiguration.class,
                org.springframework.boot.test.autoconfigure.web.servlet.MockMvcWebClientAutoConfiguration.class
            })
@ActiveProfiles("test")
class AcasCertificateControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JsonMapper jsonMapper() {
            return new JsonMapper(new ObjectMapper());
        }
    }

    private static final String RETRIEVE_ACAS_CERT_URL = "/acasCertificate/retrieveCertificate";
    private static final String ACAS_CONFIRMATION_URL = "/acasCertificate/confirmation";
    private static final String AUTH_TOKEN = "some-token";
    private CCDRequest ccdRequest;

    @MockitoBean
    private VerifyTokenService verifyTokenService;
    @MockitoBean
    private AcasService acasService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() throws Exception {
        CaseData caseData = new CaseData();
        caseData.setAcasCertificate("R111111/11/11");
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
    }

    @Test
    void retrieveAcasCert_Success() throws Exception {

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(acasService.getAcasCertificate(any(), anyString(), anyString())).thenReturn(new ArrayList<>());
        mockMvc.perform(post(RETRIEVE_ACAS_CERT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(acasService, times(1)).getAcasCertificate(any(), anyString(), anyString());
    }

    @Test
    void retrieveAcasCert_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(RETRIEVE_ACAS_CERT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(acasService, never()).getAcasCertificate(any(), anyString(), anyString());
    }

    @Test
    void retrieveAcasCert_BadRequest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(RETRIEVE_ACAS_CERT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content("error"))
                .andExpect(status().isBadRequest());
        verify(acasService, never()).getAcasCertificate(any(), anyString(), anyString());
    }

    @Test
    void acasConfirmation_Success() throws Exception {

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(acasService.getAcasCertificate(any(), anyString(), anyString())).thenReturn(new ArrayList<>());
        mockMvc.perform(post(ACAS_CONFIRMATION_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void acasConfirmation_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ACAS_CONFIRMATION_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void acasConfirmation_BadRequest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ACAS_CONFIRMATION_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content("error"))
                .andExpect(status().isBadRequest());
    }
}
