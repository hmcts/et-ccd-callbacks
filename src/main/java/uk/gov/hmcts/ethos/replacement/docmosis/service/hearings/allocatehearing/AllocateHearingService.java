package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.CourtWorkerSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.JudgeSelectionService;

@Service
public class AllocateHearingService {

    private final HearingSelectionService hearingSelectionService;
    private final JudgeSelectionService judgeSelectionService;
    private final VenueSelectionService venueSelectionService;
    private final RoomSelectionService roomSelectionService;
    private final CourtWorkerSelectionService courtWorkerSelectionService;

    public AllocateHearingService(HearingSelectionService hearingSelectionService,
                                  JudgeSelectionService judgeSelectionService,
                                  VenueSelectionService venueSelectionService,
                                  RoomSelectionService roomSelectionService,
                                  CourtWorkerSelectionService courtWorkerSelectionService) {
        this.hearingSelectionService = hearingSelectionService;
        this.judgeSelectionService = judgeSelectionService;
        this.venueSelectionService = venueSelectionService;
        this.roomSelectionService = roomSelectionService;
        this.courtWorkerSelectionService = courtWorkerSelectionService;
    }

    public void initialiseAllocateHearing(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(hearingSelectionService.getHearingSelection(caseData));
        caseData.setAllocateHearingHearing(dynamicFixedListType);
    }

    public void handleListingSelected(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        TribunalOffice managingOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        caseData.setAllocateHearingJudge(judgeSelectionService.createJudgeSelection(managingOffice, selectedHearing));

        DateListedType selectedListing = getSelectedListing(caseData);
        caseData.setAllocateHearingVenue(venueSelectionService.createVenueSelection(managingOffice, selectedListing));
        caseData.setAllocateHearingSitAlone(selectedHearing.getHearingSitAlone());
        caseData.setAllocateHearingStatus(selectedListing.getHearingStatus());
        caseData.setAllocateHearingPostponedBy(selectedListing.getPostponedBy());

        addEmployerMembers(caseData, selectedHearing);
        addEmployeeMembers(caseData, selectedHearing);
        addClerk(caseData, selectedListing);
    }

    public void populateRooms(CaseData caseData) {
        DateListedType selectedListing = getSelectedListing(caseData);
        boolean venueChanged = isVenueChanged(selectedListing.getHearingVenueDay(), caseData.getAllocateHearingVenue());

        caseData.setAllocateHearingRoom(roomSelectionService.createRoomSelection(caseData, selectedListing,
                venueChanged));
    }

    public void updateCase(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        selectedHearing.setHearingSitAlone(caseData.getAllocateHearingSitAlone());
        selectedHearing.setJudge(caseData.getAllocateHearingJudge());
        selectedHearing.setHearingERMember(caseData.getAllocateHearingEmployerMember());
        selectedHearing.setHearingEEMember(caseData.getAllocateHearingEmployeeMember());

        DateListedType selectedListing = getSelectedListing(caseData);
        selectedListing.setHearingStatus(caseData.getAllocateHearingStatus());
        selectedListing.setPostponedBy(caseData.getAllocateHearingPostponedBy());
        selectedListing.setHearingVenueDay(caseData.getAllocateHearingVenue());
        selectedListing.setHearingRoom(caseData.getAllocateHearingRoom());
        selectedListing.setHearingClerk(caseData.getAllocateHearingClerk());

        Helper.updatePostponedDate(caseData);
    }

    private HearingType getSelectedHearing(CaseData caseData) {
        return hearingSelectionService.getSelectedHearing(caseData, caseData.getAllocateHearingHearing());
    }

    private DateListedType getSelectedListing(CaseData caseData) {
        return hearingSelectionService.getSelectedListing(caseData, caseData.getAllocateHearingHearing());
    }

    private boolean isVenueChanged(DynamicFixedListType currentVenue, DynamicFixedListType newVenue) {
        String currentVenueCode = currentVenue != null ? currentVenue.getSelectedCode() : null;
        String newVenueCode = newVenue != null ? newVenue.getSelectedCode() : null;
        return !StringUtils.equals(currentVenueCode, newVenueCode);
    }

    private void addEmployerMembers(CaseData caseData, HearingType selectedHearing) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.EMPLOYER_MEMBER);

        if (selectedHearing.hasHearingEmployerMember()) {
            dynamicFixedListType.setValue(selectedHearing.getHearingERMember().getValue());
        }
        caseData.setAllocateHearingEmployerMember(dynamicFixedListType);
    }

    private void addEmployeeMembers(CaseData caseData, HearingType selectedHearing) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.EMPLOYEE_MEMBER);

        if (selectedHearing.hasHearingEmployeeMember()) {
            dynamicFixedListType.setValue(selectedHearing.getHearingEEMember().getValue());
        }
        caseData.setAllocateHearingEmployeeMember(dynamicFixedListType);
    }

    private void addClerk(CaseData caseData, DateListedType selectedListing) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.CLERK);

        if (selectedListing.hasHearingClerk()) {
            dynamicFixedListType.setValue(selectedListing.getHearingClerk().getValue());
        }
        caseData.setAllocateHearingClerk(dynamicFixedListType);
    }
}
