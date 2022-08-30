package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileLocationFixedListSheetImporter implements FixedListSheetImporter {

    private final FileLocationRepository fileLocationRepository;
    private final FileLocationRowHandler rowHandler;

    @Override
    public void importSheet(TribunalOffice tribunalOffice, XSSFSheet sheet) {
        deleteExistingData(tribunalOffice);
        importRows(tribunalOffice, sheet);
    }

    private void deleteExistingData(TribunalOffice tribunalOffice) {
        log.info("Deleting file location data for " + tribunalOffice);
        fileLocationRepository.deleteByTribunalOffice(tribunalOffice);
    }

    private void importRows(TribunalOffice tribunalOffice, XSSFSheet sheet) {
        for (Row row : sheet) {
            if (rowHandler.accept(tribunalOffice, row)) {
                rowHandler.handle(tribunalOffice, row);
            }
        }
    }
}
