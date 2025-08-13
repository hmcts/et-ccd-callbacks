package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.HearingDocumentsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingDocumentsHelper.HEARING_DOCUMENT_NO_HEARING_ERROR;

@ExtendWith(SpringExtension.class)
@WebMvcTest({UploadHearingDocumentController.class, JsonMapper.class})
class UploadHearingDocumentControllerTest {
    private static final String ABOUT_TO_START_URL = "/uploadHearingDocuments/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/uploadHearingDocuments/aboutToSubmit";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    @MockBean
    private HearingDocumentsService hearingDocumentsService;
    @MockBean
    private VerifyTokenService verifyTokenService;

    @BeforeEach
    void setUp() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withHearing("1", HEARING_TYPE_JUDICIAL_HEARING, "Judge", "Venue", null, null, null, null)
            .withHearingSession(0, "2019-11-25T12:11:00.000", Constants.HEARING_STATUS_LISTED, false)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);
    }

    @Test
    void aboutToStart() throws Exception {
        mockMvc.perform(post(ABOUT_TO_START_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.uploadHearingDocumentsSelectFutureHearing", notNullValue()))
            .andExpect(jsonPath("$.data.uploadHearingDocumentsSelectPastHearing", notNullValue()));
    }

    @Test
    void aboutToStart_noHearings() throws Exception {
        ccdRequest.getCaseDetails().getCaseData().setHearingCollection(null);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]", is(HEARING_DOCUMENT_NO_HEARING_ERROR)))
            .andExpect(jsonPath("$.data.uploadHearingDocumentsSelectFutureHearing", nullValue()))
            .andExpect(jsonPath("$.data.uploadHearingDocumentsSelectPastHearing", nullValue()));
    }

    @Test
    void aboutToSubmit() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk());
    }

}
