package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
class ExcelFileSingleCasesImporter implements SingleCasesImporter {
    private final ExcelReadingService excelReadingService;

    ExcelFileSingleCasesImporter(ExcelReadingService excelReadingService) {
        this.excelReadingService = excelReadingService;
    }

    @Override
    public List<String> importCases(MultipleData multipleData, String authToken) throws ImportException {
        try (XSSFWorkbook workbook = getWorkbook(multipleData, authToken)) {
            return getEthosCasesReferences(workbook);
        } catch (IOException e) {
            throw new ImportException(String.format("Unexpected error when importing Excel file for multiple %s",
                    multipleData.getMultipleReference()), e);
        }
    }

    private XSSFWorkbook getWorkbook(MultipleData multipleData, String authToken) throws IOException {
        String downloadBinaryUrl = multipleData
                .getBulkAddSingleCasesImportFile().getUploadedDocument().getDocumentBinaryUrl();
        return excelReadingService.readWorkbook(authToken, downloadBinaryUrl);
    }

    private List<String> getEthosCasesReferences(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<String> ethosCaseReferences = new ArrayList<>();

        for (Row row : sheet) {
            // Skip header row
            if (row.getRowNum() == 0) {
                continue;
            }
            Cell cell = row.getCell(0);
            String ethosReference = cell.getStringCellValue();
            if (StringUtils.isNotBlank(ethosReference)) {
                ethosCaseReferences.add(ethosReference);
            }
        }
        return ethosCaseReferences;
    }
}
