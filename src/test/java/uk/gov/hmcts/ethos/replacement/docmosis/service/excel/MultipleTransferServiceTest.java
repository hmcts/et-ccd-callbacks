package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.et.common.model.multiples.items.CaseMultipleTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PersistentQHelperService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATING_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@RequiredArgsConstructor
@ExtendWith(SpringExtension.class)
class MultipleTransferServiceTest {

    private String ccdGatewayBaseUrl;

    @Mock
    private ExcelReadingService excelReadingService;
    @Mock
    private PersistentQHelperService persistentQHelperService;
    @Mock
    private MultipleCasesReadingService multipleCasesReadingService;
    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @Mock
    private CaseTransferUtils caseTransferUtils;

    @InjectMocks
    private MultipleTransferService multipleTransferService;
    private SortedMap<String, Object> multipleObjects;
    private MultipleDetails multipleDetails;
    private List<SubmitMultipleEvent> submitMultipleEvents;
    private List<SubmitEvent> submitEvents;
    private String userToken;
    private List<String> errors;

    @BeforeEach
    public void setUp() {
        ccdGatewayBaseUrl = null; //NOPMD - suppressed NullAssignment - Null is intentional
        multipleObjects = MultipleUtil.getMultipleObjectsAll();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        multipleDetails.setCaseId("1559817606275162");
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        submitMultipleEvents = MultipleUtil.getSubmitMultipleEvents();
        userToken = "authString";
        submitEvents = MultipleUtil.getSubmitEvents();
        errors = new ArrayList<>();
    }

    @Test
    void multipleTransferLogic() {
        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);
        when(singleCasesReadingService.retrieveSingleCases(anyString(), anyString(), anyList(), anyString()))
                .thenReturn(submitEvents);
        multipleTransferService.multipleTransferLogic(userToken,
                multipleDetails,
                errors);

        assertEquals(0, errors.size());
        assertEquals(UPDATING_STATE, multipleDetails.getCaseData().getState());
        verify(persistentQHelperService,
                times(1)).sendCreationEventToSingles(
                userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                errors,
                new ArrayList<>(multipleObjects.keySet()),
                TribunalOffice.MANCHESTER.getOfficeName(),
                "PositionTypeCT",
                ccdGatewayBaseUrl,
                multipleDetails.getCaseData().getReasonForCT(),
                multipleDetails.getCaseData().getMultipleReference(),
                YES,
                MultiplesHelper.generateMarkUp(ccdGatewayBaseUrl,
                        multipleDetails.getCaseId(),
                        multipleDetails.getCaseData().getMultipleReference()),
                true, null
        );
        verifyNoMoreInteractions(persistentQHelperService);

    }

    @Test
    void multipleTransferLogicEmptyCollection() {

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(new TreeMap<>());
        multipleTransferService.multipleTransferLogic(userToken,
                multipleDetails,
                errors);
        verifyNoMoreInteractions(persistentQHelperService);

    }

    @Test
    void populateDataIfComingFromCT() {

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);
        when(multipleCasesReadingService.retrieveMultipleCasesWithRetries(userToken,
                "Bristol",
                "246000")
        ).thenReturn(submitMultipleEvents);

        multipleDetails.getCaseData().setLinkedMultipleCT("Bristol");
        multipleDetails.getCaseData().setMultipleSource(MIGRATION_CASE_SOURCE);

        multipleTransferService.populateDataIfComingFromCT(userToken,
                multipleDetails,
                errors);

        assertEquals("<a target=\"_blank\" href=\"null/cases/case-details/0\">246000</a>",
                multipleDetails.getCaseData().getLinkedMultipleCT());
        List<CaseMultipleTypeItem> caseMultipleTypeItemList = multipleDetails.getCaseData().getCaseMultipleCollection();
        assertEquals("MultipleObjectType(ethosCaseRef=245000/2020, subMultiple=245000, flag1=null, "
                + "flag2=null, flag3=null, flag4=null)", caseMultipleTypeItemList.get(0).getValue().toString());
        assertEquals("MultipleObjectType(ethosCaseRef=245003/2020, subMultiple=245003, flag1=null, "
                + "flag2=null, flag3=null, flag4=null)", caseMultipleTypeItemList.get(1).getValue().toString());
        assertEquals("MultipleObjectType(ethosCaseRef=245004/2020, subMultiple=245002, flag1=null, "
                + "flag2=null, flag3=null, flag4=null)", caseMultipleTypeItemList.get(2).getValue().toString());

    }

    @Test
    void validateCasesBeforeTransfer() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("245004/2020");

        // Uncleared BF Action
        BFActionType bfActionType = new BFActionType();
        bfActionType.setDateEntered("2020-11-11");
        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setValue(bfActionType);
        caseData.setBfActions(List.of(bfActionTypeItem));

        // Listed hearing
        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingStatus(HEARING_STATUS_LISTED);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(dateListedTypeItem));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);
        caseData.setHearingCollection(List.of(hearingTypeItem));

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        submitEvent.setCaseId(1_232_121_232);

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);
        when(singleCasesReadingService.retrieveSingleCases(anyString(), anyString(), anyList(), anyString()))
                .thenReturn(new ArrayList<>(Collections.singletonList(submitEvent)));
        doCallRealMethod().when(caseTransferUtils).validateCase(isA(CaseData.class));

        List<String> validationErrors = caseTransferUtils.validateCase(caseData);
        assertEquals(2, validationErrors.size());
        assertEquals(String.format(CaseTransferUtils.BF_ACTIONS_ERROR_MSG, caseData.getEthosCaseReference()),
                validationErrors.get(0));
        assertEquals(String.format(CaseTransferUtils.HEARINGS_ERROR_MSG, caseData.getEthosCaseReference()),
                validationErrors.get(1));
    }

    @Test
    void validateCasesBeforeTransfer_withoutErrors() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("245004/2020");

        // Cleared BF
        BFActionType bfActionType = new BFActionType();
        bfActionType.setDateEntered("2020-11-11");
        bfActionType.setCleared("2020-11-10");
        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setValue(bfActionType);
        caseData.setBfActions(List.of(bfActionTypeItem));

        // 'Heard' hearing
        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingStatus(HEARING_STATUS_HEARD);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(dateListedTypeItem));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);
        caseData.setHearingCollection(List.of(hearingTypeItem));

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        submitEvent.setCaseId(1_232_121_232);

        SubmitEvent submitEvent2 = new SubmitEvent();
        submitEvent2.setCaseData(caseData);
        submitEvent2.setState(ACCEPTED_STATE);
        submitEvent2.setCaseId(1_232_121_232);

        when(excelReadingService.readExcel(anyString(), anyString(), anyList(), any(), any()))
                .thenReturn(multipleObjects);
        List<String> ethosCaseRefCollection = new ArrayList<>(multipleObjects.keySet());
        when(singleCasesReadingService.retrieveSingleCases(userToken, multipleDetails.getCaseTypeId(),
                ethosCaseRefCollection, multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(List.of(submitEvent, submitEvent2));
        doCallRealMethod().when(caseTransferUtils).validateCase(isA(CaseData.class));
        multipleTransferService.multipleTransferLogic(userToken,
                multipleDetails,
                errors);

        assertEquals(0, errors.size());
    }
}
