package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourtWorkerSelectionServiceTest {

    @ParameterizedTest
    @MethodSource
    void testCreateCourtWorkerSelection(TribunalOffice tribunalOffice, TribunalOffice expectedTribunalOffice) {
        String expectedCode = "TestCode";
        String expectedLabel = "TestLabel";
        CourtWorkerService courtWorkerService = mock(CourtWorkerService.class);
        when(courtWorkerService.getCourtWorkerByTribunalOffice(any(TribunalOffice.class), any(CourtWorkerType.class)))
                .thenReturn(List.of(DynamicValueType.create(expectedCode, expectedLabel)));

        CourtWorkerSelectionService courtWorkerSelectionService = new CourtWorkerSelectionService(courtWorkerService);
        DynamicFixedListType selection = courtWorkerSelectionService.createCourtWorkerSelection(tribunalOffice,
            CourtWorkerType.CLERK);
        assertEquals(1, selection.getListItems().size());
        assertEquals(expectedCode, selection.getListItems().get(0).getCode());
        assertEquals(expectedLabel, selection.getListItems().get(0).getLabel());
        verify(courtWorkerService, times(1)).getCourtWorkerByTribunalOffice(
            expectedTribunalOffice, CourtWorkerType.CLERK);
    }

    private static Stream<Arguments> testCreateCourtWorkerSelection() { //NOPMD - parameterized tests source method
        return Stream.of(
                Arguments.of(TribunalOffice.ABERDEEN, TribunalOffice.SCOTLAND),
                Arguments.of(TribunalOffice.BRISTOL, TribunalOffice.BRISTOL),
                Arguments.of(TribunalOffice.DUNDEE, TribunalOffice.SCOTLAND),
                Arguments.of(TribunalOffice.EDINBURGH, TribunalOffice.SCOTLAND),
                Arguments.of(TribunalOffice.GLASGOW, TribunalOffice.SCOTLAND),
                Arguments.of(TribunalOffice.LEEDS, TribunalOffice.LEEDS),
                Arguments.of(TribunalOffice.LONDON_CENTRAL, TribunalOffice.LONDON_CENTRAL),
                Arguments.of(TribunalOffice.LONDON_EAST, TribunalOffice.LONDON_EAST),
                Arguments.of(TribunalOffice.LONDON_SOUTH, TribunalOffice.LONDON_SOUTH),
                Arguments.of(TribunalOffice.MANCHESTER, TribunalOffice.MANCHESTER),
                Arguments.of(TribunalOffice.MIDLANDS_EAST, TribunalOffice.MIDLANDS_EAST),
                Arguments.of(TribunalOffice.MIDLANDS_WEST, TribunalOffice.MIDLANDS_WEST),
                Arguments.of(TribunalOffice.NEWCASTLE, TribunalOffice.NEWCASTLE),
                Arguments.of(TribunalOffice.SCOTLAND, TribunalOffice.SCOTLAND),
                Arguments.of(TribunalOffice.WALES, TribunalOffice.WALES),
                Arguments.of(TribunalOffice.WATFORD, TribunalOffice.WATFORD));
    }
}
