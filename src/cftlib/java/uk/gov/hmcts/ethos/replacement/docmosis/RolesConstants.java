package uk.gov.hmcts.ethos.replacement.docmosis;

public final class RolesConstants {
    public static final String CASEWORKER = "caseworker";
    public static final String CASEWORKER_ET_PCQEXTRACTOR = CASEWORKER.concat("-et-pcqextractor");
    public static final String CASEWORKER_CAA = CASEWORKER.concat("-caa");
    public static final String CASEWORKER_WA = CASEWORKER.concat("-wa");
    public static final String CASEWORKER_WA_TASK_OFFICER = CASEWORKER_WA.concat("-task-officer");
    public static final String CASEWORKER_EMPLOYMENT = CASEWORKER.concat("-employment");
    public static final String CASEWORKER_EMPLOYMENT_API = CASEWORKER_EMPLOYMENT.concat("-api");
    public static final String CASEWORKER_EMPLOYMENT_ENGLANDWALES = CASEWORKER_EMPLOYMENT.concat("-englandwales");
    public static final String CASEWORKER_EMPLOYMENT_SCOTLAND = CASEWORKER_EMPLOYMENT.concat("-scotland");
    public static final String CASEWORKER_EMPLOYMENT_ETJUDGE = CASEWORKER_EMPLOYMENT.concat("-etjudge");
    public static final String CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES =
        CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-englandwales");
    public static final String CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND =
        CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-scotland");
    public static final String CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR =
        CASEWORKER_EMPLOYMENT.concat("-legalrep-solicitor");

    public static final String CITIZEN = "citizen";
    public static final String CCD_IMPORT = "ccd-import";

    public static final String ET_ACAS_API = "et-acas-api";

    public static final String PUI_CASE_MANAGER = "pui-case-manager";
    public static final String PUI_FINANCE_MANAGER = "pui-finance-manager";
    public static final String PUI_ORGANISATION_MANAGER = "pui-organisation-manager";
    public static final String PUI_USER_MANAGER = "pui-user-manager";
    public static final String PUI_CAA = "pui-caa";
    public static final String MANAGE_USER = "manage-user";
    public static final String CASEWORKER_APPROVER = "caseworker-approver";
    public static final String PRD_AAC_SYSTEM = "prd-aac-system";
    public static final String PRD_ADMIN = "prd-admin";
    public static final String ACAS_API = "et-acas-api";
    public static final String GS_PROFILE = "GS_profile";
    public static final String RAS_VALIDATION = "caseworker-ras-validation";
    public static final String WA_TASK_CONFIGURATION = "caseworker-wa-task-configuration";

    // Other roles that might be linked to WA

    public static final String CASE_ALLOCATOR = "case-allocator";
    public static final String CWD_USER = "cwd-user";
    public static final String CWD_ADMIN = "cwd-admin";
    public static final String CWD_SYSTEM_USER = "cwd-system-user";
    public static final String HEARING_CENTRE_ADMIN = "hearing-centre-admin";
    public static final String HEARING_CENTRE_TEAM_LEADER = "hearing-centre-team-leader";
    public static final String HEARING_MANAGER = "hearing-manager";
    public static final String HEARING_VIEWER = "hearing-viewer";
    public static final String HMCTS_ADMIN = "hmcts-admin";
    public static final String HMCTS_LEGAL_OPERATIONS = "hmcts-legal-operations";
    public static final String SENIOR_TRIBUNAL_CASEWORKER = "senior-tribunal-caseworker";
    public static final String SPECIFIC_ACCESS_APPROVER_ADMIN = "specific-access-approver-admin";
    public static final String SPECIFIC_ACCESS_APPROVER_LEGAL_OPS = "specific-access-approver-legal-ops";
    public static final String TASK_SUPERVISOR = "task-supervisor";
    public static final String TRIBUNAL_CASEWORKER = "tribunal-caseworker";
    public static final String EMPLOYMENT_TRIBUNAL_CASEWORKER = "employment-tribunal-caseworker";
    public static final String EMPLOYMENT_HEARING_CENTRE_ADMIN = "employment-hearing-centre-admin";
    public static final String EMPLOYMENT_SENIOR_TRIBUNAL_CASEWORKER = "employment-senior-tribunal-caseworker";
    public static final String EMPLOYMENT_HEARING_CENTRE_TEAM_LEADER = "employment-hearing-centre-team-leader";
    public static final String STAFF_ADMIN = "staff-admin";
    public static final String CTSC_TEAM_LEADER = "ctsc-team-leader";
    public static final String REGIONAL_CENTRE_TEAM_LEADER = "regional-centre-team-leader";

    //Emails constants
    public static final String JUDGE_EW_EMAIL = "judge.ew@hmcts.net";
    public static final String JUDGE_SC_EMAIL = "judge.sc@hmcts.net";
    public static final String LEGALREP_EMAIL = "legalrep@hmcts.net";
    public static final String CCD_DOCKER_DEFAULT_EMAIL = "ccd.docker.default@hmcts.net";
    public static final String ENGLANDWALES_EMAIL = "englandwales@hmcts.net";
    public static final String SCOTLAND_EMAIL = "scotland@hmcts.net";
    public static final String ADMIN_EMAIL = "admin@hmcts.net";
    public static final String SUPERUSER_EMAIL = "superuser@etorganisation1.com";
    public static final String SOLICITOR_1_EMAIL = "solicitor1@etorganisation1.com";
    public static final String CITIZEN_EMAIL = "citizen@gmail.com";
    public static final String RESPONDENT_EMAIL = "respondent@gmail.com";
    public static final String IDAM_SYSTEM_USER_EMAIL = "data.store.idam.system.user@gmail.com";
    public static final String MCA_SYSTEM_IDAM_ACC_EMAIL = "mca.system.idam.acc@gmail.com";
    public static final String MCA_NOC_APPROVER_EMAIL = "mca.noc.approver@gmail.com";
    public static final String ACAS_EMAIL = "et@acas.com";
    public static final String ET_SYSTEM_EMAIL = "et.service@hmcts.net";
    public static final String ROLE_ASSIGNMENT_ADMIN_EMAIL = "role.assignment.admin@gmail.com";
    public static final String ET_CASEADMIN_EMAIL = "et.caseadmin@hmcts.net";
    public static final String WA_SYSTEM_USER_EMAIL = "wa-system-user@fake.hmcts.net";
    public static final String ET_DEV_EMAIL = "et.dev@hmcts.net";

    public static final String[] ECM_CASEWORKER_ROLES = {
            CASEWORKER_EMPLOYMENT.concat("-bristol"), CASEWORKER_EMPLOYMENT.concat("-leeds"),
            CASEWORKER_EMPLOYMENT.concat("-londoncentral"), CASEWORKER_EMPLOYMENT.concat("-londoneast"),
            CASEWORKER_EMPLOYMENT.concat("-londonsouth"), CASEWORKER_EMPLOYMENT.concat("-manchester"),
            CASEWORKER_EMPLOYMENT.concat("-midlandseast"), CASEWORKER_EMPLOYMENT.concat("-midlandswest"),
            CASEWORKER_EMPLOYMENT.concat("-newcastle"), CASEWORKER_EMPLOYMENT.concat("-wales"),
            CASEWORKER_EMPLOYMENT.concat("-watford")
    };
    public static final String[] ECM_JUDGE_ROLES = {
            CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-bristol"), CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-leeds"),
            CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-londoncentral"), CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-londoneast"),
            CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-londonsouth"), CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-manchester"),
            CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-midlandseast"),
            CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-midlandswest"), CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-newcastle"),
            CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-wales"), CASEWORKER_EMPLOYMENT_ETJUDGE.concat("-watford")
    };

    //Paths to definition files
    public static final String ENGLANGWALES_CONFIG_FILE = "/definitions/xlsx/et-englandwales-ccd-config-cftlib.xlsx";
    public static final String SCOTLAND_CONFIG_FILE = "/definitions/xlsx/et-scotland-ccd-config-cftlib.xlsx";
    public static final String ADMIN_CONFIG_FILE = "/definitions/xlsx/et-admin-ccd-config-cftlib.xlsx";

    public static final String[] CASEWORKER_ROLES = {CASEWORKER_ET_PCQEXTRACTOR, CASEWORKER_CAA,
        CASEWORKER_EMPLOYMENT, CASEWORKER_EMPLOYMENT_API, CASEWORKER_EMPLOYMENT_ENGLANDWALES,
        CASEWORKER_EMPLOYMENT_SCOTLAND, CASEWORKER_EMPLOYMENT_ETJUDGE, CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
        CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, CITIZEN, ET_ACAS_API,
        PUI_CASE_MANAGER, PUI_FINANCE_MANAGER, PUI_ORGANISATION_MANAGER, PUI_USER_MANAGER, PUI_CAA, MANAGE_USER,
        CASEWORKER_APPROVER, PRD_AAC_SYSTEM, PRD_ADMIN, ACAS_API, GS_PROFILE, RAS_VALIDATION, WA_TASK_CONFIGURATION,
        "caseworker-divorce-solicitor", CASEWORKER_WA, CASEWORKER_WA_TASK_OFFICER, CASE_ALLOCATOR, CWD_USER,
        HEARING_CENTRE_ADMIN, HEARING_CENTRE_TEAM_LEADER, HEARING_MANAGER, HEARING_VIEWER, HMCTS_ADMIN, 
        HMCTS_LEGAL_OPERATIONS, SENIOR_TRIBUNAL_CASEWORKER, SPECIFIC_ACCESS_APPROVER_ADMIN,
        SPECIFIC_ACCESS_APPROVER_LEGAL_OPS, TASK_SUPERVISOR, TRIBUNAL_CASEWORKER, EMPLOYMENT_TRIBUNAL_CASEWORKER,
        EMPLOYMENT_HEARING_CENTRE_ADMIN, EMPLOYMENT_SENIOR_TRIBUNAL_CASEWORKER,
        EMPLOYMENT_HEARING_CENTRE_TEAM_LEADER, CWD_ADMIN, CWD_SYSTEM_USER, CTSC_TEAM_LEADER,
        REGIONAL_CENTRE_TEAM_LEADER, "allocated-admin-caseworker" };

    private RolesConstants() {
    }
}
