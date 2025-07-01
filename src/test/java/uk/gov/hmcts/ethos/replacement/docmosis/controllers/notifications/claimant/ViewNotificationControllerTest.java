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
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ProvideSomethingElseViewService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.controllers.BaseControllerTest.AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@WebMvcTest({ViewNotificationController.class, JsonMapper.class})
@ActiveProfiles("test")
class ViewNotificationControllerTest {
    private static final String ALL_ABOUT_TO_START_URL = "/claimantViewNotification/all/aboutToStart";
    private static final String ALL_ABOUT_TO_SUBMIT_URL = "/claimantViewNotification/all/aboutToSubmit";
    private static final String ABOUT_TO_START_URL = "/claimantViewNotification/aboutToStart";
    private static final String MID_DETAILS_TABLE_URL = "/claimantViewNotification/midDetailsTable";

    @MockitoBean
    private VerifyTokenService verifyTokenService;
    @MockitoBean
    private ProvideSomethingElseViewService provideSomethingElseViewService;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("6000001/2024")
                .withClaimant("John Doe")
                .withRespondent(RespondentSumType.builder().respondentName("Jane Doe").build())
                .withClaimantRepresentedQuestion(YES)
                .withRepresentativeClaimantType("Mark Doe", "claimantrep@test.com")
                .withRespondentRepresentative("Jane Doe", "James Doe", "respondentrep@test.com")
                .withNotification("Notification One", "Hearing").build();

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("1234567890123456")
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
    }

    @Test
    void allAboutToStart() throws Exception {
        doCallRealMethod().when(provideSomethingElseViewService).generateViewNotificationsMarkdown(any(), anyString());
        mockMvc.perform(post(ALL_ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.pseViewNotifications").exists());
    }

    @Test
    void allAboutToSubmit() throws Exception {
        doCallRealMethod().when(provideSomethingElseViewService).generateViewNotificationsMarkdown(any(), anyString());
        mockMvc.perform(post(ALL_ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.pseViewNotifications", nullValue()));
    }

    @Test
    void aboutToStart() throws Exception {
        doCallRealMethod().when(provideSomethingElseViewService).populateSelectDropdownView(any(), anyString());
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.claimantSelectNotification").exists());
    }

    @Test
    void midDetailsTable() throws Exception {
        doCallRealMethod().when(provideSomethingElseViewService).initialOrdReqDetailsTableMarkUp(any(), anyString());
        mockMvc.perform(post(MID_DETAILS_TABLE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.data.claimantNotificationTableMarkdown").exists());
    }

}
