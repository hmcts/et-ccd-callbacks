package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.CourtWorkerSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.JudgeSelectionService;

import java.util.Objects;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HearingConstants.FULL_PANEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HearingConstants.TWO_JUDGES;

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
        dynamicFixedListType.setListItems(hearingSelectionService.getHearingSelectionAllocateHearing(caseData));
        caseData.setAllocateHearingHearing(dynamicFixedListType);
    }

    public void handleListingSelected(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        TribunalOffice managingOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        caseData.setAllocateHearingJudge(judgeSelectionService.createJudgeSelection(managingOffice, selectedHearing,
                false));
        caseData.setAllocateHearingAdditionalJudge(judgeSelectionService.createJudgeSelection(managingOffice,
                selectedHearing, true));
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

    /**
     * Update common fields for EW and Scotland.
     * @param caseData the case data
     */
    public void updateSelectedHearing(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        selectedHearing.setHearingSitAlone(caseData.getAllocateHearingSitAlone());

        selectedHearing.setJudge(caseData.getAllocateHearingJudge());
        selectedHearing.getJudge().setListItems(null);

        if (TWO_JUDGES.equals(caseData.getAllocateHearingSitAlone())) {
            selectedHearing.setAdditionalJudge(caseData.getAllocateHearingAdditionalJudge());
            selectedHearing.getAdditionalJudge().setListItems(null);
        } else {
            selectedHearing.setAdditionalJudge(null);
        }

        if (FULL_PANEL.equals(caseData.getAllocateHearingSitAlone())) {
            selectedHearing.setHearingERMember(caseData.getAllocateHearingEmployerMember());
            selectedHearing.getHearingERMember().setListItems(null);
            selectedHearing.setHearingEEMember(caseData.getAllocateHearingEmployeeMember());
            selectedHearing.getHearingEEMember().setListItems(null);
        } else {
            selectedHearing.setHearingERMember(null);
            selectedHearing.setHearingEEMember(null);
        }
    }

    /**
     * Update other fields for EW.
     * @param caseData the case data
     */
    public void updateCase(CaseData caseData) {
        DateListedType selectedListing = getSelectedListing(caseData);
        selectedListing.setHearingStatus(caseData.getAllocateHearingStatus());
        selectedListing.setHearingVenueDay(caseData.getAllocateHearingVenue());
        selectedListing.setHearingRoom(caseData.getAllocateHearingRoom());
        selectedListing.setHearingClerk(caseData.getAllocateHearingClerk());
        HearingsHelper.updatePostponedDate(caseData, selectedListing);
    }

    private HearingType getSelectedHearing(CaseData caseData) {
        return hearingSelectionService.getSelectedHearingAllocateHearing(caseData);
    }

    private DateListedType getSelectedListing(CaseData caseData) {
        return hearingSelectionService.getSelectedListing(caseData);
    }

    private boolean isVenueChanged(DynamicFixedListType currentVenue, DynamicFixedListType newVenue) {
        String currentVenueCode = currentVenue != null ? currentVenue.getSelectedCode() : null;
        String newVenueCode = newVenue != null ? newVenue.getSelectedCode() : null;
        return !Objects.equals(currentVenueCode, newVenueCode);
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

    /**
     * Clears the dynamic fixed lists for allocateHearing event.
     * @param caseData the case data
     */
    public void clearDynamicFixedList(CaseData caseData) {
        caseData.setAllocateHearingJudge(null);
        caseData.setAllocateHearingAdditionalJudge(null);
        caseData.setAllocateHearingEmployerMember(null);
        caseData.setAllocateHearingEmployeeMember(null);
    }
}
