package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EcmMigrationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;

@ExtendWith(SpringExtension.class)
@WebMvcTest({MigrationController.class, JsonMapper.class})
class MigrationControllerTest extends BaseControllerTest {

    private static final String ROLLBACK_ABOUT_TO_SUBMIT_URL = "/migrate/rollback/aboutToSubmit";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @MockBean
    private EcmMigrationService ecmMigrationService;

    @Test
    @SneakyThrows
    void rollbackAboutToSubmit() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(CaseDataBuilder.builder()
                        .withEthosCaseReference("123456/2021")
                        .withClaimantIndType("First", "Last")
                        .withDocumentCollection(ET1)
                        .build())
                .withCaseId("1234567891234567")
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        mockMvc.perform(post(ROLLBACK_ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}