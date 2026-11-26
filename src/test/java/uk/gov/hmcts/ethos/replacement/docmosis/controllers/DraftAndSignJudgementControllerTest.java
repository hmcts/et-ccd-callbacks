package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DraftAndSignJudgementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.DraftAndSignJudgementService.ERROR_MSG_MISSING_NON_PUBLICATION_REASON;

@ExtendWith(SpringExtension.class)
@WebMvcTest({DraftAndSignJudgementController.class, JsonMapper.class})
class DraftAndSignJudgementControllerTest {

    private static final String AUTH_TOKEN = "some-token";

    @MockitoBean
    private VerifyTokenService verifyTokenService;
    @MockitoBean
    private DraftAndSignJudgementService draftAndSignJudgementService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void testMidEventNoErrors() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(draftAndSignJudgementService.validateNonPublicationReason(any(CaseData.class))).thenReturn(List.of());

        mockMvc.perform(post("/draftAndSignJudgement/midEvent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(draftAndSignJudgementService, times(1)).validateNonPublicationReason(any(CaseData.class));
    }

    @Test
    void testMidEventWithValidationError() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(draftAndSignJudgementService.validateNonPublicationReason(any(CaseData.class)))
            .thenReturn(List.of(ERROR_MSG_MISSING_NON_PUBLICATION_REASON));

        mockMvc.perform(post("/draftAndSignJudgement/midEvent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath("$.errors[0]", is(ERROR_MSG_MISSING_NON_PUBLICATION_REASON)))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(draftAndSignJudgementService, times(1)).validateNonPublicationReason(any(CaseData.class));
    }

    @Test
    void testAboutToSubmit() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(draftAndSignJudgementService.validateNonPublicationReason(any(CaseData.class))).thenReturn(List.of());

        mockMvc.perform(post("/draftAndSignJudgement/aboutToSubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(draftAndSignJudgementService, times(1)).validateNonPublicationReason(any(CaseData.class));
        verify(draftAndSignJudgementService, times(1)).checkAndSendEmails(any(CaseDetails.class), eq(AUTH_TOKEN));
    }

    @Test
    void testAboutToSubmitWithValidationError() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(draftAndSignJudgementService.validateNonPublicationReason(any(CaseData.class)))
            .thenReturn(List.of(ERROR_MSG_MISSING_NON_PUBLICATION_REASON));

        mockMvc.perform(post("/draftAndSignJudgement/aboutToSubmit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath("$.errors[0]", is(ERROR_MSG_MISSING_NON_PUBLICATION_REASON)))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(draftAndSignJudgementService, times(1)).validateNonPublicationReason(any(CaseData.class));
        verify(draftAndSignJudgementService, never()).checkAndSendEmails(any(CaseDetails.class), any());
    }
}