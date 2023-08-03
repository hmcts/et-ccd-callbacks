package uk.gov.hmcts.ethos.replacement.docmosis;

public final class RolesConstants {
    public static final String CASEWORKER = "caseworker";
    public static final String CASEWORKER_ET_PCQEXTRACTOR = CASEWORKER.concat("-et-pcqextractor");
    public static final String CASEWORKER_CAA = CASEWORKER.concat("-caa");
    public static final String CASEWORKER_EMPLOYMENT = CASEWORKER.concat("-employment");
    public static final String CASEWORKER_APPROVER = CASEWORKER.concat("-approver");
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
    public static final String CASEWORKER_DIVORCE_SOLICITOR = CASEWORKER.concat("divorce-solicitor");

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

    //Emails constants
    public static final String CCD_DOCKER_DEFAULT_EMAIL = "ccd.docker.default@hmcts.net";
    public static final String ENGLANDWALES_EMAIL = "englandwales@hmcts.net";
    public static final String SCOTLAND_EMAIL = "scotland@hmcts.net";
    public static final String ADMIN_EMAIL = "admin@hmcts.net";
    public static final String SUPERUSER_EMAIL = "superuser@etorganisation1.com";
    public static final String SOLICITOR_1_EMAIL = "solicitor1@etorganisation1.com";
    public static final String CITIZEN_EMAIL = "citizen@gmail.com";
    public static final String IDAM_SYSTEM_USER_EMAIL = "data.store.idam.system.user@gmail.com";

    public static final String MCA_SYSTEM_IDAM_ACC_EMAIL = "mca.system.idam.acc@gmail.com";
    public static final String MCA_NOC_APPROVER_EMAIL = "mca.noc.approver@gmail.com";

    public static final String ET_SYSTEM_EMAIL = "et.service@hmcts.net";

    //Paths to definition files
    public static final String ENGLANGWALES_CONFIG_FILE = "/definitions/xlsx/et-englandwales-ccd-config-cftlib.xlsx";
    public static final String SCOTLAND_CONFIG_FILE = "/definitions/xlsx/et-scotland-ccd-config-cftlib.xlsx";
    public static final String ADMIN_CONFIG_FILE = "/definitions/xlsx/et-admin-ccd-config-cftlib.xlsx";

    public static final String[] CASEWORKER_ROLES = {CASEWORKER_ET_PCQEXTRACTOR, CASEWORKER_CAA,
        CASEWORKER_EMPLOYMENT, CASEWORKER_EMPLOYMENT_API, CASEWORKER_EMPLOYMENT_ENGLANDWALES,
        CASEWORKER_EMPLOYMENT_SCOTLAND, CASEWORKER_EMPLOYMENT_ETJUDGE, CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
        CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND, CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR, CITIZEN, ET_ACAS_API,
        PUI_CASE_MANAGER, PUI_FINANCE_MANAGER, PUI_ORGANISATION_MANAGER, PUI_USER_MANAGER, PUI_CAA, MANAGE_USER,
        CASEWORKER_APPROVER};

    private RolesConstants() {
    }
}
