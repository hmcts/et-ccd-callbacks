package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.AddJudgeService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({AddJudgeController.class, JsonMapper.class})
class AddJudgeControllerTest {
    @MockBean
    private VerifyTokenService verifyTokenService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockBean
    private AddJudgeService addJudgeService;

    @Test
    void testAddJudgeSuccess() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "ABERDEEN", "SALARIED")
                .buildAsCCDRequest();

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        when(addJudgeService.saveJudge(ccdRequest.getCaseDetails().getAdminData())).thenReturn(true);

        mockMvc.perform(post("/admin/addJudge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(addJudgeService, times(1)).saveJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testAddJudgeConflict() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "ABERDEEN", "SALARIED")
                .buildAsCCDRequest();

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        when(addJudgeService.saveJudge(ccdRequest.getCaseDetails().getAdminData())).thenReturn(false);

        mockMvc.perform(post("/admin/addJudge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isConflict());
        verify(addJudgeService, times(1)).saveJudge(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testAddJudgeInvalidToken() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withJudgeData("testCode", "testName", "ABERDEEN", "SALARIED")
                .buildAsCCDRequest();

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/addJudge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(addJudgeService, never()).saveJudge(ccdRequest.getCaseDetails().getAdminData());
    }

}