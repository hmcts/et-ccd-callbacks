package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.CreateService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.CreateController.ADMIN_CASE_NAME;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CreateController.class, JsonMapper.class})
class CreateControllerTest {

    private String token;

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private CreateService createService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        token = "some-token";
        when(createService.initCreateAdmin(token)).thenReturn(new ArrayList<>());
    }

    @Test
    void testHandleAboutToSubmitEventSuccess() throws Exception {
        var ccdRequest = AdminDataBuilder.builder().buildAsCCDRequest();
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        ResultActions resultActions = mockMvc.perform(post("/admin/create/aboutToSubmitEvent")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));

        var result = resultActions.andReturn();
        var contentAsString = result.getResponse().getContentAsString();
        var json = new JSONObject(contentAsString).getJSONObject("data");
        var adminData = jsonMapper.fromJson(json.toString(), AdminData.class);

        verify(createService, times(1)).initCreateAdmin(token);
        assertEquals(ADMIN_CASE_NAME, adminData.getName());
    }

    @Test
    void testHandleAboutToSubmitEventForbidden() throws Exception {
        var ccdRequest = AdminDataBuilder.builder().buildAsCCDRequest();
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/create/aboutToSubmitEvent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(createService, never()).initCreateAdmin(token);
    }

    @Test
    void testHandleAboutToSubmitEventBadRequest() throws Exception {
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/create/aboutToSubmitEvent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("error"))
                .andExpect(status().isBadRequest());
        verify(createService, never()).initCreateAdmin(token);
    }
}
