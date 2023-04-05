package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferDifferentCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferSameCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferToEcmService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService.UNASSIGNED_OFFICE;

@RunWith(SpringRunner.class)
@WebMvcTest({CaseTransferController.class, JsonMapper.class})
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.LawOfDemeter"})
public class CaseTransferControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";

    private static final String INIT_TRANSFER_TO_SCOTLAND_URL = "/caseTransfer/initTransferToScotland";
    private static final String INIT_TRANSFER_TO_ENGLANDWALES_URL = "/caseTransfer/initTransferToEnglandWales";
    private static final String CASE_TRANSFER_SAME_COUNTRY_URL = "/caseTransfer/transferSameCountry";
    private static final String CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL =
            "/caseTransfer/transferSameCountryEccLinkedCase";
    private static final String CASE_TRANSFER_DIFFERENT_COUNTRY_URL = "/caseTransfer/transferDifferentCountry";
    private static final String CASE_TRANSFER_TO_ECM = "/caseTransfer/transferToEcm";
    private static final String ASSIGN_CASE = "/caseTransfer/assignCase";

    @MockBean
    VerifyTokenService verifyTokenService;

    @MockBean
    CaseTransferSameCountryService caseTransferSameCountryService;

    @MockBean
    CaseTransferDifferentCountryService caseTransferDifferentCountryService;

    @MockBean
    CaseTransferToEcmService caseTransferToEcmService;

    @MockBean
    DefaultValuesReaderService defaultValuesReaderService;

    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testInitTransferToScotland() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testInitTransferToScotlandError400() throws Exception {
        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInitTransferToScotlandForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testInitTransferToEnglandWales() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testInitTransferToEnglandWalesError400() throws Exception {
        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInitTransferToEnglandWalesForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testTransferSameCountry() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(caseTransferSameCountryService, times(1)).transferCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferSameCountryError400() throws Exception {
        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferSameCountryService, never()).transferCase(any(CaseDetails.class), anyString());
    }

    @Test
    public void testTransferSameCountryForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());

        verify(caseTransferSameCountryService, never()).transferCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferSameCountryEccLinkedCase() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(caseTransferSameCountryService, times(1)).updateEccLinkedCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferSameCountryEccLinkedCaseError400() throws Exception {
        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferSameCountryService, never()).updateEccLinkedCase(any(CaseDetails.class), anyString());
    }

    @Test
    public void testTransferSameCountryEccLinkedCaseForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());

        verify(caseTransferSameCountryService, never()).updateEccLinkedCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferDifferentCountry() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(caseTransferDifferentCountryService, times(1)).transferCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testTransferDifferentCountryError400() throws Exception {
        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferDifferentCountryService, never()).transferCase(any(CaseDetails.class), anyString());
    }

    @Test
    public void testTransferDifferentCountryForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());

        verify(caseTransferDifferentCountryService, never()).transferCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void testCaseTransferToECM() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCaseTransferToEcmError400() throws Exception {
        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCaseTransferToEcmForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void assignCaseEnglandWales() throws Exception {
        CaseData caseData = CaseDataBuilder.builder()
                .withManagingOffice(UNASSIGNED_OFFICE)
                .withAssignOffice(TribunalOffice.LEEDS.getOfficeName())
                .build();

        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ASSIGN_CASE)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.managingOffice",
                        hasToString(TribunalOffice.LEEDS.getOfficeName())));
    }

    @Test
    public void assignCaseScotland() throws Exception {
        CaseData caseData = CaseDataBuilder.builder()
                .withManagingOffice(UNASSIGNED_OFFICE)
                .build();

        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseTypeId(SCOTLAND_CASE_TYPE_ID)
                .build();

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ASSIGN_CASE)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.managingOffice",
                    hasToString(TribunalOffice.GLASGOW.getOfficeName())))
                .andExpect(jsonPath("$.data.allocatedOffice",
                    hasToString(TribunalOffice.GLASGOW.getOfficeName())));
    }

    @Test
    public void assignCaseForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(ASSIGN_CASE)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }
}
