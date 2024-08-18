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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CaseLinkUpdatesTaskTest {
    private NoticeOfChangeFieldsTask noticeOfChangeFieldsTask;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;
    @InjectMocks
    private CaseLinkUpdatesTask caseLinkUpdatesTask;

    private SubmitEvent targetSubmitEvent;
    private SubmitEvent duplicateEvent1;
    private SubmitEvent duplicateEvent2;
    private String adminUserToken = "admin-token";
    private String targetCaseTypeId = "TargetCaseType";
    private String sourceCaseTypeId = "SourceCaseType";
    private static final String EVENT_ID = "migrateCaseLinkDetails";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set the caseTypeIdsString for the service
        ReflectionTestUtils.setField(caseLinkUpdatesTask, "caseTypeIdsString", "type1,type2");

        targetSubmitEvent = new SubmitEvent();
        targetSubmitEvent.setCaseId(1L);
        CaseData targetCaseData = new CaseData();
        targetCaseData.setCcdID("ccd1");
        targetSubmitEvent.setCaseData(targetCaseData);

        duplicateEvent1 = new SubmitEvent();
        duplicateEvent1.setCaseId(2L);
        CaseData duplicateCaseData1 = new CaseData();
        duplicateCaseData1.setCcdID("ccd2");
        duplicateEvent1.setCaseData(duplicateCaseData1);

        duplicateEvent2 = new SubmitEvent();
        duplicateEvent2.setCaseId(3L);
        CaseData duplicateCaseData2 = new CaseData();
        duplicateCaseData2.setCcdID("ccd3");
        duplicateEvent2.setCaseData(duplicateCaseData2);
    }

    @Test
    void testUpdateTransferredCaseLinks_FeatureDisabled() {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(false);

        caseLinkUpdatesTask.updateTransferredCaseLinks();

        verify(featureToggleService).isUpdateTransferredCaseLinksEnabled();
        verifyNoMoreInteractions(featureToggleService, adminUserService, ccdClient);
    }

    @Test
    void testUpdateTransferredCaseLinks_NoTransferredCases() throws Exception {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        caseLinkUpdatesTask.updateTransferredCaseLinks();

        verify(featureToggleService).isUpdateTransferredCaseLinksEnabled();
        verify(adminUserService).getAdminUserToken();
        verify(ccdClient, times(2))
                .buildAndGetElasticSearchRequest(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void testUpdateTransferredCaseLinks_WithTransferredCases() throws Exception {
        when(featureToggleService.isUpdateTransferredCaseLinksEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");

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
        var coll = Collections.singletonList(Pair.of("type1", duplicates));
        when(caseLinkUpdatesTask.findCaseByEthosReference("token", "testEthosRef"))
                .thenReturn(coll);

        caseLinkUpdatesTask.updateTransferredCaseLinks();

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
        when(caseLinkUpdatesTask.findCaseByEthosReference("token", "testEthosRef"))
                .thenReturn(coll);

        caseLinkUpdatesTask.updateTransferredCaseLinks();

        verify(ccdClient, times(7)).buildAndGetElasticSearchRequest(
                anyString(), anyString(), anyString());
    }
}
