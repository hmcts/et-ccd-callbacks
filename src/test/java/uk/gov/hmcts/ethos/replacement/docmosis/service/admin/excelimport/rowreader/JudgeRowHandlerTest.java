package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
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
        var row = mock(Row.class);
        mockCell(row, 0, cellValue);
        var judgeRepository = mock(JudgeRepository.class);

        var judgeRowHandler = new JudgeRowHandler(judgeRepository);
        assertEquals(expected, judgeRowHandler.accept(row));
    }

    @ParameterizedTest
    @CsvSource({"S, SALARIED", "FP, FEE_PAID", "Unknown, UNKNOWN", "x, UNKNOWN"})
    void testHandle(String statusCode, String expectedEmploymentStatus) {
        var code = "01_JudgeCode";
        var name = "Judge Fudge";
        var tribunalOffice = TribunalOffice.NEWCASTLE;
        var row = mock(Row.class);
        mockCell(row, 0, JUDGE_ROW_ID);
        mockCell(row, 1, code);
        mockCell(row, 2, name);
        mockCell(row, 3, "1");
        mockCell(row, 4, statusCode);

        var judgeRepository = mock(JudgeRepository.class);
        var judgeRowHandler = new JudgeRowHandler(judgeRepository);
        judgeRowHandler.handle(tribunalOffice, row);

        var captor = ArgumentCaptor.forClass(Judge.class);
        verify(judgeRepository, times(1)).save(captor.capture());

        var actual = captor.getValue();
        assertEquals(code, actual.getCode());
        assertEquals(name, actual.getName());
        assertEquals(expectedEmploymentStatus, actual.getEmploymentStatus().name());
        assertEquals(tribunalOffice, actual.getTribunalOffice());
    }

    private void mockCell(Row row, int cellNum, String value) {
        var cell = mock(Cell.class);
        when(row.getCell(cellNum)).thenReturn(cell);
        when(cell.getStringCellValue()).thenReturn(value);
    }
}
