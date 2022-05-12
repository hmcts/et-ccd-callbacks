package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff.judge;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.judge.JudgeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.judge.SaveJudgeException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.judge.JudgeService.ADD_JUDGE_CONFLICT_ERROR;

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
    void testAddJudgeError() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "ABERDEEN", "SALARIED")
                .buildAsCCDRequest();

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        String error = String.format(ADD_JUDGE_CONFLICT_ERROR,
                adminData.getJudgeCode(), adminData.getTribunalOffice());

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        doThrow(new SaveJudgeException(error)).when(judgeService).saveJudge(adminData);

        mockMvc.perform(post("/admin/staff/judge/addJudge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(jsonPath("$.errors[0]", is(error)));
        verify(judgeService, times(1)).saveJudge(adminData);
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