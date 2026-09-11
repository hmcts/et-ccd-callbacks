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
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({ClaimantRepresentativeController.class, JsonMapper.class})
class ClaimantRepresentativeControllerTest {

    @MockitoBean
    private VerifyTokenService verifyTokenService;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void testRemoveOwnRepresentative_ClaimantRepresentedQuestionEmpty() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(
                CaseDataBuilder.builder().withClaimantRepresentedQuestion(null).build()).build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/claimantRepresentative/removeOwnRepresentative")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void testRemoveOwnRepresentative_ClaimantRepresentedQuestionNo() throws Exception {
        CaseData caseData = CaseDataBuilder.builder().withClaimantRepresentedQuestion(NO).build();
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder().build());
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(CaseFlagsType.builder().build())
                .claimantExternalFlags(CaseFlagsType.builder().build())
                .claimantRepresentativeFlags(CaseFlagsType.builder().build())
                .claimantRepresentativeExternalFlags(CaseFlagsType.builder().build())
                .build());
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        when(featureToggleService.isCaseFlagsV2Enabled(anyString())).thenReturn(true);

        mockMvc.perform(post("/claimantRepresentative/removeOwnRepresentative")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.representativeClaimantType", nullValue()))
                .andExpect(jsonPath("$.data.claimantRepresentativeFlags", nullValue()))
                .andExpect(jsonPath("$.data.claimantRepresentativeExternalFlags", nullValue()))
                .andExpect(jsonPath("$.data.claimantFlags", notNullValue()))
                .andExpect(jsonPath("$.data.claimantExternalFlags", notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void testRemoveOwnRepresentative_PreservesCaseFlagsWhenV2Disabled() throws Exception {
        CaseData caseData = CaseDataBuilder.builder().withClaimantRepresentedQuestion(NO).build();
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder().build());
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantRepresentativeFlags(CaseFlagsType.builder().build())
                .claimantRepresentativeExternalFlags(CaseFlagsType.builder().build())
                .build());
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        when(featureToggleService.isCaseFlagsV2Enabled(ENGLANDWALES_CASE_TYPE_ID)).thenReturn(false);

        mockMvc.perform(post("/claimantRepresentative/removeOwnRepresentative")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.representativeClaimantType", nullValue()))
                .andExpect(jsonPath("$.data.claimantRepresentativeFlags", notNullValue()))
                .andExpect(jsonPath("$.data.claimantRepresentativeExternalFlags", notNullValue()));
    }

    @Test
    void testRemoveOwnRepresentative_ClaimantRepresentedQuestionYes() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(
                CaseDataBuilder.builder().withClaimantRepresentedQuestion(YES).build()).build();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/claimantRepresentative/removeOwnRepresentative")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }
}
