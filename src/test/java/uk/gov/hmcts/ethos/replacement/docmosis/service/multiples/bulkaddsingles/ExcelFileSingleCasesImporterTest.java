package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.CaseImporterFile;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExcelFileSingleCasesImporterTest {

    @Test
    public void shouldImportCases() throws ImportException, IOException {
        String downloadUrl = "a-test-download-url";
        MultipleData multipleData = createMultipleData(downloadUrl);
        String authToken = "some-token";
        List<String> ethosCaseReferences = List.of("header", "case1", "case2", "", "case3");
        try (XSSFWorkbook workbook = createWorkbook(ethosCaseReferences)) {
            ExcelReadingService excelReadingService = mock(ExcelReadingService.class);
            when(excelReadingService.readWorkbook(authToken, downloadUrl)).thenReturn(workbook);
            ExcelFileSingleCasesImporter excelFileSingleCasesImporter = new ExcelFileSingleCasesImporter(
                excelReadingService);
            List<String> cases = excelFileSingleCasesImporter.importCases(multipleData, authToken);
            assertEquals(3, cases.size());
            assertEquals("case1", cases.get(0));
            assertEquals("case2", cases.get(1));
            assertEquals("case3", cases.get(2));
        }
    }

    @Test
    public void shouldThrowImportException() throws ImportException, IOException {
assertThrows(ImportException.class, () -> {});        String downloadUrl = "a-test-download-url";
        MultipleData multipleData = createMultipleData(downloadUrl);
        String authToken = "some-token";
        ExcelReadingService excelReadingService = mock(ExcelReadingService.class);
        when(excelReadingService.readWorkbook(authToken, downloadUrl)).thenThrow(IOException.class);

        ExcelFileSingleCasesImporter excelFileSingleCasesImporter = new ExcelFileSingleCasesImporter(
            excelReadingService);
        excelFileSingleCasesImporter.importCases(multipleData, authToken);
    }

    private MultipleData createMultipleData(String downloadUrl) {
        MultipleData multipleData = new MultipleData();
        CaseImporterFile caseImporterFile = new CaseImporterFile();
        UploadedDocumentType uploadedDocument = new UploadedDocumentType();
        uploadedDocument.setDocumentBinaryUrl(downloadUrl);
        caseImporterFile.setUploadedDocument(uploadedDocument);
        multipleData.setBulkAddSingleCasesImportFile(caseImporterFile);

        return multipleData;
    }

    private XSSFWorkbook createWorkbook(List<String> ethosCaseReferences) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        for (int i = 0; i < ethosCaseReferences.size(); i++) {
            String ethosCaseReference = ethosCaseReferences.get(i);
            sheet.createRow(i).createCell(0).setCellValue(ethosCaseReference);
        }
        return workbook;
    }

}
