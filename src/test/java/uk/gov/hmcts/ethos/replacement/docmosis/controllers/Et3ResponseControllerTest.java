package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest({Et3ResponseController.class, JsonMapper.class})
class Et3ResponseControllerTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String INIT_ET3_RESPONSE_URL = "/et3Response/initEt3Response";
    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    VerifyTokenService verifyTokenService;

    @Test
    void testEt3InitResponse() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        var caseData = ccdRequest.getCaseDetails();
        caseData.setCaseId("caseTest24");
        ccdRequest.setCaseDetails(caseData);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        var result = mockMvc.perform(post(INIT_ET3_RESPONSE_URL)
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.toJson(ccdRequest)))
            .andExpect(status().isOk())
            .andReturn();

        var contentAsString = result.getResponse().getContentAsString();
        assertThat(contentAsString, containsString("/cases/case-details/caseTest24"));
    }

    @Test
    void testEt3InitResponseError400() throws Exception {
        mockMvc.perform(post(INIT_ET3_RESPONSE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("error"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEt3InitResponseForbidden() throws Exception {
        var ccdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(INIT_ET3_RESPONSE_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
    }
}
