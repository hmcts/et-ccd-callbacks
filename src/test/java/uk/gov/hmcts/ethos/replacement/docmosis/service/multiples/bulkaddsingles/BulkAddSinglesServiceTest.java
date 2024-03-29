package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleAmendService;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BulkAddSinglesServiceTest {
    private BulkAddSinglesService bulkAddSinglesService;
    private SingleCasesImporter singleCasesImporter;
    private MultipleAmendService multipleAmendService;
    private MultipleDetails multipleDetails;
    private static final String AUTH_TOKEN = "some-token";

    @BeforeEach
    public void setup() {
        multipleDetails = createMultipleDetails();

        singleCasesImporter = mock(SingleCasesImporter.class);
        multipleAmendService = mock(MultipleAmendService.class);
        bulkAddSinglesService = new BulkAddSinglesService(singleCasesImporter, multipleAmendService);
    }

    @Test
    void shouldSubmitCases() throws ImportException {
        List<String> ethosCaseReferences = List.of("case1");
        when(singleCasesImporter.importCases(multipleDetails.getCaseData(), AUTH_TOKEN))
            .thenReturn(ethosCaseReferences);

        List<String> errors = bulkAddSinglesService.execute(multipleDetails, AUTH_TOKEN);

        assertTrue(errors.isEmpty());
        verify(multipleAmendService, times(1)).bulkAmendMultipleLogic(anyString(),
            any(MultipleDetails.class),
                anyList());
    }

    @Test
    void shouldReturnErrorWhenImportCasesFails() throws ImportException {
        when(singleCasesImporter.importCases(multipleDetails.getCaseData(), AUTH_TOKEN))
                .thenThrow(ImportException.class);

        List<String> errors = bulkAddSinglesService.execute(multipleDetails, AUTH_TOKEN);

        assertEquals(1, errors.size());
        assertEquals("Unexpected error when importing single cases", errors.get(0));
    }

    private MultipleDetails createMultipleDetails() {
        MultipleData multipleData = new MultipleData();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        return multipleDetails;
    }
}
