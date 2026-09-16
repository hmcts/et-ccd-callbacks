package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.SupportTaskState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_REVIEW_SUPPORT_TASKS;

@ExtendWith(MockitoExtension.class)
class SupportTaskEventServiceTest {
    private static final String ADMIN_TOKEN = "admin-token";
    private static final String CASE_ID = "1234567890123456";
    private static final String CASE_TYPE_ID = "ET_Scotland";

    @Mock
    private CcdClient ccdClient;
    @Mock
    private AdminUserService adminUserService;

    private SupportTaskEventService service;

    @BeforeEach
    void setUp() {
        service = new SupportTaskEventService(ccdClient, adminUserService);
    }

    @Test
    void startsAndSubmitsDedicatedTaskClosureEventUsingLatestCaseData() throws Exception {
        CaseData completedCaseData = new CaseData();
        completedCaseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(NO)
                .legalOfficerTaskCreated(YES)
                .judgeTaskCreated(YES)
                .build());
        CaseDetails completedDetails = new CaseDetails();
        completedDetails.setCaseId(CASE_ID);
        completedDetails.setCaseTypeId(CASE_TYPE_ID);
        completedDetails.setCaseData(completedCaseData);

        CaseData latestCaseData = new CaseData();
        latestCaseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(YES)
                .adminTaskRequired(YES)
                .legalOfficerTaskCreated(YES)
                .legalOfficerTaskRequired(YES)
                .judgeTaskCreated(YES)
                .judgeTaskRequired(YES)
                .build());
        CaseDetails latestDetails = new CaseDetails();
        latestDetails.setCaseId(CASE_ID);
        latestDetails.setCaseTypeId(CASE_TYPE_ID);
        latestDetails.setJurisdiction(EMPLOYMENT);
        latestDetails.setCaseData(latestCaseData);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(latestDetails);

        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(ccdClient.startEventForCase(
                ADMIN_TOKEN, CASE_TYPE_ID, EMPLOYMENT, CASE_ID, EVENT_CLOSE_REVIEW_SUPPORT_TASKS))
                .thenReturn(ccdRequest);

        service.triggerCloseReviewSupportTasks(completedDetails);

        assertEquals(NO, latestCaseData.getSupportTaskState().getAdminTaskRequired());
        assertEquals(YES, latestCaseData.getSupportTaskState().getLegalOfficerTaskRequired());
        assertEquals(YES, latestCaseData.getSupportTaskState().getJudgeTaskRequired());
        verify(ccdClient).submitEventForCase(
                ADMIN_TOKEN, latestCaseData, CASE_TYPE_ID, EMPLOYMENT, ccdRequest, CASE_ID);
    }
}
