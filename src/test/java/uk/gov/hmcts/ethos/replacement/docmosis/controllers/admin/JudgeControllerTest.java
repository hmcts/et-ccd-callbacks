package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff.judge.JudgeController;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.judge.JudgeService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({JudgeController.class, JsonMapper.class})
class JudgeControllerTest {
    @MockBean
    private VerifyTokenService verifyTokenService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockBean
    private JudgeService judgeService;

    @Test
    void testAddJudgeSuccess() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "ABERDEEN", "SALARIED")
                .buildAsCCDRequest();

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        when(judgeService.saveJudge(ccdRequest.getCaseDetails().getAdminData())).thenReturn(true);

        mockMvc.perform(post("/admin/staff/judge/addJudge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(judgeService, times(1)).saveJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testAddJudgeConflict() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "ABERDEEN", "SALARIED")
                .buildAsCCDRequest();

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        when(judgeService.saveJudge(ccdRequest.getCaseDetails().getAdminData())).thenReturn(false);

        mockMvc.perform(post("/admin/staff/judge/addJudge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isConflict());
        verify(judgeService, times(1)).saveJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testAddJudgeInvalidToken() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "ABERDEEN", "SALARIED")
                .buildAsCCDRequest();

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/staff/judge/addJudge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(judgeService, never()).saveJudge(ccdRequest.getCaseDetails().getAdminData());
    }

}