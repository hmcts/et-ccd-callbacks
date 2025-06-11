package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentPolicyConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class NoticeOfChangeFieldsTaskTest {
    private NoticeOfChangeFieldsTask noticeOfChangeFieldsTask;
    @MockBean
    private AdminUserService adminUserService;
    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<CaseData> caseDataArgumentCaptor;

    @BeforeEach
    void setUp() {
        RespondentPolicyConverter policyConverter = new RespondentPolicyConverter();
        NoticeOfChangeAnswersConverter answersConverter = new NoticeOfChangeAnswersConverter();
        NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator = new NoticeOfChangeFieldPopulator(policyConverter,
                answersConverter);
        CaseConverter caseConverter = new CaseConverter(new ObjectMapper());
        noticeOfChangeFieldsTask = new NoticeOfChangeFieldsTask(adminUserService, ccdClient,
                featureToggleService, caseConverter, noticeOfChangeFieldPopulator);
        when(featureToggleService.isNoticeOfChangeFieldsEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("AuthToken");
        ReflectionTestUtils.setField(noticeOfChangeFieldsTask, "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(noticeOfChangeFieldsTask, "maxCases", 10);
    }

    @Test
    void testTask_featureOff() throws IOException {
        when(featureToggleService.isNoticeOfChangeFieldsEnabled()).thenReturn(false);
        noticeOfChangeFieldsTask.generateNoticeOfChangeFields();
        verify(ccdClient, times(0)).buildAndGetElasticSearchRequest(any(), any(), any());
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void testNoticeOfChangeFields_featureOn() throws URISyntaxException, IOException {
        SubmitEvent submitEvent = new ObjectMapper().readValue(ResourceLoader.getResource("caseDetailsTest1.json"),
                SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(Collections.emptyList()).thenReturn(Collections.emptyList());
        CaseData caseData = submitEvent.getCaseData();
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .build();
        ccdRequest.getCaseDetails().setJurisdiction(EMPLOYMENT);
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(ccdRequest);
        noticeOfChangeFieldsTask.generateNoticeOfChangeFields();
        verify(ccdClient, times(1)).submitEventForCase(eq("AuthToken"), caseDataArgumentCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq("123456789"));
        CaseData caseDataCaptured = caseDataArgumentCaptor.getValue();
        assertNotNull(caseDataCaptured.getNoticeOfChangeAnswers0());
        assertEquals(caseDataCaptured.getNoticeOfChangeAnswers0().getPartyName(), submitEvent.getCaseData()
                .getRespondentCollection().get(0).getValue().getRespondentName());

    }
}
