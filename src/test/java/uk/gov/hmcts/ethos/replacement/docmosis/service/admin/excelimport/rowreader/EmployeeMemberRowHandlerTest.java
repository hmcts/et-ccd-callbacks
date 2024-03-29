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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.EmployeeMemberRowHandler.EMPLOYEE_MEMBER_ROW_ID;

class EmployeeMemberRowHandlerTest {
    @ParameterizedTest
    @CsvSource({"TRIB_fl_EEMember, true", "fl_Judge, false"})
    void testAcceptRow(String cellValue, boolean expected) {
        Row row = mock(Row.class);
        mockCell(row, 0, cellValue);
        CourtWorkerRepository courtWorkerRepository = mock(CourtWorkerRepository.class);

        EmployeeMemberRowHandler employeeMemberRowHandler = new EmployeeMemberRowHandler(courtWorkerRepository);
        assertEquals(expected, employeeMemberRowHandler.accept(row));
    }

    @Test
    void testHandle() {
        String code = "EECode";
        String name = "Employee Member";
        Row row = mock(Row.class);
        mockCell(row, 0, EMPLOYEE_MEMBER_ROW_ID);
        mockCell(row, 1, code);
        mockCell(row, 2, name);
        mockCell(row, 3, "1");
        Row invalidRow = mock(Row.class);
        mockCell(invalidRow, 0, "fl_Judge");

        CourtWorkerRepository courtWorkerRepository = mock(CourtWorkerRepository.class);

        EmployeeMemberRowHandler employeeMemberRowHandler = new EmployeeMemberRowHandler(courtWorkerRepository);
        TribunalOffice tribunalOffice = TribunalOffice.NEWCASTLE;
        employeeMemberRowHandler.handle(tribunalOffice, row);

        ArgumentCaptor<CourtWorker> captor = ArgumentCaptor.forClass(CourtWorker.class);
        verify(courtWorkerRepository, times(1)).save(captor.capture());

        CourtWorker actual = captor.getValue();
        assertEquals(CourtWorkerType.EMPLOYEE_MEMBER, actual.getType());
        assertEquals(code, actual.getCode());
        assertEquals(name, actual.getName());
        assertEquals(tribunalOffice, actual.getTribunalOffice());
    }

    private void mockCell(Row row, int cellNum, String value) {
        Cell cell = mock(Cell.class);
        when(row.getCell(cellNum)).thenReturn(cell);
        when(cell.getStringCellValue()).thenReturn(value);
    }
}
