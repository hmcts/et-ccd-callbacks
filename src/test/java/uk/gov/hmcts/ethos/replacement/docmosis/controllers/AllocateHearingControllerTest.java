package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.AllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandAllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@RunWith(SpringRunner.class)
@WebMvcTest({AllocateHearingController.class, JsonMapper.class})
public class AllocateHearingControllerTest {

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private AllocateHearingService allocateHearingService;

    @MockBean
    private ScotlandAllocateHearingService scotlandAllocateHearingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    public void testInitialiseHearingDynamicList() throws Exception {
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
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(allocateHearingService, times(1)).initialiseAllocateHearing(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testInitialiseHearingDynamicListInvalidToken() throws Exception {
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
        verify(allocateHearingService, never()).initialiseAllocateHearing(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testHandleListingSelected() throws Exception {
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
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(allocateHearingService, times(1)).handleListingSelected(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testHandleListingSelectedInvalidToken() throws Exception {
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
        verify(allocateHearingService, never()).handleListingSelected(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testHandleListingSelectedInvalidCaseTypeId() throws Exception {
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
        verify(allocateHearingService, never()).handleListingSelected(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testPopulateRoomsEnglandWales() throws Exception {
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
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(allocateHearingService, times(1)).populateRooms(ccdRequest.getCaseDetails().getCaseData());
        verify(scotlandAllocateHearingService, never()).populateRooms(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testPopulateRoomsScotland() throws Exception {
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
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(scotlandAllocateHearingService, times(1)).populateRooms(ccdRequest.getCaseDetails().getCaseData());
        verify(allocateHearingService, never()).populateRooms(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testPopulateRoomsInvalidToken() throws Exception {
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
        verify(allocateHearingService, never()).populateRooms(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testAboutToSubmit() throws Exception {
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
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(allocateHearingService, times(1)).updateCase(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    public void testAboutToSubmitInvalidToken() throws Exception {
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
        verify(allocateHearingService, never()).updateCase(ccdRequest.getCaseDetails().getCaseData());
    }
}
