package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.compat.common.client.CcdClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY;

@ExtendWith(SpringExtension.class)
class CaseTransferTaskTest {

    private static final String ADMIN_TOKEN = "AdminToken";
    private static final String CASE_ID = "1234567890";
    private static final String OLD_CASE_TYPE_ID = "ET_EnglandWales";
    private static final String NEW_CASE_TYPE_ID = "ET_Scotland";
    private static final String JURISDICTION = "EMPLOYMENT";

    private CaseTransferTask caseTransferTask;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private CcdClient ccdClient;

    @Captor
    private ArgumentCaptor<CaseData> caseDataCaptor;

    @BeforeEach
    void setUp() {
        caseTransferTask = new CaseTransferTask(adminUserService, ccdClient);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
    }

    @Test
    void run_doesNothing_whenCasesToUpdateIsNull() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", null);

        caseTransferTask.run();

        verify(ccdClient, never()).returnCaseCreationTransfer(any(), any(), any(), any());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_doesNothing_whenCasesToUpdateIsEmpty() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", "");

        caseTransferTask.run();

        verify(ccdClient, never()).returnCaseCreationTransfer(any(), any(), any(), any());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_logsError_whenCaseIdsNotMultipleOfThree() throws IOException {
        // "caseId1,oldType1" has 2 elements, not divisible by 3
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", "caseId1,oldType1");

        caseTransferTask.run();

        verify(ccdClient, never()).returnCaseCreationTransfer(any(), any(), any(), any());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_transfersSingleCase_successfully() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate",
            CASE_ID + "," + OLD_CASE_TYPE_ID + "," + NEW_CASE_TYPE_ID);

        CCDRequest returnCcdRequest = buildCcdRequest();
        CCDRequest startEventCcdRequest = buildCcdRequest();

        when(ccdClient.returnCaseCreationTransfer(ADMIN_TOKEN, OLD_CASE_TYPE_ID, EMPLOYMENT, CASE_ID))
            .thenReturn(returnCcdRequest);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, OLD_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, "caseTransfer"))
            .thenReturn(startEventCcdRequest);

        caseTransferTask.run();

        // returnCaseCreationTransfer + submitEventForCase (reset) + startEventForCase + submitEventForCase (transfer)
        verify(ccdClient, times(1)).returnCaseCreationTransfer(ADMIN_TOKEN, OLD_CASE_TYPE_ID, EMPLOYMENT, CASE_ID);
        verify(ccdClient, times(1))
            .startEventForCase(ADMIN_TOKEN, OLD_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, "caseTransfer");
        verify(ccdClient, times(2)).submitEventForCase(
            eq(ADMIN_TOKEN), any(CaseData.class), eq(OLD_CASE_TYPE_ID), eq(JURISDICTION), any(CCDRequest.class),
            eq(CASE_ID));
    }

    @Test
    void run_setsCorrectCaseDataOnTransferEvent() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate",
            CASE_ID + "," + OLD_CASE_TYPE_ID + "," + NEW_CASE_TYPE_ID);

        when(ccdClient.returnCaseCreationTransfer(ADMIN_TOKEN, OLD_CASE_TYPE_ID, EMPLOYMENT, CASE_ID))
            .thenReturn(buildCcdRequest());
        when(ccdClient.startEventForCase(ADMIN_TOKEN, OLD_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, "caseTransfer"))
            .thenReturn(buildCcdRequest());

        caseTransferTask.run();

        // The second submitEventForCase call carries the updated CaseData
        verify(ccdClient, times(2)).submitEventForCase(
            eq(ADMIN_TOKEN), caseDataCaptor.capture(), eq(OLD_CASE_TYPE_ID), eq(JURISDICTION),
            any(CCDRequest.class), eq(CASE_ID));

        CaseData transferCaseData = caseDataCaptor.getAllValues().get(1);
        assertEquals(NEW_CASE_TYPE_ID, transferCaseData.getOfficeCT().getValue().getCode());
        assertEquals(POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY, transferCaseData.getPositionTypeCT());
        assertEquals("ET Data Quality", transferCaseData.getReasonForCT());
    }

    @Test
    void run_transfersMultipleCases_successfully() throws IOException {
        String caseId2 = "9876543210";
        String oldType2 = "ET_Scotland";
        String newType2 = "ET_EnglandWales";
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate",
            CASE_ID + "," + OLD_CASE_TYPE_ID + "," + NEW_CASE_TYPE_ID
            + "," + caseId2 + "," + oldType2 + "," + newType2);

        when(ccdClient.returnCaseCreationTransfer(any(), any(), any(), any())).thenReturn(buildCcdRequest());
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(buildCcdRequest());

        caseTransferTask.run();

        verify(ccdClient, times(2)).returnCaseCreationTransfer(any(), any(), any(), any());
        verify(ccdClient, times(2)).startEventForCase(any(), any(), any(), any(), any());
        // 2 resets + 2 transfers = 4 total submitEventForCase calls
        verify(ccdClient, times(4)).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_continuesProcessing_whenOneCaseFails() throws IOException {
        String caseId2 = "9876543210";
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate",
            CASE_ID + "," + OLD_CASE_TYPE_ID + "," + NEW_CASE_TYPE_ID
            + "," + caseId2 + "," + OLD_CASE_TYPE_ID + "," + NEW_CASE_TYPE_ID);

        // First case throws; second case succeeds
        when(ccdClient.returnCaseCreationTransfer(eq(ADMIN_TOKEN), eq(OLD_CASE_TYPE_ID), eq(EMPLOYMENT),
            eq(CASE_ID))).thenThrow(new IOException("CCD unavailable"));
        when(ccdClient.returnCaseCreationTransfer(eq(ADMIN_TOKEN), eq(OLD_CASE_TYPE_ID), eq(EMPLOYMENT),
            eq(caseId2))).thenReturn(buildCcdRequest());
        when(ccdClient.startEventForCase(any(), any(), any(), eq(caseId2), any())).thenReturn(buildCcdRequest());

        caseTransferTask.run();

        // Both cases attempted the first step
        verify(ccdClient, times(2)).returnCaseCreationTransfer(any(), any(), any(), any());
        // Only the successful case proceeds to startEventForCase
        verify(ccdClient, times(1)).startEventForCase(any(), any(), any(), any(), any());
    }

    // ---- helpers ----

    private CCDRequest buildCcdRequest() {
        CaseData caseData = new CaseData();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setJurisdiction(JURISDICTION);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }
}
