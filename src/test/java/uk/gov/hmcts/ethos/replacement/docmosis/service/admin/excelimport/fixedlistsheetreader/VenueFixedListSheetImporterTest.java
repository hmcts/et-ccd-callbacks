package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.RoomRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.VenueRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = VenueFixedListSheetImporter.class)
@EnableConfigurationProperties({FixedListMappings.class})
class VenueFixedListSheetImporterTest {

    private static XSSFWorkbook workbook;

    @Autowired
    private VenueFixedListSheetImporter venueFixedListSheetImporter;

    @MockBean
    private VenueRepository venueRepository;

    @MockBean
    private RoomRepository roomRepository;

    @Autowired
    private FixedListMappings fixedListMappings;

    @BeforeAll
    static void setup() throws IOException, InvalidFormatException {
        var file = new File(VenueFixedListSheetImporterTest.class.getClassLoader()
                .getResource("admin/VenuesImportFile.xlsx").getFile());
        workbook = new XSSFWorkbook(file);
    }

    @AfterAll
    static void tearDown() throws IOException {
        workbook.close();
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource
    void testImportSheet(TribunalOffice tribunalOffice, String sheetName, int expectedVenues, int expectedRooms) {
        var sheet = workbook.getSheet(sheetName);

        venueFixedListSheetImporter.importSheet(tribunalOffice, sheet);

        var venuesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(venueRepository, times(1)).saveAll(venuesArgumentCaptor.capture());
        assertEquals(expectedVenues, venuesArgumentCaptor.getValue().size());
        var roomsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(roomRepository, times(1)).saveAll(roomsArgumentCaptor.capture());
        assertEquals(expectedRooms, roomsArgumentCaptor.getValue().size());
    }

    private static Stream<Arguments> testImportSheet() {
        return Stream.of(
                Arguments.of(TribunalOffice.ABERDEEN, "Scotland Scrubbed", 9, 11),
                Arguments.of(TribunalOffice.BRISTOL, "Bristol Scrubbed", 41, 141),
                Arguments.of(TribunalOffice.DUNDEE, "Scotland Scrubbed", 2, 5),
                Arguments.of(TribunalOffice.EDINBURGH, "Scotland Scrubbed", 2, 7),
                Arguments.of(TribunalOffice.GLASGOW, "Scotland Scrubbed", 12, 33),
                Arguments.of(TribunalOffice.LEEDS, "Leeds Scrubbed", 9, 28),
                Arguments.of(TribunalOffice.LONDON_CENTRAL, "LondonCentral Scrubbed", 4, 28),
                Arguments.of(TribunalOffice.LONDON_EAST, "LondonEast Scrubbed", 4, 24),
                Arguments.of(TribunalOffice.LONDON_SOUTH, "LondonSouth Scrubbed", 3, 26),
                Arguments.of(TribunalOffice.MANCHESTER, "Manchester Scrubbed", 17, 48),
                Arguments.of(TribunalOffice.MIDLANDS_EAST, "MidlandsEast Scrubbed", 14, 54),
                Arguments.of(TribunalOffice.MIDLANDS_WEST, "MidlandsWest Scrubbed", 9, 31),
                Arguments.of(TribunalOffice.NEWCASTLE, "Newcastle Scrubbed", 11, 56),
                Arguments.of(TribunalOffice.WALES, "Wales Scrubbed", 66, 77),
                Arguments.of(TribunalOffice.WATFORD, "Watford Scrubbed", 14, 31)
        );
    }
}
