package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocClaimantRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({AddAmendClaimantRepresentativeController.class, JsonMapper.class})
class AddAmendClaimantRepresentativeControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ABOUT_TO_SUBMIT_URL = "/addAmendClaimantRepresentative/aboutToSubmit";
    private static final String SUBMITTED_URL = "/addAmendClaimantRepresentative/amendClaimantRepSubmitted";
    private static final String AMEND_CLAIMANT_REPRESENTATIVE_MID_EVENT =
            "/addAmendClaimantRepresentative/amendClaimantRepresentativeMidEvent";
    private static final String CASE_ID = "1234567890123456";
    private static final String TEST_USER_EMAIL = "test@test.com";

    private static final String EXPECTED_ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES =
            "Representative Representative Name organisation does not match with selected organisation ORG1";

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private NocClaimantRepresentativeService nocClaimantRepresentativeService;
    @MockBean
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @MockBean
    private UserIdamService userIdamService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    private CCDRequest ccdRequest;

    @BeforeEach
    void setUp() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.getCaseData().setEthosCaseReference("1234");

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseDetails.getCaseData())
                .withCaseId(CASE_ID)
                .build();
    }

    @Test
    void testAboutToSubmitSetsClaimantRepresentativeId() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocClaimantRepresentativeService.validateClaimantRepresentativeOrganisation(any(CaseDetails.class)))
                .thenReturn(StringUtils.EMPTY);
        doNothing().when(nocRespondentRepresentativeService)
                .revokeRespondentRepresentativesWithSameOrganisationAsClaimant(any(CaseDetails.class));
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", empty()))
                .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void testAboutToSubmitSetsClaimantRepresentativeId_WithErrors() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocClaimantRepresentativeService.validateClaimantRepresentativeOrganisation(any(CaseDetails.class)))
                .thenReturn(EXPECTED_ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES);
        doNothing().when(nocRespondentRepresentativeService)
                .revokeRespondentRepresentativesWithSameOrganisationAsClaimant(any(CaseDetails.class));
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", contains(
                        EXPECTED_ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES)))
                .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void testAmendClaimantRepresentativeMidEvent() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        UserDetails userDetails = new UserDetails();
        userDetails.setEmail("test@test.com");
        when(userIdamService.getUserDetails(any())).thenReturn(userDetails);
        mockMvc.perform(post(SUBMITTED_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(nocClaimantRepresentativeService, times(1))
                .updateClaimantRepAccess(any());
    }

    @Test
    void testAmendClaimantRepSubmitted() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        UserDetails userDetails = new UserDetails();
        userDetails.setEmail(TEST_USER_EMAIL);
        when(userIdamService.getUserDetails(any())).thenReturn(userDetails);
        doNothing().when(nocClaimantRepresentativeService).validateRepresentativeOrganisationAndEmail(any(
                CaseData.class));
        mockMvc.perform(post(AMEND_CLAIMANT_REPRESENTATIVE_MID_EVENT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(nocClaimantRepresentativeService, times(LoggerTestUtils.INTEGER_ONE))
                .validateRepresentativeOrganisationAndEmail(any(CaseData.class));
    }
}
