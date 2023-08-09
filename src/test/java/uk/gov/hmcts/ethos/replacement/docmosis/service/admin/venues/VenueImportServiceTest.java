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

    private static final String TEST_TOKEN = "test-token";
    private static final String DOCUMENT_URL = "test-document-url";
    private static final String USER_NAME = "Morris Johnson";

    @BeforeEach
    void setup() {
        excelReadingService = mock(ExcelReadingService.class);
        venueFixedListSheetImporter = mock(VenueFixedListSheetImporter.class);
        fileLocationFixedListSheetImporter = mock(FileLocationFixedListSheetImporter.class);

        userService = mock(UserService.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getName()).thenReturn(USER_NAME);
        when(userService.getUserDetails(TEST_TOKEN)).thenReturn(userDetails);
    }

    @Test
    void testInitImport() {
        AdminData adminData = createAdminData(TribunalOffice.MANCHESTER);

        VenueImportService venueImportService = new VenueImportService(excelReadingService, venueFixedListSheetImporter,
                fileLocationFixedListSheetImporter, userService);
        venueImportService.initImport(adminData);

        assertNull(adminData.getVenueImport().getVenueImportFile());
        assertNull(adminData.getVenueImport().getVenueImportOffice());
    }

    @ParameterizedTest
    @MethodSource
    void testImportVenuesEnglandWales(TribunalOffice tribunalOffice) throws FixedListSheetReaderException, IOException {
        AdminData adminData = createAdminData(tribunalOffice);
        try (XSSFWorkbook workbook = createWorkbook(tribunalOffice)) {
            when(excelReadingService.readWorkbook(TEST_TOKEN, DOCUMENT_URL)).thenReturn(workbook);
        }

        VenueImportService venueImportService = new VenueImportService(excelReadingService, venueFixedListSheetImporter,
                fileLocationFixedListSheetImporter, userService);
        venueImportService.importVenues(adminData, TEST_TOKEN);

        verify(venueFixedListSheetImporter, times(1)).importSheet(eq(tribunalOffice), any(XSSFSheet.class));
        verify(fileLocationFixedListSheetImporter, times(1)).importSheet(eq(tribunalOffice), any(XSSFSheet.class));
        assertEquals(USER_NAME, adminData.getVenueImport().getVenueImportFile().getUser());
        assertNotNull(adminData.getVenueImport().getVenueImportFile().getLastImported());
    }

    private static Stream<TribunalOffice> testImportVenuesEnglandWales() { //NOPMD - parameterized tests
        return TribunalOffice.ENGLANDWALES_OFFICES.stream();
    }

    @Test
    void testImportVenuesScotland() throws FixedListSheetReaderException, IOException {
        VenueImportService venueImportService = new VenueImportService(excelReadingService, venueFixedListSheetImporter,
                fileLocationFixedListSheetImporter, userService);

        AdminData adminData = createAdminData(TribunalOffice.SCOTLAND);
        try (XSSFWorkbook workbook = createWorkbook(TribunalOffice.SCOTLAND)) {
            when(excelReadingService.readWorkbook(TEST_TOKEN, DOCUMENT_URL)).thenReturn(workbook);
        }

        venueImportService.importVenues(adminData, TEST_TOKEN);

        for (TribunalOffice tribunalOffice : TribunalOffice.SCOTLAND_OFFICES) {
            verify(venueFixedListSheetImporter, times(1)).importSheet(eq(tribunalOffice), any(XSSFSheet.class));
            verify(fileLocationFixedListSheetImporter, times(1)).importSheet(eq(tribunalOffice),
                    any(XSSFSheet.class));
        }
        verifyNoMoreInteractions(venueFixedListSheetImporter);
        verifyNoMoreInteractions(fileLocationFixedListSheetImporter);

        assertEquals(USER_NAME, adminData.getVenueImport().getVenueImportFile().getUser());
        assertNotNull(adminData.getVenueImport().getVenueImportFile().getLastImported());
    }

    private XSSFWorkbook createWorkbook(TribunalOffice tribunalOffice) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("CaseField");
        workbook.createSheet(tribunalOffice.getOfficeName() + " Scrubbed");
        workbook.createSheet("ComplexTypes");

        return workbook;
    }

    private AdminData createAdminData(TribunalOffice tribunalOffice) {
        Document document = new Document();
        document.setBinaryUrl(DOCUMENT_URL);
        ImportFile importFile = new ImportFile();
        importFile.setFile(document);
        VenueImport venueImport = new VenueImport();
        venueImport.setVenueImportOffice(tribunalOffice.getOfficeName());
        venueImport.setVenueImportFile(importFile);
        AdminData adminData = new AdminData();
        adminData.setVenueImport(venueImport);

        return adminData;
    }
}
