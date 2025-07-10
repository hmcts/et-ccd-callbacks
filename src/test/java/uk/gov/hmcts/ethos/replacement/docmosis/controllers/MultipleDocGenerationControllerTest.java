package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleDocGenerationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleLetterService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleScheduleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MultipleDocGenerationController.class)
@ContextConfiguration(classes = DocmosisApplication.class)
class MultipleDocGenerationControllerTest extends BaseControllerTest {

    private static final String PRINT_SCHEDULE_URL = "/printSchedule";
    private static final String PRINT_LETTER_URL = "/printLetter";
    private static final String PRINT_DOCUMENT_CONFIRMATION_URL = "/printDocumentConfirmation";
    private static final String MID_SELECTED_ADDRESS_LABELS_MULTIPLE_URL = "/midSelectedAddressLabelsMultiple";
    private static final String MID_VALIDATE_ADDRESS_LABELS_MULTIPLE_URL = "/midValidateAddressLabelsMultiple";
    private static final String DYNAMIC_MULTIPLE_LETTERS = "/dynamicMultipleLetters";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private MultipleLetterService multipleLetterService;

    @MockBean
    private MultipleScheduleService multipleScheduleService;

    @MockBean
    private MultipleDocGenerationService multipleDocGenerationService;

    private MockMvc mvc;
    private JsonNode requestContent;
    private DocumentInfo documentInfo;

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleV1.json")).toURI()));
    }

    @BeforeEach
    @Override
    @SneakyThrows
    public void setUp() {
        super.setUp();
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        doRequestSetUp();
        documentInfo = new DocumentInfo();
        documentInfo.setMarkUp("<a target=\"_blank\" href=\"null/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary\">Document</a>");
    }

    @Test
    @SneakyThrows
    void printSchedule() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(multipleScheduleService.bulkScheduleLogic(eq(AUTH_TOKEN), isA(MultipleDetails.class),
                isA(List.class))).thenReturn(documentInfo);
        mvc.perform(post(PRINT_SCHEDULE_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void printLetter() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(multipleLetterService.bulkLetterLogic(eq(AUTH_TOKEN), isA(MultipleDetails.class),
                isA(List.class), isA(Boolean.class))).thenReturn(documentInfo);
        mvc.perform(post(PRINT_LETTER_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void printDocumentConfirmation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRINT_DOCUMENT_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midSelectedAddressLabelsMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_SELECTED_ADDRESS_LABELS_MULTIPLE_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void midValidateAddressLabelsMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_VALIDATE_ADDRESS_LABELS_MULTIPLE_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void printScheduleError400() {
        mvc.perform(post(PRINT_SCHEDULE_URL)
                .content("error")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void printLetterError400() {
        mvc.perform(post(PRINT_LETTER_URL)
                .content("error")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void printDocumentConfirmationError400() {
        mvc.perform(post(PRINT_DOCUMENT_CONFIRMATION_URL)
                .content("error")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midSelectedAddressLabelsMultiple400() {
        mvc.perform(post(MID_SELECTED_ADDRESS_LABELS_MULTIPLE_URL)
                .content("error")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midValidateAddressLabelsMultiple400() {
        mvc.perform(post(MID_VALIDATE_ADDRESS_LABELS_MULTIPLE_URL)
                .content("error")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void printScheduleError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(multipleScheduleService).bulkScheduleLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), isA(List.class));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRINT_SCHEDULE_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void printLetterError500() {
        when(multipleLetterService.bulkLetterLogic(eq(AUTH_TOKEN), isA(MultipleDetails.class),
                isA(List.class), isA(Boolean.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRINT_LETTER_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void printScheduleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(PRINT_SCHEDULE_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void printLetterForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(PRINT_LETTER_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void printDocumentConfirmationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(PRINT_DOCUMENT_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midSelectedAddressLabelsMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_SELECTED_ADDRESS_LABELS_MULTIPLE_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midValidateAddressLabelsMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_VALIDATE_ADDRESS_LABELS_MULTIPLE_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicMultipleLetters() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_MULTIPLE_LETTERS)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void dynamicMultipleLettersForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_MULTIPLE_LETTERS)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}