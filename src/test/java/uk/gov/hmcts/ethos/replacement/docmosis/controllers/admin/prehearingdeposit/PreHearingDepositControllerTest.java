package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.prehearingdeposit;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.et.common.model.ccd.GenericTypeCaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.Document;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.prehearingdeposit.PreHearingDepositService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({PreHearingDepositController.class, JsonMapper.class})
@ActiveProfiles("test")
class PreHearingDepositControllerTest {

    private String token;
    private CCDRequest ccdRequest;
    private PreHearingDepositRequest preHearingDepositRequest;
    private static final String TEST_FILE_NAME = "Test File";
    private static final String TEST_FILE_URL = "Test File URL";
    private static final String TEST_PRE_HEARING_DEPOSIT_CASE_NUMBER = "Test Pre-Hearing Deposit Case Number";
    private static final String TEST_PRE_HEARING_DEPOSIT_CLAIMANT_OR_RESPONDENT_NAME = "Michael Jackson";
    @MockitoBean
    private VerifyTokenService verifyTokenService;
    @MockitoBean
    private PreHearingDepositService preHearingDepositService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        token = "some-token";
        Document document = new Document();
        document.setUrl(TEST_FILE_URL);
        document.setFilename(TEST_FILE_NAME);
        ImportFile importFile = new ImportFile();
        importFile.setFile(document);
        AdminData adminData;
        adminData = new AdminData();
        adminData.setPreHearingDepositImportFile(importFile);
        CaseDetails caseDetails;
        caseDetails = new CaseDetails();
        caseDetails.setAdminData(adminData);
        AdminDataBuilder.builder().buildAsCCDRequest();
        ccdRequest = AdminDataBuilder.builder().buildAsCCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        PreHearingDepositData preHearingDepositData = new PreHearingDepositData();
        preHearingDepositData.setCaseNumber(TEST_PRE_HEARING_DEPOSIT_CASE_NUMBER);
        preHearingDepositData.setClaimantOrRespondentName(TEST_PRE_HEARING_DEPOSIT_CLAIMANT_OR_RESPONDENT_NAME);
        GenericTypeCaseDetails<PreHearingDepositData> preHearingDepositCaseDetails = new GenericTypeCaseDetails<>();
        preHearingDepositCaseDetails.setCaseData(preHearingDepositData);
        preHearingDepositRequest = new PreHearingDepositRequest();
        preHearingDepositRequest.setCaseDetails(preHearingDepositCaseDetails);
    }

    @Test
    void testImportPHRDepositsSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        doNothing().when(preHearingDepositService)
                .importPreHearingDepositData(
                        ccdRequest.getCaseDetails().getAdminData().getPreHearingDepositImportFile(), token);
        ResultActions resultActions = mockMvc.perform(post("/admin/preHearingDeposit/importPHRDeposits")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString).getJSONObject("data");
        AdminData returnAdminData = jsonMapper.fromJson(json.toString(), AdminData.class);
        assertEquals(TEST_FILE_NAME, returnAdminData.getPreHearingDepositImportFile().getFile().getFilename());
        assertEquals(TEST_FILE_URL, returnAdminData.getPreHearingDepositImportFile().getFile().getUrl());
        verify(preHearingDepositService, times(1)).importPreHearingDepositData(
                ccdRequest.getCaseDetails().getAdminData().getPreHearingDepositImportFile(), token);

    }

    @Test
    void testImportPHRDepositsForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);
        mockMvc.perform(post("/admin/preHearingDeposit/importPHRDeposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(preHearingDepositService, never()).importPreHearingDepositData(
                ccdRequest.getCaseDetails().getAdminData().getPreHearingDepositImportFile(), token);
    }

    @Test
    void testImportPHRDepositsBadRequest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        mockMvc.perform(post("/admin/preHearingDeposit/importPHRDeposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("error"))
                .andExpect(status().isBadRequest());
        verify(preHearingDepositService, never()).importPreHearingDepositData(
                ccdRequest.getCaseDetails().getAdminData().getPreHearingDepositImportFile(), token);
    }

    @Test
    void testCreatePHRDepositSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        ResultActions resultActions = mockMvc.perform(post("/admin/preHearingDeposit/createPHRDeposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(preHearingDepositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString).getJSONObject("data");
        PreHearingDepositData preHearingDepositData = jsonMapper.fromJson(json.toString(), PreHearingDepositData.class);
        assertEquals(TEST_PRE_HEARING_DEPOSIT_CASE_NUMBER, preHearingDepositData.getCaseNumber());
        assertEquals(TEST_PRE_HEARING_DEPOSIT_CLAIMANT_OR_RESPONDENT_NAME,
                preHearingDepositData.getClaimantOrRespondentName());

    }

    @Test
    void testCreatePHRDepositForbidden() throws Exception {
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);
        mockMvc.perform(post("/admin/preHearingDeposit/createPHRDeposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(preHearingDepositRequest)))
                .andExpect(status().isForbidden());
        verify(preHearingDepositService, never()).importPreHearingDepositData(
                ccdRequest.getCaseDetails().getAdminData().getPreHearingDepositImportFile(), token);
    }

    @Test
    void testCreatePHRDepositBadRequest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        mockMvc.perform(post("/admin/preHearingDeposit/createPHRDeposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("error"))
                .andExpect(status().isBadRequest());
        verify(preHearingDepositService, never()).importPreHearingDepositData(
                ccdRequest.getCaseDetails().getAdminData().getPreHearingDepositImportFile(), token);
    }
}
