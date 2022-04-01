package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ecm.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.CourtWorkerSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.JudgeSelectionService;

@Service
public class ScotlandAllocateHearingService {
    private final HearingSelectionService hearingSelectionService;
    private final JudgeSelectionService judgeSelectionService;
    private final ScotlandVenueSelectionService scotlandVenueSelectionService;
    private final CourtWorkerSelectionService courtWorkerSelectionService;

    public ScotlandAllocateHearingService(HearingSelectionService hearingSelectionService,
                                          JudgeSelectionService judgeSelectionService,
                                          ScotlandVenueSelectionService scotlandVenueSelectionService,
                                          CourtWorkerSelectionService courtWorkerSelectionService) {
        this.hearingSelectionService = hearingSelectionService;
        this.judgeSelectionService = judgeSelectionService;
        this.scotlandVenueSelectionService = scotlandVenueSelectionService;
        this.courtWorkerSelectionService = courtWorkerSelectionService;
    }

    public void handleListingSelected(CaseData caseData) {
        var selectedListing = getSelectedListing(caseData);
        caseData.setAllocateHearingManagingOffice(selectedListing.getHearingVenueDayScotland());
    }

    public void handleManagingOfficeSelected(CaseData caseData) {
        var selectedHearing = getSelectedHearing(caseData);
        var selectedListing = getSelectedListing(caseData);
        var managingOffice = TribunalOffice.valueOfOfficeName(caseData.getAllocateHearingManagingOffice());

        caseData.setAllocateHearingJudge(judgeSelectionService.createJudgeSelection(managingOffice, selectedHearing));
        caseData.setAllocateHearingVenue(scotlandVenueSelectionService.createVenueSelection(managingOffice,
                selectedListing));
        caseData.setAllocateHearingSitAlone(selectedHearing.getHearingSitAlone());
        caseData.setAllocateHearingReadingDeliberation(selectedListing.getHearingTypeReadingDeliberation());
        caseData.setAllocateHearingStatus(selectedListing.getHearingStatus());
        caseData.setAllocateHearingPostponedBy(selectedListing.getPostponedBy());
        caseData.setAllocateHearingEmployerMember(getEmployerMembers(managingOffice, selectedHearing));
        caseData.setAllocateHearingEmployeeMember(getEmployeeMembers(managingOffice, selectedHearing));
        caseData.setAllocateHearingClerk(getClerks(managingOffice, selectedListing));

        // If the managing office has changed then we need to remove any selected
        // values that are controlled by it so that no invalid options are selected
        var existingManagingOffice = selectedListing.getHearingVenueDayScotland();
        if (!managingOffice.getOfficeName().equals(existingManagingOffice)) {
            caseData.getAllocateHearingJudge().setValue(null);
            caseData.getAllocateHearingVenue().setValue(null);
            if (caseData.getAllocateHearingRoom() != null) {
                caseData.getAllocateHearingRoom().setValue(null);
            }
            caseData.getAllocateHearingClerk().setValue(null);
            caseData.getAllocateHearingEmployeeMember().setValue(null);
            caseData.getAllocateHearingEmployerMember().setValue(null);
        }
    }

    public void updateCase(CaseData caseData) {
        var selectedHearing = getSelectedHearing(caseData);
        selectedHearing.setHearingSitAlone(caseData.getAllocateHearingSitAlone());
        selectedHearing.setJudge(caseData.getAllocateHearingJudge());
        selectedHearing.setHearingERMember(caseData.getAllocateHearingEmployerMember());
        selectedHearing.setHearingEEMember(caseData.getAllocateHearingEmployeeMember());

        var selectedListing = getSelectedListing(caseData);
        selectedListing.setHearingTypeReadingDeliberation(caseData.getAllocateHearingReadingDeliberation());
        selectedListing.setHearingStatus(caseData.getAllocateHearingStatus());
        selectedListing.setPostponedBy(caseData.getAllocateHearingPostponedBy());

        var managingOffice = caseData.getAllocateHearingManagingOffice();
        selectedListing.setHearingVenueDayScotland(managingOffice);
        selectedListing.setHearingGlasgow(null);
        selectedListing.setHearingAberdeen(null);
        selectedListing.setHearingDundee(null);
        selectedListing.setHearingEdinburgh(null);

        final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(managingOffice);
        switch (tribunalOffice) {
            case GLASGOW:
                selectedHearing.setHearingGlasgow(caseData.getAllocateHearingVenue());
                selectedListing.setHearingGlasgow(caseData.getAllocateHearingVenue());
                break;
            case ABERDEEN:
                selectedHearing.setHearingAberdeen(caseData.getAllocateHearingVenue());
                selectedListing.setHearingAberdeen(caseData.getAllocateHearingVenue());
                break;
            case DUNDEE:
                selectedHearing.setHearingDundee(caseData.getAllocateHearingVenue());
                selectedListing.setHearingDundee(caseData.getAllocateHearingVenue());
                break;
            case EDINBURGH:
                selectedHearing.setHearingEdinburgh(caseData.getAllocateHearingVenue());
                selectedListing.setHearingEdinburgh(caseData.getAllocateHearingVenue());
                break;
            default:
                break;
        }

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

    private DynamicFixedListType getEmployerMembers(TribunalOffice tribunalOffice, HearingType selectedHearing) {
        var dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(tribunalOffice,
                CourtWorkerType.EMPLOYER_MEMBER);

        if (selectedHearing.hasHearingEmployerMember()) {
            dynamicFixedListType.setValue(selectedHearing.getHearingERMember().getValue());
        }
        return dynamicFixedListType;
    }

    private DynamicFixedListType getEmployeeMembers(TribunalOffice tribunalOffice, HearingType selectedHearing) {
        var dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(tribunalOffice,
                CourtWorkerType.EMPLOYEE_MEMBER);

        if (selectedHearing.hasHearingEmployeeMember()) {
            dynamicFixedListType.setValue(selectedHearing.getHearingEEMember().getValue());
        }
        return dynamicFixedListType;
    }

    private DynamicFixedListType getClerks(TribunalOffice tribunalOffice, DateListedType selectedListing) {
        var dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(tribunalOffice,
                CourtWorkerType.CLERK);

        if (selectedListing.hasHearingClerk()) {
            dynamicFixedListType.setValue(selectedListing.getHearingClerk().getValue());
        }
        return dynamicFixedListType;
    }
}
