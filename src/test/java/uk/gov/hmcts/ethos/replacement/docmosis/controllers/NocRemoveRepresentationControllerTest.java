package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRemoveRepNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRemoveRepresentationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@WebMvcTest({NocRemoveRepresentationController.class, JsonMapper.class})
class NocRemoveRepresentationControllerTest extends BaseControllerTest {

    private static final String NOC_REQUEST_CLAIMANT_ABOUT_TO_START =
            "/nocRemoveRepresentation/claimant/aboutToStart";
    private static final String NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT =
            "/nocRemoveRepresentation/claimant/aboutToSubmit";

    private static final String BAD_REQUEST_CONTENT = "bad request content";
    private static final String USER_ID = "dummyUserId";
    private static final String USER_NAME = "dummyUserName";

    private CCDRequest ccdRequest;
    private UserDetails userDetails;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private NocRemoveRepresentationService nocRemoveRepresentationService;
    @MockitoBean
    private NocRemoveRepNotificationService nocRemoveRepNotificationService;

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
        userDetails = new UserDetails();
        userDetails.setUid(USER_ID);
        userDetails.setEmail(USER_NAME);
    }

    @Test
    @SneakyThrows
    void aboutToStartClaimant_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(userService.getValidatedUserDetails(AUTH_TOKEN, ccdRequest.getCaseDetails().getCaseId()))
                .thenReturn(userDetails);
        doNothing().when(nocRemoveRepresentationService).setNocRemoveOption(userDetails, ccdRequest.getCaseDetails());
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_START)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS).value((empty())))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToStartClaimant_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_START)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToStartClaimant_badRequest() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_START)
                        .content(BAD_REQUEST_CONTENT)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void aboutToSubmitClaimant_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(userService.getValidatedUserDetails(AUTH_TOKEN, ccdRequest.getCaseDetails().getCaseId()))
                .thenReturn(userDetails);
        doNothing().when(nocRemoveRepresentationService).setNocRemoveOption(userDetails, ccdRequest.getCaseDetails());
        doNothing().when(nocRemoveRepresentationService).revokeClaimantLegalRep(any(CaseDetails.class));
        doNothing().when(nocRemoveRepNotificationService).sendClaimantRepresentativeRemovalNotifications(
                eq(userDetails), any(CaseDetails.class));
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS).value((empty())))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToSubmitClaimant_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToSubmitClaimant_badRequest() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT)
                        .content(BAD_REQUEST_CONTENT)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}