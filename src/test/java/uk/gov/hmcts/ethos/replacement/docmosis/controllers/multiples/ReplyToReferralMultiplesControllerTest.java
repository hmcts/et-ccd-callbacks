package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.BaseControllerTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ReferralsUtil.createReferralTypeItem;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ReferralsUtil.createRespondentType;

@ExtendWith(SpringExtension.class)
@WebMvcTest({ReplyToReferralMultiplesController.class, JsonMapper.class})
@ActiveProfiles("test")
class ReplyToReferralMultiplesControllerTest extends BaseControllerTest {
    private static final String START_REPLY_REFERRAL_URL = "/multiples/replyReferral/aboutToStart";
    private static final String MID_HEARING_DETAILS_URL = "/multiples/replyReferral/initHearingAndReferralDetails";
    private static final String ABOUT_TO_SUBMIT_URL = "/multiples/replyReferral/aboutToSubmit";
    private static final String SUBMITTED_REFERRAL_URL = "/multiples/replyReferral/completeReplyToReferral";
    private static final String VALIDATE_EMAIL_URL = "/multiples/replyReferral/validateReplyToEmail";

    @MockitoBean
    private UserIdamService userIdamService;
    @MockitoBean
    private CaseLookupService caseLookupService;
    @MockitoBean
    private ReferralService referralService;
    @MockitoBean
    private DocumentManagementService documentManagementService;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private MultipleRequest request;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        when(emailService.getExuiCaseLink(any())).thenReturn("exui");
        CaseData caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingScotland("hearingNumber", HEARING_TYPE_JUDICIAL_HEARING, "Judge",
                TribunalOffice.ABERDEEN, "venue")
            .withHearingSession(
                0, "2019-11-25T12:11:00.000",
                Constants.HEARING_STATUS_HEARD,
                true)
            .build();

        List<HearingTypeItem> hearings = new ArrayList<>();
        caseData.setHearingCollection(hearings);
        caseData.setIsJudge("Yes");
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));
        caseData.setEthosCaseReference("caseRef");
        caseData.setClaimant("claimant");
        caseData.setIsUrgent("Yes");
        caseData.setReplyToEmailAddress("test@gmail.com");

        MultipleData multipleData = MultipleData.builder().build();
        request = new MultipleRequest();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        request.setCaseDetails(multipleDetails);

        multipleData.setReferralCollection(List.of(createReferralTypeItem()));
        DynamicFixedListType selectReferralList = 
            ReferralHelper.populateSelectReferralDropdown(multipleData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        multipleData.setSelectReferral(selectReferralList);

        UserDetails userDetails = new UserDetails();
        userDetails.setRoles(List.of("role1"));
        when(userIdamService.getUserDetails(any())).thenReturn(userDetails);
        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(any())).thenReturn(caseData);
    }

    @Test
    void aboutToStartReferralReply_Success() throws Exception {

        mockMvc.perform(post(START_REPLY_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToSubmit_NoReplyToEmailAddress_tokenOk() throws Exception {

        UserDetails details = new UserDetails();
        details.setName("First Last");
        request.getCaseDetails().getCaseData().setReplyToEmailAddress("");
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void initHearingAndReferralDetails_Success() throws Exception {

        mockMvc.perform(post(MID_HEARING_DETAILS_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToSubmitReferralReply_Success() throws Exception {

        UserDetails details = new UserDetails();
        details.setName("First Last");
        when(userIdamService.getUserDetails(any())).thenReturn(details);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToStartReferralReply_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(START_REPLY_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void initHearingAndReferralDetails_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_HEARING_DETAILS_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmitReferralReply_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        when(userIdamService.getUserDetails(any())).thenReturn(new UserDetails());
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void completeReplyToReferral_tokenOk() throws Exception {

        mockMvc.perform(post(SUBMITTED_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_body", notNullValue()));
    }

    @Test
    void completeReplyToReferral_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(SUBMITTED_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void validateReplyToEmail_tokenOk() throws Exception {
        mockMvc.perform(post(VALIDATE_EMAIL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)));
    }

    @Test
    void validateNoReplyToEmail_tokenOk() throws Exception {
        request.getCaseDetails().getCaseData().setReplyToEmailAddress("");
        mockMvc.perform(post(VALIDATE_EMAIL_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)));
    }

    @Test
    void validateReplyToEmail_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_EMAIL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isForbidden());
    }
}
