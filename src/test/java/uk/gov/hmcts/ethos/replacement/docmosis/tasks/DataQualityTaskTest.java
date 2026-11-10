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
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class DataQualityTaskTest {

    private static final String ADMIN_TOKEN = "AdminToken";
    private static final String CASE_ID = "1234567890123456";
    private static final String CASE_ID_2 = "9876543210987654";
    private static final String ET_CASE_TYPE_ID = "ET_EnglandWales";
    private static final String ECM_CASE_TYPE_ID = "Manchester";
    private static final String FIX_CASE_API_EVENT_ID = "fixCaseAPI";
    private static final String ACCEPTED_DATE = "2025-04-10";
    private static final String REJECTED_DATE = "2025-04-12";

    private DataQualityTask dataQualityTask;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private CcdClient ccdClient;

    @MockitoBean
    private uk.gov.hmcts.ecm.compat.common.client.CcdClient ecmCcdClient;

    @MockitoBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<CaseData> etCaseDataCaptor;

    @Captor
    private ArgumentCaptor<uk.gov.hmcts.ecm.common.model.ccd.CaseData> ecmCaseDataCaptor;

    @BeforeEach
    void setUp() {
        dataQualityTask = new DataQualityTask(adminUserService, ccdClient, ecmCcdClient, coreCaseDataApi,
            authTokenGenerator, emailService);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        ReflectionTestUtils.setField(dataQualityTask, "serviceOwnerEmail", "service.owner@example.com");
        ReflectionTestUtils.setField(dataQualityTask, "dataQualityReportTemplateId", "report-template-id");
        setEtCase();
    }

    @Test
    void run_doesNothing_whenCasesToUpdateIsEmptyOrNull() {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", null);
        dataQualityTask.run();
        verify(coreCaseDataApi, never()).getCase(any(), any(), any());

        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", "");
        dataQualityTask.run();
        verify(coreCaseDataApi, never()).getCase(any(), any(), any());

        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", " , ");
        dataQualityTask.run();
        verify(coreCaseDataApi, never()).getCase(any(), any(), any());
    }

    @Test
    void processCaseUpdate_skipsCase_whenUnknownCaseType() throws Exception {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", CASE_ID);
        mockGetCase(CASE_ID, "UnknownCaseTypeId");

        dataQualityTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ecmCcdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void processCaseUpdate_recordsFailure_whenCoreCaseDataApiThrows() throws Exception {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", CASE_ID);
        when(coreCaseDataApi.getCase(eq(ADMIN_TOKEN), any(), eq(CASE_ID)))
            .thenThrow(new RuntimeException("CCD Get Case Error"));

        dataQualityTask.run();

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(emailService).sendEmail(eq("report-template-id"), eq("service.owner@example.com"), any());
    }

    @Test
    void processCaseUpdate_recordsFailure_whenCcdClientThrows() throws Exception {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", CASE_ID);
        mockGetCase(CASE_ID, ET_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ET_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API_EVENT_ID))
            .thenThrow(new IOException("CCD Start Event Error"));

        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
        verify(emailService).sendEmail(eq("report-template-id"), eq("service.owner@example.com"), any());
    }

    @Test
    void updatePreAcceptCase_et_setsCaseAcceptedNoAndCopiesDateAcceptedToDateRejected() throws Exception {
        CaseData caseData = buildEtCaseData(YES, ACCEPTED_DATE, null, null);
        caseData.setStateAPI("Accepted");
        mockEtStartEvent(ET_CASE_TYPE_ID, CASE_ID, caseData);

        dataQualityTask.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), etCaseDataCaptor.capture(),
            eq(ET_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
        CaseData submittedData = etCaseDataCaptor.getValue();
        assertNull(submittedData.getStateAPI());
        assertEquals(NO, submittedData.getPreAcceptCase().getCaseAccepted());
        assertEquals(ACCEPTED_DATE, submittedData.getPreAcceptCase().getDateRejected());
        assertNull(submittedData.getPreAcceptCase().getDateAccepted());
        assertEquals(List.of(DataQualityTask.REJECT_REASON_CASE_INPUT_IN_ERROR),
            submittedData.getPreAcceptCase().getRejectReason());
    }

    @Test
    void updatePreAcceptCase_etScotland_updatesCase() throws Exception {
        mockGetCase(CASE_ID, SCOTLAND_CASE_TYPE_ID);
        CaseData caseData = buildEtCaseData(YES, ACCEPTED_DATE, null, null);
        mockEtStartEvent(SCOTLAND_CASE_TYPE_ID, CASE_ID, caseData);

        dataQualityTask.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), etCaseDataCaptor.capture(),
            eq(SCOTLAND_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
        assertEquals(NO, etCaseDataCaptor.getValue().getPreAcceptCase().getCaseAccepted());
        assertEquals(ACCEPTED_DATE, etCaseDataCaptor.getValue().getPreAcceptCase().getDateRejected());
        assertEquals(List.of(DataQualityTask.REJECT_REASON_CASE_INPUT_IN_ERROR),
            etCaseDataCaptor.getValue().getPreAcceptCase().getRejectReason());
    }

    @Test
    void updatePreAcceptCase_ecm_setsCaseAcceptedNoAndCopiesDateAcceptedToDateRejected() throws Exception {
        setEcmCase();
        uk.gov.hmcts.ecm.common.model.ccd.CaseData ecmCaseData = buildEcmCaseData(YES, ACCEPTED_DATE, null, null);
        ecmCaseData.setStateAPI("Accepted");
        when(ecmCcdClient.startEventForCase(ADMIN_TOKEN, ECM_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API_EVENT_ID))
            .thenReturn(buildEcmCcdRequest(ecmCaseData));

        dataQualityTask.run();

        verify(ecmCcdClient).submitEventForCase(eq(ADMIN_TOKEN), ecmCaseDataCaptor.capture(),
            eq(ECM_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
        uk.gov.hmcts.ecm.common.model.ccd.CaseData submittedData = ecmCaseDataCaptor.getValue();
        assertNull(submittedData.getStateAPI());
        assertEquals(NO, submittedData.getPreAcceptCase().getCaseAccepted());
        assertEquals(ACCEPTED_DATE, submittedData.getPreAcceptCase().getDateRejected());
        assertNull(submittedData.getPreAcceptCase().getDateAccepted());
        assertEquals(List.of(DataQualityTask.REJECT_REASON_CASE_INPUT_IN_ERROR),
            submittedData.getPreAcceptCase().getRejectReason());
    }

    @Test
    void updatePreAcceptCase_doesNotSubmit_whenPreAcceptCaseIsNull() throws Exception {
        CaseData caseData = new CaseData();
        mockEtStartEvent(ET_CASE_TYPE_ID, CASE_ID, caseData);

        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void updatePreAcceptCase_doesNotSubmit_whenAlreadyRejectedWithCaseInputInError() throws Exception {
        CaseData caseData = buildEtCaseData(
            NO, null, REJECTED_DATE, List.of(DataQualityTask.REJECT_REASON_CASE_INPUT_IN_ERROR));
        mockEtStartEvent(ET_CASE_TYPE_ID, CASE_ID, caseData);

        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void updatePreAcceptCase_preservesExistingDateRejected_whenDateAcceptedIsNull() throws Exception {
        CaseData caseData = buildEtCaseData(YES, null, REJECTED_DATE, null);
        mockEtStartEvent(ET_CASE_TYPE_ID, CASE_ID, caseData);

        dataQualityTask.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), etCaseDataCaptor.capture(),
            eq(ET_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
        assertEquals(REJECTED_DATE, etCaseDataCaptor.getValue().getPreAcceptCase().getDateRejected());
        assertEquals(NO, etCaseDataCaptor.getValue().getPreAcceptCase().getCaseAccepted());
        assertEquals(List.of(DataQualityTask.REJECT_REASON_CASE_INPUT_IN_ERROR),
            etCaseDataCaptor.getValue().getPreAcceptCase().getRejectReason());
    }

    @Test
    void run_processesBothEtAndEcmCasesInSameBatch() throws Exception {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", CASE_ID + "," + CASE_ID_2);
        mockGetCase(CASE_ID, ET_CASE_TYPE_ID);
        mockGetCase(CASE_ID_2, ECM_CASE_TYPE_ID);

        mockEtStartEvent(ET_CASE_TYPE_ID, CASE_ID, buildEtCaseData(YES, ACCEPTED_DATE, null, null));
        when(ecmCcdClient.startEventForCase(
            ADMIN_TOKEN, ECM_CASE_TYPE_ID, EMPLOYMENT, CASE_ID_2, FIX_CASE_API_EVENT_ID))
            .thenReturn(buildEcmCcdRequest(buildEcmCaseData(YES, ACCEPTED_DATE, null, null)));

        dataQualityTask.run();

        verify(ccdClient).submitEventForCase(
            eq(ADMIN_TOKEN), any(CaseData.class), eq(ET_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
        verify(ecmCcdClient).submitEventForCase(
            eq(ADMIN_TOKEN), any(uk.gov.hmcts.ecm.common.model.ccd.CaseData.class),
            eq(ECM_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID_2));
    }

    @Test
    void dryRun_doesNotSubmitButUploadsAndEmailsReport() throws Exception {
        CaseData caseData = buildEtCaseData(YES, ACCEPTED_DATE, null, null);
        mockEtStartEvent(ET_CASE_TYPE_ID, CASE_ID, caseData);
        ReflectionTestUtils.setField(dataQualityTask, "dryRun", true);

        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
        verify(emailService).sendEmail(eq("report-template-id"), eq("service.owner@example.com"), any());
    }

    @Test
    void liveRun_submitsCorrectionAndEmailsReport() throws Exception {
        CaseData caseData = buildEtCaseData(YES, ACCEPTED_DATE, null, null);
        mockEtStartEvent(ET_CASE_TYPE_ID, CASE_ID, caseData);

        dataQualityTask.run();

        verify(ccdClient).submitEventForCase(any(), any(), any(), any(), any(), any());
        verify(emailService).sendEmail(eq("report-template-id"), eq("service.owner@example.com"), any());
    }

    private void setEtCase() {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", CASE_ID);
        mockGetCase(CASE_ID, ET_CASE_TYPE_ID);
    }

    private void setEcmCase() {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", CASE_ID);
        mockGetCase(CASE_ID, ECM_CASE_TYPE_ID);
    }

    private void mockGetCase(String caseId, String caseTypeId) {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails reformCaseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().caseTypeId(caseTypeId).build();
        when(coreCaseDataApi.getCase(eq(ADMIN_TOKEN), any(), eq(caseId)))
            .thenReturn(reformCaseDetails);
    }

    private void mockEtStartEvent(String caseTypeId, String caseId, CaseData caseData) throws IOException {
        when(ccdClient.startEventForCase(ADMIN_TOKEN, caseTypeId, EMPLOYMENT, caseId, FIX_CASE_API_EVENT_ID))
            .thenReturn(buildEtCcdRequest(caseData));
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

    private CaseData buildEtCaseData(String caseAccepted, String dateAccepted,
                                     String dateRejected, List<String> rejectReason) {
        CasePreAcceptType preAccept = new CasePreAcceptType();
        preAccept.setCaseAccepted(caseAccepted);
        preAccept.setDateAccepted(dateAccepted);
        preAccept.setDateRejected(dateRejected);
        preAccept.setRejectReason(rejectReason);
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("6000001/2025");
        caseData.setPreAcceptCase(preAccept);
        return caseData;
    }

    private uk.gov.hmcts.ecm.common.model.ccd.CaseData buildEcmCaseData(String caseAccepted, String dateAccepted,
                                                                        String dateRejected,
                                                                        List<String> rejectReason) {
        uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType preAccept =
            new uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType();
        preAccept.setCaseAccepted(caseAccepted);
        preAccept.setDateAccepted(dateAccepted);
        preAccept.setDateRejected(dateRejected);
        preAccept.setRejectReason(rejectReason);
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData =
            new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        caseData.setEthosCaseReference("1800001/2025");
        caseData.setPreAcceptCase(preAccept);
        return caseData;
    }
}
