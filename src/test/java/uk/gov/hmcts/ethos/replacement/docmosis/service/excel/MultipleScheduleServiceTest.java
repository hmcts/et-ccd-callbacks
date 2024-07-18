package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.SchedulePayload;
import uk.gov.hmcts.ecm.common.model.schedule.SchedulePayloadEvent;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIST_CASES_CONFIG;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleScheduleService.SCHEDULE_LIMIT_CASES;

@ExtendWith(SpringExtension.class)
class MultipleScheduleServiceTest {

    @Mock
    private ExcelReadingService excelReadingService;
    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @Mock
    private ExcelDocManagementService excelDocManagementService;
    @Mock
    FeatureToggleService featureToggleService;
    @InjectMocks
    private MultipleScheduleService multipleScheduleService;

    @Captor
    ArgumentCaptor<List<SchedulePayload>> schedulePayloads;

    private SortedMap<String, Object> multipleObjectsFlags;
    private SortedMap<String, Object> multipleObjectsSubMultiple;
    private MultipleDetails multipleDetails;
    private Set<SchedulePayloadEvent> schedulePayloadEvents;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleObjectsFlags = MultipleUtil.getMultipleObjectsFlags();
        multipleObjectsSubMultiple = MultipleUtil.getMultipleObjectsSubMultiple();
        schedulePayloadEvents = MultipleUtil.getSchedulePayloadEvents();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        userToken = "authString";
    }

    @Test
    void bulkScheduleLogicFlags() {
        schedulePayloadEvents.iterator().next().getSchedulePayloadES().setClaimantCompany(null);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(schedulePayloadEvents);
        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(singleCasesReadingService, times(1)).retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void bulkScheduleLiveCasesFilter() {
        schedulePayloadEvents.iterator().next().getSchedulePayloadES().setClaimantCompany(null);
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
        multipleDetails.getCaseData().setLiveCases(YES);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        schedulePayloadEvents.iterator().next().setState(CLOSED_STATE);
        when(singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(schedulePayloadEvents);

        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(singleCasesReadingService, times(1)).retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verifyNoMoreInteractions(singleCasesReadingService);
        verify(excelDocManagementService, times(1))
                .writeAndUploadScheduleDocument(any(), any(), any(), schedulePayloads.capture());
        List<SchedulePayload> schedulePayloadList = schedulePayloads.getValue();
        assertEquals(1, schedulePayloadList.size());
        assertNull(multipleDetails.getCaseData().getLiveCases());
    }

    @Test
    void bulkScheduleLiveCasesFilterNoClosedCases() {
        schedulePayloadEvents.iterator().next().getSchedulePayloadES().setClaimantCompany(null);
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        schedulePayloadEvents.iterator().next().setState(CLOSED_STATE);
        when(singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(schedulePayloadEvents);

        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(singleCasesReadingService, times(1)).retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verifyNoMoreInteractions(singleCasesReadingService);
        verify(excelDocManagementService, times(1))
                .writeAndUploadScheduleDocument(any(), any(), any(), schedulePayloads.capture());
        List<SchedulePayload> schedulePayloadList = schedulePayloads.getValue();
        assertEquals(2, schedulePayloadList.size());
    }

    @Test
    void bulkScheduleLiveCasesFilterFeatureDisabled() {
        schedulePayloadEvents.iterator().next().getSchedulePayloadES().setClaimantCompany(null);
        when(featureToggleService.isMultiplesEnabled()).thenReturn(false);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        schedulePayloadEvents.iterator().next().setState(CLOSED_STATE);
        when(singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(schedulePayloadEvents);

        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(singleCasesReadingService, times(1)).retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verifyNoMoreInteractions(singleCasesReadingService);
        verify(excelDocManagementService, times(1))
                .writeAndUploadScheduleDocument(any(), any(), any(), schedulePayloads.capture());
        List<SchedulePayload> schedulePayloadList = schedulePayloads.getValue();
        assertEquals(2, schedulePayloadList.size());
    }

    @Test
    void bulkScheduleLogicFlagsWithoutCompanyNorClaimant() {
        schedulePayloadEvents.iterator().next().getSchedulePayloadES().setClaimantCompany(null);
        schedulePayloadEvents.iterator().next().getSchedulePayloadES().setClaimantIndType(null);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(schedulePayloadEvents);
        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(singleCasesReadingService, times(1)).retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void bulkScheduleLogicFlagsMultipleRespondents() {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(new RespondentSumType());
        schedulePayloadEvents.iterator().next()
                .getSchedulePayloadES().getRespondentCollection().add(respondentSumTypeItem);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsFlags);
        when(singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet())))
                .thenReturn(schedulePayloadEvents);
        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(singleCasesReadingService, times(1)).retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(multipleObjectsFlags.keySet()));
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void bulkScheduleLogicSubMultiple() {
        multipleDetails.getCaseData().setScheduleDocName(LIST_CASES_CONFIG);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjectsSubMultiple);
        when(singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                MultiplesScheduleHelper.getSubMultipleCaseIds(multipleObjectsSubMultiple)))
                .thenReturn(schedulePayloadEvents);
        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verify(singleCasesReadingService, times(1)).retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                MultiplesScheduleHelper.getSubMultipleCaseIds(multipleObjectsSubMultiple));
        verifyNoMoreInteractions(singleCasesReadingService);
    }

    @Test
    void bulkScheduleLogicSubMultipleNoCasesFiltered() {
        multipleDetails.getCaseData().setScheduleDocName(LIST_CASES_CONFIG);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(new TreeMap<>());
        when(singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                MultiplesScheduleHelper.getSubMultipleCaseIds(multipleObjectsSubMultiple)))
                .thenReturn(schedulePayloadEvents);
        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                new ArrayList<>());
        verifyNoMoreInteractions(excelDocManagementService);
    }

    @Test
    void bulkScheduleLogicCasesFilteredExceeded() {
        List<String> errors = new ArrayList<>();
        multipleDetails.getCaseData().setScheduleDocName(LIST_CASES_CONFIG);
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(createBigTreeMap());
        multipleScheduleService.bulkScheduleLogic(userToken,
                multipleDetails,
                errors);
        assertEquals(1, errors.size());
        assertEquals("Number of cases exceed the limit of " + SCHEDULE_LIMIT_CASES, errors.get(0));

    }

    private SortedMap<String, Object> createBigTreeMap() {

        SortedMap<String, Object> treeMap = new TreeMap<>();

        for (int i = 0; i < SCHEDULE_LIMIT_CASES + 1; i++) {
            treeMap.put(String.valueOf(i), "Dummy");
        }

        return treeMap;
    }

}