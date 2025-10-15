package uk.gov.hmcts.ethos.replacement.docmosis.controllers.citizen;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.RespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({RespondentController.class, JsonMapper.class})
public class RespondentControllerTest {

    private static final String TEST_AUTHORIZATION = "Bearer testAuthorization";
    private static final String TEST_SUBMISSION_REFERENCE = "123";

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private RespondentService respondentService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    public void removeOwnRepresentative_success() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        when(respondentService.revokeRespondentSolicitorRole(TEST_AUTHORIZATION,
                TEST_SUBMISSION_REFERENCE, NumberUtils.INTEGER_ZERO.toString())).thenReturn(caseDetails);
        when(verifyTokenService.verifyTokenSignature(TEST_AUTHORIZATION)).thenReturn(true);
        mockMvc.perform(post("/citizen/respondent/removeOwnRepresentative")
                        .param("submissionReference", TEST_SUBMISSION_REFERENCE)
                        .param("respondentIndex", NumberUtils.INTEGER_ZERO.toString())
                        .header("Authorization", TEST_AUTHORIZATION)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value("123"));
    }

}
