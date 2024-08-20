package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MigratedCaseLinkUpdatesTaskTest {
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;
    @InjectMocks
    private MigratedCaseLinkUpdatesTask migratedCaseLinkUpdatesTask;
    private static final String ADMIN_TOKEN = "adminToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set the caseTypeIdsString for the service
        ReflectionTestUtils.setField(migratedCaseLinkUpdatesTask, "caseTypeIdsString", "type1,type2");
    }

    @Test
    void testUpdateTransferredCaseLinks_FeatureDisabled() {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(false);

        migratedCaseLinkUpdatesTask.updateTransferredCaseLinks();

        verify(featureToggleService).isUpdateTransferredCaseLinksEnabled();
        verifyNoMoreInteractions(featureToggleService, adminUserService, ccdClient);
    }

    @Test
    void testUpdateTransferredCaseLinks_NoTransferredCases() throws Exception {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        migratedCaseLinkUpdatesTask.updateTransferredCaseLinks();

        verify(featureToggleService).isUpdateTransferredCaseLinksEnabled();
        verify(adminUserService).getAdminUserToken();
        verify(ccdClient, times(2))
                .buildAndGetElasticSearchRequest(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void testUpdateTransferredCaseLinks_WithTransferredCases() throws Exception {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("caseRef1");
        caseData.setClaimant("claimant1");
        caseData.setRespondent("respondent1");
        caseData.setReceiptDate("2021-01-01");
        caseData.setFeeGroupReference("feeGroup1");
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);

        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any()))
                .thenReturn(Collections.singletonList(submitEvent));

        List<SubmitEvent> duplicates = new ArrayList<>();
        duplicates.add(submitEvent);
        duplicates.add(submitEvent);
        List<Pair<String, List<SubmitEvent>>> coll = Collections.singletonList(Pair.of("type1", duplicates));
        when(migratedCaseLinkUpdatesTask.findCaseByEthosReference(ADMIN_TOKEN, "testEthosRef"))
                .thenReturn(coll);

        migratedCaseLinkUpdatesTask.updateTransferredCaseLinks();

        verify(featureToggleService).isUpdateTransferredCaseLinksEnabled();
        verify(adminUserService).getAdminUserToken();
        verify(ccdClient, times(7))
                .buildAndGetElasticSearchRequest(any(), any(), any());
    }

    @Test
    void testUpdateTransferredCaseLinks_WithTransferredCasesAndDuplicates() throws Exception {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        SubmitEvent transferredCase = new SubmitEvent();
        transferredCase.setCaseData(new CaseData());
        transferredCase.getCaseData().setEthosCaseReference("ETHOS1231");
        transferredCase.getCaseData().setEthosCaseReference("caseRef11");
        transferredCase.getCaseData().setClaimant("claimant11");
        transferredCase.getCaseData().setRespondent("respondent11");
        transferredCase.getCaseData().setReceiptDate("2021-11-01");
        transferredCase.getCaseData().setFeeGroupReference("feeGroup11");
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
                .thenReturn(Collections.singletonList(transferredCase));

        List<SubmitEvent> duplicates = new ArrayList<>();
        duplicates.add(transferredCase);
        duplicates.add(transferredCase);
        var coll = Collections.singletonList(Pair.of("type1", duplicates));
        when(migratedCaseLinkUpdatesTask.findCaseByEthosReference(ADMIN_TOKEN, "testEthosRef"))
                .thenReturn(coll);

        migratedCaseLinkUpdatesTask.updateTransferredCaseLinks();

        verify(ccdClient, times(7)).buildAndGetElasticSearchRequest(
                anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateTransferredCaseLinks_WithTransferredCasesAndNotDuplicates() throws Exception {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        SubmitEvent transferredCase1 = new SubmitEvent();
        transferredCase1.setCaseData(new CaseData());
        transferredCase1.getCaseData().setEthosCaseReference("ETHOS1231");
        transferredCase1.getCaseData().setEthosCaseReference("caseRef11");
        transferredCase1.getCaseData().setClaimant("claimant11");
        transferredCase1.getCaseData().setRespondent("respondent11");
        transferredCase1.getCaseData().setReceiptDate("2021-11-01");
        transferredCase1.getCaseData().setFeeGroupReference("feeGroup11");

        SubmitEvent transferredCase2 = new SubmitEvent();
        transferredCase2.setCaseData(new CaseData());
        transferredCase2.getCaseData().setEthosCaseReference("ETHOS1231");
        transferredCase2.getCaseData().setEthosCaseReference("caseRef435");
        transferredCase2.getCaseData().setClaimant("claimant345");
        transferredCase2.getCaseData().setRespondent("respondent345");
        transferredCase2.getCaseData().setReceiptDate("2022-12-02");
        transferredCase2.getCaseData().setFeeGroupReference("feeGroup345");
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
                .thenReturn(List.of(transferredCase1, transferredCase2));

        List<SubmitEvent> notMatchedDuplicates = new ArrayList<>();
        notMatchedDuplicates.add(transferredCase1);
        notMatchedDuplicates.add(transferredCase2);
        when(migratedCaseLinkUpdatesTask.findCaseByEthosReference(ADMIN_TOKEN, "ETHOS1231"))
                .thenReturn(List.of(Pair.of("type1", notMatchedDuplicates)));

        migratedCaseLinkUpdatesTask.updateTransferredCaseLinks();
        // 11 invocations of buildAndGetElasticSearchRequest using ccdClient
        // because the method calls were made covering two case types(EW, SC), and two transferred cases during
        // run for each case type
        verify(ccdClient, times(11)).buildAndGetElasticSearchRequest(
                anyString(), anyString(), anyString());
        verify(ccdClient, times(0)).startEventForCase(
                anyString(), anyString(), anyString(), anyString(), anyString());
        verify(ccdClient, times(0)).submitEventForCase(
                anyString(), any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void shouldHandleExceptionDuringProcessing() throws Exception {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Test Exception"));

        migratedCaseLinkUpdatesTask.updateTransferredCaseLinks();

        verify(featureToggleService).isUpdateTransferredCaseLinksEnabled();
        verify(adminUserService).getAdminUserToken();
        verify(ccdClient, times(2)).buildAndGetElasticSearchRequest(
                anyString(), anyString(), anyString());
        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(),
                anyString(), anyString());
        verify(ccdClient, never()).submitEventForCase(anyString(), any(), anyString(),
                anyString(), any(), anyString());
    }
}
