package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

@Service
@Slf4j
public class BulkAddSinglesValidator {

    private final SingleCasesImporter singleCasesImporter;
    private final SingleCasesValidator singleCasesValidator;

    public BulkAddSinglesValidator(SingleCasesImporter singleCasesImporter, SingleCasesValidator singleCasesValidator) {
        this.singleCasesImporter = singleCasesImporter;
        this.singleCasesValidator = singleCasesValidator;
    }

    public List<String> validate(MultipleDetails multipleDetails, String authToken)  {
        String multipleEthosReference = multipleDetails.getCaseData().getMultipleReference();
        List<String> ethosCaseReferences;
        try {
            ethosCaseReferences = singleCasesImporter.importCases(multipleDetails.getCaseData(), authToken);
        } catch (ImportException e) {
            log.error("Unexpected error when importing single cases for " + multipleEthosReference, e);
            return List.of("Unexpected error when importing single cases");
        }

        log.info(String.format("Multiple %s import file contains %d cases", multipleEthosReference,
                ethosCaseReferences.size()));

        if (ethosCaseReferences.isEmpty()) {
            return List.of("No cases found");
        }

        try {
            List<ValidatedSingleCase> validatedSingleCases = singleCasesValidator.getValidatedCases(ethosCaseReferences,
                multipleDetails, authToken);

            return validatedSingleCases.stream()
                    .filter(Predicate.not(ValidatedSingleCase::isValid))
                    .map(s -> s.getEthosReference() + ": " + s.getInvalidReason())
                    .toList();
        } catch (IOException e) {
            log.error("Unexpected error when validating single cases for " + multipleEthosReference, e);
            return List.of("Unexpected error when validating single cases");
        }
    }
}
