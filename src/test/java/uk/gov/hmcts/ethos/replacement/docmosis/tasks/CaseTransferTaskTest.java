package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class CaseTransferTaskTest {

    private static final String ADMIN_TOKEN = "AdminToken";
    private static final String S2S_TOKEN = "S2SToken";
    private static final String CASE_ID = "1234567890123456";
    private static final String CASE_ID_2 = "9876543210987654";
    private static final String TARGET_CASE_TYPE = "MidlandsWest";
    private static final String EXPECTED_ECM_OFFICE = "Midlands West";
    private static final String CASE_TRANSFER_ECM_EVENT_ID = "caseTransferECM";

    private CaseTransferTask caseTransferTask;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private CcdClient ccdClient;

    @MockitoBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @Captor
    private ArgumentCaptor<CaseData> caseDataCaptor;

    @BeforeEach
    void setUp() {
        caseTransferTask = new CaseTransferTask(adminUserService, ccdClient, coreCaseDataApi, authTokenGenerator);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Test
    void run_doesNothing_whenCasesToUpdateIsNull() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", null);

        caseTransferTask.run();

        verify(coreCaseDataApi, never()).getCase(any(), any(), any());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_doesNothing_whenCasesToUpdateIsEmpty() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", "   ");

        caseTransferTask.run();

        verify(coreCaseDataApi, never()).getCase(any(), any(), any());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_logsError_whenCaseIdsNotMultipleOfTwo() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate",
            CASE_ID + "," + TARGET_CASE_TYPE + "," + CASE_ID_2);

        caseTransferTask.run();

        verify(coreCaseDataApi, never()).getCase(any(), any(), any());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_skipsInvalidCaseIdAndContinuesProcessing() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate",
            "12345," + TARGET_CASE_TYPE + "," + CASE_ID + "," + TARGET_CASE_TYPE);

        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(buildCoreCaseDetails(ENGLANDWALES_CASE_TYPE_ID));
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, CASE_TRANSFER_ECM_EVENT_ID))
            .thenReturn(buildCcdRequest(new CaseData()));

        caseTransferTask.run();

        verify(coreCaseDataApi, never()).getCase(ADMIN_TOKEN, S2S_TOKEN, "12345");
        verify(coreCaseDataApi, times(1)).getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID);
        verify(ccdClient, times(1)).submitEventForCase(
            eq(ADMIN_TOKEN), any(CaseData.class), eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT),
            any(CCDRequest.class), eq(CASE_ID));
    }

    @Test
    void run_skipsInvalidTargetCaseType() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", CASE_ID + ",InvalidOffice");

        caseTransferTask.run();

        verify(coreCaseDataApi, never()).getCase(any(), any(), any());
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_skipsUnsupportedSourceCaseType() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", CASE_ID + "," + TARGET_CASE_TYPE);
        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(buildCoreCaseDetails("MidlandsWest"));

        caseTransferTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_transfersSingleCase_successfully() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", CASE_ID + "," + TARGET_CASE_TYPE);

        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(buildCoreCaseDetails(ENGLANDWALES_CASE_TYPE_ID));
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, CASE_TRANSFER_ECM_EVENT_ID))
            .thenReturn(buildCcdRequest(new CaseData()));

        caseTransferTask.run();

        verify(ccdClient, times(1)).startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, CASE_TRANSFER_ECM_EVENT_ID);
        verify(ccdClient, times(1)).submitEventForCase(
            eq(ADMIN_TOKEN), caseDataCaptor.capture(), eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT),
            any(CCDRequest.class), eq(CASE_ID));

        CaseData submittedData = caseDataCaptor.getValue();
        assertEquals(EXPECTED_ECM_OFFICE, submittedData.getEcmOfficeCT());
        assertEquals(CaseTransferTask.REASON_FOR_CT, submittedData.getReasonForCT());
    }

    @Test
    void run_clearsOnlyUnclearedBfActions() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", CASE_ID + "," + TARGET_CASE_TYPE);

        BFActionType unclearedBfAction = new BFActionType();
        BFActionTypeItem unclearedItem = new BFActionTypeItem();
        unclearedItem.setId("1");
        unclearedItem.setValue(unclearedBfAction);

        BFActionType clearedBfAction = new BFActionType();
        clearedBfAction.setCleared("2025-01-15");
        BFActionTypeItem clearedItem = new BFActionTypeItem();
        clearedItem.setId("2");
        clearedItem.setValue(clearedBfAction);

        CaseData caseData = new CaseData();
        caseData.setBfActions(List.of(unclearedItem, clearedItem));

        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(buildCoreCaseDetails(ENGLANDWALES_CASE_TYPE_ID));
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, CASE_TRANSFER_ECM_EVENT_ID))
            .thenReturn(buildCcdRequest(caseData));

        caseTransferTask.run();

        verify(ccdClient, times(1)).submitEventForCase(
            eq(ADMIN_TOKEN), caseDataCaptor.capture(), eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT),
            any(CCDRequest.class), eq(CASE_ID));

        CaseData submittedData = caseDataCaptor.getValue();
        assertEquals(LocalDate.now().toString(), submittedData.getBfActions().get(0).getValue().getCleared());
        assertEquals("2025-01-15", submittedData.getBfActions().get(1).getValue().getCleared());
    }

    @Test
    void run_transfersMultipleCasesAcrossJurisdictions_successfully() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate",
            CASE_ID + ", " + TARGET_CASE_TYPE + " , " + CASE_ID_2 + ",Scotland");

        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(buildCoreCaseDetails(ENGLANDWALES_CASE_TYPE_ID));
        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID_2))
            .thenReturn(buildCoreCaseDetails(SCOTLAND_CASE_TYPE_ID));

        when(ccdClient.startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, CASE_TRANSFER_ECM_EVENT_ID))
            .thenReturn(buildCcdRequest(new CaseData()));
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN, SCOTLAND_CASE_TYPE_ID, EMPLOYMENT, CASE_ID_2, CASE_TRANSFER_ECM_EVENT_ID))
            .thenReturn(buildCcdRequest(new CaseData()));

        caseTransferTask.run();

        verify(ccdClient, times(1)).submitEventForCase(
            eq(ADMIN_TOKEN), any(CaseData.class), eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT),
            any(CCDRequest.class), eq(CASE_ID));
        verify(ccdClient, times(1)).submitEventForCase(
            eq(ADMIN_TOKEN), caseDataCaptor.capture(), eq(SCOTLAND_CASE_TYPE_ID), eq(EMPLOYMENT),
            any(CCDRequest.class), eq(CASE_ID_2));

        assertEquals("Glasgow", caseDataCaptor.getValue().getEcmOfficeCT());
    }

    @Test
    void run_continuesProcessing_whenOneCaseFails() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate",
            CASE_ID + "," + TARGET_CASE_TYPE + "," + CASE_ID_2 + "," + TARGET_CASE_TYPE);

        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(buildCoreCaseDetails(ENGLANDWALES_CASE_TYPE_ID));
        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID_2))
            .thenReturn(buildCoreCaseDetails(ENGLANDWALES_CASE_TYPE_ID));

        when(ccdClient.startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, CASE_TRANSFER_ECM_EVENT_ID))
            .thenThrow(new IOException("CCD unavailable"));
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID_2, CASE_TRANSFER_ECM_EVENT_ID))
            .thenReturn(buildCcdRequest(new CaseData()));

        caseTransferTask.run();

        verify(ccdClient, times(2)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(1)).submitEventForCase(
            eq(ADMIN_TOKEN), any(CaseData.class), eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT),
            any(CCDRequest.class), eq(CASE_ID_2));
    }

    @Test
    void run_skipsSubmit_whenStartEventReturnsNullCaseData() throws IOException {
        ReflectionTestUtils.setField(caseTransferTask, "casesToUpdate", CASE_ID + "," + TARGET_CASE_TYPE);

        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(buildCoreCaseDetails(ENGLANDWALES_CASE_TYPE_ID));
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, CASE_TRANSFER_ECM_EVENT_ID))
            .thenReturn(new CCDRequest());

        caseTransferTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails buildCoreCaseDetails(String caseTypeId) {
        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .caseTypeId(caseTypeId)
            .jurisdiction(EMPLOYMENT)
            .build();
    }

    private CCDRequest buildCcdRequest(CaseData caseData) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setJurisdiction(EMPLOYMENT);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }
}
