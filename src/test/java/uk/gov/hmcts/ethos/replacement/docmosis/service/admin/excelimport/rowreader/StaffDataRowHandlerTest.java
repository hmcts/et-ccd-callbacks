package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Row;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StaffDataRowHandlerTest {

    @Test
    void testAcceptRowFirstHandlerAccepts() {
        RowHandler handler1 = mockRowHandler(true);
        RowHandler handler2 = mockRowHandler(false);
        StaffDataRowHandler staffDataRowHandler = new StaffDataRowHandler(List.of(handler1, handler2));
        Row row = mock(Row.class);

        assertTrue(staffDataRowHandler.accept(row));

        verify(handler1, times(1)).accept(row);
        verify(handler2, never()).accept(row);
    }

    @Test
    void testAcceptRowSecondHandlerAccepts() {
        RowHandler handler1 = mockRowHandler(false);
        RowHandler handler2 = mockRowHandler(true);
        StaffDataRowHandler staffDataRowHandler = new StaffDataRowHandler(List.of(handler1, handler2));
        Row row = mock(Row.class);

        assertTrue(staffDataRowHandler.accept(row));

        verify(handler1, times(1)).accept(row);
        verify(handler2, times(1)).accept(row);
    }

    @Test
    void testAcceptRowNoneAccepts() {
        RowHandler handler1 = mockRowHandler(false);
        RowHandler handler2 = mockRowHandler(false);
        StaffDataRowHandler staffDataRowHandler = new StaffDataRowHandler(List.of(handler1, handler2));
        Row row = mock(Row.class);

        assertFalse(staffDataRowHandler.accept(row));

        verify(handler1, times(1)).accept(row);
        verify(handler2, times(1)).accept(row);
    }

    @Test
    void testHandleFirstHandlerAccepts() {
        RowHandler handler1 = mockRowHandler(true);
        RowHandler handler2 = mockRowHandler(false);
        StaffDataRowHandler staffDataRowHandler = new StaffDataRowHandler(List.of(handler1, handler2));
        Row row = mock(Row.class);

        staffDataRowHandler.handle(TribunalOffice.ABERDEEN, row);

        verify(handler1, times(1)).handle(TribunalOffice.ABERDEEN, row);
        verify(handler2, never()).accept(row);
    }

    @Test
    void testHandleSecondHandlerAccepts() {
        RowHandler handler1 = mockRowHandler(false);
        RowHandler handler2 = mockRowHandler(true);
        StaffDataRowHandler staffDataRowHandler = new StaffDataRowHandler(List.of(handler1, handler2));
        Row row = mock(Row.class);

        staffDataRowHandler.handle(TribunalOffice.ABERDEEN, row);

        verify(handler1, never()).handle(TribunalOffice.ABERDEEN, row);
        verify(handler2, times(1)).handle(TribunalOffice.ABERDEEN, row);
    }

    @Test
    void testHandleNoneAccepts() {
        RowHandler handler1 = mockRowHandler(false);
        RowHandler handler2 = mockRowHandler(false);
        StaffDataRowHandler staffDataRowHandler = new StaffDataRowHandler(List.of(handler1, handler2));
        Row row = mock(Row.class);

        staffDataRowHandler.handle(TribunalOffice.ABERDEEN, row);

        verify(handler1, never()).handle(TribunalOffice.ABERDEEN, row);
        verify(handler2, never()).handle(TribunalOffice.ABERDEEN, row);
    }

    private RowHandler mockRowHandler(boolean accept) {
        RowHandler handler = mock(RowHandler.class);
        when(handler.accept(isA(Row.class))).thenReturn(accept);
        return handler;
    }
}
