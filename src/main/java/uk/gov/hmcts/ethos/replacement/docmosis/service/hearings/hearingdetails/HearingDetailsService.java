package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.hearingdetails;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import static com.google.common.base.Strings.isNullOrEmpty;

@Service
public class HearingDetailsService {

    private final HearingSelectionService hearingSelectionService;

    public HearingDetailsService(HearingSelectionService hearingSelectionService) {
        this.hearingSelectionService = hearingSelectionService;
    }

    public void initialiseHearingDetails(CaseData caseData) {
        var dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(hearingSelectionService.getHearingSelection(caseData));
        caseData.setHearingDetailsHearing(dynamicFixedListType);
    }

    public void handleListingSelected(CaseData caseData) {
        DateListedType selectedListing = getSelectedListing(caseData);

        caseData.setHearingDetailsStatus(nonNull(selectedListing.getHearingStatus()));
        caseData.setHearingDetailsPostponedBy(nonNull(selectedListing.getPostponedBy()));
        caseData.setHearingDetailsCaseDisposed(nonNull(selectedListing.getHearingCaseDisposed()));
        caseData.setHearingDetailsPartHeard(nonNull(selectedListing.getHearingPartHeard()));
        caseData.setHearingDetailsReservedJudgment(nonNull(selectedListing.getHearingReservedJudgement()));
        caseData.setHearingDetailsAttendeeClaimant(nonNull(selectedListing.getAttendeeClaimant()));
        caseData.setHearingDetailsAttendeeNonAttendees(nonNull(selectedListing.getAttendeeNonAttendees()));
        caseData.setHearingDetailsAttendeeRespNoRep(nonNull(selectedListing.getAttendeeRespNoRep()));
        caseData.setHearingDetailsAttendeeRespAndRep(nonNull(selectedListing.getAttendeeRespAndRep()));
        caseData.setHearingDetailsAttendeeRepOnly(nonNull(selectedListing.getAttendeeRepOnly()));
        caseData.setHearingDetailsTimingStart(nonNull(selectedListing.getHearingTimingStart()));
        caseData.setHearingDetailsTimingBreak(selectedListing.getHearingTimingBreak());
        caseData.setHearingDetailsTimingResume(selectedListing.getHearingTimingResume());
        caseData.setHearingDetailsTimingFinish(nonNull(selectedListing.getHearingTimingFinish()));
        caseData.setHearingDetailsTimingDuration(nonNull(selectedListing.getHearingTimingDuration()));
        caseData.setHearingDetailsHearingNotes2(nonNull(selectedListing.getHearingNotes2()));
    }

    private String nonNull(String value) {
        return isNullOrEmpty(value) ? " " : value;
    }

    public void updateCase(CaseDetails caseDetails) {
        var caseData = caseDetails.getCaseData();
        var selectedListing = getSelectedListing(caseData);

        selectedListing.setHearingStatus(caseData.getHearingDetailsStatus());
        selectedListing.setPostponedBy(caseData.getHearingDetailsPostponedBy());
        selectedListing.setHearingCaseDisposed(caseData.getHearingDetailsCaseDisposed());
        selectedListing.setHearingPartHeard(caseData.getHearingDetailsPartHeard());
        selectedListing.setHearingReservedJudgement(caseData.getHearingDetailsReservedJudgment());
        selectedListing.setAttendeeClaimant(caseData.getHearingDetailsAttendeeClaimant());
        selectedListing.setAttendeeNonAttendees(caseData.getHearingDetailsAttendeeNonAttendees());
        selectedListing.setAttendeeRespNoRep(caseData.getHearingDetailsAttendeeRespNoRep());
        selectedListing.setAttendeeRespAndRep(caseData.getHearingDetailsAttendeeRespAndRep());
        selectedListing.setAttendeeRepOnly(caseData.getHearingDetailsAttendeeRepOnly());
        selectedListing.setHearingTimingStart(caseData.getHearingDetailsTimingStart());
        selectedListing.setHearingTimingBreak(caseData.getHearingDetailsTimingBreak());
        selectedListing.setHearingTimingResume(caseData.getHearingDetailsTimingResume());
        selectedListing.setHearingTimingFinish(caseData.getHearingDetailsTimingFinish());
        selectedListing.setHearingTimingDuration(caseData.getHearingDetailsTimingDuration());
        selectedListing.setHearingNotes2(caseData.getHearingDetailsHearingNotes2());

        Helper.updatePostponedDate(caseData);
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
    }

    private DateListedType getSelectedListing(CaseData caseData) {
        return hearingSelectionService.getSelectedListing(caseData, caseData.getHearingDetailsHearing());
    }
}