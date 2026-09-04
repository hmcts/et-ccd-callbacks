package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

public record RetentionTaskResult(
    int deletedCases,
    int simulatedCases,
    int skippedCases
) {
}
