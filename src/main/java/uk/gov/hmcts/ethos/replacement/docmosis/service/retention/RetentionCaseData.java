package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

public record RetentionCaseData(
    Long reference,
    Long id,
    String caseTypeId,
    String jurisdiction,
    LocalDate resolvedTtl,
    JsonNode data
) {
}
