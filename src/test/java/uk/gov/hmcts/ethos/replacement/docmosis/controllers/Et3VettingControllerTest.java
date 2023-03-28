package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

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
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentBuilder;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@RunWith(SpringRunner.class)
@WebMvcTest({Et3VettingController.class, Et3VettingService.class, JsonMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.LawOfDemeter", "PMD.UnusedPrivateField", "PMD.TooManyMethods",
    "PMD.ExcessiveImports"})
class Et3VettingControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String POPULATE_ET3_DATES_URL = "/et3Vetting/midPopulateRespondentEt3Response";
    private static final String INIT_ET3_RESPONDENT_LIST_URL = "/et3Vetting/aboutToStart";
    private static final String CALCULATE_RESPONSE_TIME_URL = "/et3Vetting/midCalculateResponseInTime";
    private static final String INIT_RESPONDENT_DETAILS = "/et3Vetting/midRespondentNameAndAddressTable";
    private static final String ET3_PROCESSING_COMPLETE_URL = "/et3Vetting/processingComplete";
    private static final String CHECK_HEARING_LISTED_URL = "/et3Vetting/midHearingListedTable";
    private static final String TRANSFER_APPLICATION_URL = "/et3Vetting/midTransferApplicationTable";
    private static final String ABOUT_TO_SUBMIT_URL = "/et3Vetting/aboutToSubmit";

    @Autowired
    private WebApplicationContext applicationContext;
    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private TornadoService tornadoService;
    private MockMvc mvc;
    private CCDRequest ccdRequest;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();

        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(YES, "2022-03-01")
                .build()
            )
            .withManagingOffice(TribunalOffice.MANCHESTER.getOfficeName())
            .withClaimServedDate("2022-01-01")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseDetails.getCaseData())
            .build();
    }

    @Test
    void populateEt3Dates_tokenOk_Et3Response() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(POPULATE_ET3_DATES_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void populateEt3Dates_tokenOk_NoEt3Response() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ccdRequest.getCaseDetails().getCaseData().getRespondentCollection().get(0).getValue().setResponseReceived(NO);
        mvc.perform(post(POPULATE_ET3_DATES_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void populateEt3Dates_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(POPULATE_ET3_DATES_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void populateEt3Dates_badRequest() throws Exception {
        mvc.perform(post(POPULATE_ET3_DATES_URL)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void initEt3RespondentList_tokenOkWithRespondentCollection() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INIT_ET3_RESPONDENT_LIST_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", hasSize(0)))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void initEt3RespondentList_tokenOkWithoutRespondentCollection() throws Exception {
        ccdRequest.getCaseDetails().getCaseData().setRespondentCollection(null);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INIT_ET3_RESPONDENT_LIST_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void initEt3RespondentList_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(INIT_ET3_RESPONDENT_LIST_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void initEt3RespondentList_badRequest() throws Exception {
        mvc.perform(post(INIT_ET3_RESPONDENT_LIST_URL)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void calculateResponseInTime_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CALCULATE_RESPONSE_TIME_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", hasSize(0)))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void calculateResponseInTime_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CALCULATE_RESPONSE_TIME_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void calculateResponseInTime_badRequest() throws Exception {
        mvc.perform(post(CALCULATE_RESPONSE_TIME_URL)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void initRespondentDetails_tokenOk() throws Exception {
        ccdRequest.getCaseDetails().getCaseData().setRespondentCollection(new ArrayList<>());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INIT_RESPONDENT_DETAILS)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void initRespondentDetails_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(INIT_RESPONDENT_DETAILS)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void initRespondentDetails_badRequest() throws Exception {
        mvc.perform(post(INIT_RESPONDENT_DETAILS)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void et3ProcessingComplete_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(ET3_PROCESSING_COMPLETE_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_header", notNullValue()))
            .andExpect(jsonPath("$.confirmation_body", notNullValue()))
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void et3ProcessingComplete_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ET3_PROCESSING_COMPLETE_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void et3ProcessingComplete_badRequest() throws Exception {
        mvc.perform(post(ET3_PROCESSING_COMPLETE_URL)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void checkHearingListed_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CHECK_HEARING_LISTED_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void checkHearingListed_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CHECK_HEARING_LISTED_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void checkHearingListed_badRequest() throws Exception {
        mvc.perform(post(CHECK_HEARING_LISTED_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void transferApplication_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(TRANSFER_APPLICATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void transferApplication_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(TRANSFER_APPLICATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void transferApplication_badRequest() throws Exception {
        mvc.perform(post(TRANSFER_APPLICATION_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void aboutToSubmit_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void aboutToSubmit_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmit_badRequest() throws Exception {
        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}