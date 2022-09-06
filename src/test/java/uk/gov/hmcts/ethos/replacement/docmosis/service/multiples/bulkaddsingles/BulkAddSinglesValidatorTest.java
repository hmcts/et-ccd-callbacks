package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;

public class BulkAddSinglesValidatorTest {
    private BulkAddSinglesValidator bulkAddSinglesValidator;
    private SingleCasesImporter singleCasesImporter;
    private SingleCasesValidator singleCasesValidator;

    private MultipleDetails multipleDetails;
    private static final String AUTH_TOKEN = "some-token";
    private List<String> ethosCaseReferences;
    private List<ValidatedSingleCase> validatedSingleCases;

    @Before
    public void setup() throws ImportException, IOException {
        multipleDetails = createMultipleDetails();
        ethosCaseReferences = new ArrayList<>();
        singleCasesImporter = mock(SingleCasesImporter.class);
        when(singleCasesImporter.importCases(multipleDetails.getCaseData(), AUTH_TOKEN))
                .thenReturn(ethosCaseReferences);

        validatedSingleCases = new ArrayList<>();
        singleCasesValidator = mock(SingleCasesValidator.class);
        when(singleCasesValidator.getValidatedCases(ethosCaseReferences, multipleDetails, AUTH_TOKEN))
                .thenReturn(validatedSingleCases);

        bulkAddSinglesValidator = new BulkAddSinglesValidator(singleCasesImporter, singleCasesValidator);
    }

    @Test
    public void shouldReturnImportError() {
        var errors = bulkAddSinglesValidator.validate(multipleDetails, AUTH_TOKEN);

        assertEquals(1, errors.size());
        assertEquals("No cases found", errors.get(0));
    }

    @Test
    public void shouldReturnInvalidCaseErrors() {
        ethosCaseReferences.add("case1");
        validatedSingleCases.add(ValidatedSingleCase.createInvalidCase("case1", "Case not found"));

        var errors = bulkAddSinglesValidator.validate(multipleDetails, AUTH_TOKEN);

        assertEquals(1, errors.size());
        assertEquals("case1: Case not found", errors.get(0));
    }

    @Test
    public void shouldReturnNoInvalidCases() {
        ethosCaseReferences.add("case1");
        validatedSingleCases.add(ValidatedSingleCase.createValidCase("case1"));

        var errors = bulkAddSinglesValidator.validate(multipleDetails, AUTH_TOKEN);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldReturnErrorWhenImportCasesFails() throws ImportException {
        when(singleCasesImporter.importCases(multipleDetails.getCaseData(), AUTH_TOKEN))
                .thenThrow(ImportException.class);

        var errors = bulkAddSinglesValidator.validate(multipleDetails, AUTH_TOKEN);

        assertEquals(1, errors.size());
        assertEquals("Unexpected error when importing single cases", errors.get(0));
    }

    @Test
    public void shouldReturnErrorWhenValidationThrowsException() throws IOException {
        ethosCaseReferences.add("case1");
        validatedSingleCases.add(ValidatedSingleCase.createValidCase("case1"));
        when(singleCasesValidator.getValidatedCases(ethosCaseReferences, multipleDetails, AUTH_TOKEN))
                .thenThrow(IOException.class);

        var errors = bulkAddSinglesValidator.validate(multipleDetails, AUTH_TOKEN);

        assertEquals(1, errors.size());
        assertEquals("Unexpected error when validating single cases", errors.get(0));
    }

    private MultipleDetails createMultipleDetails() {
        var multipleReference = "2500001/2021";
        var multipleData = new MultipleData();
        multipleData.setMultipleReference(multipleReference);
        var multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        multipleDetails.setCaseData(multipleData);
        return multipleDetails;
    }
}
