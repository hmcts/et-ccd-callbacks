package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(MockitoExtension.class)
class BatchReconfigurationTaskTest {

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;

    private BatchReconfigurationTask task;

    private static final String TOKEN = "authToken";
    private static final String RECONFIGURE_EVENT = "RECONFIGURE_WA_TASKS";
    private static final String CASE_ID_1 = "1111111111111111";
    private static final String CASE_ID_2 = "2222222222222222";
    private static final String CASE_ID_3 = "3333333333333333";

    @BeforeEach
    void setUp() {
        task = new BatchReconfigurationTask(adminUserService, ccdClient);
        ReflectionTestUtils.setField(task, "caseTypeIdsString", ENGLANDWALES_CASE_TYPE_ID);
        ReflectionTestUtils.setField(
            task,
            "caseIdsToReconfigure",
            String.join(",", CASE_ID_1, CASE_ID_2, CASE_ID_3)
        );
        ReflectionTestUtils.setField(task, "limit", 2);

        lenient().when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
    }

    @Test
    void run_triggersReconfigureEvent_forFirstNCaseIds() throws Exception {
        // given
        CCDRequest ccdRequest = buildCcdRequestMock();
        when(ccdClient.startEventForCase(
                eq(TOKEN),
                eq(ENGLANDWALES_CASE_TYPE_ID),
                eq(EMPLOYMENT),
                anyString(),
                eq(RECONFIGURE_EVENT))
        ).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(
                eq(TOKEN),
                any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID),
                anyString(),
                eq(ccdRequest),
                anyString())
        ).thenReturn(mock(SubmitEvent.class));

        // when
        task.run();

        // then
        verify(adminUserService, times(1)).getAdminUserToken();

        // capture the processed case ids
        ArgumentCaptor<String> caseIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(ccdClient, times(2))
            .startEventForCase(
                eq(TOKEN),
                eq(ENGLANDWALES_CASE_TYPE_ID),
                eq(EMPLOYMENT),
                caseIdCaptor.capture(),
                eq(RECONFIGURE_EVENT));

        List<String> processedCases = caseIdCaptor.getAllValues();
        assertThat(processedCases).contains(CASE_ID_1, CASE_ID_2);

        // ensure submit was called for each processed case id
        ArgumentCaptor<String> submittedCaseId = ArgumentCaptor.forClass(String.class);
        verify(ccdClient, times(2))
            .submitEventForCase(
                eq(TOKEN),
                any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID),
                anyString(),
                eq(ccdRequest),
                submittedCaseId.capture());
        assertThat(submittedCaseId.getAllValues()).contains(CASE_ID_1, CASE_ID_2);

    }

    @Test
    void run_withZeroLimit_callsNoCcdClientMethods() {
        // given
        ReflectionTestUtils.setField(task, "limit", 0);

        // when
        task.run();

        // then
        verify(adminUserService, times(1)).getAdminUserToken();
        verifyNoInteractions(ccdClient);
    }

    @Test
    void run_withNullCaseIds_noInteractions() {
        // given
        ReflectionTestUtils.setField(task, "caseIdsToReconfigure", null);

        // when
        task.run();

        // then
        verifyNoInteractions(adminUserService, ccdClient);
    }

    @Test
    void run_withEmptyCaseIds_noInteractions() {
        // given
        ReflectionTestUtils.setField(task, "caseIdsToReconfigure", "");

        // when
        task.run();

        // then
        verifyNoInteractions(adminUserService, ccdClient);
    }

    @Test
    void run_continuesWhenStartEventThrows_forOneCase() throws Exception {
        // given
        ReflectionTestUtils.setField(task, "caseIdsToReconfigure", String.join(",", CASE_ID_1, CASE_ID_2));
        ReflectionTestUtils.setField(task, "limit", 2);

        // first case throws, second succeeds
        when(ccdClient.startEventForCase(
                TOKEN,
                ENGLANDWALES_CASE_TYPE_ID,
                EMPLOYMENT,
                CASE_ID_1,
                RECONFIGURE_EVENT)
        ).thenThrow(new IOException("error"));
        CCDRequest okReq = buildCcdRequestMock();
        when(ccdClient.startEventForCase(
                TOKEN,
                ENGLANDWALES_CASE_TYPE_ID,
                EMPLOYMENT,
                CASE_ID_2,
                RECONFIGURE_EVENT)
        ).thenReturn(okReq);
        when(ccdClient.submitEventForCase(
                eq(TOKEN),
                any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID),
                anyString(),
                eq(okReq),
                eq(CASE_ID_2))
        ).thenReturn(mock(SubmitEvent.class));

        // when
        task.run();

        // then
        verify(ccdClient, times(1)).submitEventForCase(
            eq(TOKEN),
            any(CaseData.class),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            anyString(),
            eq(okReq),
            eq(CASE_ID_2));
        // ensure submit was NOT called for the failing case
        verify(ccdClient, never()).submitEventForCase(
            eq(TOKEN),
            any(CaseData.class),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            anyString(),
            any(CCDRequest.class),
            eq(CASE_ID_1));
    }

    @Test
    void run_continuesWhenSubmitEventThrows_forOneCase() throws Exception {
        // given
        ReflectionTestUtils.setField(task, "caseIdsToReconfigure", String.join(",", CASE_ID_1, CASE_ID_2));
        ReflectionTestUtils.setField(task, "limit", 2);

        CCDRequest ccdRequestCase1 = buildCcdRequestMock();
        CCDRequest ccdRequestCase2 = buildCcdRequestMock();

        when(ccdClient.startEventForCase(
                TOKEN,
                ENGLANDWALES_CASE_TYPE_ID,
                EMPLOYMENT,
                CASE_ID_1,
                RECONFIGURE_EVENT)
        ).thenReturn(ccdRequestCase1);
        when(ccdClient.startEventForCase(
                TOKEN,
                ENGLANDWALES_CASE_TYPE_ID,
                EMPLOYMENT,
                CASE_ID_2,
                RECONFIGURE_EVENT)
        ).thenReturn(ccdRequestCase2);

        // first submit throws, second succeeds
        when(ccdClient.submitEventForCase(
                eq(TOKEN),
                any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID),
                anyString(),
                eq(ccdRequestCase1),
                eq(CASE_ID_1))
        ).thenThrow(new IOException("submit failed"));
        when(ccdClient.submitEventForCase(
                eq(TOKEN),
                any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID),
                anyString(),
                eq(ccdRequestCase2),
                eq(CASE_ID_2))
        ).thenReturn(mock(SubmitEvent.class));

        // when
        task.run();

        // then - verify both start calls happened and the second case submit happened
        verify(ccdClient).startEventForCase(
            TOKEN,
            ENGLANDWALES_CASE_TYPE_ID,
            EMPLOYMENT,
            CASE_ID_1,
            RECONFIGURE_EVENT);
        verify(ccdClient).startEventForCase(
            TOKEN,
            ENGLANDWALES_CASE_TYPE_ID,
            EMPLOYMENT,
            CASE_ID_2,
            RECONFIGURE_EVENT);
        verify(ccdClient, times(1)).submitEventForCase(
            eq(TOKEN),
            any(CaseData.class),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            anyString(),
            eq(ccdRequestCase2),
            eq(CASE_ID_2));
    }

    private CCDRequest buildCcdRequestMock() {
        CaseData caseData = mock(CaseData.class);
        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseData()).thenReturn(caseData);
        when(caseDetails.getJurisdiction()).thenReturn(EMPLOYMENT);

        CCDRequest ccdRequest = mock(CCDRequest.class);
        when(ccdRequest.getCaseDetails()).thenReturn(caseDetails);
        return ccdRequest;
    }
}
