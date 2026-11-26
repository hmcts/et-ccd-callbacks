package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRemoveClaimantRepresentationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRemoveRepNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private NocRemoveClaimantRepresentationService nocRemoveClaimantRepresentationService;
    @MockitoBean
    private NocRemoveRepNotificationService nocRemoveRepNotificationService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    private static final String NOC_REQUEST_CLAIMANT_ABOUT_TO_START =
            "/nocRemoveRepresentation/claimant/aboutToStart";
    private static final String NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT =
            "/nocRemoveRepresentation/claimant/aboutToSubmit";

    private CCDRequest ccdRequest;
    private UserDetails userDetails;

    private static final String BAD_REQUEST_CONTENT = "bad request content";
    private static final String CASE_ID = "1234567890123456";
    private static final String USER_ID = "dda9d1c3-1a11-3c3a-819e-74174fbec26b";
    private static final String USER_FIRST_NAME = "Joe";
    private static final String USER_LAST_NAME = "Doe";
    private static final String USER_EMAIL = "testUserEmail@hmcts.net";

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseId(CASE_ID)
                .withCaseData(CaseDataBuilder.builder()
                        .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID)
                        .getCaseData())
                .build();
        userDetails = new UserDetails();
        userDetails.setUid(USER_ID);
        userDetails.setEmail(USER_EMAIL);
        userDetails.setFirstName(USER_FIRST_NAME);
        userDetails.setLastName(USER_LAST_NAME);
        userDetails.setRoles(List.of(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()));
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().idamId(USER_ID)
                .representativeEmailAddress(USER_EMAIL).build();
        ccdRequest.getCaseDetails().getCaseData().setRepresentativeClaimantType(claimantRepresentative);
        when(userService.fetchAndValidateUserDetails(AUTH_TOKEN, ccdRequest.getCaseDetails().getCaseId()))
                .thenReturn(userDetails);
    }

    @Test
    @SneakyThrows
    void aboutToStartClaimant_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(userService.fetchAndValidateUserDetails(AUTH_TOKEN, ccdRequest.getCaseDetails().getCaseId()))
                .thenReturn(userDetails);
        doNothing().when(nocRemoveClaimantRepresentationService).populateNocRemovalDetails(eq(userDetails),
                eq(ccdRequest.getCaseDetails()), anyString());
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
    void aboutToSubmitClaimant_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        doNothing().when(nocRemoveClaimantRepresentationService).populateNocRemovalDetails(eq(userDetails),
                eq(ccdRequest.getCaseDetails()), anyString());
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToStartClaimant_badRequest() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doNothing().when(nocRemoveClaimantRepresentationService).populateNocRemovalDetails(eq(userDetails),
                eq(ccdRequest.getCaseDetails()), anyString());
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
        when(userService.fetchAndValidateUserDetails(AUTH_TOKEN, ccdRequest.getCaseDetails().getCaseId()))
                .thenReturn(userDetails);
        doNothing().when(nocRemoveClaimantRepresentationService).revokeClaimantLegalRep(eq(userDetails),
                any(CaseDetails.class));
        doNothing().when(nocRemoveRepNotificationService).sendClaimantRepresentativeRemovalNotifications(
                eq(userDetails), any(CaseDetails.class),
                eq(ccdRequest.getCaseDetails().getCaseData().getRepresentativeClaimantType()));
        doNothing().when(nocRemoveClaimantRepresentationService).populateNocRemovalDetails(eq(userDetails),
                eq(ccdRequest.getCaseDetails()), anyString());
        mockMvc.perform(post(NOC_REQUEST_CLAIMANT_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS).value((empty())))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }
}