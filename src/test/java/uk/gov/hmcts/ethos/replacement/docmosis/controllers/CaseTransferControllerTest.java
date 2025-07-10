package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferDifferentCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferSameCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferToEcmService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

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

@ExtendWith(SpringExtension.class)
@WebMvcTest({CaseTransferController.class, JsonMapper.class})
class CaseTransferControllerTest extends BaseControllerTest {

    private static final String INIT_TRANSFER_TO_SCOTLAND_URL = "/caseTransfer/initTransferToScotland";
    private static final String INIT_TRANSFER_TO_ENGLANDWALES_URL = "/caseTransfer/initTransferToEnglandWales";
    private static final String CASE_TRANSFER_SAME_COUNTRY_URL = "/caseTransfer/transferSameCountry";
    private static final String CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL =
            "/caseTransfer/transferSameCountryEccLinkedCase";
    private static final String CASE_TRANSFER_DIFFERENT_COUNTRY_URL = "/caseTransfer/transferDifferentCountry";
    private static final String CASE_TRANSFER_TO_ECM = "/caseTransfer/transferToEcm";
    private static final String ASSIGN_CASE = "/caseTransfer/assignCase";

    @MockBean
    CaseTransferSameCountryService caseTransferSameCountryService;

    @MockBean
    CaseTransferDifferentCountryService caseTransferDifferentCountryService;

    @MockBean
    CaseTransferToEcmService caseTransferToEcmService;

    @MockBean
    DefaultValuesReaderService defaultValuesReaderService;

    @MockBean
    CaseManagementLocationService caseManagementLocationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
    }

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
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseTransferSameCountryService, times(1)).transferCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    @SneakyThrows
    void testTransferSameCountryError400() {
        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"error\""))
                .andExpect(status().isBadRequest());

        verify(caseTransferSameCountryService, never()).transferCase(any(CaseDetails.class), anyString());
    }

    @Test
    @SneakyThrows
    void testTransferSameCountryForbidden() {
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
    @SneakyThrows
    void testTransferSameCountryEccLinkedCase() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseTransferSameCountryService, times(1)).updateEccLinkedCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    @SneakyThrows
    void testTransferSameCountryEccLinkedCaseError400() {
        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"error\""))
                .andExpect(status().isBadRequest());

        verify(caseTransferSameCountryService, never()).updateEccLinkedCase(any(CaseDetails.class), anyString());
    }

    @Test
    @SneakyThrows
    void testTransferSameCountryEccLinkedCaseForbidden() {
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
    @SneakyThrows
    void testTransferDifferentCountry() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseTransferDifferentCountryService, times(1)).transferCase(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    @SneakyThrows
    void testTransferDifferentCountryError400() {
        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"error\""))
                .andExpect(status().isBadRequest());

        verify(caseTransferDifferentCountryService, never()).transferCase(any(CaseDetails.class), anyString());
    }

    @Test
    @SneakyThrows
    void testTransferDifferentCountryForbidden() {
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
    @SneakyThrows
    void testCaseTransferToECM() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testCaseTransferToEcmError400() {
        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"error\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void testCaseTransferToEcmForbidden() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void assignCaseEnglandWales() {
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
    @SneakyThrows
    void assignCaseScotland() {
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
    @SneakyThrows
    void assignCaseForbidden() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(ASSIGN_CASE)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void testTransferSameCountryAddsCaseMgtLocationCode() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseManagementLocationService, times(1))
                .setCaseManagementLocationCode(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    @SneakyThrows
    void testTransferDifferentCountryUpdatesCaseMgtLocation() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseManagementLocationService, times(1))
                .setCaseManagementLocationCode(ccdRequest.getCaseDetails().getCaseData());

    }

    @Test
    @SneakyThrows
    void assignCaseEnglandWalesUpdatesCaseMgtLocation() {
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
                .andExpect(status().isOk());

        verify(caseManagementLocationService, times(1))
                .setCaseManagementLocationCode(any());
    }
}
