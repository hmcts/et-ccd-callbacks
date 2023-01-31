package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(NoticeOfChangeController.class)
@ContextConfiguration(classes = {
    NoticeOfChangeController.class,
    NocRespondentRepresentativeService.class,
    VerifyTokenService.class,
    CcdCaseAssignment.class
})
@SuppressWarnings({"PMD.ExcessiveImports"})
class NoticeOfChangeControllerTest {

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @MockBean
    private CcdCaseAssignment ccdCaseAssignment;

    @MockBean
    private NocNotificationService notificationService;

    @Autowired
    private WebApplicationContext applicationContext;

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";

    private static final String ABOUT_TO_SUBMIT_URL = "/noc-decision/about-to-submit";
    private static final String UPDATE_RESPONDENTS_URL = "/noc-decision/update-respondents";
    private static final String SUBMITTED_URL = "/noc-decision/submitted";
    private static final String AUTHORIZATION = "Authorization";

    private JsonNode requestContent;
    private MockMvc mvc;

    private CaseData caseData;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
            .getResource("/exampleV1.json")).toURI()));
        CallbackRequest callbackRequest = objectMapper.treeToValue(requestContent, CallbackRequest.class);
        caseData = callbackRequest.getCaseDetails().getCaseData();
    }

    @Test
    void handleAboutToSubmit() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(nocRespondentRepresentativeService
            .updateRepresentation(any())).thenReturn(caseData);
        when(ccdCaseAssignment.applyNoc(any(), any())).thenReturn(CCDCallbackResponse.builder()
            .data(caseData)
            .build());

        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void updateNocRespondents() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        when(ccdCaseAssignment.applyNocAsAdmin(any())).thenReturn(CCDCallbackResponse.builder()
            .data(caseData)
            .build());

        mvc.perform(post(UPDATE_RESPONDENTS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void nocSubmitted() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doNothing().when(notificationService).sendNotificationOfChangeEmails(any(),
            any());

        mvc.perform(post(SUBMITTED_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }
}