package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.hearingdetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingDetailType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.NcssCount"})
class HearingDetailServiceTest {

    private HearingDetailsService hearingDetailsService;
    private DateListedType selectedListing;
    private HearingSelectionService hearingSelectionService;
    private static final String TEST_ID = UUID.randomUUID().toString();

    @BeforeEach
    public void setup() {
        selectedListing = new DateListedType();
        hearingSelectionService = mock(HearingSelectionService.class);
        hearingDetailsService = new HearingDetailsService(mockHearingSelectionService());
    }

    @Test
    void testInitialiseHearingDetails() {
        CaseData caseData = new CaseData();
        hearingDetailsService.initialiseHearingDetails(caseData);
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                caseData.getHearingDetailsHearing(), "hearing", "Hearing ");
    }

    @Test
    void testHandleListingSelected() {
        String hearingStatus = Constants.HEARING_STATUS_HEARD;
        selectedListing.setHearingStatus(hearingStatus);
        String postponedBy = "Arthur";
        selectedListing.setPostponedBy(postponedBy);
        String caseDisposed = String.valueOf(Boolean.TRUE);
        selectedListing.setHearingCaseDisposed(caseDisposed);
        String partHeard = String.valueOf(Boolean.TRUE);
        selectedListing.setHearingPartHeard(partHeard);
        String reservedJudgment = String.valueOf(Boolean.TRUE);
        selectedListing.setHearingReservedJudgement(reservedJudgment);
        String attendeeClaimant = "1";
        selectedListing.setAttendeeClaimant(attendeeClaimant);
        String attendeeNonAttendees = "2";
        selectedListing.setAttendeeNonAttendees(attendeeNonAttendees);
        String attendeeRespNoRep = "3";
        selectedListing.setAttendeeRespNoRep(attendeeRespNoRep);
        String attendeeRespAndRep = "4";
        selectedListing.setAttendeeRespAndRep(attendeeRespAndRep);
        String attendeeRepOnly = "5";
        selectedListing.setAttendeeRepOnly(attendeeRepOnly);
        String hearingTimeStart = "09:00";
        selectedListing.setHearingTimingStart(hearingTimeStart);
        String hearingTimeBreak = "10:00";
        selectedListing.setHearingTimingBreak(hearingTimeBreak);
        String hearingTimeResume = "11:00";
        selectedListing.setHearingTimingResume(hearingTimeResume);
        String hearingTimeFinish = "12:00";
        selectedListing.setHearingTimingFinish(hearingTimeFinish);
        String duration = "6";
        selectedListing.setHearingTimingDuration(duration);
        String notes = "Some notes";
        selectedListing.setHearingNotes2(notes);
        CaseData caseData = createCaseData();
        hearingDetailsService.handleListingSelected(caseData);
        HearingDetailType hearingDetailType = caseData.getHearingDetailsCollection().get(0).getValue();
        assertEquals(hearingStatus, hearingDetailType.getHearingDetailsStatus());
        assertEquals(postponedBy, hearingDetailType.getHearingDetailsPostponedBy());
        assertEquals(caseDisposed, hearingDetailType.getHearingDetailsCaseDisposed());
        assertEquals(partHeard, hearingDetailType.getHearingDetailsPartHeard());
        assertEquals(reservedJudgment, hearingDetailType.getHearingDetailsReservedJudgment());
        assertEquals(attendeeClaimant, hearingDetailType.getHearingDetailsAttendeeClaimant());
        assertEquals(attendeeNonAttendees, hearingDetailType.getHearingDetailsAttendeeNonAttendees());
        assertEquals(attendeeRespNoRep, hearingDetailType.getHearingDetailsAttendeeRespNoRep());
        assertEquals(attendeeRespAndRep, hearingDetailType.getHearingDetailsAttendeeRespAndRep());
        assertEquals(attendeeRepOnly, hearingDetailType.getHearingDetailsAttendeeRepOnly());
        assertEquals(hearingTimeStart, hearingDetailType.getHearingDetailsTimingStart());
        assertEquals(hearingTimeBreak, hearingDetailType.getHearingDetailsTimingBreak());
        assertEquals(hearingTimeResume, hearingDetailType.getHearingDetailsTimingResume());
        assertEquals(hearingTimeFinish, hearingDetailType.getHearingDetailsTimingFinish());
        assertEquals(duration, hearingDetailType.getHearingDetailsTimingDuration());
        assertEquals(notes, hearingDetailType.getHearingDetailsHearingNotes2());
    }

    @Test
    void testHandleListingSelectedNullValue() {
        selectedListing.setHearingStatus(null);
        selectedListing.setPostponedBy(null);
        selectedListing.setHearingCaseDisposed(null);
        selectedListing.setHearingPartHeard(null);
        selectedListing.setHearingReservedJudgement(null);
        selectedListing.setAttendeeClaimant(null);
        selectedListing.setAttendeeNonAttendees(null);
        selectedListing.setAttendeeRespNoRep(null);
        selectedListing.setAttendeeRespAndRep(null);
        selectedListing.setAttendeeRepOnly(null);
        selectedListing.setHearingTimingStart(null);
        selectedListing.setHearingTimingFinish(null);
        selectedListing.setHearingTimingDuration(null);
        selectedListing.setHearingNotes2(null);
        selectedListing.setListedDate("2022-11-11 11:00:00");
        CaseData caseData = createCaseData();
        hearingDetailsService.handleListingSelected(caseData);
        HearingDetailType hearingDetailType = caseData.getHearingDetailsCollection().get(0).getValue();
        assertEquals(" ", hearingDetailType.getHearingDetailsStatus());
        assertEquals(" ", hearingDetailType.getHearingDetailsPostponedBy());
        assertEquals(" ", hearingDetailType.getHearingDetailsCaseDisposed());
        assertEquals(" ", hearingDetailType.getHearingDetailsPartHeard());
        assertEquals(" ", hearingDetailType.getHearingDetailsReservedJudgment());
        assertEquals(" ", hearingDetailType.getHearingDetailsAttendeeClaimant());
        assertEquals(" ", hearingDetailType.getHearingDetailsAttendeeNonAttendees());
        assertEquals(" ", hearingDetailType.getHearingDetailsAttendeeRespNoRep());
        assertEquals(" ", hearingDetailType.getHearingDetailsAttendeeRespAndRep());
        assertEquals(" ", hearingDetailType.getHearingDetailsAttendeeRepOnly());
        assertEquals(" ", hearingDetailType.getHearingDetailsTimingStart());
        assertEquals(" ", hearingDetailType.getHearingDetailsTimingFinish());
        assertEquals(" ", hearingDetailType.getHearingDetailsTimingDuration());
        assertEquals(" ", hearingDetailType.getHearingDetailsHearingNotes2());
    }

    @Test
    void testUpdateCase() {
        HearingDetailType hearingDetailType = new HearingDetailType();
        selectedListing.setListedDate("2022-11-11 11:00:00");
        hearingDetailType.setHearingDetailsDate(selectedListing.getListedDate());
        String hearingStatus = Constants.HEARING_STATUS_HEARD;
        hearingDetailType.setHearingDetailsStatus(hearingStatus);
        String postponedBy = "Arthur";
        hearingDetailType.setHearingDetailsPostponedBy(postponedBy);
        String caseDisposed = String.valueOf(Boolean.TRUE);
        hearingDetailType.setHearingDetailsCaseDisposed(caseDisposed);
        String partHeard = String.valueOf(Boolean.TRUE);
        hearingDetailType.setHearingDetailsPartHeard(partHeard);
        String reservedJudgment = String.valueOf(Boolean.TRUE);
        hearingDetailType.setHearingDetailsReservedJudgment(reservedJudgment);
        String attendeeClaimant = "1";
        hearingDetailType.setHearingDetailsAttendeeClaimant(attendeeClaimant);
        String attendeeNonAttendees = "2";
        hearingDetailType.setHearingDetailsAttendeeNonAttendees(attendeeNonAttendees);
        String attendeeRespNoRep = "3";
        hearingDetailType.setHearingDetailsAttendeeRespNoRep(attendeeRespNoRep);
        String attendeeRespAndRep = "4";
        hearingDetailType.setHearingDetailsAttendeeRespAndRep(attendeeRespAndRep);
        String attendeeRepOnly = "5";
        hearingDetailType.setHearingDetailsAttendeeRepOnly(attendeeRepOnly);
        String hearingTimeStart = "09:00";
        hearingDetailType.setHearingDetailsTimingStart(hearingTimeStart);
        String hearingTimeBreak = "10:00";
        hearingDetailType.setHearingDetailsTimingBreak(hearingTimeBreak);
        String hearingTimeResume = "11:00";
        hearingDetailType.setHearingDetailsTimingResume(hearingTimeResume);
        String hearingTimeFinish = "12:00";
        hearingDetailType.setHearingDetailsTimingFinish(hearingTimeFinish);
        String duration = "6";
        hearingDetailType.setHearingDetailsTimingDuration(duration);
        String notes = "Some notes";
        hearingDetailType.setHearingDetailsHearingNotes2(notes);
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setValue(hearingDetailType);
        CaseData caseData = createCaseData();
        caseData.setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId(UUID.randomUUID().toString());
        dateListedTypeItem.setValue(selectedListing);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(dateListedTypeItem));
        when(hearingSelectionService.getSelectedHearing(isA(CaseData.class), isA(DynamicFixedListType.class)))
            .thenReturn(hearingType);
        hearingDetailsService.updateCase(caseDetails);

        assertEquals(hearingStatus, selectedListing.getHearingStatus());
        assertEquals(postponedBy, selectedListing.getPostponedBy());
        assertEquals(caseDisposed, selectedListing.getHearingCaseDisposed());
        assertEquals(partHeard, selectedListing.getHearingPartHeard());
        assertEquals(reservedJudgment, selectedListing.getHearingReservedJudgement());
        assertEquals(attendeeClaimant, selectedListing.getAttendeeClaimant());
        assertEquals(attendeeNonAttendees, selectedListing.getAttendeeNonAttendees());
        assertEquals(attendeeRespNoRep, selectedListing.getAttendeeRespNoRep());
        assertEquals(attendeeRespAndRep, selectedListing.getAttendeeRespAndRep());
        assertEquals(attendeeRepOnly, selectedListing.getAttendeeRepOnly());
        assertEquals(hearingTimeStart, selectedListing.getHearingTimingStart());
        assertEquals(hearingTimeBreak, selectedListing.getHearingTimingBreak());
        assertEquals(hearingTimeResume, selectedListing.getHearingTimingResume());
        assertEquals(hearingTimeFinish, selectedListing.getHearingTimingFinish());
        assertEquals(duration, selectedListing.getHearingTimingDuration());
        assertEquals(notes, selectedListing.getHearingNotes2());
    }

    @Test
    void testUpdateCase_Null_Or_Empty_HearingDetailsCollection() {
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        String id = "22";
        hearingTypeItem.setId(id);
        String expectedHearingType =  "Preliminary Hearing";
        String expectedHearingNotes = "Test hearing notes";
        HearingType hearingType = new HearingType();
        hearingType.setHearingType(expectedHearingType);
        hearingType.setHearingNotes(expectedHearingNotes);
        hearingTypeItem.setValue(hearingType);
        CaseData caseData = createCaseData();
        caseData.setHearingCollection(List.of(hearingTypeItem));
        caseData.setHearingDetailsCollection(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        hearingDetailsService.updateCase(caseDetails);
        // No updates are made as there are no hearings in the case data.
        assertEquals(expectedHearingType, caseData.getHearingCollection().get(0).getValue().getHearingType());
    }

    @Test
    void testUpdateCase_Null_Or_Empty_HearingDateCollection() {
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId(TEST_ID);
        String expectedHearingType =  "Preliminary Hearing";
        String expectedHearingNotes = "Test hearing notes";
        HearingType hearingType = new HearingType();
        hearingType.setHearingType(expectedHearingType);
        hearingType.setHearingNotes(expectedHearingNotes);
        hearingTypeItem.setValue(hearingType);
        CaseData caseData2 = createCaseData();
        caseData2.getHearingCollection().get(0).getValue().getHearingDateCollection().remove(0);
        DynamicFixedListType fixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(TEST_ID);
        fixedListType.setValue(dynamicValueType);
        caseData2.setHearingDetailsHearing(fixedListType);
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        caseData2.setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData2);

        when(hearingSelectionService.getDateListedItemsFromSelectedHearing(isA(CaseData.class),
            isA(DynamicFixedListType.class))).thenReturn(new ArrayList<>());

        hearingDetailsService.updateCase(caseDetails);
        assertEquals(0, caseData2.getHearingCollection().get(0).getValue().getHearingDateCollection().size());
    }

    private HearingSelectionService mockHearingSelectionService() {
        List<DynamicValueType> hearings = SelectionServiceTestUtils.createListItems("hearing",
            "Hearing ");
        when(hearingSelectionService.getHearingSelection(isA(CaseData.class))).thenReturn(hearings);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId(UUID.randomUUID().toString());
        dateListedTypeItem.setValue(selectedListing);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(dateListedTypeItem));
        when(hearingSelectionService.getSelectedHearing(isA(CaseData.class), isA(DynamicFixedListType.class)))
                .thenReturn(hearingType);
        when(hearingSelectionService.getDateListedItemsFromSelectedHearing(isA(CaseData.class),
            isA(DynamicFixedListType.class))).thenReturn(List.of(dateListedTypeItem));
        return hearingSelectionService;
    }

    private CaseData createCaseData() {
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId(TEST_ID);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(selectedListing);
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(TEST_ID);
        dynamicFixedListType.setValue(dynamicValueType);

        List<DateListedTypeItem> dateListedTypeItemList = new ArrayList<>();
        dateListedTypeItemList.add(dateListedTypeItem);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(dateListedTypeItemList);
        hearingTypeItem.setValue(hearingType);

        List<HearingTypeItem> hearingTypeItemList = new ArrayList<>();
        hearingTypeItemList.add(hearingTypeItem);
        CaseData caseData = new CaseData();
        caseData.setHearingDetailsHearing(dynamicFixedListType);
        caseData.setHearingCollection(hearingTypeItemList);
        return caseData;
    }
}
