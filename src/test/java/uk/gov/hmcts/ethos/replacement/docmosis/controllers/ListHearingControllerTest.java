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
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandVenueSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.VenueSelectionService;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest({ListHearingController.class, JsonMapper.class})
class ListHearingControllerTest extends BaseControllerTest {

    @MockBean
    private VenueSelectionService venueSelectionService;

    @MockBean
    private ScotlandVenueSelectionService scotlandVenueSelectionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired

    private JsonMapper jsonMapper;

    @Test
    void testInitialiseHearingEnglandWales() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID).build();
        String userToken = "some-token";
        when(verifyTokenService.verifyTokenSignature(userToken)).thenReturn(true);

        mockMvc.perform(post("/initialiseHearings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", userToken)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(venueSelectionService, times(1)).initHearingCollection(ccdRequest.getCaseDetails().getCaseData());
        verify(scotlandVenueSelectionService, never()).initHearingCollection(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void testInitialiseHearingScotland() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseTypeId(SCOTLAND_CASE_TYPE_ID).build();
        String userToken = "some-token";
        when(verifyTokenService.verifyTokenSignature(userToken)).thenReturn(true);

        mockMvc.perform(post("/initialiseHearings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", userToken)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(scotlandVenueSelectionService, times(1)).initHearingCollection(
                ccdRequest.getCaseDetails().getCaseData());
        verify(venueSelectionService, never()).initHearingCollection(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void testInitialiseHearingInvalidToken() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID).build();
        String userToken = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(userToken)).thenReturn(false);

        mockMvc.perform(post("/initialiseHearings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", userToken)
                .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isForbidden());

        verify(venueSelectionService, never()).initHearingCollection(ccdRequest.getCaseDetails().getCaseData());
        verify(scotlandVenueSelectionService, never()).initHearingCollection(ccdRequest.getCaseDetails().getCaseData());
    }
}
