package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.internal.mapping.JsonbMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.controllers.BaseControllerTest.AUTH_TOKEN;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CaseAccessController.class, JsonbMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
class CaseAccessControllerTest {
    private CCDRequest ccdRequest;
    private static final String CLAIMANT_TRANSFERRED_CASE_URL = "/caseAccess/claimant/transferredCase";

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private CaseAccessService caseAccessService;
    @Autowired
    private WebApplicationContext applicationContext;
    private JsonMapper jsonMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jsonMapper = new JsonMapper(new ObjectMapper());
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        CaseData caseData = new CaseData();
        caseData.setLinkedCaseCT("http://example.com/1234567890123456");
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("1111222233334444")
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
    }

    @Test
    void assignClaimantTransferredCaseAccess_success() throws Exception {
        when(caseAccessService.assignClaimantCaseAccess(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(post(CLAIMANT_TRANSFERRED_CASE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseAccessService, times(1)).assignClaimantCaseAccess(any());
    }

    @Test
    void assignClaimantTransferredCaseAccess_error() throws Exception {
        ccdRequest.getCaseDetails().getCaseData().setLinkedCaseCT(null);
        doCallRealMethod().when(caseAccessService).assignClaimantCaseAccess(ccdRequest.getCaseDetails());

        mockMvc.perform(post(CLAIMANT_TRANSFERRED_CASE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is("Error getting original case id")))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseAccessService, times(1)).assignClaimantCaseAccess(any());
    }
}
