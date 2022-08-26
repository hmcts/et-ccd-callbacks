package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.CourtWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CourtWorkerController.class, JsonMapper.class})
class CourtWorkerControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String INIT_ADD_COURT_WORKER_URL = "/admin/staff/initAddCourtWorker";
    private static final String ADD_COURT_WORKER_URL = "/admin/staff/addCourtWorker";
    private static final String UPDATE_COURT_WORKER_MID_OFFICE_URL =
            "/admin/staff/midEventCourtWorkerSelectOffice";
    private static final String UPDATE_COURT_WORKER_MID_CLERK_URL =
            "/admin/staff/midEventCourtWorkerSelectCourtWorker";
    private static final String UPDATE_COURT_WORKER_URL = "/admin/staff/updateCourtWorker";
    private static final String DELETE_COURT_WORKER_URL = "/admin/staff/deleteCourtWorker";
    private CCDRequest ccdRequest;

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private CourtWorkerService courtWorkerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        ccdRequest = AdminDataBuilder
            .builder()
            .withEmployeeMember(TribunalOffice.LEEDS.getOfficeName(), "TestCode", "TestName")
            .buildAsCCDRequest();
    }

    @Test
    void initAddCourtWorker_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(courtWorkerService.addCourtWorker(any())).thenReturn(new ArrayList<>());
        mockMvc.perform(post(INIT_ADD_COURT_WORKER_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(courtWorkerService, times(1))
                .initAddCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void initAddCourtWorker_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(INIT_ADD_COURT_WORKER_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(courtWorkerService, never()).initAddCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void addCourtWorker_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(courtWorkerService.addCourtWorker(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(post(ADD_COURT_WORKER_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(courtWorkerService, times(1)).addCourtWorker(ccdRequest.getCaseDetails().getAdminData());

    }

    @Test
    void addCourtWorker_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ADD_COURT_WORKER_URL)
            .contentType(APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
            .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isForbidden());
        verify(courtWorkerService, never()).addCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorkerMidEventSelectOffice_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(courtWorkerService.getCourtWorkerMidEventSelectOffice(any())).thenReturn(new ArrayList<>());
        mockMvc.perform(post(UPDATE_COURT_WORKER_MID_OFFICE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(courtWorkerService, times(1))
                .getCourtWorkerMidEventSelectOffice(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorkerMidEventSelectOffice_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(UPDATE_COURT_WORKER_MID_OFFICE_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(courtWorkerService, never())
                .getCourtWorkerMidEventSelectOffice(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorkerMidEventSelectCourtWorker_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(courtWorkerService.getCourtWorkerMidEventSelectCourtWorker(any())).thenReturn(new ArrayList<>());
        mockMvc.perform(post(UPDATE_COURT_WORKER_MID_CLERK_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(courtWorkerService, times(1))
                .getCourtWorkerMidEventSelectCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorkerMidEventSelectCourtWorker_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(UPDATE_COURT_WORKER_MID_CLERK_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(courtWorkerService, never())
                .getCourtWorkerMidEventSelectCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorker_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(courtWorkerService.updateCourtWorker(any())).thenReturn(new ArrayList<>());
        mockMvc.perform(post(UPDATE_COURT_WORKER_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(courtWorkerService, times(1))
                .updateCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void updateCourtWorker_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(UPDATE_COURT_WORKER_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(courtWorkerService, never()).updateCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void deleteCourtWorker_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(courtWorkerService.deleteCourtWorker(any())).thenReturn(new ArrayList<>());
        mockMvc.perform(post(DELETE_COURT_WORKER_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(courtWorkerService, times(1))
                .deleteCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void deleteCourtWorker_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(DELETE_COURT_WORKER_URL)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(courtWorkerService, never()).deleteCourtWorker(ccdRequest.getCaseDetails().getAdminData());
    }
}
