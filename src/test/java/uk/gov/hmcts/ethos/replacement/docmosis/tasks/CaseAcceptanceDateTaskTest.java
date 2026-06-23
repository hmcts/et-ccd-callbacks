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
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PreAcceptanceCaseService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
        ReflectionTestUtils.setField(caseAcceptanceDateTask, "maxCasesToProcess", 100);
    }

    @Test
    void run_englandWalesCaseType_fetchesAndUpdatesCase() throws IOException {
        setEtCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        SubmitEvent submitEvent = buildEtSubmitEvent();
        CCDRequest ccdRequest = buildEtCcdRequest(submitEvent.getCaseData());

        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
            .thenReturn(List.of(submitEvent));
        when(ccdClient.startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT),
            eq(CASE_ID), any()))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ccdClient, times(1)).startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID,
            EMPLOYMENT, CASE_ID, "fixCaseAPI");
        verify(preAcceptanceCaseService, times(1)).clearPreAcceptanceDates(any());
        verify(ccdClient, times(1)).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
            eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
    }

    @Test
    void run_scotlandCaseType_fetchesAndUpdatesCase() throws IOException {
        setEtCaseTypeId(SCOTLAND_CASE_TYPE_ID);
        SubmitEvent submitEvent = buildEtSubmitEvent();
        CCDRequest ccdRequest = buildEtCcdRequest(submitEvent.getCaseData());

        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), any()))
            .thenReturn(List.of(submitEvent));
        when(ccdClient.startEventForCase(any(), eq(SCOTLAND_CASE_TYPE_ID), eq(EMPLOYMENT),
            eq(CASE_ID), any()))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ccdClient, times(1)).startEventForCase(ADMIN_TOKEN, SCOTLAND_CASE_TYPE_ID,
            EMPLOYMENT, CASE_ID, "fixCaseAPI");
        verify(ccdClient, times(1)).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
            eq(SCOTLAND_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
    }

    @Test
    void run_etCaseType_emptyCaseList_doesNotTriggerEvent() throws IOException {
        setEtCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any()))
            .thenReturn(Collections.emptyList());

        caseAcceptanceDateTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_etCaseType_fetchThrowsIoException_doesNotPropagateAndSkipsProcessing() throws IOException {
        setEtCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any()))
            .thenThrow(new IOException("ES unavailable"));

        caseAcceptanceDateTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_etCaseType_startEventThrowsException_logsWarnAndContinues() throws IOException {
        setEtCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        SubmitEvent submitEvent = buildEtSubmitEvent();

        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any()))
            .thenReturn(List.of(submitEvent));
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("CCD error"));

        caseAcceptanceDateTask.run();

        verify(ccdClient, times(1)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_unknownCaseType_neitherClientCalled() throws IOException {
        setEtCaseTypeId("UnknownCaseType");

        caseAcceptanceDateTask.run();

        verify(ccdClient, never()).buildAndGetElasticSearchRequest(any(), any(), any());
        verify(ecmCcdClient, never()).buildAndGetElasticSearchRequest(any(), any(), any());
    }

    @Test
    void run_ecmCaseType_fetchesAndUpdatesCase() throws IOException {
        setEtCaseTypeId(LEEDS_CASE_TYPE_ID);
        uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent submitEvent = buildEcmSubmitEvent();
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest =
            buildEcmCcdRequest(submitEvent.getCaseData());

        when(ecmCcdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(LEEDS_CASE_TYPE_ID), any()))
            .thenReturn(List.of(submitEvent));
        when(ecmCcdClient.startEventForCase(any(), eq(LEEDS_CASE_TYPE_ID), eq(EMPLOYMENT),
            eq(CASE_ID), any()))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ecmCcdClient, times(1)).startEventForCase(ADMIN_TOKEN, LEEDS_CASE_TYPE_ID,
            EMPLOYMENT, CASE_ID, "fixCaseAPI");
        verify(ecmCcdClient, times(1)).submitEventForCase(eq(ADMIN_TOKEN),
            any(uk.gov.hmcts.ecm.common.model.ccd.CaseData.class),
            eq(LEEDS_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
    }

    @Test
    void run_ecmCaseType_emptyCaseList_doesNotTriggerEvent() throws IOException {
        setEtCaseTypeId(LEEDS_CASE_TYPE_ID);
        when(ecmCcdClient.buildAndGetElasticSearchRequest(any(), any(), any()))
            .thenReturn(Collections.emptyList());

        caseAcceptanceDateTask.run();

        verify(ecmCcdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ecmCcdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_ecmCaseType_fetchThrowsIoException_doesNotPropagateAndSkipsProcessing() throws IOException {
        setEtCaseTypeId(LEEDS_CASE_TYPE_ID);
        when(ecmCcdClient.buildAndGetElasticSearchRequest(any(), any(), any()))
            .thenThrow(new IOException("ES unavailable"));

        caseAcceptanceDateTask.run();

        verify(ecmCcdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }

    // --- clearEcmPreAcceptanceDates ---

    @Test
    void run_ecmCaseType_accepted_clearsDateRejected() throws IOException {
        setEtCaseTypeId(LEEDS_CASE_TYPE_ID);

        uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType preAccept =
            new uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType();
        preAccept.setCaseAccepted(YES);
        preAccept.setDateAccepted("2024-01-01");
        preAccept.setDateRejected("2024-01-02");
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData =
            new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        caseData.setPreAcceptCase(preAccept);

        uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent submitEvent = buildEcmSubmitEvent(caseData);
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest = buildEcmCcdRequest(caseData);

        when(ecmCcdClient.buildAndGetElasticSearchRequest(any(), any(), any()))
            .thenReturn(List.of(submitEvent));
        when(ecmCcdClient.startEventForCase(any(), any(), any(), any(), any()))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ecmCcdClient, times(1)).submitEventForCase(any(), ecmCaseDataCaptor.capture(),
            any(), any(), any(), any());
        assertNull(ecmCaseDataCaptor.getValue().getPreAcceptCase().getDateRejected());
    }

    @Test
    void run_ecmCaseType_rejected_clearsDateAccepted() throws IOException {
        setEtCaseTypeId(LEEDS_CASE_TYPE_ID);

        uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType preAccept =
            new uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType();
        preAccept.setCaseAccepted(NO);
        preAccept.setDateAccepted("2024-01-01");
        preAccept.setDateRejected("2024-01-02");
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData =
            new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        caseData.setPreAcceptCase(preAccept);

        uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent submitEvent = buildEcmSubmitEvent(caseData);
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest = buildEcmCcdRequest(caseData);

        when(ecmCcdClient.buildAndGetElasticSearchRequest(any(), any(), any()))
            .thenReturn(List.of(submitEvent));
        when(ecmCcdClient.startEventForCase(any(), any(), any(), any(), any()))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ecmCcdClient, times(1)).submitEventForCase(any(), ecmCaseDataCaptor.capture(),
            any(), any(), any(), any());
        assertNull(ecmCaseDataCaptor.getValue().getPreAcceptCase().getDateAccepted());
    }
    
    @Test
    void run_mixedCaseTypes_processesBothEtAndEcm() throws IOException {
        ReflectionTestUtils.setField(caseAcceptanceDateTask, "caseTypeIdsString",
            ENGLANDWALES_CASE_TYPE_ID + "," + LEEDS_CASE_TYPE_ID);

        SubmitEvent etSubmitEvent = buildEtSubmitEvent();
        CCDRequest etCcdRequest = buildEtCcdRequest(etSubmitEvent.getCaseData());
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
            .thenReturn(List.of(etSubmitEvent));
        when(ccdClient.startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any(), any(), any()))
            .thenReturn(etCcdRequest);

        uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent ecmSubmitEvent = buildEcmSubmitEvent();
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ecmCcdRequest =
            buildEcmCcdRequest(ecmSubmitEvent.getCaseData());
        when(ecmCcdClient.buildAndGetElasticSearchRequest(any(), eq(LEEDS_CASE_TYPE_ID), any()))
            .thenReturn(List.of(ecmSubmitEvent));
        when(ecmCcdClient.startEventForCase(any(), eq(LEEDS_CASE_TYPE_ID), any(), any(), any()))
            .thenReturn(ecmCcdRequest);

        caseAcceptanceDateTask.run();

        verify(ccdClient, times(1)).submitEventForCase(any(), any(), eq(ENGLANDWALES_CASE_TYPE_ID),
            any(), any(), any());
        verify(ecmCcdClient, times(1)).submitEventForCase(any(), any(), eq(LEEDS_CASE_TYPE_ID),
            any(), any(), any());
    }
    
    @Test
    void run_etCaseType_exceedsMaxCasesToProcess_limitsProcessedCases() throws IOException {
        setEtCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        ReflectionTestUtils.setField(caseAcceptanceDateTask, "maxCasesToProcess", 2);

        List<SubmitEvent> fiveCases = List.of(
            buildEtSubmitEventWithId(1L),
            buildEtSubmitEventWithId(2L),
            buildEtSubmitEventWithId(3L),
            buildEtSubmitEventWithId(4L),
            buildEtSubmitEventWithId(5L)
        );
        CCDRequest ccdRequest = buildEtCcdRequest(new CaseData());

        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
            .thenReturn(fiveCases);
        when(ccdClient.startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(), any()))
            .thenReturn(ccdRequest);

        caseAcceptanceDateTask.run();

        verify(ccdClient, times(2)).startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID),
            eq(EMPLOYMENT), any(), any());
        verify(ccdClient, times(2)).submitEventForCase(any(), any(CaseData.class),
            eq(ENGLANDWALES_CASE_TYPE_ID), any(), any(), any());
    }

    private void setEtCaseTypeId(String caseTypeId) {
        ReflectionTestUtils.setField(caseAcceptanceDateTask, "caseTypeIdsString", caseTypeId);
    }

    private SubmitEvent buildEtSubmitEvent() {
        return buildEtSubmitEventWithId(Long.parseLong(CASE_ID));
    }

    private SubmitEvent buildEtSubmitEventWithId(long id) {
        CasePreAcceptType preAccept = new CasePreAcceptType();
        preAccept.setCaseAccepted(YES);
        preAccept.setDateAccepted("2024-01-01");
        preAccept.setDateRejected("2024-01-02");
        CaseData caseData = new CaseData();
        caseData.setPreAcceptCase(preAccept);

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(id);
        submitEvent.setCaseData(caseData);
        return submitEvent;
    }

    private CCDRequest buildEtCcdRequest(CaseData caseData) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setJurisdiction(EMPLOYMENT);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }

    private uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent buildEcmSubmitEvent() {
        return buildEcmSubmitEvent(new uk.gov.hmcts.ecm.common.model.ccd.CaseData());
    }

    private uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent buildEcmSubmitEvent(
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData) {
        uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent submitEvent =
            new uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent();
        submitEvent.setCaseId(Long.parseLong(CASE_ID));
        submitEvent.setCaseData(caseData);
        return submitEvent;
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
