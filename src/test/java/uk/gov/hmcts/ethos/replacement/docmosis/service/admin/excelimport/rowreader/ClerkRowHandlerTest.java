package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.ClerkRowHandler.CLERK_ROW_ID;

class ClerkRowHandlerTest {

    @ParameterizedTest
    @CsvSource({"TRIB_fl_Clerks, true", "fl_Judge, false"})
    void testAcceptRow(String cellValue, boolean expected) {
        var row = mock(Row.class);
        mockCell(row, 0, cellValue);
        var courtWorkerRepository = mock(CourtWorkerRepository.class);

        var clerkRowHandler = new ClerkRowHandler(courtWorkerRepository);
        assertEquals(expected, clerkRowHandler.accept(row));
    }

    @Test
    void testHandle() {
        var code = "ClerkCode";
        var name = "Clerk Kent";
        var tribunalOffice = TribunalOffice.NEWCASTLE;
        var row = mock(Row.class);
        mockCell(row, 0, CLERK_ROW_ID);
        mockCell(row, 1, code);
        mockCell(row, 2, name);
        mockCell(row, 3, "1");
        var invalidRow = mock(Row.class);
        mockCell(invalidRow, 0, "fl_Judge");

        var courtWorkerRepository = mock(CourtWorkerRepository.class);

        var clerkRowHandler = new ClerkRowHandler(courtWorkerRepository);
        clerkRowHandler.handle(tribunalOffice, row);

        var captor = ArgumentCaptor.forClass(CourtWorker.class);
        verify(courtWorkerRepository, times(1)).save(captor.capture());

        var actual = captor.getValue();
        assertEquals(CourtWorkerType.CLERK, actual.getType());
        assertEquals(code, actual.getCode());
        assertEquals(name, actual.getName());
        assertEquals(tribunalOffice, actual.getTribunalOffice());
    }

    private void mockCell(Row row, int cellNum, String value) {
        var cell = mock(Cell.class);
        when(row.getCell(cellNum)).thenReturn(cell);
        when(cell.getStringCellValue()).thenReturn(value);
    }
}
