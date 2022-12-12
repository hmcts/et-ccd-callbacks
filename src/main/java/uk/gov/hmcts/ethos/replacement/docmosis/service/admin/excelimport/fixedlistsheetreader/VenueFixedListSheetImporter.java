package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Room;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Venue;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.RoomRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.VenueRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VenueFixedListSheetImporter implements FixedListSheetImporter {

    private final VenueRepository venueRepository;
    private final RoomRepository roomRepository;
    private final FixedListMappings fixedListMappings;

    @Override
    public void importSheet(TribunalOffice tribunalOffice, XSSFSheet sheet) {
        deleteExistingData(tribunalOffice);

        String fixedListId = getVenuesFixedListId(tribunalOffice);
        List<Venue> venues = getVenues(tribunalOffice, fixedListId, sheet);
        venueRepository.saveAll(venues);
        List<Room> rooms = getRooms(tribunalOffice, venues, sheet);
        roomRepository.saveAll(rooms);
    }

    private void deleteExistingData(TribunalOffice tribunalOffice) {
        log.info("Deleting venue data for " + tribunalOffice);
        List<Venue> venues = venueRepository.findByTribunalOffice(tribunalOffice);
        for (Venue venue : venues) {
            List<Room> rooms = roomRepository.findByVenueCode(venue.getCode());
            roomRepository.deleteAll(rooms);
        }
        venueRepository.deleteAll(venues);
    }

    private String getVenuesFixedListId(TribunalOffice tribunalOffice) {
        switch (tribunalOffice) {
            case MIDLANDS_EAST:
                return "VenueNottingham";
            case MIDLANDS_WEST:
                return "VenueBirmingham";
            default:
                return "Venue" + tribunalOffice.getOfficeName().replace(" ", "");
        }
    }

    private List<Venue> getVenues(TribunalOffice tribunalOffice, String fixedListId, XSSFSheet sheet) {
        VenueRowHandler venueRowHandler = new VenueRowHandler(fixedListId);
        for (Row row : sheet) {
            if (venueRowHandler.accept(row)) {
                venueRowHandler.handle(tribunalOffice, row);
            }
        }

        return venueRowHandler.getVenues();
    }

    private List<Room> getRooms(TribunalOffice tribunalOffice, List<Venue> venues, XSSFSheet sheet) {
        RoomRowHandler roomRowHandler = new RoomRowHandler(tribunalOffice, fixedListMappings, venues);
        for (Row row : sheet) {
            if (roomRowHandler.accept(row)) {
                roomRowHandler.handle(row);
            }
        }

        return roomRowHandler.getRooms();
    }
}
