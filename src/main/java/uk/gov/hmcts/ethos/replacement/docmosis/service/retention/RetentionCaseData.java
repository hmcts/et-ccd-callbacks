package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import com.fasterxml.jackson.databind.JsonNode;

public record RetentionCaseData(
    Long reference,
    Long id,
    String caseTypeId,
    String jurisdiction,
    JsonNode data
) {
}
