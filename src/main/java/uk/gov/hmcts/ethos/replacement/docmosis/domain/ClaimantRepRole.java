package uk.gov.hmcts.ethos.replacement.docmosis.domain;

public enum ClaimantRepRole {
    CASE_FIELD("claimantRepCollection");

    private final String caseField;

    ClaimantRepRole(String caseField) {
        this.caseField = caseField;
    }

    public String getCaseField() {
        return caseField;
    }
}
