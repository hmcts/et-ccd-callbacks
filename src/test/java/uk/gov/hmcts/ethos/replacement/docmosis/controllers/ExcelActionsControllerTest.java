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
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClerkService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScotlandFileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.FixMultipleCaseApiService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleAmendService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCloseEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCreationMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCreationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleDynamicListFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleHelperService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultiplePreAcceptService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleSingleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleTransferService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUpdateService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUploadService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SubMultipleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SubMultipleUpdateService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ExcelActionsController.class)
@ContextConfiguration(classes = DocmosisApplication.class)
class ExcelActionsControllerTest extends BaseControllerTest {

    private static final String CREATE_MULTIPLE_URL = "/createMultiple";
    private static final String AMEND_MULTIPLE_URL = "/amendMultiple";
    private static final String AMEND_MULTIPLE_API_URL = "/amendMultipleAPI";
    private static final String IMPORT_MULTIPLE_URL = "/importMultiple";
    private static final String PRE_ACCEPT_MULTIPLE_URL = "/preAcceptMultiple";
    private static final String BATCH_UPDATE_URL = "/batchUpdate";
    private static final String UPDATE_SUB_MULTIPLE_URL = "/updateSubMultiple";
    private static final String DYNAMIC_LIST_FLAGS_URL = "/dynamicListFlags";
    private static final String MULTIPLE_MID_EVENT_VALIDATION_URL = "/multipleMidEventValidation";
    private static final String SUB_MULTIPLE_MID_EVENT_VALIDATION_URL = "/subMultipleMidEventValidation";
    private static final String MULTIPLE_CREATION_MID_EVENT_VALIDATION_URL = "/multipleCreationMidEventValidation";
    private static final String MULTIPLE_AMEND_CASE_IDS_MID_EVENT_VALIDATION_URL =
            "/multipleAmendCaseIdsMidEventValidation";
    private static final String MULTIPLE_REMOVE_CASE_IDS_MID_EVENT_VALIDATION_URL =
            "/multipleRemoveCaseIdsMidEventValidation";
    private static final String MULTIPLE_SINGLE_MID_EVENT_VALIDATION_URL = "/multipleSingleMidEventValidation";
    private static final String MULTIPLE_MID_BATCH_1_VALIDATION_URL = "/multipleMidBatch1Validation";
    private static final String CLOSE_MULTIPLE_URL = "/closeMultiple";
    private static final String UPDATE_PAYLOAD_MULTIPLE_URL = "/updatePayloadMultiple";
    private static final String RESET_MULTIPLE_STATE_URL = "/resetMultipleState";
    private static final String DYNAMIC_LIST_OFFICES_MULTIPLE_URL = "/dynamicListOfficesMultiple";
    private static final String MULTIPLE_TRANSFER_URL = "/multipleTransfer";
    private static final String LISTINGS_DATE_RANGE_MID_EVENT_VALIDATION_URL = "/listingsDateRangeMidEventValidation";
    private static final String INITIALISE_BATCH_UPDATE_URL = "/initialiseBatchUpdate";
    private static final String INITIALISE_CLOSE_MULTIPLE_URL = "/initialiseCloseMultiple";
    private static final String FIX_MULTIPLE_CASE_API_URL = "/fixMultipleCaseApi";
    private static final String AUTHORIZATION = "Authorization";
    
    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private MultipleCreationService multipleCreationService;

    @MockBean
    private MultiplePreAcceptService multiplePreAcceptService;

    @MockBean
    private MultipleUpdateService multipleUpdateService;

    @MockBean
    private SubMultipleUpdateService subMultipleUpdateService;

    @MockBean
    private MultipleAmendService multipleAmendService;

    @MockBean
    private MultipleUploadService multipleUploadService;

    @MockBean
    private MultipleDynamicListFlagsService multipleDynamicListFlagsService;

    @MockBean
    private MultipleMidEventValidationService multipleMidEventValidationService;

    @MockBean
    private SubMultipleMidEventValidationService subMultipleMidEventValidationService;

    @MockBean
    private MultipleCreationMidEventValidationService multipleCreationMidEventValidationService;

    @MockBean
    private MultipleSingleMidEventValidationService multipleSingleMidEventValidationService;

    @MockBean
    private EventValidationService eventValidationService;

    @MockBean
    private MultipleHelperService multipleHelperService;

    @MockBean
    private MultipleTransferService multipleTransferService;

    @MockBean
    private FileLocationSelectionService fileLocationSelectionService;

    @MockBean
    private ScotlandFileLocationSelectionService scotlandFileLocationSelectionService;

    @MockBean
    private MultipleCloseEventValidationService multipleCloseEventValidationService;

    @MockBean
    private FixMultipleCaseApiService fixMultipleCaseApiService;

    @MockBean
    private ClerkService clerkService;

    private MockMvc mvc;
    private JsonNode requestContent;
    private JsonNode listingsValidationRequestContent;

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleMultiplesV1.json")).toURI()));

        ObjectMapper dateValidationObjectMapper = new ObjectMapper();
        listingsValidationRequestContent = dateValidationObjectMapper.readTree(
                new File(Objects.requireNonNull(getClass()
                .getResource("/exampleListingV3.json")).toURI()));
    }

    @BeforeEach
    @Override
    @SneakyThrows
    public void setUp() {
        super.setUp();
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        doRequestSetUp();
    }

    @Test
    @SneakyThrows
    void createMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATE_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void fixMultipleCaseApi() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(FIX_MULTIPLE_CASE_API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendMultipleAPI() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_MULTIPLE_API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void uploadBulkExcel() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(IMPORT_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void preAcceptMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_ACCEPT_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void initialiseBatchUpdate() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INITIALISE_BATCH_UPDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationSelectionService, times(1)).initialiseFileLocation(isA(MultipleData.class));
    }

    @Test
    @SneakyThrows
    void batchUpdate() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(BATCH_UPDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void updateSubMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void dynamicListFlags() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_LIST_FLAGS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void multipleMidEventValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void subMultipleMidEventValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SUB_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void multipleCreationMidEventValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_CREATION_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, hasSize(0)));
    }

    @Test
    @SneakyThrows
    void multipleAmendCaseIdsMidEventValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_AMEND_CASE_IDS_MID_EVENT_VALIDATION_URL)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, hasSize(0)));
    }

    @Test
    @SneakyThrows
    void multipleRemoveCaseIdsMidEventValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_REMOVE_CASE_IDS_MID_EVENT_VALIDATION_URL)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void multipleSingleMidEventValidation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_SINGLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void multipleMidBatch1Validation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_MID_BATCH_1_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void initialiseCloseMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(INITIALISE_CLOSE_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationSelectionService, times(1)).initialiseFileLocation(isA(MultipleData.class));
    }

    @Test
    @SneakyThrows
    void closeMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(multipleCloseEventValidationService.validateCasesBeforeCloseEvent(eq(AUTH_TOKEN),
                isA(MultipleDetails.class))).thenReturn(new ArrayList<>());
        mvc.perform(post(CLOSE_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void closeMultipleValidationErrors() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(multipleCloseEventValidationService.validateCasesBeforeCloseEvent(eq(AUTH_TOKEN),
                isA(MultipleDetails.class))).thenReturn(List.of("some error"));
        mvc.perform(post(CLOSE_MULTIPLE_URL)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void updatePayloadMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_PAYLOAD_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void resetMultipleState() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(RESET_MULTIPLE_STATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void dynamicListOfficesMultiple() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_LIST_OFFICES_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void multipleTransfer() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_TRANSFER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void createMultipleError400() {
        mvc.perform(post(CREATE_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void amendMultipleError400() {
        mvc.perform(post(AMEND_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void fixMultipleCaseApi400() {
        mvc.perform(post(FIX_MULTIPLE_CASE_API_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void amendMultipleAPIError400() {
        mvc.perform(post(AMEND_MULTIPLE_API_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void uploadBulkExcelError400() {
        mvc.perform(post(IMPORT_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void preAcceptMultipleError400() {
        mvc.perform(post(PRE_ACCEPT_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void batchUpdateError400() {
        mvc.perform(post(BATCH_UPDATE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updateSubMultipleError400() {
        mvc.perform(post(UPDATE_SUB_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dynamicListFlagsError400() {
        mvc.perform(post(DYNAMIC_LIST_FLAGS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void multipleMidEventValidationError400() {
        mvc.perform(post(MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void subMultipleMidEventValidationError400() {
        mvc.perform(post(SUB_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void multipleCreationMidEventValidationError400() {
        mvc.perform(post(MULTIPLE_CREATION_MID_EVENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void multipleAmendCaseIdsMidEventValidationError400() {
        mvc.perform(post(MULTIPLE_AMEND_CASE_IDS_MID_EVENT_VALIDATION_URL)
                        .content("error")
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void multipleRemoveCaseIdsMidEventValidationError400() {
        mvc.perform(post(MULTIPLE_REMOVE_CASE_IDS_MID_EVENT_VALIDATION_URL)
                        .content("error")
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void multipleSingleMidEventValidationError400() {
        mvc.perform(post(MULTIPLE_SINGLE_MID_EVENT_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void multipleMidBatch1tValidationError400() {
        mvc.perform(post(MULTIPLE_MID_BATCH_1_VALIDATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void closeMultipleError400() {
        mvc.perform(post(CLOSE_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updatePayloadMultipleError400() {
        mvc.perform(post(UPDATE_PAYLOAD_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void resetMultipleStateError400() {
        mvc.perform(post(RESET_MULTIPLE_STATE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void dynamicListOfficesMultipleError400() {
        mvc.perform(post(DYNAMIC_LIST_OFFICES_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void multipleTransferError400() {
        mvc.perform(post(MULTIPLE_TRANSFER_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createMultipleError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(multipleCreationService).bulkCreationLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATE_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void amendMultipleError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(multipleAmendService).bulkAmendMultipleLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AMEND_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void uploadBulkExcelError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(multipleUploadService).bulkUploadLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(IMPORT_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void preAcceptMultipleError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(multiplePreAcceptService).bulkPreAcceptLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_ACCEPT_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void batchUpdateError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(multipleUpdateService).bulkUpdateLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(BATCH_UPDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void updateSubMultipleError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(subMultipleUpdateService).subMultipleUpdateLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void dynamicListFlagsError500() {
        doThrow(new InternalException(ERROR_MESSAGE))
                .when(multipleDynamicListFlagsService).populateDynamicListFlagsLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_LIST_FLAGS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void multipleMidEventValidationError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(multipleMidEventValidationService).multipleValidationLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void subMultipleMidEventValidationError500() {
        doThrow(new InternalException(ERROR_MESSAGE))
                .when(subMultipleMidEventValidationService).subMultipleValidationLogic(
                isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SUB_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void multipleCreationMidEventValidationError500() {
        doThrow(new InternalException(ERROR_MESSAGE))
                .when(multipleCreationMidEventValidationService).multipleCreationValidationLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList(), anyList(), isA(Boolean.class));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_CREATION_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void multipleAmendCaseIdsMidEventValidationError500() {
        doThrow(new InternalException(ERROR_MESSAGE))
                .when(multipleCreationMidEventValidationService).multipleCreationValidationLogic(
                        eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList(), anyList(), isA(Boolean.class));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_AMEND_CASE_IDS_MID_EVENT_VALIDATION_URL)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void multipleRemoveCaseIdsMidEventValidationError500() {
        doThrow(new InternalException(ERROR_MESSAGE))
                .when(multipleCreationMidEventValidationService).multipleRemoveCasesValidationLogic(
                        eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_REMOVE_CASE_IDS_MID_EVENT_VALIDATION_URL)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void multipleSingleMidEventValidationError500() {
        doThrow(new InternalException(ERROR_MESSAGE))
                .when(multipleSingleMidEventValidationService).multipleSingleValidationLogic(
                eq(AUTH_TOKEN), isA(MultipleDetails.class), anyList());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MULTIPLE_SINGLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void createMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CREATE_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void amendMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void amendMultipleAPIForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AMEND_MULTIPLE_API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void uploadBulkExcelForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(IMPORT_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void preAcceptMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(PRE_ACCEPT_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void batchUpdateForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(BATCH_UPDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void initialiseBatchUpdateForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(INITIALISE_BATCH_UPDATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(fileLocationSelectionService, never()).initialiseFileLocation(isA(MultipleData.class));
    }

    @Test
    @SneakyThrows
    void updateSubMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicListFlagsForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_LIST_FLAGS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void multipleMidEventValidationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void subMultipleMidEventValidationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SUB_MULTIPLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void multipleCreationMidEventValidationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MULTIPLE_CREATION_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void multipleAmendCaseIdsMidEventValidationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MULTIPLE_AMEND_CASE_IDS_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void multipleSingleMidEventValidationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MULTIPLE_SINGLE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void multipleMidBatch1ValidationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MULTIPLE_MID_BATCH_1_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void initialiseCloseMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(INITIALISE_CLOSE_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(fileLocationSelectionService, never()).initialiseFileLocation(isA(MultipleData.class));
    }

    @Test
    @SneakyThrows
    void closeMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CLOSE_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void fixMultipleCaseForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(FIX_MULTIPLE_CASE_API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void updatePayloadMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_PAYLOAD_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void resetMultipleStateForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(RESET_MULTIPLE_STATE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicListOfficesMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DYNAMIC_LIST_OFFICES_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void multipleTransferForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MULTIPLE_TRANSFER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void listingsDateRangeMidEventValidationOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(LISTINGS_DATE_RANGE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void listingsDateRangeMidEventValidationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(LISTINGS_DATE_RANGE_MID_EVENT_VALIDATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void listingsDateRangeMidEventValidationError500() {
        doThrow(new InternalException(ERROR_MESSAGE)).when(eventValidationService).validateListingDateRange(
                isA(String.class), isA(String.class));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(LISTINGS_DATE_RANGE_MID_EVENT_VALIDATION_URL)
                .content(listingsValidationRequestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}