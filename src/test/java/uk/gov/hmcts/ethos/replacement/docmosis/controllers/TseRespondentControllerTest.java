package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTse;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({TseRespondentController.class, JsonMapper.class})
class TseRespondentControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TseService tseService;

    @MockBean
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    private JsonMapper jsonMapper;

    private CCDRequest ccdRequest;

    private static final String ABOUT_TO_SUBMIT = "/tseRespondent/aboutToSubmit";
    private static final String AUTH_TOKEN = "Bearer token";

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("1234")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.setCaseId("4321");

        caseDetails.getCaseData().setTseApplicationStoredCollection(List.of(
                GenericTseApplicationTypeItem.builder()
                        .id("2468")
                        .value(new GenericTseApplicationType())
                        .build()
        ));
        RespondentTse respondentTse = new RespondentTse();
        caseDetails.getCaseData().setRespondentTse(respondentTse);

        ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
    }

    @Test
    @SneakyThrows
    void aboutToSubmit_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
    }
}