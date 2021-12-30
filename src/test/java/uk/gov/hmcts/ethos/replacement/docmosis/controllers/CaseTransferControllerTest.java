package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseTransferService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

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

@RunWith(SpringRunner.class)
@WebMvcTest({CaseTransferController.class, JsonMapper.class})
public class CaseTransferControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";

    private static final String DYNAMIC_LIST_OFFICES_URL = "/caseTransfer/dynamicListOffices";
    private static final String CASE_TRANSFER_SAME_COUNTRY_URL = "/caseTransfer/transferSameCountry";
    private static final String CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL =
            "/caseTransfer/transferSameCountryEccLinkedCase";
    private static final String CASE_TRANSFER_DIFFERENT_COUNTRY_URL = "/caseTransfer/transferDifferentCountry";
    
    @MockBean
    VerifyTokenService verifyTokenService;

    @MockBean
    CaseTransferService caseTransferService;

    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void dynamicListOffices() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(DYNAMIC_LIST_OFFICES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    public void dynamicListOfficesError400() throws Exception {
        mockMvc.perform(post(DYNAMIC_LIST_OFFICES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void dynamicListOfficesForbidden() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(DYNAMIC_LIST_OFFICES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testTransferSameCountry() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(caseTransferService, times(1)).caseTransferSameCountry(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferSameCountryError400() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferService, never()).caseTransferSameCountry(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferSameCountryForbidden() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());

        verify(caseTransferService, never()).caseTransferSameCountry(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferSameCountryEccLinkedCase() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(caseTransferService, times(1)).caseTransferSameCountryEccLinkedCase(ccdRequest.getCaseDetails(),
                AUTH_TOKEN);
    }

    @Test
    public void testTransferSameCountryEccLinkedCaseError400() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferService, never()).caseTransferSameCountryEccLinkedCase(ccdRequest.getCaseDetails(),
                AUTH_TOKEN);
    }

    @Test
    public void testTransferSameCountryEccLinkedCaseForbidden() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());

        verify(caseTransferService, never()).caseTransferSameCountryEccLinkedCase(ccdRequest.getCaseDetails(),
                AUTH_TOKEN);
    }

    @Test
    public void testTransferDifferentCountry() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(caseTransferService, times(1)).caseTransferDifferentCountry(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferDifferentCountryError400() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferService, never()).caseTransferDifferentCountry(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferDifferentCountryForbidden() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());

        verify(caseTransferService, never()).caseTransferDifferentCountry(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }
}
