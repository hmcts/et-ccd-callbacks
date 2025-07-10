package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples.AddLegalRepToMultipleController;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({AddLegalRepToMultipleController.class, JsonMapper.class})
class AddLegalRepToMultipleControllerTest extends BaseControllerTest {

    private static final String START_ADD_LEGAL_REP_TO_MULTIPLE_URL =
            "/multiples/addLegalRepToMultiple/aboutToStart";
    private static final String SUBMIT_ADD_LEGAL_REP_TO_MULTIPLE_URL =
            "/multiples/addLegalRepToMultiple/aboutToSubmit";
    private static final String COMPLETE_ADD_LEGAL_REP_TO_MULTIPLE_URL =
            "/multiples/addLegalRepToMultiple/completed";

    @MockBean
    private MultipleReferenceService multipleReferenceService;
    @MockBean
    private UserIdamService userService;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mvc;

    private CCDRequest ccdRequest;

    @Autowired
    private JsonMapper jsonMapper;

    private UserDetails userDetails;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();

        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Cosmo")
            .withRespondent("Cosmo", YES, "2022-03-01", false)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseDetails.getCaseData())
            .build();

        userDetails = new UserDetails();
        userDetails.setUid("Test UUID");
    }

    @Test
    @SneakyThrows
    void startAddLegalRepToMultipleTest_TokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(multipleReferenceService.validateSubcaseIsOfMultiple(any())).thenReturn(new ArrayList<>());
        mvc.perform(post(START_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()));
    }

    @Test
    @SneakyThrows
    void startAddLegalRepToMultiple_TokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(START_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void startAddLegalRepToMultiple_BadRequest() {
        mvc.perform(post(START_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                        .content("bad request")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void submitAddLegalRepToMultiple_TokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(userService.getUserDetails(AUTH_TOKEN)).thenReturn(userDetails);
        mvc.perform(post(SUBMIT_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void submitAddLegalRepToMultiple_TokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SUBMIT_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void submitAddLegalRepToMultiple_BadRequest() {
        mvc.perform(post(SUBMIT_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                        .content("bad request")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void completeAddLegalRepToMultiple_TokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(COMPLETE_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_header", notNullValue()));
    }

    @Test
    @SneakyThrows
    void completeAddLegalRepToMultiple_TokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(COMPLETE_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void completeAddLegalRepToMultiple_BadRequest() {
        mvc.perform(post(COMPLETE_ADD_LEGAL_REP_TO_MULTIPLE_URL)
                        .content("bad request")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}