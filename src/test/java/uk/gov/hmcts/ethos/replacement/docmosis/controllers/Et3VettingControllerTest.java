package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
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
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest({Et3VettingController.class, Et3VettingService.class, JsonMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
class Et3VettingControllerTest extends BaseControllerTest {

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
    private DocumentManagementService documentManagementService;
    @MockBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @MockBean
    private TornadoService tornadoService;
    private MockMvc mvc;
    private CCDRequest ccdRequest;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
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
    @SneakyThrows
    void populateEt3Dates_tokenOk_Et3Response() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(POPULATE_ET3_DATES_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void populateEt3Dates_tokenOk_NoEt3Response() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ccdRequest.getCaseDetails().getCaseData().getRespondentCollection()
                .getFirst().getValue().setResponseReceived(NO);
        mvc.perform(post(POPULATE_ET3_DATES_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void populateEt3Dates_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(POPULATE_ET3_DATES_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void populateEt3Dates_badRequest() {
        mvc.perform(post(POPULATE_ET3_DATES_URL)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void initEt3RespondentList_tokenOkWithRespondentCollection() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INIT_ET3_RESPONDENT_LIST_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void initEt3RespondentList_tokenOkWithoutRespondentCollection() {
        ccdRequest.getCaseDetails().getCaseData().setRespondentCollection(null);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INIT_ET3_RESPONDENT_LIST_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void initEt3RespondentList_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(INIT_ET3_RESPONDENT_LIST_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void initEt3RespondentList_badRequest() {
        mvc.perform(post(INIT_ET3_RESPONDENT_LIST_URL)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void calculateResponseInTime_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CALCULATE_RESPONSE_TIME_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void calculateResponseInTime_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CALCULATE_RESPONSE_TIME_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void calculateResponseInTime_badRequest() {
        mvc.perform(post(CALCULATE_RESPONSE_TIME_URL)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void initRespondentDetails_tokenOk() {
        ccdRequest.getCaseDetails().getCaseData().setRespondentCollection(new ArrayList<>());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INIT_RESPONDENT_DETAILS)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void initRespondentDetails_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(INIT_RESPONDENT_DETAILS)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void initRespondentDetails_badRequest() {
        mvc.perform(post(INIT_RESPONDENT_DETAILS)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void et3ProcessingComplete_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(ET3_PROCESSING_COMPLETE_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_header", notNullValue()))
            .andExpect(jsonPath("$.confirmation_body", notNullValue()))
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void et3ProcessingComplete_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ET3_PROCESSING_COMPLETE_URL)
            .content(jsonMapper.toJson(ccdRequest))
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void et3ProcessingComplete_badRequest() {
        mvc.perform(post(ET3_PROCESSING_COMPLETE_URL)
            .content("garbage content")
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void checkHearingListed_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CHECK_HEARING_LISTED_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void checkHearingListed_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CHECK_HEARING_LISTED_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void checkHearingListed_badRequest() {
        mvc.perform(post(CHECK_HEARING_LISTED_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void transferApplication_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(TRANSFER_APPLICATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void transferApplication_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(TRANSFER_APPLICATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void transferApplication_badRequest() {
        mvc.perform(post(TRANSFER_APPLICATION_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_badRequest() {
        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}