package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    @MockitoBean
    private AdminUserService adminUserService;
    @MockitoBean
    private CcdClient ccdClient;
    @MockitoBean
    private FeatureToggleService featureToggleService;
    @Captor
    private ArgumentCaptor<CaseData> caseDataArgumentCaptor;

    private static final String ADMIN_TOKEN = "adminToken";
    private static final String CASE_ID = "1234567890123456";
    private static final String CASE_TYPE_ID = "ET_EnglandWales";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String RESPONDENT_ID = "abcdef_ghijk_lmnopq_rstuvw_xyzab";
    private static final String REPRESENTATIVE_ID = "xyzab_rstuvw_lmnopq_ghijk_abcdef";
    private static final String RESPONDENT_NAME = "Respondent Name";
    private static final String ROLE_SOLICITOR_A = "[SOLICITORA]";
    private static final String CASE_DETAILS_TEST_JSON = "caseDetailsTest1.json";
    private static final String EVENT_ID_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";

    @BeforeEach
    void setUp() {
        noticeOfChangeFieldsTask = new NoticeOfChangeFieldsTask(adminUserService, ccdClient, featureToggleService);
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
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID)
                .value(RepresentedTypeR.builder().respondentId(RESPONDENT_ID).build()).build();
        caseData.setRepCollection(List.of(representative));
        caseData.getRespondentCollection().getFirst().setId(RESPONDENT_ID);
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .build();
        ccdRequest.getCaseDetails().setCaseId(CASE_ID);
        ccdRequest.getCaseDetails().setCaseTypeId(CASE_TYPE_ID);
        ccdRequest.getCaseDetails().setJurisdiction(JURISDICTION);
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(ccdRequest);
        noticeOfChangeFieldsTask.generateNoticeOfChangeFields();
        verify(ccdClient, times(1)).submitEventForCase(eq("AuthToken"), caseDataArgumentCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), any(), eq(CASE_ID));
        CaseData caseDataCaptured = caseDataArgumentCaptor.getValue();
        assertThat(caseDataCaptured.getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isEqualTo(REPRESENTATIVE_ID);
        assertThat(caseDataCaptured.getRepCollection().getFirst().getId()).isEqualTo(REPRESENTATIVE_ID);
        assertThat(caseDataCaptured.getRepCollection().getFirst().getValue().getRole()).isEqualTo(ROLE_SOLICITOR_A);
        // when start event for case throws exception
        ReflectionTestUtils.setField(noticeOfChangeFieldsTask,
                "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        SubmitEvent submitEventWithCaseData = new SubmitEvent();
        submitEventWithCaseData.setCaseData(caseData);
        submitEventWithCaseData.getCaseData().getRepCollection().getFirst().getValue().setRole(null);
        submitEventWithCaseData.getCaseData().getRespondentCollection().getFirst().getValue().setRepresentativeId(null);
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), anyString(), any()))
                .thenReturn(List.of(submitEventWithCaseData));
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenThrow(new IOException());
        noticeOfChangeFieldsTask.generateNoticeOfChangeFields();
        assertThat(caseDataCaptured.getRepCollection().getFirst().getValue().getRole()).isNull();
        assertThat(caseDataCaptured.getRespondentCollection().getFirst().getValue().getRepresentativeId()).isNull();
    }

    @Test
    void tesFindCaseId() {
        // When submit event is null then return <unknown>
        assertThat(NoticeOfChangeFieldsTask.findCaseId(null)).isEqualTo("<unknown>");
        // When submit event is not null but case id is 0
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(0);
        assertThat(NoticeOfChangeFieldsTask.findCaseId(submitEvent)).isEqualTo("<unknown>");
        // When submit event case id is not 0
        submitEvent.setCaseId(1);
        assertThat(NoticeOfChangeFieldsTask.findCaseId(submitEvent)).isEqualTo(NumberUtils.INTEGER_ONE.toString());
    }

    @Test
    @SneakyThrows
    void theTriggerEventForCase() {
        SubmitEvent submitEvent = new ObjectMapper().readValue(ResourceLoader.getResource(CASE_DETAILS_TEST_JSON),
                SubmitEvent.class);
        // when admin user token is empty should not update case data
        noticeOfChangeFieldsTask.triggerEventForCase(StringUtils.EMPTY, submitEvent, CASE_TYPE_ID);
        assertThat(submitEvent.getCaseData().getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isNull();
        // when submit event is empty should not update case data
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, null, CASE_TYPE_ID);
        assertThat(submitEvent.getCaseData().getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isNull();
        // when submit event does not have case data should not update case data
        submitEvent.setCaseData(null);
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, submitEvent, CASE_TYPE_ID);
        assertThat(submitEvent.getCaseData()).isNull();
        // when case data does not have respondent collection should not update case data
        submitEvent.setCaseData(new CaseData());
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, submitEvent, CASE_TYPE_ID);
        assertThat(submitEvent.getCaseData().getRespondentCollection()).isNull();
        // when case data does not have representative collection should not update case data
        submitEvent = new ObjectMapper().readValue(ResourceLoader.getResource(CASE_DETAILS_TEST_JSON),
                SubmitEvent.class);
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, submitEvent, CASE_TYPE_ID);
        assertThat(submitEvent.getCaseData().getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isNull();
        // when case type id is empty should not update case data
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        submitEvent.getCaseData().setRepCollection(List.of(representative));
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, submitEvent, StringUtils.EMPTY);
        assertThat(submitEvent.getCaseData().getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isNull();
        // when CCD Request is not valid should not update case data
        when(ccdClient.startEventForCase(ADMIN_TOKEN, CASE_TYPE_ID, JURISDICTION, CASE_ID,
                EVENT_ID_UPDATE_CASE_SUBMITTED)).thenReturn(null);
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, submitEvent, CASE_TYPE_ID);
        assertThat(submitEvent.getCaseData().getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isNull();
        // when representative is not valid should not update case data
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseTypeId(CASE_TYPE_ID);
        caseDetails.setJurisdiction(JURISDICTION);
        caseDetails.setCaseData(submitEvent.getCaseData());
        caseDetails.getCaseData().setRepCollection(List.of(representative));
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, CASE_TYPE_ID, JURISDICTION, CASE_ID,
                EVENT_ID_UPDATE_CASE_SUBMITTED)).thenReturn(ccdRequest);
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, submitEvent, CASE_TYPE_ID);
        assertThat(submitEvent.getCaseData().getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isNull();
        // when representative does not have id should set representative id
        representative.setValue(RepresentedTypeR.builder().build());
        representative.getValue().setRespondentId(RESPONDENT_ID);
        caseDetails.getCaseData().getRespondentCollection().getFirst().setId(RESPONDENT_ID);
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, submitEvent, CASE_TYPE_ID);
        assertThat(caseDetails.getCaseData().getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isEqualTo(representative.getId());
        assertThat(caseDetails.getCaseData().getRepCollection().getFirst().getValue().getRole())
                .isEqualTo(ROLE_SOLICITOR_A);
        // when representative has id should set existing representative id to respondent
        representative.setId(REPRESENTATIVE_ID);
        noticeOfChangeFieldsTask.triggerEventForCase(ADMIN_TOKEN, submitEvent, CASE_TYPE_ID);
        assertThat(caseDetails.getCaseData().getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isEqualTo(REPRESENTATIVE_ID);
        assertThat(caseDetails.getCaseData().getRepCollection().getFirst().getValue().getRole())
                .isEqualTo(ROLE_SOLICITOR_A);
    }

    @Test
    void theIsCCDRequestValid() {
        // when ccdRequest is empty should return false
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(null)).isFalse();
        // when ccdRequest is not empty but caseDetails is empty should return false.
        CCDRequest ccdRequest = new CCDRequest();
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(ccdRequest)).isFalse();
        // when caseId is empty should return false.
        CaseDetails caseDetails = new CaseDetails();
        ccdRequest.setCaseDetails(caseDetails);
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(ccdRequest)).isFalse();
        // when case type id is empty should return false
        caseDetails.setCaseId(CASE_ID);
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(ccdRequest)).isFalse();
        // when jurisdiction is empty should return false
        caseDetails.setCaseTypeId(CASE_TYPE_ID);
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(ccdRequest)).isFalse();
        // when case data is empty should return false
        caseDetails.setJurisdiction(JURISDICTION);
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(ccdRequest)).isFalse();
        // when respondent collection is empty should return false
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(ccdRequest)).isFalse();
        // when representative collection is empty should return false
        caseData.setRespondentCollection(List.of(new RespondentSumTypeItem()));
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(ccdRequest)).isFalse();
        // when representative collection is not empty should return true
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().build()));
        assertThat(NoticeOfChangeFieldsTask.isCCDRequestValid(ccdRequest)).isTrue();
    }

    @Test
    void theIsValidRepresentative() {
        // when representative is empty should return false
        assertThat(NoticeOfChangeFieldsTask.isValidRepresentative(null)).isFalse();
        // when representative does not have value should return false
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        assertThat(NoticeOfChangeFieldsTask.isValidRepresentative(representative)).isFalse();
        // when representative has value should return true
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(NoticeOfChangeFieldsTask.isValidRepresentative(representative)).isTrue();
    }

    @Test
    void theSetRespondentValues() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        CaseData caseData = new CaseData();
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID)
                .value(RepresentedTypeR.builder().build()).build();
        caseData.setRespondentCollection(List.of(respondent));
        caseData.setRepCollection(List.of(representative));
        // when respondent is not valid should not set role and representative id
        NoticeOfChangeFieldsTask.setRespondentValues(caseData, representative, respondent);
        assertThat(respondent.getValue()).isNull();
        assertThat(representative.getValue().getRole()).isNull();
        // when respondent is valid should set role and representative id
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME).build());
        NoticeOfChangeFieldsTask.setRespondentValues(caseData, representative, respondent);
        assertThat(respondent.getValue().getRepresentativeId()).isEqualTo(REPRESENTATIVE_ID);
        assertThat(representative.getValue().getRole()).isEqualTo(ROLE_SOLICITOR_A);
    }
}
