package uk.gov.hmcts.ethos.replacement.docmosis.controllers.notifications.claimant;

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
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.controllers.BaseControllerTest.AUTH_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService.GIVE_MISSING_DETAIL;

@ExtendWith(SpringExtension.class)
@WebMvcTest({RespondToNotificationController.class, JsonMapper.class})
@ActiveProfiles("test")
class RespondToNotificationControllerTest {
    private static final String ABOUT_TO_START_URL = "/claimantRespondNotification/aboutToStart";
    private static final String MID_DETAILS_TABLE_URL = "/claimantRespondNotification/midDetailsTable";
    private static final String ABOUT_TO_SUBMIT_URL = "/claimantRespondNotification/aboutToSubmit";
    private static final String SUBMITTED_URL = "/claimantRespondNotification/submitted";
    private static final String MID_VALIDATE_INPUT_URL = "/claimantRespondNotification/midValidateInput";

    @MockitoBean
    private VerifyTokenService verifyTokenService;
    @MockitoBean
    private PseRespondToTribunalService pseRespondToTribunalService;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    private CaseData caseData;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);

        caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("6000001/2024")
                .withClaimant("John Doe")
                .withRespondent(RespondentSumType.builder().respondentName("Jane Doe").build())
                .withClaimantRepresentedQuestion(YES)
                .withRepresentativeClaimantType("Mark Doe", "claimantrep@test.com")
                .withRespondentRepresentative("Jane Doe", "James Doe", "respondentrep@test.com")
                .withNotification("Notification One", "Hearing")
                .build();

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("1234567890123456")
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
    }

    @Test
    void aboutToStart() throws Exception {
        caseData.getRespondentCollection().get(0).getValue().setIdamId("789");
        doCallRealMethod().when(pseRespondToTribunalService).populateSelectDropdown(any(), anyString());
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.claimantSelectNotification").exists())
                .andExpect(jsonPath("$.data.isRespondentSystemUser").value(YES));
    }

    @Test
    void aboutToStart_respondentNotASystemUser() throws Exception {
        doCallRealMethod().when(pseRespondToTribunalService).populateSelectDropdown(any(), anyString());
        caseData.setRepCollection(null);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.claimantSelectNotification").exists())
                .andExpect(jsonPath("$.data.isRespondentSystemUser").value(NO));
    }

    @Test
    void midDetailsUrl() throws Exception {
        doCallRealMethod().when(pseRespondToTribunalService).initialOrdReqDetailsTableMarkUp(any(), anyString());
        ccdRequest.getCaseDetails().getCaseData()
                .setClaimantSelectNotification(DynamicFixedListType.from("1", "1 - Notification One", true));
        mockMvc.perform(post(MID_DETAILS_TABLE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.claimantNotificationTableMarkdown").exists());
    }

    @Test
    void aboutToSubmit() throws Exception {
        doCallRealMethod().when(pseRespondToTribunalService).saveClaimantResponse(any());
        doCallRealMethod().when(pseRespondToTribunalService).clearClaimantNotificationDetails(any());
        ccdRequest.getCaseDetails().getCaseData()
                .setClaimantSelectNotification(DynamicFixedListType.from("1", "1 - Notification One", true));
        caseData.setClaimantNotificationResponseText("Response text");
        caseData.setClaimantNotificationSupportingMaterial(NO);
        caseData.setClaimantNotificationCopyToOtherParty(YES);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.sendNotificationCollection[0].value.respondCollection").exists());
    }

    @Test
    void submitted() throws Exception {
        mockMvc.perform(post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()));
    }

    @Test
    void midValidateInput_textAndNoDoc() throws Exception {
        doCallRealMethod().when(pseRespondToTribunalService).validateClaimantInput(any());

        caseData.setClaimantNotificationResponseText("Response text");
        caseData.setClaimantNotificationSupportingMaterial(NO);

        mockMvc.perform(post(MID_VALIDATE_INPUT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors", equalTo(new ArrayList<>())));
    }

    @Test
    void midValidateInput_noTextAndDoc() throws Exception {
        doCallRealMethod().when(pseRespondToTribunalService).validateClaimantInput(any());

        caseData.setClaimantNotificationResponseText(null);
        caseData.setClaimantNotificationSupportingMaterial(YES);

        mockMvc.perform(post(MID_VALIDATE_INPUT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors", equalTo(new ArrayList<>())));
    }

    @Test
    void midValidateInput_noTextAndNoDocError() throws Exception {
        doCallRealMethod().when(pseRespondToTribunalService).validateClaimantInput(any());

        caseData.setClaimantNotificationResponseText(null);
        caseData.setClaimantNotificationSupportingMaterial(NO);

        mockMvc.perform(post(MID_VALIDATE_INPUT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.[0]", equalTo(GIVE_MISSING_DETAIL)));
    }
}
