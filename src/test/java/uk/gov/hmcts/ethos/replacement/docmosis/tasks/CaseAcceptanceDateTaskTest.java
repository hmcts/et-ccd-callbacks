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
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PreAcceptanceCaseService;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.LEEDS_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class CaseAcceptanceDateTaskTest {

    private static final String ADMIN_TOKEN = "AdminToken";
    private static final String CASE_ID = "123456789";
    private static final String SECOND_CASE_ID = "987654321";

    private CaseAcceptanceDateTask caseAcceptanceDateTask;

    @MockitoBean
    private AdminUserService adminUserService;
    @MockitoBean
    private CcdClient ccdClient;
    @MockitoBean
    private uk.gov.hmcts.ecm.compat.common.client.CcdClient ecmCcdClient;
    @MockitoBean
    private PreAcceptanceCaseService preAcceptanceCaseService;

    @Captor
    private ArgumentCaptor<uk.gov.hmcts.ecm.common.model.ccd.CaseData> ecmCaseDataCaptor;

    @BeforeEach
    void setUp() {
        caseAcceptanceDateTask = new CaseAcceptanceDateTask(
            adminUserService, ccdClient, ecmCcdClient, preAcceptanceCaseService);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
    }

    @Test
    void run_nullCasesToUpdate_returnsEarlyWithoutCallingCcd() throws Exception {
        caseAcceptanceDateTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ecmCcdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void run_emptyCasesToUpdate_returnsEarlyWithoutCallingCcd() throws Exception {
        setCasesToUpdate("");

        caseAcceptanceDateTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ecmCcdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void run_oddNumberOfTokens_returnsEarlyWithoutCallingCcd() throws Exception {
        setCasesToUpdate(CASE_ID);

        caseAcceptanceDateTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ecmCcdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void run_englandWalesCaseType_updatesCase() throws Exception {
        setCasesToUpdate(CASE_ID + "," + ENGLANDWALES_CASE_TYPE_ID);
        CCDRequest ccdRequest = buildEtCcdRequest(new CaseData());

        when(ccdClient.startEventForCase(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID),
            eq(EMPLOYMENT), eq(CASE_ID), eq("fixCaseAPI")))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ccdClient).startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID,
            EMPLOYMENT, CASE_ID, "fixCaseAPI");
        verify(preAcceptanceCaseService).clearPreAcceptanceDates(any());
        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
            eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
    }

    @Test
    void run_scotlandCaseType_updatesCase() throws Exception {
        setCasesToUpdate(CASE_ID + "," + SCOTLAND_CASE_TYPE_ID);
        CCDRequest ccdRequest = buildEtCcdRequest(new CaseData());

        when(ccdClient.startEventForCase(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID),
            eq(EMPLOYMENT), eq(CASE_ID), eq("fixCaseAPI")))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ccdClient).startEventForCase(ADMIN_TOKEN, SCOTLAND_CASE_TYPE_ID,
            EMPLOYMENT, CASE_ID, "fixCaseAPI");
        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
            eq(SCOTLAND_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
    }

    @Test
    void run_ecmCaseType_updatesCase() throws Exception {
        setCasesToUpdate(CASE_ID + "," + LEEDS_CASE_TYPE_ID);
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest =
            buildEcmCcdRequest(new uk.gov.hmcts.ecm.common.model.ccd.CaseData());

        when(ecmCcdClient.startEventForCase(eq(ADMIN_TOKEN), eq(LEEDS_CASE_TYPE_ID),
            eq(EMPLOYMENT), eq(CASE_ID), eq("fixCaseAPI")))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ecmCcdClient).startEventForCase(ADMIN_TOKEN, LEEDS_CASE_TYPE_ID,
            EMPLOYMENT, CASE_ID, "fixCaseAPI");
        verify(ecmCcdClient).submitEventForCase(eq(ADMIN_TOKEN),
            any(uk.gov.hmcts.ecm.common.model.ccd.CaseData.class),
            eq(LEEDS_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
    }

    @Test
    void run_unknownCaseType_neitherClientCalled() throws Exception {
        setCasesToUpdate(CASE_ID + ",UnknownCaseType");

        caseAcceptanceDateTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ecmCcdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void run_startEventThrowsException_swallowsExceptionAndDoesNotSubmit() throws Exception {
        setCasesToUpdate(CASE_ID + "," + ENGLANDWALES_CASE_TYPE_ID);

        when(ccdClient.startEventForCase(any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("CCD unavailable"));

        caseAcceptanceDateTask.run();

        verify(ccdClient, times(1)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_mixedCaseTypes_processesBothEtAndEcm() throws Exception {
        setCasesToUpdate(CASE_ID + "," + ENGLANDWALES_CASE_TYPE_ID
            + "," + SECOND_CASE_ID + "," + LEEDS_CASE_TYPE_ID);

        CCDRequest etCcdRequest = buildEtCcdRequest(new CaseData());
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ecmCcdRequest =
            buildEcmCcdRequest(new uk.gov.hmcts.ecm.common.model.ccd.CaseData());

        when(ccdClient.startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any(), any(), any()))
            .thenReturn(etCcdRequest);
        when(ecmCcdClient.startEventForCase(any(), eq(LEEDS_CASE_TYPE_ID), any(), any(), any()))
            .thenReturn(ecmCcdRequest);

        caseAcceptanceDateTask.run();

        verify(ccdClient, times(1)).submitEventForCase(any(), any(),
            eq(ENGLANDWALES_CASE_TYPE_ID), any(), any(), any());
        verify(ecmCcdClient, times(1)).submitEventForCase(any(), any(),
            eq(LEEDS_CASE_TYPE_ID), any(), any(), any());
    }

    @Test
    void run_multipleCasesOfSameType_processesAll() throws Exception {
        setCasesToUpdate(CASE_ID + "," + ENGLANDWALES_CASE_TYPE_ID
            + "," + SECOND_CASE_ID + "," + ENGLANDWALES_CASE_TYPE_ID);
        CCDRequest ccdRequest = buildEtCcdRequest(new CaseData());

        when(ccdClient.startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any(), any(), any()))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ccdClient, times(2)).startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID),
            eq(EMPLOYMENT), any(), eq("fixCaseAPI"));
        verify(ccdClient, times(2)).submitEventForCase(any(), any(CaseData.class),
            eq(ENGLANDWALES_CASE_TYPE_ID), any(), any(), any());
    }

    // --- clearEcmPreAcceptanceDates ---

    @Test
    void run_ecmCaseType_accepted_clearsDateRejected() throws Exception {
        setCasesToUpdate(CASE_ID + "," + LEEDS_CASE_TYPE_ID);

        uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType preAccept =
            new uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType();
        preAccept.setCaseAccepted(YES);
        preAccept.setDateAccepted("2024-01-01");
        preAccept.setDateRejected("2024-01-02");
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData =
            new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        caseData.setPreAcceptCase(preAccept);

        when(ecmCcdClient.startEventForCase(any(), any(), any(), any(), any()))
            .thenReturn(buildEcmCcdRequest(caseData));

        caseAcceptanceDateTask.run();

        verify(ecmCcdClient).submitEventForCase(any(), ecmCaseDataCaptor.capture(),
            any(), any(), any(), any());
        assertNull(ecmCaseDataCaptor.getValue().getPreAcceptCase().getDateRejected());
    }

    @Test
    void run_ecmCaseType_rejected_clearsDateAccepted() throws Exception {
        setCasesToUpdate(CASE_ID + "," + LEEDS_CASE_TYPE_ID);

        uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType preAccept =
            new uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType();
        preAccept.setCaseAccepted(NO);
        preAccept.setDateAccepted("2024-01-01");
        preAccept.setDateRejected("2024-01-02");
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData =
            new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        caseData.setPreAcceptCase(preAccept);

        when(ecmCcdClient.startEventForCase(any(), any(), any(), any(), any()))
            .thenReturn(buildEcmCcdRequest(caseData));

        caseAcceptanceDateTask.run();

        verify(ecmCcdClient).submitEventForCase(any(), ecmCaseDataCaptor.capture(),
            any(), any(), any(), any());
        assertNull(ecmCaseDataCaptor.getValue().getPreAcceptCase().getDateAccepted());
    }
    
    private void setCasesToUpdate(String value) {
        ReflectionTestUtils.setField(caseAcceptanceDateTask, "casesToUpdate", value);
    }

    private CCDRequest buildEtCcdRequest(CaseData caseData) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setJurisdiction(EMPLOYMENT);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }

    private uk.gov.hmcts.ecm.common.model.ccd.CCDRequest buildEcmCcdRequest(
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData) {
        uk.gov.hmcts.ecm.common.model.ccd.CaseDetails caseDetails =
            new uk.gov.hmcts.ecm.common.model.ccd.CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setJurisdiction(EMPLOYMENT);
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest =
            new uk.gov.hmcts.ecm.common.model.ccd.CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }
}
