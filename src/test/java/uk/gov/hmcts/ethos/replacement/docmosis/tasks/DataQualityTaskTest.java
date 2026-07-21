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
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;

@ExtendWith(SpringExtension.class)
class DataQualityTaskTest {

    private static final String ADMIN_TOKEN = "AdminToken";
    private static final String CASE_ID = "1234567890";
    private static final String ET_CASE_TYPE_ID = "ET_EnglandWales";
    private static final String ECM_CASE_TYPE_ID = "Manchester";
    private static final String FIX_CASE_API_EVENT_ID = "fixCaseAPI";

    // Judgment dates (yyyy-MM-dd)
    private static final String SENT_DATE = "2023-01-20";
    private static final String HEARING_DATE_VALID = "2023-01-15";
    private static final String HEARING_DATE_INVALID = "2023-01-25";

    // Listed hearing dates (yyyy-MM-dd'T'HH:mm:ss.SSS)
    private static final String LISTED_DATE_LATEST_BEFORE_SENT = "2023-01-15T10:00:00.000";
    private static final String LISTED_DATE_EARLIER_BEFORE_SENT = "2023-01-10T10:00:00.000";
    private static final String LISTED_DATE_AFTER_SENT = "2023-01-22T10:00:00.000";

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

    @Captor
    private ArgumentCaptor<CaseData> etCaseDataCaptor;

    @BeforeEach
    void setUp() {
        dataQualityTask = new DataQualityTask(adminUserService, ccdClient, ecmCcdClient, coreCaseDataApi,
            authTokenGenerator);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        setEtCase();
    }

    @Test
    void updateHearingInJudgment_doesNotUpdate_whenHearingCollectionIsEmpty() throws Exception {
        CaseData caseData = new CaseData();
        caseData.setJudgementCollection(List.of(buildEtJudgmentItem(HEARING_DATE_INVALID)));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
        assertEquals(HEARING_DATE_INVALID, caseData.getJudgementCollection().getFirst()
            .getValue().getJudgmentHearingDate());
    }

    @Test
    void updateHearingInJudgment_doesNotUpdate_whenJudgementCollectionIsEmpty() throws Exception {
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(
            buildEtHearingItem(LISTED_DATE_LATEST_BEFORE_SENT, HEARING_STATUS_HEARD)));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateHearingInJudgment_doesNotUpdate_whenNoHeardDates() throws Exception {
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(
            buildEtHearingItem(LISTED_DATE_LATEST_BEFORE_SENT, "Listed")));
        caseData.setJudgementCollection(List.of(buildEtJudgmentItem(HEARING_DATE_INVALID)));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
        assertEquals(HEARING_DATE_INVALID, caseData.getJudgementCollection().getFirst()
            .getValue().getJudgmentHearingDate());
    }

    @Test
    void updateHearingInJudgment_doesNotUpdate_whenHearingDateAlreadyBeforeSentDate() throws Exception {
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(
            buildEtHearingItem(LISTED_DATE_LATEST_BEFORE_SENT, HEARING_STATUS_HEARD)));
        caseData.setJudgementCollection(List.of(buildEtJudgmentItem(HEARING_DATE_VALID)));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
        assertEquals(HEARING_DATE_VALID, caseData.getJudgementCollection().getFirst()
            .getValue().getJudgmentHearingDate());
    }

    @Test
    void updateHearingInJudgment_doesNotUpdate_whenJudgmentHearingDateIsNull() throws Exception {
        JudgementType judgment = new JudgementType();
        judgment.setDateJudgmentSent(SENT_DATE);
        JudgementTypeItem item = new JudgementTypeItem();
        item.setValue(judgment);

        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(
            buildEtHearingItem(LISTED_DATE_LATEST_BEFORE_SENT, HEARING_STATUS_HEARD)));
        caseData.setJudgementCollection(List.of(item));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
        assertNull(caseData.getJudgementCollection().getFirst().getValue().getJudgmentHearingDate());
    }

    @Test
    void updateHearingInJudgment_doesNotUpdate_whenAllHeardDatesAreAfterSentDate() throws Exception {
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(
            buildEtHearingItem(LISTED_DATE_AFTER_SENT, HEARING_STATUS_HEARD)));
        caseData.setJudgementCollection(List.of(buildEtJudgmentItem(HEARING_DATE_INVALID)));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
        assertEquals(HEARING_DATE_INVALID, caseData.getJudgementCollection().getFirst()
            .getValue().getJudgmentHearingDate());
    }

    @Test
    void updateHearingInJudgment_updatesHearingDate_andClearsDynamicHearing_whenHearingDateIsAfterSentDate()
            throws Exception {
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(
            buildEtHearingItem(LISTED_DATE_LATEST_BEFORE_SENT, HEARING_STATUS_HEARD)));
        caseData.setJudgementCollection(List.of(buildEtJudgmentItem(HEARING_DATE_INVALID)));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        captureEtCaseData();
        JudgementType judgment = etCaseDataCaptor.getValue().getJudgementCollection().getFirst().getValue();
        assertEquals("2023-01-15", judgment.getJudgmentHearingDate());
        assertNull(judgment.getDynamicJudgementHearing());
    }

    @Test
    void updateHearingInJudgment_picksLatestHeardDate_beforeSentDate() throws Exception {
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(
            buildEtDateListedItem(LISTED_DATE_EARLIER_BEFORE_SENT, HEARING_STATUS_HEARD), // 2023-01-10
            buildEtDateListedItem(LISTED_DATE_LATEST_BEFORE_SENT, HEARING_STATUS_HEARD),  // 2023-01-15
            buildEtDateListedItem(LISTED_DATE_AFTER_SENT, HEARING_STATUS_HEARD)            // 2023-01-22 (after sent)
        ));
        HearingTypeItem hearingItem = new HearingTypeItem();
        hearingItem.setValue(hearingType);

        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(hearingItem));
        caseData.setJudgementCollection(List.of(buildEtJudgmentItem(HEARING_DATE_INVALID)));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        captureEtCaseData();
        assertEquals("2023-01-15", capturedEtJudgmentHearingDate());
    }

    @Test
    void updateHearingInJudgment_onlyUpdatesInvalidJudgments_whenMixedCollection() throws Exception {
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(List.of(
            buildEtHearingItem(LISTED_DATE_LATEST_BEFORE_SENT, HEARING_STATUS_HEARD)));
        caseData.setJudgementCollection(List.of(
            buildEtJudgmentItem(HEARING_DATE_INVALID), // needs fixing
            buildEtJudgmentItem(HEARING_DATE_VALID)    // already valid
        ));

        mockEtStartEvent(caseData);
        dataQualityTask.run();

        captureEtCaseData();
        List<JudgementTypeItem> judgments = etCaseDataCaptor.getValue().getJudgementCollection();
        assertEquals("2023-01-15", judgments.get(0).getValue().getJudgmentHearingDate());
        assertNull(judgments.get(0).getValue().getDynamicJudgementHearing());
        assertEquals(HEARING_DATE_VALID, judgments.get(1).getValue().getJudgmentHearingDate());
        assertNotNull(judgments.get(1).getValue().getDynamicJudgementHearing());
    }

    @Test
    void updateHearingInJudgment_ecm_updatesHearingDate_whenHearingDateIsAfterSentDate() throws Exception {
        setEcmCase();

        uk.gov.hmcts.ecm.common.model.ccd.CaseData ecmCaseData =
            new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        ecmCaseData.setHearingCollection(
            List.of(buildEcmHearingItem(LISTED_DATE_LATEST_BEFORE_SENT, HEARING_STATUS_HEARD)));
        ecmCaseData.setJudgementCollection(
            List.of(buildEcmJudgmentItem()));

        when(ecmCcdClient.startEventForCase(ADMIN_TOKEN, ECM_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API_EVENT_ID))
            .thenReturn(buildEcmCcdRequest(ecmCaseData));

        dataQualityTask.run();

        ArgumentCaptor<uk.gov.hmcts.ecm.common.model.ccd.CaseData> ecmCaptor =
            ArgumentCaptor.forClass(uk.gov.hmcts.ecm.common.model.ccd.CaseData.class);
        verify(ecmCcdClient).submitEventForCase(eq(ADMIN_TOKEN), ecmCaptor.capture(),
            eq(ECM_CASE_TYPE_ID), any(), any(), eq(CASE_ID));
        uk.gov.hmcts.ecm.common.model.ccd.types.JudgementType ecmJudgment =
            ecmCaptor.getValue().getJudgementCollection().getFirst().getValue();
        assertEquals("2023-01-15", ecmJudgment.getJudgmentHearingDate());
        assertNull(ecmJudgment.getDynamicJudgementHearing());
    }

    private void setEtCase() {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", CASE_ID);
        mockGetCase(ET_CASE_TYPE_ID);
    }

    private void setEcmCase() {
        ReflectionTestUtils.setField(dataQualityTask, "casesToUpdate", CASE_ID);
        mockGetCase(ECM_CASE_TYPE_ID);
    }

    private void mockGetCase(String caseTypeId) {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails reformCaseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().caseTypeId(caseTypeId).build();
        when(coreCaseDataApi.getCase(eq(ADMIN_TOKEN), any(), eq(CASE_ID)))
            .thenReturn(reformCaseDetails);
    }

    private void mockEtStartEvent(CaseData caseData) throws IOException {
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ET_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API_EVENT_ID))
            .thenReturn(buildEtCcdRequest(caseData));
    }

    private void captureEtCaseData() throws IOException {
        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), etCaseDataCaptor.capture(),
            eq(ET_CASE_TYPE_ID), any(), any(), eq(CASE_ID));
    }

    private String capturedEtJudgmentHearingDate() {
        return etCaseDataCaptor.getValue().getJudgementCollection().getFirst().getValue().getJudgmentHearingDate();
    }

    private CCDRequest buildEtCcdRequest(CaseData caseData) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setJurisdiction(EMPLOYMENT);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }

    private HearingTypeItem buildEtHearingItem(String listedDate, String hearingStatus) {
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(buildEtDateListedItem(listedDate, hearingStatus)));
        HearingTypeItem item = new HearingTypeItem();
        item.setValue(hearingType);
        return item;
    }

    private DateListedTypeItem buildEtDateListedItem(String listedDate, String hearingStatus) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        dateListedType.setHearingStatus(hearingStatus);
        DateListedTypeItem item = new DateListedTypeItem();
        item.setValue(dateListedType);
        return item;
    }

    private JudgementTypeItem buildEtJudgmentItem(String judgmentHearingDate) {
        JudgementType judgment = new JudgementType();
        judgment.setJudgmentHearingDate(judgmentHearingDate);
        judgment.setDateJudgmentSent(DataQualityTaskTest.SENT_DATE);
        judgment.setDynamicJudgementHearing(new DynamicFixedListType());
        JudgementTypeItem item = new JudgementTypeItem();
        item.setValue(judgment);
        return item;
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

    private uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem buildEcmHearingItem(
            String listedDate, String hearingStatus) {
        uk.gov.hmcts.ecm.common.model.ccd.types.DateListedType dateListedType =
            new uk.gov.hmcts.ecm.common.model.ccd.types.DateListedType();
        dateListedType.setListedDate(listedDate);
        dateListedType.setHearingStatus(hearingStatus);
        uk.gov.hmcts.ecm.common.model.ccd.items.DateListedTypeItem dateItem =
            new uk.gov.hmcts.ecm.common.model.ccd.items.DateListedTypeItem();
        dateItem.setValue(dateListedType);
        uk.gov.hmcts.ecm.common.model.ccd.types.HearingType hearingType =
            new uk.gov.hmcts.ecm.common.model.ccd.types.HearingType();
        hearingType.setHearingDateCollection(List.of(dateItem));
        uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem item =
            new uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem();
        item.setValue(hearingType);
        return item;
    }

    private uk.gov.hmcts.ecm.common.model.ccd.items.JudgementTypeItem buildEcmJudgmentItem() {
        uk.gov.hmcts.ecm.common.model.ccd.types.JudgementType judgment =
            new uk.gov.hmcts.ecm.common.model.ccd.types.JudgementType();
        judgment.setJudgmentHearingDate(DataQualityTaskTest.HEARING_DATE_INVALID);
        judgment.setDateJudgmentSent(DataQualityTaskTest.SENT_DATE);
        judgment.setDynamicJudgementHearing(new uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType());
        uk.gov.hmcts.ecm.common.model.ccd.items.JudgementTypeItem item =
            new uk.gov.hmcts.ecm.common.model.ccd.items.JudgementTypeItem();
        item.setValue(judgment);
        return item;
    }
}
