package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.InitialConsiderationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({InitialConsiderationController.class, JsonMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
class InitialConsiderationControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String COMPLETE_INITIAL_CONSIDERATION_URL = "/completeInitialConsideration";
    private static final String START_INITIAL_CONSIDERATION_URL = "/startInitialConsideration";
    private static final String SUBMIT_INITIAL_CONSIDERATION_URL = "/submitInitialConsideration";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private InitialConsiderationService initialConsiderationService;

    @MockBean
    private DocumentManagementService documentManagementService;

    @MockBean
    private ReportDataService reportDataService;

    @MockBean
    private TornadoService tornadoService;

    @MockBean
    private CaseFlagsService caseFlagsService;

    private MockMvc mvc;

    private CCDRequest ccdRequest;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();

        String hearingNumber = "123";
        String venue = "Some venue";

        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .withRespondent("Jack", YES, "2022-03-01", false)
            .withClaimServedDate("2022-01-01")
            .withHearing(hearingNumber, HEARING_TYPE_JUDICIAL_HEARING, "Judge", venue, null, null, null, null)
            .withHearingSession(0, hearingNumber, "2019-11-25T12:11:00.000", Constants.HEARING_STATUS_LISTED, false)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseDetails.getCaseData())
            .build();
    }

    @Test
    void initICCompleteTokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(COMPLETE_INITIAL_CONSIDERATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_header", notNullValue()));
    }

    @Test
    void initICCompleteTokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(COMPLETE_INITIAL_CONSIDERATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void initICCompleteBadRequest() throws Exception {
        mvc.perform(post(COMPLETE_INITIAL_CONSIDERATION_URL)
                .content("bad request")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitInitialConsideration_TokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SUBMIT_INITIAL_CONSIDERATION_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(caseFlagsService, times(1)).setPrivateHearingFlag(any());
    }

    @Test
    void submitInitialConsideration_TokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SUBMIT_INITIAL_CONSIDERATION_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitInitialConsideration_BadRequest() throws Exception {
        mvc.perform(post(SUBMIT_INITIAL_CONSIDERATION_URL)
                        .content("bad request")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startInitialConsiderationTest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(initialConsiderationService.generateJurisdictionCodesHtml(anyList(), any())).thenReturn("Jurisdictions");
        when(initialConsiderationService.getHearingDetails(anyList())).thenReturn("hearings");
        when(initialConsiderationService.getRespondentName(anyList())).thenReturn("respondents");
        mvc.perform(post(START_INITIAL_CONSIDERATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()));
    }
}