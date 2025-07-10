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
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({TseViewApplicationsController.class, JsonMapper.class})
class TseViewApplicationsControllerTest extends BaseControllerTest {

    private static final String MID_POPULATE_CHOOSE_APPLICATION_URL =
            "/viewRespondentTSEApplications/midPopulateChooseApplication";

    private static final String ABOUT_TO_START_URL =
            "/viewRespondentTSEApplications/aboutToStart";

    private static final String MID_POPULATE_SELECTED_APPLICATION =
            "/viewRespondentTSEApplications/midPopulateSelectedApplicationData";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserIdamService userIdamService;
    @Autowired
    private JsonMapper jsonMapper;

    @MockBean
    private TseService tseService;
    @MockBean
    private TornadoService tornadoService;
    private CCDRequest ccdRequest;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        CaseData caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("9876")
            .withClaimantType("testing@Test.com")
            .withRespondent("respondent", YES, "01-Jan-2023", false)
            .build();

        caseData.setClaimant("Firstname Lastname");

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
            .withCaseId("1234")
            .withCaseData(caseData)
            .build();

        UserDetails userDetails = HelperTest.getUserDetails();
        when(userIdamService.getUserDetails("")).thenReturn(userDetails);
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void aboutToStart_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void aboutToStart_badRequest() {
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midPopulateChooseApplication_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_CHOOSE_APPLICATION_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midPopulateChooseApplication_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_POPULATE_CHOOSE_APPLICATION_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midPopulateChooseApplication_badRequest() {
        mockMvc.perform(post(MID_POPULATE_CHOOSE_APPLICATION_URL)
                        .content("test content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midPopulateReply_tokenOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MID_POPULATE_SELECTED_APPLICATION)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midPopulateReply_tokenFail() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MID_POPULATE_SELECTED_APPLICATION)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midPopulateReply_badRequest() {
        mockMvc.perform(post(MID_POPULATE_SELECTED_APPLICATION)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
