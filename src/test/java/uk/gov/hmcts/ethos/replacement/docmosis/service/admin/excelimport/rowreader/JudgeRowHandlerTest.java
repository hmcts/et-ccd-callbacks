package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.JudgeRowHandler.JUDGE_ROW_ID;

class JudgeRowHandlerTest {

    @ParameterizedTest
    @CsvSource({"fl_Judge, true", "fl_Clerk, false"})
    void testAcceptRow(String cellValue, boolean expected) {
        Row row = mock(Row.class);
        mockCell(row, 0, cellValue);
        JudgeRepository judgeRepository = mock(JudgeRepository.class);

        JudgeRowHandler judgeRowHandler = new JudgeRowHandler(judgeRepository);
        assertEquals(expected, judgeRowHandler.accept(row));
    }

    @ParameterizedTest
    @CsvSource({"S, SALARIED", "FP, FEE_PAID", "Unknown, UNKNOWN", "x, UNKNOWN"})
    void testHandle(String statusCode, String expectedEmploymentStatus) {
        String code = "01_JudgeCode";
        String name = "Judge Fudge";
        Row row = mock(Row.class);
        mockCell(row, 0, JUDGE_ROW_ID);
        mockCell(row, 1, code);
        mockCell(row, 2, name);
        mockCell(row, 3, "1");
        mockCell(row, 4, statusCode);

        JudgeRepository judgeRepository = mock(JudgeRepository.class);
        JudgeRowHandler judgeRowHandler = new JudgeRowHandler(judgeRepository);
        TribunalOffice tribunalOffice = TribunalOffice.NEWCASTLE;
        judgeRowHandler.handle(tribunalOffice, row);

        ArgumentCaptor<Judge> captor = ArgumentCaptor.forClass(Judge.class);
        verify(judgeRepository, times(1)).save(captor.capture());

        Judge actual = captor.getValue();
        assertEquals(code, actual.getCode());
        assertEquals(name, actual.getName());
        assertEquals(expectedEmploymentStatus, actual.getEmploymentStatus().name());
        assertEquals(tribunalOffice, actual.getTribunalOffice());
    }

    @Test
    void testEmptyValue() {
        Row row = mock(Row.class);
        mockCell(row, 0, JUDGE_ROW_ID);
        mockCell(row, 1, "01_JudgeCode");
        mockCell(row, 2, "Judge Fudge");
        mockCell(row, 3, "1");
        mockCell(row, 4, null);

        JudgeRepository judgeRepository = mock(JudgeRepository.class);
        JudgeRowHandler judgeRowHandler = new JudgeRowHandler(judgeRepository);
        TribunalOffice tribunalOffice = TribunalOffice.NEWCASTLE;
        judgeRowHandler.handle(tribunalOffice, row);

        ArgumentCaptor<Judge> captor = ArgumentCaptor.forClass(Judge.class);
        verify(judgeRepository, times(1)).save(captor.capture());

        Judge actual = captor.getValue();
        assertEquals(JudgeEmploymentStatus.UNKNOWN, actual.getEmploymentStatus());
    }

    private void mockCell(Row row, int cellNum, String value) {
        Cell cell = mock(Cell.class);
        when(row.getCell(cellNum)).thenReturn(cell);
        when(cell.getStringCellValue()).thenReturn(value);
    }
}
