package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

public record RetentionCaseData(
    Long reference,
    String caseTypeId,
    String jurisdiction
) {
}
