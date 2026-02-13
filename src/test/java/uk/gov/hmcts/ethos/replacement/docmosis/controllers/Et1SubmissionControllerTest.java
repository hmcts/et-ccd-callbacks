package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({Et1SubmissionController.class, JsonMapper.class})
class Et1SubmissionControllerTest extends BaseControllerTest {
    private static final String SUBMITTED_URL = "/et1Submission/submitted";

    @MockBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @MockBean
    private Et1SubmissionService et1SubmissionService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private JsonMapper jsonMapper;

    private CCDRequest ccdRequest;

    @BeforeEach
    @Override
    protected void setUp() throws IOException, URISyntaxException {
        super.setUp();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1234567/2024");
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("1234567890123456")
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
    }

    @Test
    void addServiceId_success() throws Exception {
        doNothing().when(caseManagementForCaseWorkerService).setHmctsServiceIdSupplementary(any());
        doNothing().when(et1SubmissionService).sendEt1ConfirmationClaimant(any(), eq(AUTH_TOKEN));

        mvc.perform(post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseManagementForCaseWorkerService, times(1))
                .setHmctsServiceIdSupplementary(ccdRequest.getCaseDetails());
        verify(et1SubmissionService, times(1))
                .sendEt1ConfirmationClaimant(ccdRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void addServiceId_missingAuthToken() throws Exception {
        mvc.perform(post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void addServiceId_invalidRequestBody() throws Exception {
        mvc.perform(post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content("{invalid json"))
                .andExpect(status().is4xxClientError());
    }
}
