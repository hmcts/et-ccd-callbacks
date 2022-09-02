package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CloseReferralController.class, JsonMapper.class})
class CloseReferralControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String START_CLOSE_REFERRAL_URL = "/closeReferral/aboutToStart";
    private static final String INIT_HEARING_AND_REFERRAL_DETAILS_URL = "/closeReferral/initHearingAndReferralDetails";
    private static final String ABOUT_TO_SUBMIT_URL = "/closeReferral/aboutToSubmit";
    private static final String SUBMITTED_REFERRAL_URL = "/closeReferral/completeCloseReferral";

    @MockBean
    private VerifyTokenService verifyTokenService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;

    @BeforeEach
    void setUp() {
        CaseData caseData = CaseDataBuilder.builder()
            .withHearingScotland("hearingNumber", HEARING_TYPE_JUDICIAL_HEARING, "Judge",
                TribunalOffice.ABERDEEN, "venue")
            .withHearingSession(
                0,
                "hearingNumber",
                "2019-11-25T12:11:00.000",
                Constants.HEARING_STATUS_HEARD,
                true)
            .build();
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        DynamicFixedListType selectReferralList = ReferralHelper.populateSelectReferralDropdown(caseData);
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        caseData.setSelectReferral(selectReferralList);
        caseData.setEthosCaseReference("caseRef");
        caseData.setClaimant("claimant");
        caseData.setIsUrgent("Yes");
        ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
    }

    @Test
    void aboutToStartReferralReply_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(START_CLOSE_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void initHearingAndReferralDetails_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(INIT_HEARING_AND_REFERRAL_DETAILS_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void aboutToSubmitCloseReferral_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void aboutToStartCloseReferral_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(START_CLOSE_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void initHearingAndReferralDetails_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(INIT_HEARING_AND_REFERRAL_DETAILS_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmitCloseReferral_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void completeCloseReferral_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(SUBMITTED_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_body", notNullValue()));
    }

    @Test
    void completeCloseReferral_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(SUBMITTED_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    private ReferralTypeItem createReferralTypeItem() {
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        referralType.setReferralSubject("Other");
        referralTypeItem.setValue(referralType);
        referralType.setReferralStatus("referralStatus");
        return referralTypeItem;
    }
}