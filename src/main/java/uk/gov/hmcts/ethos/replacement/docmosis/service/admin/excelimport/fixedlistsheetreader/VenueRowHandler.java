package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Venue;

import java.util.ArrayList;
import java.util.List;

@Slf4j
class VenueRowHandler {
    private final String rowId;

    private final List<Venue> venues = new ArrayList<>();

    VenueRowHandler(String rowId) {
        this.rowId = rowId;
    }

    boolean accept(Row row) {
        Cell cell = row.getCell(0);
        return cell != null && rowId.equals(cell.getStringCellValue());
    }

    void handle(TribunalOffice tribunalOffice, Row row) {
        String code = row.getCell(1).getStringCellValue();
        String name = row.getCell(2).getStringCellValue();

        Venue venue = new Venue();
        venue.setCode(code);
        venue.setName(name);
        venue.setTribunalOffice(tribunalOffice);

        venues.add(venue);
        log.info("Found " + venue.getCode());
    }

    List<Venue> getVenues() {
        return venues;
    }
}
