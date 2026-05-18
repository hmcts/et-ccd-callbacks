package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.HasRole;

public enum EtUserRole implements HasRole {
    CITIZEN("citizen", "CRUD"),
    CREATOR("[CREATOR]", "CRUD"),
    DEFENDANT("[DEFENDANT]", "CRU"),
    CASEWORKER_EMPLOYMENT_API("caseworker-employment-api", "CRUD"),
    CASEWORKER_EMPLOYMENT_ENGLANDWALES("caseworker-employment-englandwales", "CRU"),
    CASEWORKER_EMPLOYMENT_ETJUDGE("caseworker-employment-etjudge", "R"),
    CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES("caseworker-employment-etjudge-englandwales", "CRU"),
    CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND("caseworker-employment-etjudge-scotland", "CRU"),
    CASEWORKER_EMPLOYMENT_SCOTLAND("caseworker-employment-scotland", "CRU"),
    CASEWORKER_WA_TASK_CONFIGURATION("caseworker-wa-task-configuration", "CRU");

    private final String role;
    private final String caseTypePermissions;

    EtUserRole(String role, String caseTypePermissions) {
        this.role = role;
        this.caseTypePermissions = caseTypePermissions;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public String getCaseTypePermissions() {
        return caseTypePermissions;
    }
}
