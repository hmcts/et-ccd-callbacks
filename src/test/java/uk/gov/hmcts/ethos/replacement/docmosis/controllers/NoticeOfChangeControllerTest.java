package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

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

@ExtendWith(SpringExtension.class)
@WebMvcTest(NoticeOfChangeController.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
    NoticeOfChangeController.class,
    NocRespondentRepresentativeService.class,
    VerifyTokenService.class,
    CcdCaseAssignment.class
})
class NoticeOfChangeControllerTest {

    @MockitoBean
    private VerifyTokenService verifyTokenService;
    @MockitoBean
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @MockitoBean
    private CcdCaseAssignment ccdCaseAssignment;

    @MockitoBean
    private NocNotificationService notificationService;

    @Autowired
    private WebApplicationContext applicationContext;

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";

    private static final String ABOUT_TO_SUBMIT_URL = "/noc-decision/about-to-submit";
    private static final String SUBMITTED_URL = "/noc-decision/submitted";
    private static final String AUTHORIZATION = "Authorization";

    private JsonNode requestContent;
    private MockMvc mvc;

    private CaseData caseData;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
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
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void nocSubmitted() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doNothing().when(notificationService).sendNotificationOfChangeEmails(any(),
            any(), any());

        mvc.perform(post(SUBMITTED_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }
}
