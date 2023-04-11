package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FileLocationFixedListSheetImporter.class, FileLocationRowHandler.class})
class FileLocationFixedListSheetImporterTest {

    private static XSSFWorkbook workbook;

    @Autowired
    private FileLocationFixedListSheetImporter fileLocationFixedListSheetImporter;

    @MockBean
    private FileLocationRepository fileLocationRepository;

    @BeforeAll
    static void setup() throws IOException, InvalidFormatException {
        File file = new File(FileLocationFixedListSheetImporterTest.class.getClassLoader()
                .getResource("admin/VenuesImportFile.xlsx").getFile());
        workbook = new XSSFWorkbook(file);
    }

    @AfterAll
    static void tearDown() throws IOException {
        workbook.close();
    }

    @ParameterizedTest
    @MethodSource
    void testImportSheet(TribunalOffice tribunalOffice, String sheetName, int expectedFileLocations) {
        XSSFSheet sheet = workbook.getSheet(sheetName);

        fileLocationFixedListSheetImporter.importSheet(tribunalOffice, sheet);

        verify(fileLocationRepository, times(expectedFileLocations)).save(any(FileLocation.class));
    }

    private static Stream<Arguments> testImportSheet() { //NOPMD - parameterized tests
        return Stream.of(
                Arguments.of(TribunalOffice.ABERDEEN, "Scotland Scrubbed", 46),
                Arguments.of(TribunalOffice.BRISTOL, "Bristol Scrubbed", 162),
                Arguments.of(TribunalOffice.DUNDEE, "Scotland Scrubbed", 12),
                Arguments.of(TribunalOffice.EDINBURGH, "Scotland Scrubbed", 27),
                Arguments.of(TribunalOffice.GLASGOW, "Scotland Scrubbed", 87),
                Arguments.of(TribunalOffice.LEEDS, "Leeds Scrubbed", 78),
                Arguments.of(TribunalOffice.LONDON_CENTRAL, "LondonCentral Scrubbed", 186),
                Arguments.of(TribunalOffice.LONDON_EAST, "LondonEast Scrubbed", 140),
                Arguments.of(TribunalOffice.LONDON_SOUTH, "LondonSouth Scrubbed", 208),
                Arguments.of(TribunalOffice.MANCHESTER, "Manchester Scrubbed", 88),
                Arguments.of(TribunalOffice.MIDLANDS_EAST, "MidlandsEast Scrubbed", 126),
                Arguments.of(TribunalOffice.MIDLANDS_WEST, "MidlandsWest Scrubbed", 30),
                Arguments.of(TribunalOffice.NEWCASTLE, "Newcastle Scrubbed", 123),
                Arguments.of(TribunalOffice.WALES, "Wales Scrubbed", 142),
                Arguments.of(TribunalOffice.WATFORD, "Watford Scrubbed", 262)
        );
    }
}
