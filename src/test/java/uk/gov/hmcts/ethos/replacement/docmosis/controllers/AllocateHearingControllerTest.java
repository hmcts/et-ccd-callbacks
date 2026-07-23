package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.AllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandAllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({AllocateHearingController.class, JsonMapper.class})
class AllocateHearingControllerTest {

    @MockitoBean
    private VerifyTokenService verifyTokenService;

    @MockitoBean
    private AllocateHearingService allocateHearingService;

    @MockitoBean
    private ScotlandAllocateHearingService scotlandAllocateHearingService;

    @MockitoBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void testInitialiseHearingDynamicList() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/allocatehearing/initialiseHearings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(allocateHearingService, times(1)).initialiseAllocateHearing(any(CaseData.class));
    }

    @Test
    void testInitialiseHearingDynamicListInvalidToken() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/allocatehearing/initialiseHearings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());
        verify(allocateHearingService, never()).initialiseAllocateHearing(any(CaseData.class));
    }

    @Test
    void testHandleListingSelected() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/allocatehearing/handleListingSelected")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(allocateHearingService, times(1)).handleListingSelected(any(CaseData.class));
    }

    @Test
    void testHandleListingSelectedInvalidToken() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/allocatehearing/handleListingSelected")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());
        verify(allocateHearingService, never()).handleListingSelected(any(CaseData.class));
    }

    @Test
    void testHandleListingSelectedInvalidCaseTypeId() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId("InvalidCaseTypeId")
                .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/allocatehearing/handleListingSelected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isBadRequest());
        verify(allocateHearingService, never()).handleListingSelected(any(CaseData.class));
    }

    @Test
    void testPopulateRoomsEnglandWales() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/allocatehearing/populateRooms")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(allocateHearingService, times(1)).populateRooms(any(CaseData.class));
        verify(scotlandAllocateHearingService, never()).populateRooms(any(CaseData.class));
    }

    @Test
    void testPopulateRoomsScotland() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(SCOTLAND_CASE_TYPE_ID)
                .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/allocatehearing/populateRooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(scotlandAllocateHearingService, times(1)).populateRooms(any(CaseData.class));
        verify(allocateHearingService, never()).populateRooms(any(CaseData.class));
    }

    @Test
    void testPopulateRoomsInvalidToken() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/allocatehearing/populateRooms")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());
        verify(allocateHearingService, never()).populateRooms(any(CaseData.class));
    }

    @Test
    void testAboutToSubmit_EnglandWales() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/allocatehearing/aboutToSubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(allocateHearingService, times(1)).updateSelectedHearing(any(CaseData.class));
        verify(allocateHearingService, times(1)).updateCase(any(CaseData.class));
    }

    @Test
    void testAboutToSubmit_Scotland() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
            .withCaseTypeId(SCOTLAND_CASE_TYPE_ID)
            .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/allocatehearing/aboutToSubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(allocateHearingService, times(1)).updateSelectedHearing(any(CaseData.class));
        verify(scotlandAllocateHearingService, times(1)).updateCase(any(CaseData.class));
    }

    @Test
    void testAboutToSubmitInvalidToken() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/allocatehearing/aboutToSubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());
        verify(allocateHearingService, never()).updateSelectedHearing(any(CaseData.class));
        verify(allocateHearingService, never()).updateCase(any(CaseData.class));
    }
}
