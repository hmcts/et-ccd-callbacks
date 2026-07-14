package uk.gov.hmcts.et.common.model.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

public enum SingleRole implements HasRole {
    GS_PROFILE("GS_profile", "R"),
    TTL_PROFILE("TTL_profile", "CRU"),
    @CCD(label = "Claimant Solicitor", hint = "Claimant Solicitor 1")
    CLAIMANTSOLICITOR("[CLAIMANTSOLICITOR]", ""),
    @CCD(label = "Creator", hint = "Creator of case")
    CREATOR("[CREATOR]", ""),
    @CCD(label = "Defendant", hint = "Respondent of case", liveFrom = "15/08/2024")
    DEFENDANT("[DEFENDANT]", ""),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORA("[SOLICITORA]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORB("[SOLICITORB]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORC("[SOLICITORC]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORD("[SOLICITORD]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORE("[SOLICITORE]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORF("[SOLICITORF]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORG("[SOLICITORG]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORH("[SOLICITORH]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORI("[SOLICITORI]", "CRU"),
    @CCD(label = "Respondent Solicitor", hint = "Respondent Solicitor role")
    SOLICITORJ("[SOLICITORJ]", "CRU"),
    CASEWORKER_APPROVER("caseworker-approver", "CRU"),
    CASEWORKER_CAA("caseworker-caa", "CRU"),
    CASEWORKER_EMPLOYMENT("caseworker-employment", "R"),
    CASEWORKER_EMPLOYMENT_API("caseworker-employment-api", "CRUD"),
    CASEWORKER_EMPLOYMENT_ENGLANDWALES("caseworker-employment-englandwales", "CRU"),
    CASEWORKER_EMPLOYMENT_ETJUDGE("caseworker-employment-etjudge", "R"),
    CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES("caseworker-employment-etjudge-englandwales", "CRU"),
    CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND("caseworker-employment-etjudge-scotland", "CRU"),
    CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR("caseworker-employment-legalrep-solicitor", "CRU"),
    CASEWORKER_EMPLOYMENT_SCOTLAND("caseworker-employment-scotland", "CRU"),
    CASEWORKER_ET_PCQEXTRACTOR("caseworker-et-pcqextractor", "R"),
    CASEWORKER_RAS_VALIDATION("caseworker-ras-validation", "R"),
    CASEWORKER_WA_TASK_CONFIGURATION("caseworker-wa-task-configuration", "CRU"),
    CFT_TTL_MANAGER("cft-ttl-manager", ""),
    CHALLENGED_ACCESS_ADMIN("challenged-access-admin", ""),
    CHALLENGED_ACCESS_CTSC("challenged-access-ctsc", ""),
    CHALLENGED_ACCESS_JUDICIARY("challenged-access-judiciary", ""),
    CHALLENGED_ACCESS_LEGAL_OPS("challenged-access-legal-ops", ""),
    CIRCUIT_JUDGE("circuit-judge", ""),
    CITIZEN("citizen", "CRUD"),
    CLERK("clerk", ""),
    CTSC("ctsc", ""),
    CTSC_TEAM_LEADER("ctsc-team-leader", ""),
    ET_ACAS_API("et-acas-api", "R"),
    FEE_PAID_JUDGE("fee-paid-judge", ""),
    HEARING_CENTRE_ADMIN("hearing-centre-admin", ""),
    HEARING_CENTRE_TEAM_LEADER("hearing-centre-team-leader", ""),
    HMCTS_ADMIN("hmcts-admin", ""),
    HMCTS_CTSC("hmcts-ctsc", ""),
    HMCTS_JUDICIARY("hmcts-judiciary", ""),
    HMCTS_LEGAL_OPERATIONS("hmcts-legal-operations", ""),
    HMCTS_STAFF("hmcts-staff", ""),
    JUDGE("judge", ""),
    LEADERSHIP_JUDGE("leadership-judge", ""),
    REGIONAL_CENTRE_ADMIN("regional-centre-admin", ""),
    REGIONAL_CENTRE_TEAM_LEADER("regional-centre-team-leader", ""),
    SENIOR_TRIBUNAL_CASEWORKER("senior-tribunal-caseworker", ""),
    SPECIFIC_ACCESS_ADMIN("specific-access-admin", ""),
    SPECIFIC_ACCESS_CTSC("specific-access-ctsc", ""),
    SPECIFIC_ACCESS_JUDICIARY("specific-access-judiciary", ""),
    SPECIFIC_ACCESS_LEGAL_OPS("specific-access-legal-ops", ""),
    TRIBUNAL_CASEWORKER("tribunal-caseworker", ""),
    TRIBUNAL_MEMBER("tribunal-member", "");

    private final String role;
    private final String caseTypePermissions;

    SingleRole(String role, String caseTypePermissions) {
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
