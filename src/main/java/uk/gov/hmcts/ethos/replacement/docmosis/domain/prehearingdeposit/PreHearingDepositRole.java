package uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum PreHearingDepositRole implements HasRole {
    EMPLOYMENT_API("caseworker-employment-api", "CRUD");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;
}
