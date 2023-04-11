package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({Et1VettingController.class, JsonMapper.class})
class Et1VettingControllerTest {

    private static final String INIT_CASE_VETTING_ENDPOINT = "/initialiseEt1Vetting";
    private static final String JURISDICTION_CODE_ENDPOINT = "/jurisdictionCodes";
    private static final String HEARING_VENUE_ENDPOINT = "/et1HearingVenue";
    private static final String ET1_PROCESSING_COMPLETE_URL = "/finishEt1Vetting";
    private static final String ET1_VETTING_ABOUT_TO_SUBMIT = "/et1VettingAboutToSubmit";
    private static final String AUTH_TOKEN = "some-token";
    private CCDRequest ccdRequest;

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private Et1VettingService et1VettingService;
    @MockBean
    private ReportDataService reportDataService;

    @MockBean
    private DocumentManagementService documentManagementService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("Manchester");
        addJurCodeToExistingCollection(caseData, JurisdictionCode.DOD.name());
        ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
    }

    @Test
    void initialiseEt1Vetting_Success() throws Exception {

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(et1VettingService.generateJurisdictionCodesHtml(anyList())).thenReturn("jurCodeHtml");
        mockMvc.perform(post(INIT_CASE_VETTING_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.existingJurisdictionCodes", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(et1VettingService, times(1)).initialiseEt1Vetting(any());
        verify(et1VettingService, times(1)).generateJurisdictionCodesHtml(anyList());
    }

    @Test
    void initialiseEt1Vetting_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(INIT_CASE_VETTING_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(et1VettingService, never()).initialiseEt1Vetting(ccdRequest.getCaseDetails());
    }

    @Test
    void initialiseEt1Vetting_BadRequest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(INIT_CASE_VETTING_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content("error"))
                .andExpect(status().isBadRequest());
        verify(et1VettingService, never()).initialiseEt1Vetting(ccdRequest.getCaseDetails());
    }

    @Test
    void jurisdictionCodes_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(et1VettingService.validateJurisdictionCodes(any())).thenReturn(List.of());
        when(et1VettingService.populateEt1TrackAllocationHtml(any())).thenReturn("jurCodeHtml");
        mockMvc.perform(post(JURISDICTION_CODE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.trackAllocation", notNullValue()))
            .andExpect(jsonPath("$.errors", empty()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(et1VettingService, times(1)).validateJurisdictionCodes(any());
        verify(et1VettingService, times(1)).populateEt1TrackAllocationHtml(any());
        verify(et1VettingService, times(1)).populateTribunalOfficeFields(any());

    }

    @Test
    void jurisdictionCodes_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(JURISDICTION_CODE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
        verify(et1VettingService, never()).initialiseEt1Vetting(ccdRequest.getCaseDetails());
    }

    @Test
    void hearingVenues_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(et1VettingService.getAddressesHtml(any())).thenReturn("addressesHtml");
        when(et1VettingService.getHearingVenuesList(any())).thenReturn(new DynamicFixedListType());
        mockMvc.perform(post(HEARING_VENUE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.et1AddressDetails", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void hearingVenues_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(HEARING_VENUE_ENDPOINT)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
        verify(et1VettingService, never()).initialiseEt1Vetting(ccdRequest.getCaseDetails());
    }

    private void addJurCodeToExistingCollection(CaseData caseData, String code) {
        JurCodesType newCode = new JurCodesType();
        newCode.setJuridictionCodesList(code);
        JurCodesTypeItem codesTypeItem = new JurCodesTypeItem();
        codesTypeItem.setValue(newCode);
        caseData.setJurCodesCollection(new ArrayList<>());
        caseData.getJurCodesCollection().add(codesTypeItem);
    }

    @Test
    void et1ProcessingComplete_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ET1_PROCESSING_COMPLETE_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void et1ProcessingComplete_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ET1_PROCESSING_COMPLETE_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void et1ProcessingComplete_badRequest() throws Exception {
        mockMvc.perform(post(ET1_PROCESSING_COMPLETE_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void et1VettingAboutToSubmittokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ET1_VETTING_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void et1VettingAboutToSubmit_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ET1_VETTING_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void et1VettingAboutToSubmit_badRequest() throws Exception {
        mockMvc.perform(post(ET1_VETTING_ABOUT_TO_SUBMIT)
                        .content("garbage content")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}