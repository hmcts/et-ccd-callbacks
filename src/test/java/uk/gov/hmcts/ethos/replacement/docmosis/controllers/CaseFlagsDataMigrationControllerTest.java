package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CaseFlagsDataMigrationController.class)
@ContextConfiguration(classes = DocmosisApplication.class)
class CaseFlagsDataMigrationControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String CASE_FLAGS_DATA_MIGRATION = "/case-flags-migration/about-to-submit";
    private static final String CASE_FLAGS_DATA_ROLLBACK = "/case-flags-rollback/about-to-submit";
    public static final String AUTHORIZATION = "Authorization";

    @MockitoBean
    private VerifyTokenService verifyTokenService;

    @MockitoBean
    private CaseFlagsService caseFlagsService;

    @Autowired
    private WebApplicationContext applicationContext;
    private JsonNode requestContent;
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {

        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleV2.json")).toURI()));
    }

    @Test
    @SneakyThrows
    void shouldMigrateCaseFlags() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CASE_FLAGS_DATA_MIGRATION)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseFlagsService).setupCaseFlags(caseDataCaptor.capture());
        assertNotNull(caseDataCaptor.getValue().getAllPartyFlags());
        assertNotNull(caseDataCaptor.getValue().getAllPartyFlags().getClaimantFlags());
        assertNotNull(caseDataCaptor.getValue().getAllPartyFlags().getRespondentFlags());
    }

    @Test
    @SneakyThrows
    void shouldAddCaseFlagGroupIdAndVisibilityForPreviouslyMigratedFlags() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mvc.perform(post(CASE_FLAGS_DATA_MIGRATION)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimantFlags.groupId").value("Claimant"))
                .andExpect(jsonPath("$.data.claimantFlags.visibility").value("Internal"))
                .andExpect(jsonPath("$.data.respondentFlags.groupId").value("Respondent 1"))
                .andExpect(jsonPath("$.data.respondentFlags.visibility").value("Internal"));
    }

    @Test
    @SneakyThrows
    void shouldNotOverwriteExistingCaseFlagGroupIdAndVisibility() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        ObjectNode caseData = (ObjectNode) requestContent.at("/case_details/case_data");
        ObjectNode claimantFlags = (ObjectNode) caseData.get("claimantFlags");
        claimantFlags.put("groupId", "existing-claimant-group");
        claimantFlags.put("visibility", "Internal");
        ObjectNode respondentFlags = (ObjectNode) caseData.get("respondentFlags");
        respondentFlags.put("groupId", "existing-respondent-group");
        respondentFlags.put("visibility", "Internal");

        mvc.perform(post(CASE_FLAGS_DATA_MIGRATION)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimantFlags.groupId").value("existing-claimant-group"))
                .andExpect(jsonPath("$.data.claimantFlags.visibility").value("Internal"))
                .andExpect(jsonPath("$.data.respondentFlags.groupId").value("existing-respondent-group"))
                .andExpect(jsonPath("$.data.respondentFlags.visibility").value("Internal"));
    }

    @Test
    @SneakyThrows
    void shouldMigrateCaseFlags_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CASE_FLAGS_DATA_MIGRATION)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void shouldRollbackCaseFlags() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CASE_FLAGS_DATA_ROLLBACK)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(caseFlagsService).rollbackCaseFlags(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void shouldRollbackCaseFlags_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CASE_FLAGS_DATA_ROLLBACK)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
