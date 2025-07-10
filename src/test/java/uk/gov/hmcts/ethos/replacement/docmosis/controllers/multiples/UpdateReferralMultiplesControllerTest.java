package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.BaseControllerTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;

@ExtendWith(SpringExtension.class)
@WebMvcTest({UpdateReferralMultiplesController.class, JsonMapper.class})
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.ExcessiveImports", "PMD.UnusedPrivateField"})
class UpdateReferralMultiplesControllerTest extends BaseControllerTest {
    private static final String START_UPDATE_REFERRAL_URL = "/multiples/updateReferral/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/multiples/updateReferral/aboutToSubmit";
    private static final String INIT_HEARING_AND_REFERRAL_DETAILS_URL = 
        "/multiples/updateReferral/initHearingAndReferralDetails";

    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private ReferralService referralService;
    @MockBean
    private CaseLookupService caseLookupService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    private MultipleRequest request;

    @BeforeEach
    @Override
    @SneakyThrows
    protected void setUp() {
        CaseData caseData = CaseDataBuilder.builder()
            .withHearingScotland("hearingNumber", HEARING_TYPE_JUDICIAL_HEARING, "Judge",
                TribunalOffice.ABERDEEN, "venue")
            .withHearingSession(
                0, "2019-11-25T12:11:00.000",
                HEARING_STATUS_HEARD,
                true)
            .build();
        caseData.setReferralCollection(List.of(createReferralTypeItem()));
        caseData.setEthosCaseReference("caseRef");
        caseData.setClaimant("claimant");
        caseData.setIsUrgent("Yes");
        caseData.setReferralSubject("ET1");
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .build();

        MultipleData multipleData = MultipleData.builder().build();
        request = new MultipleRequest();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        request.setCaseDetails(multipleDetails);

        multipleData.setReferralCollection(List.of(createReferralTypeItem()));
        DynamicFixedListType selectReferralList = 
            ReferralHelper.populateSelectReferralDropdown(multipleData.getReferralCollection());
        Assertions.assertNotNull(selectReferralList);
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        multipleData.setSelectReferral(selectReferralList);
        UserDetails userDetails = new UserDetails();
        userDetails.setRoles(List.of("role1"));
        when(userIdamService.getUserDetails(any())).thenReturn(userDetails);
        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(any())).thenReturn(caseData);
        super.setUp();
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

    @Test
    @SneakyThrows
    void startUpdate_Success() {
        mockMvc.perform(post(START_UPDATE_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.data.referralHearingDetails", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    @SneakyThrows
    void initReferralHearingDetails_invalidToken() {

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(START_UPDATE_REFERRAL_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_tokenOk() {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId(UUID.randomUUID().toString());
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("respondent Name");
        respondentSumTypeItem.setValue(respondentSumType);
        ccdRequest.getCaseDetails().getCaseData().setRespondentCollection(
                List.of(respondentSumTypeItem));
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setUpdateIsUrgent("Yes");
        caseData.setUpdateReferentEmail("example@example.com");
        caseData.setUpdateReferralSubject("subject");
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToSubmitNoUpdateReferentEmailAddress_tokenOk() {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId(UUID.randomUUID().toString());
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("respondent Name");
        respondentSumTypeItem.setValue(respondentSumType);
        ccdRequest.getCaseDetails().getCaseData().setRespondentCollection(
                List.of(respondentSumTypeItem));
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setUpdateIsUrgent("Yes");
        caseData.setUpdateReferentEmail("");
        caseData.setUpdateReferralSubject("subject");
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    @SneakyThrows
    void referralStatusNotCorrect() {
        request.getCaseDetails().getCaseData().getReferralCollection()
                .getFirst().getValue().setReferralStatus("Closed");
        mockMvc.perform(post(INIT_HEARING_AND_REFERRAL_DETAILS_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors").value(
                        "Only referrals with status awaiting instructions can be updated."))
                .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    @SneakyThrows
    void referralStatusCorrect() {
        request.getCaseDetails().getCaseData().getReferralCollection()
                .getFirst().getValue().setReferralStatus("Awaiting instructions");
        mockMvc.perform(post(INIT_HEARING_AND_REFERRAL_DETAILS_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_invalidToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(request)))
            .andExpect(status().isForbidden());
    }

}
