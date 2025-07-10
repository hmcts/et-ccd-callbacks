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
import uk.gov.hmcts.et.common.model.bulk.BulkRequest;
import uk.gov.hmcts.et.common.model.bulk.items.MultipleTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkCreationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkSearchService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkUpdateService;
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
@WebMvcTest(PersistentQueueActionsController.class)
@ContextConfiguration(classes = DocmosisApplication.class)
class PersistentQueueActionsControllerTest extends BaseControllerTest {

    private static final String AFTER_SUBMITTED_PQ_BULK_URL = "/afterSubmittedBulkPQ";
    private static final String PRE_ACCEPT_PQ_BULK_URL = "/preAcceptBulkPQ";
    private static final String UPDATE_BULK_CASE_PQ_URL = "/updateBulkCasePQ";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private BulkCreationService bulkCreationService;

    @MockBean
    private BulkUpdateService bulkUpdateService;

    @MockBean
    private BulkSearchService bulkSearchService;

    private MockMvc mvc;
    private JsonNode requestContent;
    private BulkCasesPayload bulkCasesPayload;
    private BulkRequestPayload bulkRequestPayload;

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
    void afterSubmittedBulkPQ() {
        when(bulkSearchService.bulkCasesRetrievalRequestElasticSearch(
                isA(BulkDetails.class), eq(AUTH_TOKEN), isA(Boolean.class), isA(Boolean.class)))
                .thenReturn(bulkCasesPayload);
        when(bulkCreationService.bulkCreationLogic(
                isA(BulkDetails.class), isA(BulkCasesPayload.class), eq(AUTH_TOKEN), isA(String.class)))
                .thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN))
                .thenReturn(true);
        mvc.perform(post(AFTER_SUBMITTED_PQ_BULK_URL)
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
    void preAcceptBulkPq() {
        when(bulkSearchService.retrievalCasesForPreAcceptRequest(
                isA(BulkDetails.class), eq(AUTH_TOKEN)))
                .thenReturn(bulkCasesPayload.getSubmitEvents());
        when(bulkUpdateService.bulkPreAcceptLogic(
                isA(BulkDetails.class), any(), eq(AUTH_TOKEN), eq(true)))
                .thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_ACCEPT_PQ_BULK_URL)
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
    void updateBulkCasePq() {
        when(bulkCreationService.bulkUpdateCaseIdsLogic(
                isA(BulkRequest.class), eq(AUTH_TOKEN), isA(Boolean.class)))
                .thenReturn(bulkRequestPayload);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN))
                .thenReturn(true);
        mvc.perform(post(UPDATE_BULK_CASE_PQ_URL)
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
    void preAcceptBulkPqError400() {
        mvc.perform(post(PRE_ACCEPT_PQ_BULK_URL)
                .content("error")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void updateBulkCasePqError400() {
        mvc.perform(post(UPDATE_BULK_CASE_PQ_URL)
                .content("error")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void preAcceptBulkPqError500() {
        when(bulkSearchService.retrievalCasesForPreAcceptRequest(
                isA(BulkDetails.class), eq(AUTH_TOKEN)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(PRE_ACCEPT_PQ_BULK_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void updateBulkCasePqError500() {
        when(bulkCreationService.bulkUpdateCaseIdsLogic(
                isA(BulkRequest.class), eq(AUTH_TOKEN), isA(Boolean.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(UPDATE_BULK_CASE_PQ_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void afterSubmittedPqBulkForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(AFTER_SUBMITTED_PQ_BULK_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void preAcceptBulkPqForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(PRE_ACCEPT_PQ_BULK_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void updateBulkCasePqForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(UPDATE_BULK_CASE_PQ_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}