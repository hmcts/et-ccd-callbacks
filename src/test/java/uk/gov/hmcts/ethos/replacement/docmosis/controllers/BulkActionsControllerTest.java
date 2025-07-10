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
import uk.gov.hmcts.ecm.common.model.helper.BulkCasesPayload;
import uk.gov.hmcts.ecm.common.model.helper.BulkRequestPayload;
import uk.gov.hmcts.et.common.model.bulk.BulkData;
import uk.gov.hmcts.et.common.model.bulk.BulkDetails;
import uk.gov.hmcts.et.common.model.bulk.BulkDocumentInfo;
import uk.gov.hmcts.et.common.model.bulk.BulkRequest;
import uk.gov.hmcts.et.common.model.bulk.items.MultipleTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkCreationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkSearchService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkUpdateService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentGenerationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SubMultipleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BulkActionsController.class)
@ContextConfiguration(classes = DocmosisApplication.class)
class BulkActionsControllerTest extends BaseControllerTest {

    private static final String CREATION_BULK_URL = "/createBulk";
    private static final String CREATION_BULK_ES_URL = "/createBulkES";
    private static final String SEARCH_BULK_URL = "/searchBulk";
    private static final String MID_SEARCH_BULK_URL = "/midSearchBulk";
    private static final String UPDATE_BULK_URL = "/updateBulk";
    private static final String UPDATE_BULK_CASE_URL = "/updateBulkCase";
    private static final String GENERATE_BULK_LETTER_URL = "/generateBulkLetter";
    private static final String GENERATE_BULK_LETTER_CONFIRMATION_URL = "/generateBulkLetterConfirmation";

    private static final String SUB_MULTIPLE_DYNAMIC_LIST_URL = "/subMultipleDynamicList";
    private static final String FILTER_DEFAULTED_ALL_DYNAMIC_LIST_URL = "/filterDefaultedAllDynamicList";
    private static final String FILTER_DEFAULTED_NONE_DYNAMIC_LIST_URL = "/filterDefaultedNoneDynamicList";
    private static final String MID_CREATE_SUB_MULTIPLE_URL = "/midCreateSubMultiple";
    private static final String CREATE_SUB_MULTIPLE_URL = "/createSubMultiple";
    private static final String MID_UPDATE_SUB_MULTIPLE_URL = "/midUpdateSubMultiple";
    private static final String UPDATE_SUB_MULTIPLE_URL = "/updateSubMultiple1";
    private static final String DELETE_SUB_MULTIPLE_URL = "/deleteSubMultiple";
    private static final String GENERATE_BULK_SCHEDULE_URL = "/generateBulkSchedule";
    private static final String GENERATE_BULK_SCHEDULE_CONFIRMATION_URL = "/generateBulkScheduleConfirmation";
    private static final String PRE_ACCEPT_BULK_URL = "/preAcceptBulk";
    private static final String AFTER_SUBMITTED_BULK_URL = "/afterSubmittedBulk";
    private static final String AUTHORIZATION = "Authorization";
    
    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private BulkCreationService bulkCreationService;

    @MockBean
    private BulkUpdateService bulkUpdateService;

    @MockBean
    private BulkSearchService bulkSearchService;

    @MockBean
    private DocumentGenerationService documentGenerationService;

    @MockBean
    private SubMultipleService subMultipleService;

    private MockMvc mvc;
    private JsonNode requestContent;
    private BulkCasesPayload bulkCasesPayload;
    private BulkRequestPayload bulkRequestPayload;
    private BulkDocumentInfo bulkDocumentInfo;

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleBulkV1.json")).toURI()));
    }

    @BeforeEach
    @Override
    @SneakyThrows
    public void setUp() {
        super.setUp();
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        doRequestSetUp();
        List<SubmitEvent> submitEvents;
        bulkCasesPayload = new BulkCasesPayload();
        submitEvents = getSubmitEvents();
        bulkCasesPayload.setSubmitEvents(submitEvents);
        List<MultipleTypeItem> multipleTypeItems = new ArrayList<>();
        bulkCasesPayload.setMultipleTypeItems(multipleTypeItems);
        BulkData bulkData = new BulkData();
        BulkDetails bulkDetails = new BulkDetails();
        bulkDetails.setCaseData(bulkData);
        bulkRequestPayload = new BulkRequestPayload();
        bulkRequestPayload.setBulkDetails(bulkDetails);
        DocumentInfo documentInfo1 = new DocumentInfo();
        documentInfo1.setMarkUp("markup1");
        DocumentInfo documentInfo2 = new DocumentInfo();
        documentInfo2.setMarkUp("markup2");
        List<DocumentInfo> documentInfoList = new ArrayList<>(Arrays.asList(documentInfo1, documentInfo2));
        bulkDocumentInfo = new BulkDocumentInfo();
        bulkDocumentInfo.setMarkUps(
                documentInfoList.stream().map(DocumentInfo::getMarkUp).collect(Collectors.joining(", ")));
        bulkDocumentInfo.setErrors(new ArrayList<>());
    }

    @Test
    @SneakyThrows
    void createBulkCase() {
        when(bulkSearchService.bulkCasesRetrievalRequest(isA(
                BulkDetails.class), eq(AUTH_TOKEN), isA(Boolean.class))).thenReturn(bulkCasesPayload);
        when(bulkCreationService.bulkCreationLogic(isA(
                BulkDetails.class), isA(BulkCasesPayload.class), eq(AUTH_TOKEN), isA(String.class)))
                .thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN))
                .thenReturn(true);
        mvc.perform(post(CREATION_BULK_URL)
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
    void createBulkCaseES() {
        when(bulkSearchService.bulkCasesRetrievalRequestElasticSearch(
                isA(BulkDetails.class), eq(AUTH_TOKEN), isA(Boolean.class), isA(Boolean.class)))
                .thenReturn(bulkCasesPayload);
        when(bulkCreationService.bulkCreationLogic(
                isA(BulkDetails.class), isA(BulkCasesPayload.class), eq(AUTH_TOKEN), isA(String.class)))
                .thenReturn(bulkRequestPayload);

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATION_BULK_ES_URL)
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
    void createSearchBulkCase() {
        when(bulkSearchService.bulkSearchLogic(isA(BulkDetails.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SEARCH_BULK_URL)
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
    void midSearchBulkCase() {
        when(bulkSearchService.bulkMidSearchLogic(
                isA(BulkDetails.class), isA(Boolean.class)))
                .thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN))
                .thenReturn(true);
        mvc.perform(post(MID_SEARCH_BULK_URL)
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
    void updateBulk() {
        when(bulkUpdateService.bulkUpdateLogic(
                isA(BulkDetails.class), eq(AUTH_TOKEN))).thenReturn(bulkRequestPayload);
        when(bulkUpdateService.clearUpFields(
                isA(BulkRequestPayload.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_BULK_URL)
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
    void updateBulkCase() {
        when(bulkCreationService.bulkUpdateCaseIdsLogic(
                isA(BulkRequest.class), eq(AUTH_TOKEN), isA(Boolean.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_BULK_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    private List<SubmitEvent> getSubmitEvents() {
        CaseData caseData = new CaseData();
        caseData.setClerkResponsible(new DynamicFixedListType("JuanFran"));
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantLastName("Mike");
        caseData.setClaimantIndType(claimantIndType);
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Andrew Smith");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(respondentSumTypeItem)));

        caseData.setFileLocation(new DynamicFixedListType("Manchester"));
        SubmitEvent submitEvent1 = new SubmitEvent();
        submitEvent1.setCaseData(caseData);
        SubmitEvent submitEvent2 = new SubmitEvent();
        submitEvent2.setCaseData(caseData);
        return new ArrayList<>(Arrays.asList(submitEvent1, submitEvent2));
    }

    @Test
    @SneakyThrows
    void generateBulkLetter() {
        when(documentGenerationService.processBulkDocumentRequest(
                isA(BulkRequest.class), eq(AUTH_TOKEN))).thenReturn(bulkDocumentInfo);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_LETTER_URL)
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
    void generateBulkLetterConfirmation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_LETTER_CONFIRMATION_URL)
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
    void generateBulkLetterWithErrors() {
        BulkDocumentInfo bulkDocumentInfo1 = new BulkDocumentInfo();
        bulkDocumentInfo1.setErrors(new ArrayList<>(Collections.singleton(
                "There are not cases searched to generate letters")));
        bulkDocumentInfo1.setMarkUps("");
        when(documentGenerationService.processBulkDocumentRequest(
                isA(BulkRequest.class), eq(AUTH_TOKEN))).thenReturn(bulkDocumentInfo1);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_LETTER_URL)
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
    void midCreateSubMultiple() {
        when(bulkSearchService.bulkMidSearchLogic(
                isA(BulkDetails.class), isA(Boolean.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_CREATE_SUB_MULTIPLE_URL)
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
    void createSubMultiple() {
        when(subMultipleService.createSubMultipleLogic(
                isA(BulkDetails.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATE_SUB_MULTIPLE_URL)
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
    void subMultipleDynamicList() {
        when(subMultipleService.populateSubMultipleDynamicListLogic(
                isA(BulkDetails.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SUB_MULTIPLE_DYNAMIC_LIST_URL)
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
    void filterDefaultedAllDynamicList() {
        when(subMultipleService.populateFilterDefaultedDynamicListLogic(
                isA(BulkDetails.class), isA(String.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(FILTER_DEFAULTED_ALL_DYNAMIC_LIST_URL)
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
    void filterDefaultedNoneDynamicList() {
        when(subMultipleService.populateFilterDefaultedDynamicListLogic(
                isA(BulkDetails.class), isA(String.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(FILTER_DEFAULTED_NONE_DYNAMIC_LIST_URL)
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
    void midUpdateSubMultiple() {
        when(subMultipleService.bulkMidUpdateLogic(
                isA(BulkDetails.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_UPDATE_SUB_MULTIPLE_URL)
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
    void updateSubMultiple() {
        when(subMultipleService.updateSubMultipleLogic(
                isA(BulkDetails.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_SUB_MULTIPLE_URL)
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
    void deleteSubMultiple() {
        when(subMultipleService.deleteSubMultipleLogic(
                isA(BulkDetails.class))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DELETE_SUB_MULTIPLE_URL)
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
    void generateBulkSchedule() {
        when(documentGenerationService.processBulkScheduleRequest(
                isA(BulkRequest.class), isA(String.class))).thenReturn(bulkDocumentInfo);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_SCHEDULE_URL)
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
    void generateBulkScheduleConfirmation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_SCHEDULE_CONFIRMATION_URL)
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
    void generateBulkScheduleWithErrors() {
        bulkDocumentInfo.setErrors(new ArrayList<>(Collections.singleton("Error")));
        when(documentGenerationService.processBulkScheduleRequest(
                isA(BulkRequest.class), isA(String.class))).thenReturn(bulkDocumentInfo);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_SCHEDULE_URL)
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
    void generateBulkScheduleWithBulkInfo() {
        bulkDocumentInfo.setDocumentInfo(new DocumentInfo());
        when(documentGenerationService.processBulkScheduleRequest(
                isA(BulkRequest.class), isA(String.class))).thenReturn(bulkDocumentInfo);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_SCHEDULE_URL)
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
    void preAcceptBulk() {
        when(bulkSearchService.retrievalCasesForPreAcceptRequest(
                isA(BulkDetails.class), eq(AUTH_TOKEN))).thenReturn(bulkCasesPayload.getSubmitEvents());
        when(bulkUpdateService.bulkPreAcceptLogic(
                isA(BulkDetails.class), any(), eq(AUTH_TOKEN), eq(false))).thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_ACCEPT_BULK_URL)
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
    void afterSubmittedBulk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(AFTER_SUBMITTED_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void createBulkCaseError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATION_BULK_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createBulkCaseESError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATION_BULK_ES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createSearchBulkCaseError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SEARCH_BULK_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midSearchBulkCaseError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_SEARCH_BULK_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updateBulkError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_BULK_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updateBulkCaseError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_BULK_CASE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void generateBulkLetterError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_LETTER_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midCreateSubMultipleError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_CREATE_SUB_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void subMultipleDynamicListError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SUB_MULTIPLE_DYNAMIC_LIST_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createSubMultipleError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATE_SUB_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void filterDefaultedAllDynamicListError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(FILTER_DEFAULTED_ALL_DYNAMIC_LIST_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void filterDefaultedNoneDynamicListError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(FILTER_DEFAULTED_NONE_DYNAMIC_LIST_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void midUpdateSubMultipleError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_UPDATE_SUB_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updateSubMultipleError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_SUB_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void deleteSubMultipleError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DELETE_SUB_MULTIPLE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void generateBulkScheduleError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_SCHEDULE_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void preAcceptBulkError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_ACCEPT_BULK_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createBulkCaseError500() {
        when(bulkSearchService.bulkCasesRetrievalRequest(
                isA(BulkDetails.class), eq(AUTH_TOKEN),
                isA(Boolean.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATION_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void createBulkCaseESError500() {
        when(bulkSearchService.bulkCasesRetrievalRequestElasticSearch(
                isA(BulkDetails.class), eq(AUTH_TOKEN), isA(Boolean.class),
                isA(Boolean.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATION_BULK_ES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void createSearchBulkCaseError500() {
        when(bulkSearchService.bulkSearchLogic(isA(BulkDetails.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SEARCH_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void midSearchBulkCaseError500() {
        when(bulkSearchService.bulkMidSearchLogic(
                isA(BulkDetails.class), isA(Boolean.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_SEARCH_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void updateBulkError500() {
        when(bulkUpdateService.bulkUpdateLogic(
                isA(BulkDetails.class), eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void updateBulkCaseError500() {
        when(bulkCreationService.bulkUpdateCaseIdsLogic(
                isA(BulkRequest.class), eq(AUTH_TOKEN), isA(Boolean.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_BULK_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void generateBulkLetterError500() {
        when(documentGenerationService.processBulkDocumentRequest(
                isA(BulkRequest.class), eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_LETTER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void midCreateSubMultipleError500() {
        when(bulkSearchService.bulkMidSearchLogic(
                isA(BulkDetails.class), isA(Boolean.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_CREATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void createSubMultipleError500() {
        when(subMultipleService.createSubMultipleLogic(
                isA(BulkDetails.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(CREATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void subMultipleDynamicListError500() {
        when(subMultipleService.populateSubMultipleDynamicListLogic(
                isA(BulkDetails.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SUB_MULTIPLE_DYNAMIC_LIST_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void filterDefaultedAllDynamicListError500() {
        when(subMultipleService.populateFilterDefaultedDynamicListLogic(
                isA(BulkDetails.class), isA(String.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(FILTER_DEFAULTED_ALL_DYNAMIC_LIST_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void filterDefaultedNoneDynamicListError500() {
        when(subMultipleService.populateFilterDefaultedDynamicListLogic(
                isA(BulkDetails.class), isA(String.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(FILTER_DEFAULTED_NONE_DYNAMIC_LIST_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void midUpdateSubMultipleError500() {
        when(subMultipleService.bulkMidUpdateLogic(
                isA(BulkDetails.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_UPDATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void updateSubMultipleError500() {
        when(subMultipleService.updateSubMultipleLogic(
                isA(BulkDetails.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void deleteSubMultipleError500() {
        when(subMultipleService.deleteSubMultipleLogic(
                isA(BulkDetails.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DELETE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void generateBulkScheduleError500() {
        when(documentGenerationService.processBulkScheduleRequest(
                isA(BulkRequest.class), isA(String.class))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_BULK_SCHEDULE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void preAcceptBulkError500() {
        when(bulkSearchService.retrievalCasesForPreAcceptRequest(
                isA(BulkDetails.class), eq(AUTH_TOKEN))).thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_ACCEPT_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void createBulkCaseForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CREATION_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void createBulkCaseESForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CREATION_BULK_ES_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void createSearchBulkCaseForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SEARCH_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midSearchBulkCaseForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_SEARCH_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void updateBulkForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void updateBulkCaseForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_BULK_CASE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateBulkLetterForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_BULK_LETTER_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateBulkLetterConfirmationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_BULK_LETTER_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midCreateSubMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_CREATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void createSubMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(CREATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void subMultipleDynamicListForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SUB_MULTIPLE_DYNAMIC_LIST_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void filterDefaultedAllDynamicListForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(FILTER_DEFAULTED_ALL_DYNAMIC_LIST_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void filterDefaultedNoneDynamicListForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(FILTER_DEFAULTED_NONE_DYNAMIC_LIST_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void midUpdateSubMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_UPDATE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
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
    void deleteSubMultipleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(DELETE_SUB_MULTIPLE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateBulkScheduleForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_BULK_SCHEDULE_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateBulkScheduleConfirmationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_BULK_SCHEDULE_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void preAcceptBulkForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(PRE_ACCEPT_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void afterSubmittedBulkForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AFTER_SUBMITTED_BULK_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}