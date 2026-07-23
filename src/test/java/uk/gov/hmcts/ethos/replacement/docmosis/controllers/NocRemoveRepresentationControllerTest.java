package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRemoveRepresentationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({NocRemoveRepresentationController.class, JsonMapper.class})
class NocRemoveRepresentationControllerTest extends BaseControllerTest {

    private static final String NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT =
        "/nocRemoveRepresentation/claimant/aboutToSubmit";
    private static final String NOC_REQUEST_RESPONDENT_ABOUT_TO_SUBMIT =
        "/nocRemoveRepresentation/respondent/aboutToSubmit";
    private CCDRequest ccdRequest;

    @MockitoBean
    private NocRemoveRepresentationService nocRemoveRepresentationService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(CaseDataBuilder.builder()
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID)
                .getCaseData())
            .build();
    }

    @Test
    void aboutToSubmitClaimant_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT)
                .content(jsonMapper.toJson(ccdRequest))
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToSubmitClaimant_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT)
                .content(jsonMapper.toJson(ccdRequest))
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmitClaimant_badRequest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT)
                .content("garbage content")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void aboutToSubmitRespondent_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(NOC_REQUEST_RESPONDENT_ABOUT_TO_SUBMIT)
                .content(jsonMapper.toJson(ccdRequest))
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToSubmitRespondent_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(NOC_REQUEST_RESPONDENT_ABOUT_TO_SUBMIT)
                .content(jsonMapper.toJson(ccdRequest))
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmitRespondent_badRequest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(NOC_REQUEST_RESPONDENT_ABOUT_TO_SUBMIT)
                .content("garbage content")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}