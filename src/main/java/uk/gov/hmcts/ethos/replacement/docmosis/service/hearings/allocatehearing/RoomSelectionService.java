package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.RoomService;

@Service
public class RoomSelectionService {
    private final RoomService roomService;

    public RoomSelectionService(RoomService roomService) {
        this.roomService = roomService;
    }

    public DynamicFixedListType createRoomSelection(CaseData caseData, DateListedType selectedListing,
                                                    boolean venueChanged) {
        var selectedVenue = caseData.getAllocateHearingVenue();
        var venueId = selectedVenue.getValue().getCode();

        var listItems = roomService.getRooms(venueId);
        if (venueChanged) {
            return DynamicFixedListType.from(listItems);
        } else {
            var selectedRoom = selectedListing.getHearingRoom();
            return DynamicFixedListType.from(listItems, selectedRoom);
        }
    }
}
