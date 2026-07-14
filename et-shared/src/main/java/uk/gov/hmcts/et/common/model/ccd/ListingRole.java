package uk.gov.hmcts.et.common.model.ccd;

import uk.gov.hmcts.ccd.sdk.api.HasRole;

public enum ListingRole implements HasRole {
    ENGLAND_WALES_CASEWORKER("caseworker-employment-englandwales", "CRU"),
    ENGLAND_WALES_JUDGE("caseworker-employment-etjudge-englandwales", "CRU"),
    SCOTLAND_CASEWORKER("caseworker-employment-scotland", "CRU"),
    SCOTLAND_JUDGE("caseworker-employment-etjudge-scotland", "CRU"),
    EMPLOYMENT_API("caseworker-employment-api", "CRUD"),
    RAS_VALIDATION("caseworker-ras-validation", "R"),
    WA_TASK_CONFIGURATION("caseworker-wa-task-configuration", "CRU");

    private final String role;
    private final String caseTypePermissions;

    ListingRole(String role, String caseTypePermissions) {
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
