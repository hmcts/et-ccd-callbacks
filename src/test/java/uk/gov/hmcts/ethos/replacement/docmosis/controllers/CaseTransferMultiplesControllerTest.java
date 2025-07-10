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
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferDifferentCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferSameCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MultipleDataBuilder;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CaseTransferMultiplesController.class, JsonMapper.class})
class CaseTransferMultiplesControllerTest extends BaseControllerTest {

    private static final String INIT_TRANSFER_TO_SCOTLAND_URL = "/caseTransferMultiples/initTransferToScotland";
    private static final String INIT_TRANSFER_TO_ENGLANDWALES_URL = "/caseTransferMultiples/initTransferToEnglandWales";
    private static final String CASE_TRANSFER_SAME_COUNTRY_URL = "/caseTransferMultiples/transferSameCountry";
    private static final String CASE_TRANSFER_DIFFERENT_COUNTRY_URL = "/caseTransferMultiples/transferDifferentCountry";

    @MockBean
    MultipleTransferSameCountryService multipleTransferSameCountryService;

    @MockBean
    MultipleTransferDifferentCountryService multipleTransferDifferentCountryService;

    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    MockMvc mockMvc;

    @Test
    @SneakyThrows
    void testInitTransferToScotland() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testInitTransferToScotlandError400() {
        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"error\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void testInitTransferToScotlandForbidden() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void testInitTransferToEnglandWales() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testInitTransferToEnglandWalesError400() {
        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"error\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void testInitTransferToEnglandWalesForbidden() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void testTransferSameCountry() {
        MultipleRequest multipleRequest = MultipleDataBuilder.builder().buildAsMultipleRequest();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(multipleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(multipleTransferSameCountryService, times(1)).transferMultiple(multipleRequest.getCaseDetails(),
                AUTH_TOKEN);
    }

    @Test
    @SneakyThrows
    void testTransferSameCountryError400() {
        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"error\""))
                .andExpect(status().isBadRequest());

        verify(multipleTransferSameCountryService, never()).transferMultiple(any(MultipleDetails.class), anyString());
    }

    @Test
    @SneakyThrows
    void testTransferSameCountryForbidden() {
        MultipleRequest multipleRequest = MultipleDataBuilder.builder().buildAsMultipleRequest();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(multipleRequest)))
                .andExpect(status().isForbidden());

        verify(multipleTransferSameCountryService, never()).transferMultiple(any(MultipleDetails.class), anyString());
    }

    @Test
    @SneakyThrows
    void testTransferDifferentCountry() {
        MultipleRequest multipleRequest = MultipleDataBuilder.builder().buildAsMultipleRequest();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(multipleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(multipleTransferDifferentCountryService, times(1)).transferMultiple(multipleRequest.getCaseDetails(),
                AUTH_TOKEN);
    }

    @Test
    @SneakyThrows
    void testTransferDifferentCountryError400() {
        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"error\""))
                .andExpect(status().isBadRequest());

        verify(multipleTransferDifferentCountryService, never()).transferMultiple(any(MultipleDetails.class),
                anyString());
    }

    @Test
    @SneakyThrows
    void testTransferDifferentCountryForbidden() {
        MultipleRequest multipleRequest = MultipleDataBuilder.builder().buildAsMultipleRequest();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(multipleRequest)))
                .andExpect(status().isForbidden());

        verify(multipleTransferDifferentCountryService, never()).transferMultiple(any(MultipleDetails.class),
                anyString());
    }
}
