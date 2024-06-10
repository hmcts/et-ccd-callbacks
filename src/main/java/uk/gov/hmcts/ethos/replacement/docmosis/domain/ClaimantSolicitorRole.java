package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ClaimantSolicitorRole {
    CLAIMANTSOLICITOR("[CLAIMANTSOLICITOR]");

    private final String caseRoleLabel;
    public static final String POLICY_FIELD_TEMPLATE = "claimantRepresentativeOrganisationPolicy";

}
