package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileLocationRowHandler {
    static final String FILE_LOCATION_ENGLANDWALES_ROW_ID = "fl_Location";
    static final String FILE_LOCATION_SCOTLAND_ROW_ID = "fl_Locations_%s";

    private final FileLocationRepository fileLocationRepository;

    public boolean accept(TribunalOffice tribunalOffice, Row row) {
        var cell = row.getCell(0);
        if (cell == null) {
            return false;
        }

        var expectedRowId = getRowId(tribunalOffice);
        return expectedRowId.equals(cell.getStringCellValue());
    }

    public void handle(TribunalOffice tribunalOffice, Row row) {
        var fileLocation = rowToFileLocation(tribunalOffice, row);
        log.info("File location " + fileLocation.getCode());
        fileLocationRepository.save(fileLocation);
    }

    private String getRowId(TribunalOffice tribunalOffice) {
        if (TribunalOffice.isScotlandOffice(tribunalOffice.getOfficeName())) {
            return String.format(FILE_LOCATION_SCOTLAND_ROW_ID, tribunalOffice.getOfficeName());
        } else {
            return FILE_LOCATION_ENGLANDWALES_ROW_ID;
        }
    }

    private FileLocation rowToFileLocation(TribunalOffice tribunalOffice, Row row) {
        var code = row.getCell(1).getStringCellValue();
        var name = row.getCell(2).getStringCellValue();

        var fileLocation = new FileLocation();
        fileLocation.setCode(code);
        fileLocation.setName(name);
        fileLocation.setTribunalOffice(tribunalOffice);

        return fileLocation;
    }
}
