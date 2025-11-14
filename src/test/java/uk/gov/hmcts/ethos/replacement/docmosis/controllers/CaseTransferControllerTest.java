package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferDifferentCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferSameCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferToEcmService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
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
    protected void setUp() throws IOException, URISyntaxException {
        super.setUp();
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
    }

    @Test
    void testInitTransferToScotland() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testInitTransferToScotlandError400() throws Exception {
        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInitTransferToScotlandForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(INIT_TRANSFER_TO_SCOTLAND_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testInitTransferToEnglandWales() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testInitTransferToEnglandWalesError400() throws Exception {
        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInitTransferToEnglandWalesForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(INIT_TRANSFER_TO_ENGLANDWALES_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTransferSameCountry() throws Exception {
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
    void testTransferSameCountryError400() throws Exception {
        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferSameCountryService, never()).transferCase(any(CaseDetails.class), anyString());
    }

    @Test
    void testTransferSameCountryForbidden() throws Exception {
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
    void testTransferSameCountryEccLinkedCase() throws Exception {
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
    void testTransferSameCountryEccLinkedCaseError400() throws Exception {
        mockMvc.perform(post(CASE_TRANSFER_SAME_COUNTRY_ECC_LINKED_CASE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferSameCountryService, never()).updateEccLinkedCase(any(CaseDetails.class), anyString());
    }

    @Test
    void testTransferSameCountryEccLinkedCaseForbidden() throws Exception {
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
    void testTransferDifferentCountry() throws Exception {
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
    void testTransferDifferentCountryError400() throws Exception {
        mockMvc.perform(post(CASE_TRANSFER_DIFFERENT_COUNTRY_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());

        verify(caseTransferDifferentCountryService, never()).transferCase(any(CaseDetails.class), anyString());
    }

    @Test
    void testTransferDifferentCountryForbidden() throws Exception {
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
    void testCaseTransferToECM() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testCaseTransferToEcmError400() throws Exception {
        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCaseTransferToEcmForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CASE_TRANSFER_TO_ECM)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignCaseEnglandWales() throws Exception {
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
    void assignCaseScotland() throws Exception {
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
    void assignCaseForbidden() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(ASSIGN_CASE)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTransferSameCountryAddsCaseMgtLocationCode() throws Exception {
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
    void testTransferDifferentCountryUpdatesCaseMgtLocation() throws Exception {
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
    void assignCaseEnglandWalesUpdatesCaseMgtLocation() throws Exception {
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

        verify(caseManagementLocationService, times(2))
                .setCaseManagementLocationCode(any());
    }

    @Test
    void assignCase_invokesServicesAndUpdatesCaseData() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("Unassigned");
        DynamicFixedListType dl = new DynamicFixedListType();
        var dvt = new DynamicValueType();
        dvt.setLabel("Leeds2");
        dl.setValue(dvt);
        dl.getValue().setCode(TribunalOffice.LEEDS.getOfficeName());
        dl.setListItems(List.of(dvt));
        caseData.setAssignOffice(dl);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);

        VerifyTokenService verifyTokenService = mock(VerifyTokenService.class);
        CaseTransferSameCountryService sameCountryService = mock(CaseTransferSameCountryService.class);
        CaseTransferDifferentCountryService diffCountryService = mock(CaseTransferDifferentCountryService.class);
        CaseTransferToEcmService toEcmService = mock(CaseTransferToEcmService.class);
        DefaultValuesReaderService defaultValuesReaderService = mock(DefaultValuesReaderService.class);
        CaseManagementLocationService caseManagementLocationService = mock(CaseManagementLocationService.class);
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        when(verifyTokenService.verifyTokenSignature("token")).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);
        DefaultValues defaultValues = mock(DefaultValues.class);
        when(defaultValuesReaderService.getDefaultValues("Unassigned")).thenReturn(defaultValues);

        doNothing().when(caseManagementLocationService).setCaseManagementLocationCode(caseData);
        doAnswer(i -> {
            caseData.setCaseManagementLocationCode("123");
            return null;
        }).when(caseManagementLocationService).setCaseManagementLocation(caseData);

        doAnswer(i -> {
            caseData.setCaseManagementLocation(CaseLocation.builder().baseLocation("Leeds").region("321").build());
            return null;
        }).when(caseManagementLocationService).setCaseManagementLocationCode(caseData);

        CaseTransferController controller = new CaseTransferController(
                verifyTokenService, sameCountryService, diffCountryService, toEcmService,
                defaultValuesReaderService, caseManagementLocationService, featureToggleService);

        // Act
        ResponseEntity<?> response = controller.assignCase(ccdRequest, "token");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        verify(verifyTokenService).verifyTokenSignature("token");
        verify(caseManagementLocationService, times(2)).setCaseManagementLocationCode(caseData);
        verify(caseManagementLocationService).setCaseManagementLocation(caseData);
        assertNotNull(caseData.getManagingOffice());
        assertNotNull(caseData.getCaseManagementLocation());
        assertNotNull(caseData.getCaseManagementLocationCode());
    }
}
