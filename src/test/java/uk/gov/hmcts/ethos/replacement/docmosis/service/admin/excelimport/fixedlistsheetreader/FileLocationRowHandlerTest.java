package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FileLocationRowHandlerTest {
    private XSSFWorkbook workbook;

    @BeforeEach
    void setup() {
        workbook = new XSSFWorkbook();
        workbook.createSheet("Test");
    }

    @AfterEach
    void tearDown() throws IOException {
        workbook.close();
    }

    @ParameterizedTest
    @MethodSource
    void testAcceptTrue(TribunalOffice tribunalOffice, String rowId) {
        Row row = createRow(rowId, "fileLocationCode", "fileLocationName");
        FileLocationRowHandler fileLocationRowHandler = new FileLocationRowHandler(mock(FileLocationRepository.class));

        assertTrue(fileLocationRowHandler.accept(tribunalOffice, row));
    }

    private static Stream<Arguments> testAcceptTrue() { //NOPMD - parameterized tests
        return Stream.of(
          Arguments.of(TribunalOffice.ABERDEEN, "fl_Locations_Aberdeen"),
                Arguments.of(TribunalOffice.BRISTOL, "fl_Location"),
                Arguments.of(TribunalOffice.DUNDEE, "fl_Locations_Dundee"),
                Arguments.of(TribunalOffice.EDINBURGH, "fl_Locations_Edinburgh"),
                Arguments.of(TribunalOffice.GLASGOW, "fl_Locations_Glasgow"),
                Arguments.of(TribunalOffice.LEEDS, "fl_Location"),
                Arguments.of(TribunalOffice.LONDON_CENTRAL, "fl_Location"),
                Arguments.of(TribunalOffice.LONDON_EAST, "fl_Location"),
                Arguments.of(TribunalOffice.LONDON_SOUTH, "fl_Location"),
                Arguments.of(TribunalOffice.MANCHESTER, "fl_Location"),
                Arguments.of(TribunalOffice.MIDLANDS_EAST, "fl_Location"),
                Arguments.of(TribunalOffice.MIDLANDS_WEST, "fl_Location"),
                Arguments.of(TribunalOffice.NEWCASTLE, "fl_Location"),
                Arguments.of(TribunalOffice.WALES, "fl_Location"),
                Arguments.of(TribunalOffice.WATFORD, "fl_Location")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAcceptFalse(TribunalOffice tribunalOffice, String rowId) {
        Row row = createRow(rowId, "fileLocationCode", "fileLocationName");
        FileLocationRowHandler fileLocationRowHandler = new FileLocationRowHandler(mock(FileLocationRepository.class));

        assertFalse(fileLocationRowHandler.accept(tribunalOffice, row));
    }

    private static Stream<Arguments> testAcceptFalse() { //NOPMD - parameterized tests
        return Stream.of(
                Arguments.of(TribunalOffice.ABERDEEN, "fl_Location"),
                Arguments.of(TribunalOffice.BRISTOL, "fl_Locations_Bristol"),
                Arguments.of(TribunalOffice.DUNDEE, "fl_Location"),
                Arguments.of(TribunalOffice.EDINBURGH, "fl_Location"),
                Arguments.of(TribunalOffice.GLASGOW, "fl_Location"),
                Arguments.of(TribunalOffice.LEEDS, "fl_Locations_Leeds"),
                Arguments.of(TribunalOffice.LONDON_CENTRAL, "fl_Locations_LondonCentral"),
                Arguments.of(TribunalOffice.LONDON_EAST, "fl_Locations_LondonEast"),
                Arguments.of(TribunalOffice.LONDON_SOUTH, "fl_Locations_LondonSouth"),
                Arguments.of(TribunalOffice.MANCHESTER, "fl_Locations_Manchester"),
                Arguments.of(TribunalOffice.MIDLANDS_EAST, "fl_Locations_MidlandsEast"),
                Arguments.of(TribunalOffice.MIDLANDS_WEST, "fl_Locations_MidlandsWest"),
                Arguments.of(TribunalOffice.NEWCASTLE, "fl_Locations_Newcastle"),
                Arguments.of(TribunalOffice.WALES, "fl_Locations_Wales"),
                Arguments.of(TribunalOffice.WATFORD, "fl_Locations_Watford")
        );
    }

    @Test
    void testHandle() {
        TribunalOffice tribunalOffice = TribunalOffice.LEEDS;
        List<Row> rows = List.of(
                createRow("fl_Judges", "judge1", "Judge 1"),
                createRow("fl_Location", "leeds1", "Leeds File Location 1"),
                createRow("fl_Location", "leeds2", "Leeds File Location 2"),
                createRow("fl_Location", "leeds3", "Leeds File Location 3"),
                createRow("VenueLeeds", "York", "York")
        );
        FileLocationRepository fileLocationRepository = mock(FileLocationRepository.class);

        FileLocationRowHandler fileLocationRowHandler = new FileLocationRowHandler(fileLocationRepository);
        for (Row row : rows) {
            if (fileLocationRowHandler.accept(tribunalOffice, row)) {
                fileLocationRowHandler.handle(tribunalOffice, row);
            }
        }

        ArgumentCaptor<FileLocation> fileLocationArgumentCaptor = ArgumentCaptor.forClass(FileLocation.class);
        verify(fileLocationRepository, times(3)).save(fileLocationArgumentCaptor.capture());
        List<FileLocation> actualFileLocations = fileLocationArgumentCaptor.getAllValues();
        assertEquals(3, actualFileLocations.size());
        verifyFileLocation("leeds1", "Leeds File Location 1", actualFileLocations.get(0));
        verifyFileLocation("leeds2", "Leeds File Location 2", actualFileLocations.get(1));
        verifyFileLocation("leeds3", "Leeds File Location 3", actualFileLocations.get(2));
    }

    private Row createRow(String listId, String code, String name) {
        XSSFSheet sheet = workbook.getSheet("Test");
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0, CellType.STRING);
        row.getCell(0).setCellValue(listId);
        row.createCell(1, CellType.STRING);
        row.getCell(1).setCellValue(code);
        row.createCell(2, CellType.STRING);
        row.getCell(2).setCellValue(name);

        return row;
    }

    private void verifyFileLocation(String expectedCode, String expectedName, FileLocation actualFileLocation) {
        assertEquals(expectedCode, actualFileLocation.getCode());
        assertEquals(expectedName, actualFileLocation.getName());
        assertEquals(TribunalOffice.LEEDS, actualFileLocation.getTribunalOffice());
    }
}
