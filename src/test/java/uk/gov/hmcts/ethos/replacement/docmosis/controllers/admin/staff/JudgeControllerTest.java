package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff;

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
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.SaveJudgeException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService.ADD_JUDGE_CODE_AND_OFFICE_CONFLICT_ERROR;

@ExtendWith(SpringExtension.class)
@WebMvcTest({JudgeController.class, JsonMapper.class})
class JudgeControllerTest {

    private static final String INIT_ADD_JUDGE_URL = "/admin/staff/initAddJudge";
    private static final String ADD_JUDGE_URL = "/admin/staff/addJudge";
    private static final String UPDATE_JUDGE_MID_OFFICE_URL = "/admin/staff/updateJudgeMidEventSelectOffice";
    private static final String UPDATE_JUDGE_MID_JUDGE_URL = "/admin/staff/updateJudgeMidEventSelectJudge";
    private static final String UPDATE_JUDGE_URL = "/admin/staff/updateJudge";
    private static final String DELETE_JUDGE_MID_OFFICE_URL = "/admin/staff/deleteJudgeMidEventSelectOffice";
    private static final String DELETE_JUDGE_MID_JUDGE_URL = "/admin/staff/deleteJudgeMidEventSelectJudge";
    private static final String DELETE_JUDGE_URL = "/admin/staff/deleteJudge";
    private static final String AUTH_TOKEN = "some-token";
    private CCDRequest ccdRequest;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockBean
    private JudgeService judgeService;

    @BeforeEach
    void setUp() {
        ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "ABERDEEN", "SALARIED")
                .buildAsCCDRequest();
    }

    @Test
    void initAddJudge_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(INIT_ADD_JUDGE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(judgeService, times(1)).initAddJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void initAddJudge_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(INIT_ADD_JUDGE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(judgeService, never()).initAddJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testAddJudgeSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ADD_JUDGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(judgeService, times(1)).saveJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testAddJudgeError() throws Exception {
        ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "Aberdeen", "SALARIED")
                .buildAsCCDRequest();

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        String error = String.format(ADD_JUDGE_CODE_AND_OFFICE_CONFLICT_ERROR,
                adminData.getJudgeCode(), TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice()));

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doThrow(new SaveJudgeException(error)).when(judgeService).saveJudge(adminData);

        mockMvc.perform(post(ADD_JUDGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(jsonPath("$.errors[0]", is(error)));
        verify(judgeService, times(1)).saveJudge(adminData);
    }

    @Test
    void testAddJudgeInvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(ADD_JUDGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(judgeService, never()).saveJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateJudgeMidEventSelectOffice_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(UPDATE_JUDGE_MID_OFFICE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(judgeService, times(1))
                .updateJudgeMidEventSelectOffice(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorkerMidEventSelectOffice_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(UPDATE_JUDGE_MID_OFFICE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(judgeService, never())
                .updateJudgeMidEventSelectOffice(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorkerMidEventSelectCourtWorker_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(UPDATE_JUDGE_MID_JUDGE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(judgeService, times(1))
                .updateJudgeMidEventSelectJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorkerMidEventSelectCourtWorker_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(UPDATE_JUDGE_MID_JUDGE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(judgeService, never())
                .updateJudgeMidEventSelectJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorker_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(UPDATE_JUDGE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(judgeService, times(1)).updateJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorker_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(UPDATE_JUDGE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(judgeService, never()).updateJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteJudgeSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(DELETE_JUDGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.warnings", nullValue()))
            .andExpect(jsonPath("$.errors", hasSize(0)));
        verify(judgeService, times(1)).deleteJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteJudgeSuccess_InvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(DELETE_JUDGE_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
        verify(judgeService, never()).deleteJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteJudgeMidEventSelectJudge() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(DELETE_JUDGE_MID_JUDGE_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", hasSize(0)))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(judgeService, times(1))
            .deleteJudgeMidEventSelectJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteJudgeMidEventSelectJudge_InvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(DELETE_JUDGE_MID_JUDGE_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
        verify(judgeService, never()).deleteJudgeMidEventSelectJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteJudgeMidEventSelectOffice_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(DELETE_JUDGE_MID_OFFICE_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", notNullValue()))
            .andExpect(jsonPath("$.errors", hasSize(0)))
            .andExpect(jsonPath("$.warnings", nullValue()));
        verify(judgeService, times(1))
            .deleteJudgeMidEventSelectOffice(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteJudgeMidEventSelectOffice_InvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(DELETE_JUDGE_MID_OFFICE_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
        verify(judgeService, never()).deleteJudgeMidEventSelectOffice(ccdRequest.getCaseDetails().getAdminData());
    }
}