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
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentTellSomethingElseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({RespondentTellSomethingElseController.class, JsonMapper.class})
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class RespondentTellSomethingElseControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String VALIDATE_GIVE_DETAILS = "/respondentTSE/validateGiveDetails";
    private static final String ABOUT_TO_SUBMIT_URL = "/respondentTSE/aboutToSubmit";
    private static final String DISPLAY_TABLE_URL = "/respondentTSE/displayTable";
    private static final String COMPLETE_APPLICATION_URL = "/respondentTSE/completeApplication";

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private RespondentTellSomethingElseService resTseService;

    @MockBean
    private TseService tseService;

    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;
    @Autowired
    private MockMvc mockMvc;

    private static final String NO = "I do not want to copy";

    @BeforeEach
    void setUp() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResTseSelectApplication("caseRef");
        caseData.setResTseCopyToOtherPartyYesOrNo(NO);
        caseData.setEthosCaseReference("test");
        caseData.setClaimant("claimant");
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));
        caseData.setGenericTseApplicationCollection(createApplicationCollection());

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .withCaseId("123")
            .build();
    }

    @Test
    void validateGiveDetails_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(VALIDATE_GIVE_DETAILS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", notNullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(resTseService).validateGiveDetails(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void validateGiveDetails_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(VALIDATE_GIVE_DETAILS)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmitRespondentTSE_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(resTseService).sendAcknowledgeEmailAndGeneratePdf(ccdRequest.getCaseDetails(), AUTH_TOKEN);
        verify(tseService).createApplication(ccdRequest.getCaseDetails().getCaseData(), false);
    }

    @Test
    void aboutToSubmitRespondentTSE_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void displayRespondentApplicationsTable_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(DISPLAY_TABLE_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(resTseService).generateTableMarkdown(ccdRequest.getCaseDetails().getCaseData());
    }

    @Test
    void displayRespondentApplicationsTable_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(DISPLAY_TABLE_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void completeApplication_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(COMPLETE_APPLICATION_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_body", notNullValue()))
            .andExpect(jsonPath("$.data", nullValue()))
            .andExpect(jsonPath("$.errors", nullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @Test
    void completeApplication_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(COMPLETE_APPLICATION_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
    }

    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Boris Johnson");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
    }

    private List<GenericTseApplicationTypeItem> createApplicationCollection() {
        GenericTseApplicationType respondentTseType = new GenericTseApplicationType();
        respondentTseType.setCopyToOtherPartyYesOrNo(NO);

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(respondentTseType);

        return new ArrayList<>(Collections.singletonList(tseApplicationTypeItem));
    }
}