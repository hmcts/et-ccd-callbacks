package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsReferenceDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @MockitoBean
    private CaseFlagsReferenceDataService caseFlagsReferenceDataService;

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
        Map<String, String> partyFlagVisibilities = Map.of("ra0010", "External");
        when(caseFlagsReferenceDataService.getPartyFlagVisibilities(AUTH_TOKEN)).thenReturn(partyFlagVisibilities);

        mvc.perform(post(CASE_FLAGS_DATA_MIGRATION)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseFlagsReferenceDataService).getPartyFlagVisibilities(AUTH_TOKEN);
        verify(caseFlagsService).migrateExistingClaimantAndRespondentCaseFlags(
                caseDataCaptor.capture(),
                eq(partyFlagVisibilities)
        );
        assertNotNull(caseDataCaptor.getValue().getAllPartyFlags());
        assertNotNull(caseDataCaptor.getValue().getAllPartyFlags().getClaimantFlags());
        assertNotNull(caseDataCaptor.getValue().getAllPartyFlags().getRespondentFlags());
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
