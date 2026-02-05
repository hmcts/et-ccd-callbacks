package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.MultipleErrors;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MessageHandlerTestHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ERRORED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRANSFERRED_STATE;

@ExtendWith(SpringExtension.class)
class MultipleUpdateServiceTest {

    @InjectMocks
    private MultipleUpdateService multipleUpdateService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private AdminUserService adminUserService;
    @Captor
    private ArgumentCaptor<MultipleData> multipleDataArgumentCaptor;
    @Captor
    private ArgumentCaptor<MultipleData> creationMultipleDataArgumentCaptor;

    private List<SubmitMultipleEvent> submitMultipleEvents;
    private SubmitMultipleEvent submitMultipleEvent;
    private UpdateCaseMsg updateCaseMsg;
    private static final String USER_TOKEN = "Token";

    @BeforeEach
    public void setUp() {
        submitMultipleEvent = new SubmitMultipleEvent();
        MultipleData multipleData = new MultipleData();
        multipleData.setMultipleReference("4100001");
        submitMultipleEvent.setCaseData(multipleData);
        submitMultipleEvents = new ArrayList<>(Collections.singletonList(submitMultipleEvent));
        updateCaseMsg = MessageHandlerTestHelper.generateUpdateCaseMsg();

        when(adminUserService.getAdminUserToken()).thenReturn(USER_TOKEN);
    }

    @Test
    void testMultipleTransferSameCountry() throws IOException {
        updateCaseMsg = MessageHandlerTestHelper.generateCreationSingleCaseMsg();
        ((CreationSingleDataModel)updateCaseMsg.getDataModelParent()).setTransferSameCountry(true);
        var caseId = "12345";
        submitMultipleEvent.setCaseId(Long.parseLong(caseId));

        when(ccdClient.retrieveMultipleCasesElasticSearchWithRetries(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                                     updateCaseMsg.getMultipleRef()))
            .thenReturn(submitMultipleEvents);
        var ccdRequest = new CCDRequest();
        when(ccdClient.startBulkAmendEventForCase(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                  updateCaseMsg.getJurisdiction(), caseId)).thenReturn(ccdRequest);

        var multipleErrorsList = new ArrayList<MultipleErrors>();

        multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, multipleErrorsList);

        assertTrue(multipleErrorsList.isEmpty());
        verify(ccdClient).startBulkAmendEventForCase(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                     updateCaseMsg.getJurisdiction(), caseId);
        verify(ccdClient).submitMultipleEventForCase(eq(USER_TOKEN),
                                                     multipleDataArgumentCaptor.capture(),
                                                     eq(updateCaseMsg.getCaseTypeId()),
                                                     eq(updateCaseMsg.getJurisdiction()), eq(ccdRequest), eq(caseId));
        var actualMultipleData = multipleDataArgumentCaptor.getValue();
        assertEquals(OPEN_STATE, actualMultipleData.getState());
        assertNull(actualMultipleData.getLinkedMultipleCT());

        verify(ccdClient, never()).startCaseMultipleCreation(anyString(), anyString(), anyString());
    }

    @Test
    @SuppressWarnings({"PMD.LawOfDemeter"})
    void testMultipleTransferDifferentCountry() throws IOException {
        updateCaseMsg = MessageHandlerTestHelper.generateCreationSingleCaseMsg();
        String newManagingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        CreationSingleDataModel dataModel = (CreationSingleDataModel)updateCaseMsg.getDataModelParent();
        dataModel.setOfficeCT(newManagingOffice);
        dataModel.setTransferSameCountry(false);
        String caseId = "12345";
        submitMultipleEvent.setCaseId(Long.parseLong(caseId));

        when(ccdClient.retrieveMultipleCasesElasticSearchWithRetries(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                                     updateCaseMsg.getMultipleRef()))
            .thenReturn(submitMultipleEvents);
        var ccdRequest = new CCDRequest();
        when(ccdClient.startBulkAmendEventForCase(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                  updateCaseMsg.getJurisdiction(), caseId)).thenReturn(ccdRequest);

        var creationCCDRequest = new CCDRequest();
        when(ccdClient.startCaseMultipleCreation(USER_TOKEN, ENGLANDWALES_BULK_CASE_TYPE_ID,
                                                 updateCaseMsg.getJurisdiction())).thenReturn(creationCCDRequest);

        var multipleErrorsList = new ArrayList<MultipleErrors>();

        multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, multipleErrorsList);

        assertTrue(multipleErrorsList.isEmpty());
        verify(ccdClient).startBulkAmendEventForCase(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                     updateCaseMsg.getJurisdiction(), caseId);
        verify(ccdClient).submitMultipleEventForCase(eq(USER_TOKEN),
                                                     multipleDataArgumentCaptor.capture(),
                                                     eq(updateCaseMsg.getCaseTypeId()),
                                                     eq(updateCaseMsg.getJurisdiction()), eq(ccdRequest), eq(caseId));
        var actualMultipleData = multipleDataArgumentCaptor.getValue();
        assertEquals(TRANSFERRED_STATE, actualMultipleData.getState());
        assertEquals("Transferred to " + newManagingOffice, actualMultipleData.getLinkedMultipleCT());
        assertEquals(dataModel.getReasonForCT(), actualMultipleData.getReasonForCT());
        assertEquals(dataModel.getPositionTypeCT(), actualMultipleData.getPositionType());

        verify(ccdClient, times(1)).submitMultipleCreation(eq(USER_TOKEN), creationMultipleDataArgumentCaptor.capture(),
                                                           eq(ENGLANDWALES_BULK_CASE_TYPE_ID),
                                                           eq(updateCaseMsg.getJurisdiction()),
                                                           eq(creationCCDRequest));
        actualMultipleData = creationMultipleDataArgumentCaptor.getValue();
        assertEquals(newManagingOffice, actualMultipleData.getManagingOffice());
        assertEquals(updateCaseMsg.getCaseTypeId(), actualMultipleData.getLinkedMultipleCT());
        assertEquals(MIGRATION_CASE_SOURCE, actualMultipleData.getMultipleSource());
        assertEquals(updateCaseMsg.getMultipleRef(), actualMultipleData.getMultipleReference());
    }

    @Test
    void sendUpdateToMultipleLogic() throws IOException {
        when(ccdClient.retrieveMultipleCasesElasticSearchWithRetries(anyString(),
                                                                     anyString(),
                                                                     anyString())).thenReturn(submitMultipleEvents);

        when(ccdClient.submitMultipleEventForCase(anyString(), any(), anyString(),
                                                  anyString(), any(), anyString())).thenReturn(submitMultipleEvent);
        multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, new ArrayList<>());

        verifyMocks();
    }

    @Test
    void sendUpdateToMultipleLogicEmptyES() throws IOException {
        when(ccdClient.retrieveMultipleCasesElasticSearchWithRetries(anyString(),
                                                                     anyString(),
                                                                     anyString())).thenReturn(new ArrayList<>());

        multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, new ArrayList<>());

        verify(ccdClient).retrieveMultipleCasesElasticSearchWithRetries(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                                        updateCaseMsg.getMultipleRef());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void sendUpdateToMultipleLogicNullES() throws IOException {
        when(ccdClient.retrieveMultipleCasesElasticSearchWithRetries(anyString(), anyString(),
                                                                     anyString())).thenReturn(null);

        multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, new ArrayList<>());

        verify(ccdClient).retrieveMultipleCasesElasticSearchWithRetries(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                                        updateCaseMsg.getMultipleRef());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void sendUpdateToMultipleLogicWithErrors() throws IOException {
        when(ccdClient.retrieveMultipleCasesElasticSearchWithRetries(anyString(),
                                                                     anyString(),
                                                                     anyString())).thenReturn(submitMultipleEvents);

        when(ccdClient.submitMultipleEventForCase(anyString(), any(),
                                                  anyString(), anyString(), any(),
                                                  anyString())).thenReturn(submitMultipleEvent);
        MultipleErrors multipleErrors = new MultipleErrors();
        multipleErrors.setDescription(ERRORED_STATE);
        multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, new ArrayList<>(Collections.singletonList(
            multipleErrors)));

        verify(ccdClient).retrieveMultipleCasesElasticSearchWithRetries(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                                        updateCaseMsg.getMultipleRef());
        verify(ccdClient).startBulkAmendEventForCase(eq(USER_TOKEN),
                                                     eq(updateCaseMsg.getCaseTypeId()),
                                                     eq(updateCaseMsg.getJurisdiction()),
                                                     any());
        verify(ccdClient).submitMultipleEventForCase(eq(USER_TOKEN),
                                                     multipleDataArgumentCaptor.capture(),
                                                     eq(updateCaseMsg.getCaseTypeId()),
                                                     eq(updateCaseMsg.getJurisdiction()),
                                                     any(),
                                                     any());
        assertEquals(ERRORED_STATE, multipleDataArgumentCaptor.getValue().getState());
    }

    @Test
    void sendUpdateToMultipleTransferredLogic() throws IOException {
        when(ccdClient.retrieveMultipleCasesElasticSearchWithRetries(anyString(),
                                                                     anyString(),
                                                                     anyString())).thenReturn(submitMultipleEvents);

        updateCaseMsg = MessageHandlerTestHelper.generateCreationSingleCaseMsg();

        multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, new ArrayList<>());

        verify(ccdClient).retrieveMultipleCasesElasticSearchWithRetries(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                                        updateCaseMsg.getMultipleRef());
        verify(ccdClient).startBulkAmendEventForCase(eq(USER_TOKEN),
                                                     eq(updateCaseMsg.getCaseTypeId()),
                                                     eq(updateCaseMsg.getJurisdiction()),
                                                     any());
        verify(ccdClient).submitMultipleEventForCase(eq(USER_TOKEN),
                                                     any(),
                                                     eq(updateCaseMsg.getCaseTypeId()),
                                                     eq(updateCaseMsg.getJurisdiction()),
                                                     any(),
                                                     any());

    }

    private void verifyMocks() throws IOException {
        verify(ccdClient).retrieveMultipleCasesElasticSearchWithRetries(USER_TOKEN, updateCaseMsg.getCaseTypeId(),
                                                                        updateCaseMsg.getMultipleRef());
        verify(ccdClient).startBulkAmendEventForCase(eq(USER_TOKEN),
                                                     eq(updateCaseMsg.getCaseTypeId()),
                                                     eq(updateCaseMsg.getJurisdiction()),
                                                     any());
        verify(ccdClient).submitMultipleEventForCase(eq(USER_TOKEN),
                                                     any(),
                                                     eq(updateCaseMsg.getCaseTypeId()),
                                                     eq(updateCaseMsg.getJurisdiction()),
                                                     any(),
                                                     any());
        verifyNoMoreInteractions(ccdClient);
    }
}
