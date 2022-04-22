package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.venues;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.Document;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.VenueImport;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader.FileLocationFixedListSheetImporter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader.FixedListSheetReaderException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader.VenueFixedListSheetImporter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class VenueImportServiceTest {

    private ExcelReadingService excelReadingService;
    private VenueFixedListSheetImporter venueFixedListSheetImporter;
    private FileLocationFixedListSheetImporter fileLocationFixedListSheetImporter;
    private UserService userService;

    private final String userToken = "test-token";
    private final String documentUrl = "test-document-url";
    private final String userName = "Morris Johnson";

    @BeforeEach
    void setup() {
        excelReadingService = mock(ExcelReadingService.class);
        venueFixedListSheetImporter = mock(VenueFixedListSheetImporter.class);
        fileLocationFixedListSheetImporter = mock(FileLocationFixedListSheetImporter.class);

        userService = mock(UserService.class);
        var userDetails = mock(UserDetails.class);
        when(userDetails.getName()).thenReturn(userName);
        when(userService.getUserDetails(userToken)).thenReturn(userDetails);
    }

    @Test
    void testInitImport() {
        var adminData = createAdminData(TribunalOffice.MANCHESTER);

        var venueImportService = new VenueImportService(excelReadingService, venueFixedListSheetImporter,
                fileLocationFixedListSheetImporter, userService);
        venueImportService.initImport(adminData);

        assertNull(adminData.getVenueImport().getVenueImportFile());
        assertNull(adminData.getVenueImport().getVenueImportOffice());
    }

    @ParameterizedTest
    @MethodSource
    void testImportVenuesEnglandWales(TribunalOffice tribunalOffice) throws FixedListSheetReaderException, IOException {
        var adminData = createAdminData(tribunalOffice);
        var workbook = createWorkbook(tribunalOffice);
        when(excelReadingService.readWorkbook(userToken, documentUrl)).thenReturn(workbook);

        var venueImportService = new VenueImportService(excelReadingService, venueFixedListSheetImporter,
                fileLocationFixedListSheetImporter, userService);
        venueImportService.importVenues(adminData, userToken);

        verify(venueFixedListSheetImporter, times(1)).importSheet(eq(tribunalOffice), any(XSSFSheet.class));
        verify(fileLocationFixedListSheetImporter, times(1)).importSheet(eq(tribunalOffice), any(XSSFSheet.class));
        assertEquals(userName, adminData.getVenueImport().getVenueImportFile().getUser());
        assertNotNull(adminData.getVenueImport().getVenueImportFile().getLastImported());
    }

    private static Stream<TribunalOffice> testImportVenuesEnglandWales() {
        return TribunalOffice.ENGLANDWALES_OFFICES.stream();
    }

    @Test
    void testImportVenuesScotland() throws FixedListSheetReaderException, IOException {
        var venueImportService = new VenueImportService(excelReadingService, venueFixedListSheetImporter,
                fileLocationFixedListSheetImporter, userService);

        var adminData = createAdminData(TribunalOffice.SCOTLAND);
        var workbook = createWorkbook(TribunalOffice.SCOTLAND);
        when(excelReadingService.readWorkbook(userToken, documentUrl)).thenReturn(workbook);

        venueImportService.importVenues(adminData, userToken);

        for (TribunalOffice tribunalOffice : TribunalOffice.SCOTLAND_OFFICES) {
            verify(venueFixedListSheetImporter, times(1)).importSheet(eq(tribunalOffice), any(XSSFSheet.class));
            verify(fileLocationFixedListSheetImporter, times(1)).importSheet(eq(tribunalOffice),
                    any(XSSFSheet.class));
        }
        verifyNoMoreInteractions(venueFixedListSheetImporter);
        verifyNoMoreInteractions(fileLocationFixedListSheetImporter);

        assertEquals(userName, adminData.getVenueImport().getVenueImportFile().getUser());
        assertNotNull(adminData.getVenueImport().getVenueImportFile().getLastImported());
    }

    private XSSFWorkbook createWorkbook(TribunalOffice tribunalOffice) {
        var workbook = new XSSFWorkbook();
        workbook.createSheet("CaseField");
        workbook.createSheet(tribunalOffice.getOfficeName() + " Scrubbed");
        workbook.createSheet("ComplexTypes");

        return workbook;
    }

    private AdminData createAdminData(TribunalOffice tribunalOffice) {
        var document = new Document();
        document.setBinaryUrl(documentUrl);
        var importFile = new ImportFile();
        importFile.setFile(document);
        var venueImport = new VenueImport();
        venueImport.setVenueImportOffice(tribunalOffice.getOfficeName());
        venueImport.setVenueImportFile(importFile);
        var adminData = new AdminData();
        adminData.setVenueImport(venueImport);

        return adminData;
    }
}
