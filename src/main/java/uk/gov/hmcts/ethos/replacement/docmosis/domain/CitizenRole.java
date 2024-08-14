package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CitizenRole {
    CITIZEN("[CITIZEN]");

    private final String caseRoleLabel;
    public static final String POLICY_FIELD_TEMPLATE = "citizenPolicy";

}
