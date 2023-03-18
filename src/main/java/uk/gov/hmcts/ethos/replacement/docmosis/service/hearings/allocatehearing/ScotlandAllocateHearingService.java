package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.AllocateHearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllocateHearingType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.CourtWorkerSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.JudgeSelectionService;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings({"PMD.ConfusingTernary"})
public class ScotlandAllocateHearingService {
    private final HearingSelectionService hearingSelectionService;
    private final JudgeSelectionService judgeSelectionService;
    private final ScotlandVenueSelectionService scotlandVenueSelectionService;
    private final CourtWorkerSelectionService courtWorkerSelectionService;
    private final RoomSelectionService roomSelectionService;

    public ScotlandAllocateHearingService(HearingSelectionService hearingSelectionService,
                                          JudgeSelectionService judgeSelectionService,
                                          ScotlandVenueSelectionService scotlandVenueSelectionService,
                                          CourtWorkerSelectionService courtWorkerSelectionService,
                                          RoomSelectionService roomSelectionService) {
        this.hearingSelectionService = hearingSelectionService;
        this.judgeSelectionService = judgeSelectionService;
        this.scotlandVenueSelectionService = scotlandVenueSelectionService;
        this.courtWorkerSelectionService = courtWorkerSelectionService;
        this.roomSelectionService = roomSelectionService;
    }

    public void handleListingSelected(CaseData caseData) {
        List<DateListedTypeItem> listings = getListings(caseData);
        for (DateListedTypeItem dateListedTypeItem : listings) {
            DateListedType dateListedType = dateListedTypeItem.getValue();
            Optional<AllocateHearingTypeItem> item = caseData.getAllocateHearingCollection().stream().filter(a ->
                    a.getValue().getAllocateHearingDate().equals(dateListedType.getListedDate())).findFirst();
            item.ifPresent(allocateHearingTypeItem
                    -> allocateHearingTypeItem.getValue()
                    .setAllocateHearingManagingOffice(dateListedType.getHearingVenueDayScotland()));
        }
    }

    public void handleManagingOfficeSelected(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        List<DateListedTypeItem> listings = getListings(caseData);
        TribunalOffice managingOffice;

        caseData.setAllocateHearingSitAlone(selectedHearing.getHearingSitAlone());

        for (DateListedTypeItem dateListedTypeItem : listings) {
            DateListedType dateListedType = dateListedTypeItem.getValue();
            for (AllocateHearingTypeItem allocateHearingTypeItem : caseData.getAllocateHearingCollection()) {
                AllocateHearingType allocateHearingType = allocateHearingTypeItem.getValue();
                if (allocateHearingType.getAllocateHearingDate().equals(dateListedType.getListedDate())) {
                    managingOffice = TribunalOffice.valueOfOfficeName(
                            allocateHearingType.getAllocateHearingManagingOffice());
                    setAllocateHearing(allocateHearingType,
                            dateListedType, selectedHearing, managingOffice, caseData);
                    managingOfficeChanged(allocateHearingType, dateListedType, managingOffice, caseData);
                }
            }
        }
    }

    private void managingOfficeChanged(AllocateHearingType allocateHearingType,
                                      DateListedType dateListedType,
                                      TribunalOffice managingOffice,
                                      CaseData caseData) {
        // If the managing office has changed then we need to remove any selected
        // values that are controlled by it so that no invalid options are selected
        String existingManagingOffice = dateListedType.getHearingVenueDayScotland();
        if (!managingOffice.getOfficeName().equals(existingManagingOffice)) {
            caseData.getAllocateHearingJudge().setValue(null);
            allocateHearingType.getAllocateHearingVenue().setValue(null);
            if (allocateHearingType.getAllocateHearingRoom() != null) {
                allocateHearingType.getAllocateHearingRoom().setValue(null);
            }
            allocateHearingType.getAllocateHearingClerk().setValue(null);
            caseData.getAllocateHearingEmployeeMember().setValue(null);
            caseData.getAllocateHearingEmployerMember().setValue(null);
        }
    }

    private void setAllocateHearing(AllocateHearingType allocateHearingType,
                                    DateListedType dateListedType,
                                    HearingType selectedHearing,
                                    TribunalOffice managingOffice,
                                    CaseData caseData) {
        allocateHearingType.setAllocateHearingVenue(scotlandVenueSelectionService.createVenueSelection(managingOffice,
                dateListedType));
        allocateHearingType.setAllocateHearingReadingDeliberation(dateListedType.getHearingTypeReadingDeliberation());
        allocateHearingType.setAllocateHearingStatus(dateListedType.getHearingStatus());
        allocateHearingType.setAllocateHearingPostponedBy(dateListedType.getPostponedBy());
        allocateHearingType.setAllocateHearingClerk(getClerks(managingOffice, dateListedType));
        caseData.setAllocateHearingJudge(judgeSelectionService.createJudgeSelection(managingOffice, selectedHearing));
        caseData.setAllocateHearingEmployerMember(getEmployerMembers(managingOffice, selectedHearing));
        caseData.setAllocateHearingEmployeeMember(getEmployeeMembers(managingOffice, selectedHearing));
    }

    public void populateRooms(CaseData caseData) {
        List<DateListedTypeItem> listings = getListings(caseData);
        boolean venueChanged;
        for (DateListedTypeItem dateListedTypeItem : listings) {
            DateListedType dateListedType = dateListedTypeItem.getValue();
            for (AllocateHearingTypeItem allocateHearingTypeItem : caseData.getAllocateHearingCollection()) {
                AllocateHearingType allocateHearingType = allocateHearingTypeItem.getValue();
                if (allocateHearingType.getAllocateHearingDate().equals(dateListedType.getListedDate())) {
                    venueChanged = isVenueChanged(dateListedType, allocateHearingType.getAllocateHearingVenue());
                    allocateHearingType.setAllocateHearingRoom(roomSelectionService.createRoomSelection(
                            allocateHearingType.getAllocateHearingVenue(),
                            dateListedType,
                            venueChanged));
                }
            }
        }
    }

    public void updateCase(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        selectedHearing.setHearingSitAlone(caseData.getAllocateHearingSitAlone());
        selectedHearing.setJudge(caseData.getAllocateHearingJudge());
        selectedHearing.setHearingERMember(caseData.getAllocateHearingEmployerMember());
        selectedHearing.setHearingEEMember(caseData.getAllocateHearingEmployeeMember());

        List<DateListedTypeItem> listings = getListings(caseData);
        for (DateListedTypeItem dateListedTypeItem : listings) {
            DateListedType dateListedType = dateListedTypeItem.getValue();
            for (AllocateHearingTypeItem allocateHearingTypeItem : caseData.getAllocateHearingCollection()) {
                AllocateHearingType allocateHearingType = allocateHearingTypeItem.getValue();
                if (allocateHearingType.getAllocateHearingDate().equals(dateListedType.getListedDate())) {
                    setHearings(dateListedType, allocateHearingType, selectedHearing);
                }
            }
        }
        Helper.updatePostponedDate(caseData);
    }

    private void setHearings(DateListedType dateListedType,
                              AllocateHearingType allocateHearingType,
                              HearingType selectedHearing) {
        dateListedType.setHearingTypeReadingDeliberation(allocateHearingType.getAllocateHearingReadingDeliberation());
        dateListedType.setHearingStatus(allocateHearingType.getAllocateHearingStatus());
        dateListedType.setPostponedBy(allocateHearingType.getAllocateHearingPostponedBy());

        String managingOffice = allocateHearingType.getAllocateHearingManagingOffice();
        dateListedType.setHearingVenueDayScotland(managingOffice);
        dateListedType.setHearingGlasgow(null);
        dateListedType.setHearingAberdeen(null);
        dateListedType.setHearingDundee(null);
        dateListedType.setHearingEdinburgh(null);

        final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(managingOffice);
        switch (tribunalOffice) {
            case GLASGOW:
                selectedHearing.setHearingGlasgow(allocateHearingType.getAllocateHearingVenue());
                dateListedType.setHearingGlasgow(allocateHearingType.getAllocateHearingVenue());
                break;
            case ABERDEEN:
                selectedHearing.setHearingAberdeen(allocateHearingType.getAllocateHearingVenue());
                dateListedType.setHearingAberdeen(allocateHearingType.getAllocateHearingVenue());
                break;
            case DUNDEE:
                selectedHearing.setHearingDundee(allocateHearingType.getAllocateHearingVenue());
                dateListedType.setHearingDundee(allocateHearingType.getAllocateHearingVenue());
                break;
            case EDINBURGH:
                selectedHearing.setHearingEdinburgh(allocateHearingType.getAllocateHearingVenue());
                dateListedType.setHearingEdinburgh(allocateHearingType.getAllocateHearingVenue());
                break;
            default:
                break;
        }

        dateListedType.setHearingRoom(allocateHearingType.getAllocateHearingRoom());
        dateListedType.setHearingClerk(allocateHearingType.getAllocateHearingClerk());
    }

    private HearingType getSelectedHearing(CaseData caseData) {
        return hearingSelectionService.getSelectedHearing(caseData, caseData.getAllocateHearingHearing());
    }

    private List<DateListedTypeItem> getListings(CaseData caseData) {
        return hearingSelectionService.getListings(caseData, caseData.getAllocateHearingHearing());
    }

    private boolean isVenueChanged(DateListedType listing, DynamicFixedListType newVenue) {
        DynamicFixedListType currentVenue = getCurrentVenue(listing);
        return isVenueChanged(currentVenue, newVenue);
    }

    private boolean isVenueChanged(DynamicFixedListType currentVenue, DynamicFixedListType newVenue) {
        String currentVenueCode = currentVenue != null ? currentVenue.getSelectedCode() : null;
        String newVenueCode = newVenue != null ? newVenue.getSelectedCode() : null;
        return !StringUtils.equals(currentVenueCode, newVenueCode);
    }

    private DynamicFixedListType getCurrentVenue(DateListedType listing) {
        if (listing.getHearingVenueDayScotland() == null) {
            return null;
        }
        final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(listing.getHearingVenueDayScotland());
        switch (tribunalOffice) {
            case GLASGOW:
                return listing.getHearingGlasgow();
            case ABERDEEN:
                return listing.getHearingAberdeen();
            case DUNDEE:
                return listing.getHearingDundee();
            case EDINBURGH:
                return listing.getHearingEdinburgh();
            default:
                throw new IllegalArgumentException(String.format("Unexpected Scottish office %s", tribunalOffice));
        }
    }

    private DynamicFixedListType getEmployerMembers(TribunalOffice tribunalOffice, HearingType selectedHearing) {
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.EMPLOYER_MEMBER);

        if (selectedHearing.hasHearingEmployerMember()) {
            dynamicFixedListType.setValue(selectedHearing.getHearingERMember().getValue());
        }
        return dynamicFixedListType;
    }

    private DynamicFixedListType getEmployeeMembers(TribunalOffice tribunalOffice, HearingType selectedHearing) {
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.EMPLOYEE_MEMBER);

        if (selectedHearing.hasHearingEmployeeMember()) {
            dynamicFixedListType.setValue(selectedHearing.getHearingEEMember().getValue());
        }
        return dynamicFixedListType;
    }

    private DynamicFixedListType getClerks(TribunalOffice tribunalOffice, DateListedType selectedListing) {
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.CLERK);

        if (selectedListing.hasHearingClerk()) {
            dynamicFixedListType.setValue(selectedListing.getHearingClerk().getValue());
        }
        return dynamicFixedListType;
    }
}
