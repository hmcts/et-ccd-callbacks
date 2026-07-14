package uk.gov.hmcts.et.common.model.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

public enum MultipleRole implements HasRole {
    ENGLAND_WALES_CASEWORKER("caseworker-employment-englandwales", "CRU"),
    ENGLAND_WALES_JUDGE("caseworker-employment-etjudge-englandwales", "CRU"),
    SCOTLAND_CASEWORKER("caseworker-employment-scotland", "CRU"),
    SCOTLAND_JUDGE("caseworker-employment-etjudge-scotland", "CRU"),
    EMPLOYMENT_API("caseworker-employment-api", "CRUD"),
    LEGAL_REP("caseworker-employment-legalrep-solicitor", "CRU"),
    RAS_VALIDATION("caseworker-ras-validation", "R"),
    WA_TASK_CONFIGURATION("caseworker-wa-task-configuration", "CRU"),
    EMPLOYMENT_CASEWORKER("caseworker-employment", ""),
    EMPLOYMENT_JUDGE("caseworker-employment-etjudge", ""),
    CITIZEN("citizen", ""),
    ACAS_API("et-acas-api", ""),
    GS_PROFILE("GS_profile", ""),
    @CCD(label = "Creator", hint = "Creator of case")
    CREATOR("[CREATOR]", ""),
    @CCD(label = "Defendant", hint = "Respondent of case", liveFrom = "15/08/2024")
    DEFENDANT("[DEFENDANT]", "");

    private final String role;
    private final String caseTypePermissions;

    MultipleRole(String role, String caseTypePermissions) {
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
