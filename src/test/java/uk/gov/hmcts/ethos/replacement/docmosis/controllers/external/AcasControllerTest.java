package uk.gov.hmcts.ethos.replacement.docmosis.controllers.external;

import org.joda.time.LocalDateTime;
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
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({AcasController.class, JsonMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
class AcasControllerTest {

    private final List<String> caseIds = new ArrayList<>();
    private static final String AUTH_TOKEN = "some-token";
    private static final String GET_LAST_MODIFIED_CASE_LIST_URL = "/getLastModifiedCaseList";
    private static final String GET_CASE_DATA_URL = "/getCaseData";
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private VerifyTokenService verifyTokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getLastModifiedCaseListSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .param("datetime", String.valueOf(new LocalDateTime())))
                .andExpect(status().isOk());
    }

    @Test
    void getLastModifiedCaseListNoParameter() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLastModifiedCaseListInvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .param("datetime", String.valueOf(new LocalDateTime())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCaseDataSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_CASE_DATA_URL)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .param("caseIds", caseIds.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getCaseDataNoParameter() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_CASE_DATA_URL)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCaseDataInvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(get(GET_CASE_DATA_URL)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .param("caseIds", caseIds.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCaseDatalistOfCaseIds() throws Exception {
        caseIds.add("1");
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_CASE_DATA_URL)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .param("caseIds", caseIds.toString()))
                .andExpect(status().isOk());
    }
}
