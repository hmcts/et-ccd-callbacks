package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseNotesService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CaseNotesController.class, JsonMapper.class})
class CaseNotesControllerTest extends BaseControllerTest {
    private static final String MULTIPLES_ABOUT_TO_SUBMIT_URL = "/caseNotes/multiples/aboutToSubmit";
    private static final String SINGLES_ABOUT_TO_SUBMIT_URL = "/caseNotes/singles/aboutToSubmit";
    private static final String SINGLES_MANAGE_ABOUT_TO_START_URL = "/caseNotes/singles/manageCaseNote/aboutToStart";
    private static final String SINGLES_MANAGE_MID_EVENT_URL = "/caseNotes/singles/manageCaseNote/midEvent";
    private static final String SINGLES_MANAGE_ABOUT_TO_SUBMIT_URL = "/caseNotes/singles/manageCaseNote/aboutToSubmit";

    @MockitoBean
    private CaseNotesService caseNotesService;

    @Autowired
    private MockMvc mockMvc;
    private JsonNode requestContent;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;

    @BeforeEach
    @Override
    protected void setUp() throws IOException, URISyntaxException {
        super.setUp();
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/exampleBulkV1.json").toURI()));

        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        CaseData caseData = caseDetails.getCaseData();
        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .withState(ACCEPTED_STATE)
            .withCaseId("1234123412341234")
            .build();
    }

    @Test
    void aboutToSubmit_ok() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(MULTIPLES_ABOUT_TO_SUBMIT_URL)
                        .content(requestContent.toString())
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToSubmit_badToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(MULTIPLES_ABOUT_TO_SUBMIT_URL)
                        .content(requestContent.toString())
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void aboutToSubmit_badRequest() throws Exception {
        mockMvc.perform(post(MULTIPLES_ABOUT_TO_SUBMIT_URL)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aboutToSubmitSingles_ok() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(SINGLES_ABOUT_TO_SUBMIT_URL)
                        .content(requestContent.toString())
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void manageCaseNote_aboutToStart_noNotes() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doCallRealMethod().when(caseNotesService).populateCaseNoteList(any(CaseData.class));
        ccdRequest.getCaseDetails().getCaseData().setCaseNotesCollection(null);
        mockMvc.perform(post(SINGLES_MANAGE_ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()))
                .andExpect(jsonPath("$.errors[0]", is("There are no telephone notes to edit or delete")));
    }

    @Test
    void manageCaseNote_aboutToStart() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doCallRealMethod().when(caseNotesService).populateCaseNoteList(any(CaseData.class));
        GenericTypeItem<CaseNote> caseNoteItem = createCaseNoteItem();
        ccdRequest.getCaseDetails().getCaseData().setCaseNotesCollection(List.of(caseNoteItem, caseNoteItem));
        mockMvc.perform(post(SINGLES_MANAGE_ABOUT_TO_START_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath("$.data.caseNoteList", notNullValue()));
    }

    @Test
    void manageCaseNote_midEvent() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doCallRealMethod().when(caseNotesService).populateEditCaseNote(any(CaseData.class));
        GenericTypeItem<CaseNote> caseNoteItem = createCaseNoteItem();
        ccdRequest.getCaseDetails().getCaseData().setCaseNotesCollection(List.of(caseNoteItem));
        ccdRequest.getCaseDetails().getCaseData().setEditOrDeleteCaseNote("Edit");
        ccdRequest.getCaseDetails().getCaseData().setCaseNoteList(DynamicFixedListType.from(List.of(
            DynamicValueType.create(caseNoteItem.getId(), "Test Note - 2024-06-01 - Test Author"))));
        mockMvc.perform(post(SINGLES_MANAGE_MID_EVENT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()));
    }

    @Test
    void manageCaseNote_aboutToSubmit() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doCallRealMethod().when(caseNotesService).submitCaseNoteUpdate(any(CaseData.class));
        GenericTypeItem<CaseNote> caseNoteItem = createCaseNoteItem();
        ccdRequest.getCaseDetails().getCaseData().setCaseNotesCollection(List.of(caseNoteItem));
        ccdRequest.getCaseDetails().getCaseData().setEditOrDeleteCaseNote("Delete");
        ccdRequest.getCaseDetails().getCaseData().setCaseNoteList(DynamicFixedListType.from(List.of(
            DynamicValueType.create(caseNoteItem.getId(), "Test Note - 2024-06-01 - Test Author"))));
        mockMvc.perform(post(SINGLES_MANAGE_ABOUT_TO_SUBMIT_URL)
                .content(jsonMapper.toJson(ccdRequest))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()));
    }

    private static GenericTypeItem<CaseNote> createCaseNoteItem() {
        CaseNote caseNote = CaseNote.builder()
            .title("Test Note")
            .date("2024-06-01")
            .author("Test Author")
            .build();

        return GenericTypeItem.<CaseNote>builder()
            .id(UUID.randomUUID().toString())
            .value(caseNote)
            .build();
    }
}
