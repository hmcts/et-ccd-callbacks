package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole.CLAIMANTSOLICITOR;

@ExtendWith(SpringExtension.class)
@WebMvcTest({ClaimantRepresentativeController.class, JsonMapper.class})
class ClaimantRepresentativeControllerTest {

    private static final String DUMMY_TOKEN = "some-token";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String REMOVE_OWN_REPRESENTATIVE_URL = "/claimantRepresentative/removeOwnRepresentative";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String REPRESENTATIVE_NAME = "Claimant Representative";

    @MockitoBean
    private VerifyTokenService verifyTokenService;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        when(verifyTokenService.verifyTokenSignature(DUMMY_TOKEN)).thenReturn(true);
    }

    @Test
    void testRemoveOwnRepresentative_ClaimantRepresentedQuestionEmpty() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(
                CaseDataBuilder.builder().withClaimantRepresentedQuestion(null).build()).build();

        mockMvc.perform(post(REMOVE_OWN_REPRESENTATIVE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void testRemoveOwnRepresentative_ClaimantRepresentedQuestionNo() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(
                caseDataWithClaimantRepresentativeFlags(NO)).build();

        mockMvc.perform(post(REMOVE_OWN_REPRESENTATIVE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.representativeClaimantType").doesNotExist())
                .andExpect(jsonPath("$.data.claimantRepresentativeOrganisationPolicy.OrgPolicyCaseAssignedRole")
                        .value(CLAIMANTSOLICITOR.getCaseRoleLabel()))
                .andExpect(jsonPath("$.data.claimantFlags.partyName").value(CLAIMANT_NAME))
                .andExpect(jsonPath("$.data.claimantRepresentativeFlags").doesNotExist())
                .andExpect(jsonPath("$.data.claimantRepresentativeExternalFlags").doesNotExist())
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void testRemoveOwnRepresentative_ClaimantRepresentedQuestionYes() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(
                caseDataWithClaimantRepresentativeFlags(YES)).build();

        mockMvc.perform(post(REMOVE_OWN_REPRESENTATIVE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.representativeClaimantType.name_of_representative")
                        .value(REPRESENTATIVE_NAME))
                .andExpect(jsonPath("$.data.claimantRepresentativeFlags.partyName").value(REPRESENTATIVE_NAME))
                .andExpect(jsonPath("$.data.claimantRepresentativeExternalFlags.partyName")
                        .value(REPRESENTATIVE_NAME))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    private static CaseData caseDataWithClaimantRepresentativeFlags(String claimantRepresentedQuestion) {
        CaseData caseData = CaseDataBuilder.builder()
                .withClaimantRepresentedQuestion(claimantRepresentedQuestion)
                .build();
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder()
                .nameOfRepresentative(REPRESENTATIVE_NAME)
                .build());
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(CaseFlagsType.builder().partyName(CLAIMANT_NAME).build())
                .claimantRepresentativeFlags(CaseFlagsType.builder().partyName(REPRESENTATIVE_NAME).build())
                .claimantRepresentativeExternalFlags(CaseFlagsType.builder().partyName(REPRESENTATIVE_NAME).build())
                .build());
        return caseData;
    }
}
