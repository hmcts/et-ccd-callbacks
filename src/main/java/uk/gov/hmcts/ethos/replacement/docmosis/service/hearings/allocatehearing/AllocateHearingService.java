package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@SuppressWarnings({"PMD.ConfusingTernary", "PMD.AvoidInstantiatingObjectsInLoops"})
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
        if (selectedHearing != null && !CollectionUtils.isEmpty(selectedHearing.getHearingDateCollection())) {
            caseData.setAllocateHearingJudge(judgeSelectionService.createJudgeSelection(
                    managingOffice, selectedHearing));
            caseData.setAllocateHearingSitAlone(selectedHearing.getHearingSitAlone());
            addEmployerMembers(caseData, selectedHearing, caseData.getManagingOffice());
            addEmployeeMembers(caseData, selectedHearing, caseData.getManagingOffice());
            addAllocateHearingTypeItems(selectedHearing, caseData);
        }
    }

    private void addAllocateHearingTypeItems(HearingType selectedHearing, CaseData caseData) {
        TribunalOffice managingOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        List<AllocateHearingTypeItem> allocateHearingTypeItemList = new ArrayList<>();
        for (DateListedTypeItem dateListedTypeItem : selectedHearing.getHearingDateCollection()) {
            DateListedType dateListedType = dateListedTypeItem.getValue();
            AllocateHearingType allocateHearingType = new AllocateHearingType();
            allocateHearingType.setAllocateHearingDate(dateListedType.getListedDate());
            allocateHearingType.setAllocateHearingRoom(dateListedType.getHearingRoom());
            allocateHearingType.setAllocateHearingVenue(venueSelectionService.createVenueSelection(
                    managingOffice, dateListedType));
            allocateHearingType.setAllocateHearingPostponedBy(dateListedType.getPostponedBy());
            allocateHearingType.setAllocateHearingStatus(dateListedType.getHearingStatus());
            addClerk(allocateHearingType, dateListedType, caseData.getManagingOffice());
            AllocateHearingTypeItem allocateHearingItem = new AllocateHearingTypeItem();
            allocateHearingItem.setId(UUID.randomUUID().toString());
            allocateHearingItem.setValue(allocateHearingType);
            allocateHearingTypeItemList.add(allocateHearingItem);
        }
        if (!CollectionUtils.isEmpty(allocateHearingTypeItemList)) {
            caseData.setAllocateHearingCollection(allocateHearingTypeItemList);
        }
    }

    public void populateRooms(CaseData caseData) {
        List<DateListedTypeItem> listings = getListings(caseData);
        if (!CollectionUtils.isEmpty(listings)) {
            for (DateListedTypeItem dateListedTypeItem : listings) {
                DateListedType listing = dateListedTypeItem.getValue();
                for (AllocateHearingTypeItem allocateHearingTypeItem : caseData.getAllocateHearingCollection()) {
                    setAllocateHearingRoom(allocateHearingTypeItem, listing);
                }
            }
        }
    }

    private void setAllocateHearingRoom(AllocateHearingTypeItem allocateHearingTypeItem, DateListedType listing) {
        AllocateHearingType allocateHearingType = allocateHearingTypeItem.getValue();
        if (allocateHearingType.getAllocateHearingDate().equals(listing.getListedDate())) {
            boolean venueChanged = isVenueChanged(listing.getHearingVenueDay(),
                    allocateHearingType.getAllocateHearingVenue());
            allocateHearingType.setAllocateHearingRoom(roomSelectionService.createRoomSelection(
                    allocateHearingType.getAllocateHearingVenue(),
                    listing,
                    venueChanged));
        }
    }

    public void updateCase(CaseData caseData) {
        HearingType selectedHearing = getSelectedHearing(caseData);
        selectedHearing.setHearingSitAlone(caseData.getAllocateHearingSitAlone());
        selectedHearing.setJudge(caseData.getAllocateHearingJudge());
        selectedHearing.setHearingERMember(caseData.getAllocateHearingEmployerMember());
        selectedHearing.setHearingEEMember(caseData.getAllocateHearingEmployeeMember());

        if (!CollectionUtils.isEmpty(selectedHearing.getHearingDateCollection())) {
            for (DateListedTypeItem dateListedTypeItem : selectedHearing.getHearingDateCollection()) {
                DateListedType dateListedType = dateListedTypeItem.getValue();
                for (AllocateHearingTypeItem allocateHearingTypeItem : caseData.getAllocateHearingCollection()) {
                    AllocateHearingType allocateHearingType = allocateHearingTypeItem.getValue();
                    if (allocateHearingType.getAllocateHearingDate().equals(dateListedType.getListedDate())) {
                        dateListedType.setHearingStatus(allocateHearingType.getAllocateHearingStatus());
                        dateListedType.setPostponedBy(allocateHearingType.getAllocateHearingPostponedBy());
                        dateListedType.setHearingVenueDay(allocateHearingType.getAllocateHearingVenue());
                        dateListedType.setHearingRoom(allocateHearingType.getAllocateHearingRoom());
                        dateListedType.setHearingClerk(allocateHearingType.getAllocateHearingClerk());
                    }
                }
            }
        }
        Helper.updatePostponedDate(caseData);
    }

    private HearingType getSelectedHearing(CaseData caseData) {
        return hearingSelectionService.getSelectedHearing(caseData, caseData.getAllocateHearingHearing());
    }

    private List<DateListedTypeItem> getListings(CaseData caseData) {
        return hearingSelectionService.getListings(caseData, caseData.getAllocateHearingHearing());
    }

    private boolean isVenueChanged(DynamicFixedListType currentVenue, DynamicFixedListType newVenue) {
        String currentVenueCode = currentVenue != null ? currentVenue.getSelectedCode() : null;
        String newVenueCode = newVenue != null ? newVenue.getSelectedCode() : null;
        return !StringUtils.equals(currentVenueCode, newVenueCode);
    }

    private void addEmployerMembers(CaseData caseData, HearingType selectedHearing, String managingOffice) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(managingOffice);
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.EMPLOYER_MEMBER);

        if (selectedHearing.hasHearingEmployerMember()) {
            dynamicFixedListType.setValue(selectedHearing.getHearingERMember().getValue());
        }
        caseData.setAllocateHearingEmployerMember(dynamicFixedListType);
    }

    private void addEmployeeMembers(CaseData caseData, HearingType selectedHearing, String managingOffice) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(managingOffice);
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.EMPLOYEE_MEMBER);

        if (selectedHearing.hasHearingEmployeeMember()) {
            dynamicFixedListType.setValue(selectedHearing.getHearingEEMember().getValue());
        }
        caseData.setAllocateHearingEmployeeMember(dynamicFixedListType);
    }

    private void addClerk(AllocateHearingType allocateHearingType,
                          DateListedType selectedListing,
                          String managingOffice) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(managingOffice);
        DynamicFixedListType dynamicFixedListType = courtWorkerSelectionService.createCourtWorkerSelection(
            tribunalOffice, CourtWorkerType.CLERK);

        if (selectedListing.hasHearingClerk()) {
            dynamicFixedListType.setValue(selectedListing.getHearingClerk().getValue());
        }
        allocateHearingType.setAllocateHearingClerk(dynamicFixedListType);
    }
}
