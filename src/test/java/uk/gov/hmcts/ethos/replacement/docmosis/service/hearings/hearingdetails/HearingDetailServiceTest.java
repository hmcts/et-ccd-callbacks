package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.hearingdetails;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HearingDetailServiceTest {

    private HearingDetailsService hearingDetailsService;
    private DateListedType selectedListing;

    @Before
    public void setup() {
        selectedListing = new DateListedType();
        hearingDetailsService = new HearingDetailsService(mockHearingSelectionService());
    }

    @Test
    public void testInitialiseHearingDetails() {
        CaseData caseData = new CaseData();

        hearingDetailsService.initialiseHearingDetails(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                caseData.getHearingDetailsHearing(), "hearing", "Hearing ");
    }

    @Test
    public void testHandleListingSelected() {
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
        assertEquals(hearingStatus, caseData.getHearingDetailsStatus());
        assertEquals(postponedBy, caseData.getHearingDetailsPostponedBy());
        assertEquals(caseDisposed, caseData.getHearingDetailsCaseDisposed());
        assertEquals(partHeard, caseData.getHearingDetailsPartHeard());
        assertEquals(reservedJudgment, caseData.getHearingDetailsReservedJudgment());
        assertEquals(attendeeClaimant, caseData.getHearingDetailsAttendeeClaimant());
        assertEquals(attendeeNonAttendees, caseData.getHearingDetailsAttendeeNonAttendees());
        assertEquals(attendeeRespNoRep, caseData.getHearingDetailsAttendeeRespNoRep());
        assertEquals(attendeeRespAndRep, caseData.getHearingDetailsAttendeeRespAndRep());
        assertEquals(attendeeRepOnly, caseData.getHearingDetailsAttendeeRepOnly());
        assertEquals(hearingTimeStart, caseData.getHearingDetailsTimingStart());
        assertEquals(hearingTimeBreak, caseData.getHearingDetailsTimingBreak());
        assertEquals(hearingTimeResume, caseData.getHearingDetailsTimingResume());
        assertEquals(hearingTimeFinish, caseData.getHearingDetailsTimingFinish());
        assertEquals(duration, caseData.getHearingDetailsTimingDuration());
        assertEquals(notes, caseData.getHearingDetailsHearingNotes2());
    }

    @Test
    public void testHandleListingSelectedNullValue() {
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
        CaseData caseData = createCaseData();
        hearingDetailsService.handleListingSelected(caseData);

        assertEquals(" ", caseData.getHearingDetailsStatus());
        assertEquals(" ", caseData.getHearingDetailsPostponedBy());
        assertEquals(" ", caseData.getHearingDetailsCaseDisposed());
        assertEquals(" ", caseData.getHearingDetailsPartHeard());
        assertEquals(" ", caseData.getHearingDetailsReservedJudgment());
        assertEquals(" ", caseData.getHearingDetailsAttendeeClaimant());
        assertEquals(" ", caseData.getHearingDetailsAttendeeNonAttendees());
        assertEquals(" ", caseData.getHearingDetailsAttendeeRespNoRep());
        assertEquals(" ", caseData.getHearingDetailsAttendeeRespAndRep());
        assertEquals(" ", caseData.getHearingDetailsAttendeeRepOnly());
        assertEquals(" ", caseData.getHearingDetailsTimingStart());
        assertEquals(" ", caseData.getHearingDetailsTimingFinish());
        assertEquals(" ", caseData.getHearingDetailsTimingDuration());
        assertEquals(" ", caseData.getHearingDetailsHearingNotes2());
    }

    @Test
    public void testUpdateCase() {
        CaseData caseData = createCaseData();
        String hearingStatus = Constants.HEARING_STATUS_HEARD;
        caseData.setHearingDetailsStatus(hearingStatus);
        String postponedBy = "Arthur";
        caseData.setHearingDetailsPostponedBy(postponedBy);
        String caseDisposed = String.valueOf(Boolean.TRUE);
        caseData.setHearingDetailsCaseDisposed(caseDisposed);
        String partHeard = String.valueOf(Boolean.TRUE);
        caseData.setHearingDetailsPartHeard(partHeard);
        String reservedJudgment = String.valueOf(Boolean.TRUE);
        caseData.setHearingDetailsReservedJudgment(reservedJudgment);
        String attendeeClaimant = "1";
        caseData.setHearingDetailsAttendeeClaimant(attendeeClaimant);
        String attendeeNonAttendees = "2";
        caseData.setHearingDetailsAttendeeNonAttendees(attendeeNonAttendees);
        String attendeeRespNoRep = "3";
        caseData.setHearingDetailsAttendeeRespNoRep(attendeeRespNoRep);
        String attendeeRespAndRep = "4";
        caseData.setHearingDetailsAttendeeRespAndRep(attendeeRespAndRep);
        String attendeeRepOnly = "5";
        caseData.setHearingDetailsAttendeeRepOnly(attendeeRepOnly);
        String hearingTimeStart = "09:00";
        caseData.setHearingDetailsTimingStart(hearingTimeStart);
        String hearingTimeBreak = "10:00";
        caseData.setHearingDetailsTimingBreak(hearingTimeBreak);
        String hearingTimeResume = "11:00";
        caseData.setHearingDetailsTimingResume(hearingTimeResume);
        String hearingTimeFinish = "12:00";
        caseData.setHearingDetailsTimingFinish(hearingTimeFinish);
        String duration = "6";
        caseData.setHearingDetailsTimingDuration(duration);
        String notes = "Some notes";
        caseData.setHearingDetailsHearingNotes2(notes);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

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

    private HearingSelectionService mockHearingSelectionService() {
        HearingSelectionService hearingSelectionService = mock(HearingSelectionService.class);
        List<DynamicValueType> hearings = SelectionServiceTestUtils.createListItems("hearing", "Hearing ");
        when(hearingSelectionService.getHearingSelection(isA(CaseData.class))).thenReturn(hearings);

        when(hearingSelectionService.getSelectedListing(isA(CaseData.class),
                isA(DynamicFixedListType.class))).thenReturn(selectedListing);

        return hearingSelectionService;
    }

    private CaseData createCaseData() {
        CaseData caseData = new CaseData();
        caseData.setHearingDetailsHearing(new DynamicFixedListType());
        return caseData;
    }
}
