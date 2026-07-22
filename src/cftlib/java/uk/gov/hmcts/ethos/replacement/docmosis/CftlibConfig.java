package uk.gov.hmcts.ethos.replacement.docmosis;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.ControlPlane;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ACAS_API;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ACAS_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ADMIN_CONFIG_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ADMIN_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_APPROVER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_CAA;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_EMPLOYMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_EMPLOYMENT_API;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_EMPLOYMENT_ENGLANDWALES;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_EMPLOYMENT_ETJUDGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_EMPLOYMENT_SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_ROLES;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASEWORKER_WA;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CASE_ALLOCATOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CCD_DOCKER_DEFAULT_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CCD_IMPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CITIZEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CITIZEN_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CWD_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CWD_SYSTEM_USER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.CWD_USER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ECM_CASEWORKER_ROLES;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ECM_JUDGE_ROLES;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.EMPLOYMENT_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.EMPLOYMENT_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.EMPLOYMENT_SENIOR_TRIBUNAL_CASEWORKER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.EMPLOYMENT_TRIBUNAL_CASEWORKER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ENGLANDWALES_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ENGLANGWALES_CONFIG_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ET_CASEADMIN_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ET_LEGALOPS_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ET_SYSTEM_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.HEARING_MANAGER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.HEARING_VIEWER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.HMCTS_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.HMCTS_LEGAL_OPERATIONS;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.IDAM_SYSTEM_USER_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.JUDGE_EW_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.JUDGE_SC_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.LEGALREP_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.MCA_NOC_APPROVER_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.MCA_SYSTEM_IDAM_ACC_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.PRD_AAC_SYSTEM;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.PRD_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.PUI_CAA;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.PUI_CASE_MANAGER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.PUI_ORGANISATION_MANAGER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.PUI_USER_MANAGER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.RAS_VALIDATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.RESPONDENT_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.ROLE_ASSIGNMENT_ADMIN_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.SCOTLAND_CONFIG_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.SCOTLAND_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.SENIOR_TRIBUNAL_CASEWORKER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.SOLICITOR_1_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.SOLICITOR_2_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.SPECIFIC_ACCESS_APPROVER_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.SPECIFIC_ACCESS_APPROVER_LEGAL_OPS;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.STAFF_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.SUPERUSER_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.TASK_SUPERVISOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.TRIBUNAL_CASEWORKER;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.TTL_PROFILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.WA_SYSTEM_USER_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.RolesConstants.WA_TASK_CONFIGURATION;

/**
 * Configures the cftlib plugin so that the environment is ready for use with this et-ccd-callbacks service. To start
 * the cftlib environment see the bootWithCCD Gradle task.
 *
 * <p>
 * The environment is configured with:
 * <ul>
 *     <li>CCD roles</li>
 *     <li>User accounts</li>
 *     <li>Imported CCD configuration</li>
 *     <li>DM Store service</li>
 * </ul>
 * </p>
 *
 * <p>
 * To automatically import CCD definitions at startup the following environment variables should be set to point to the
 * location of the local version of the git repository:
 *
 * <ul>
 *     <li>ENGLANDWALES_CCD_CONFIG_PATH</li>
 *     <li>SCOTLAND_CCD_CONFIG_PATH</li>
 *     <li>ADMIN_CCD_CONFIG_PATH</li>
 * </ul>
 *
 * If you do not wish the CCD definitions to be imported at startup then set the following environment variable to false
 * <pre>
 *     CFTLIB_IMPORT_CCD_DEFS_ON_BOOT
 * </pre>
 * </p>
 *
 * <p>
 * The user accounts created are as follows. All accounts use a password of 'password'
 *
 * <table>
 *     <col width="50%"/>
 *     <col width="25%"/>
 *     <col width="25%"/>
 *     <thead>
 *         <tr>
 *             <td>Email</td>
 *             <td>Roles</td>
 *             <td>Purpose</td>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>ccd.docker.default@hmcts.net</td>
 *             <td>ccd-import</td>
 *             <td>Used to import CCD definitions</td>
 *         </tr>
 *         <tr>
 *             <td>englandwales@hmcts.net</td>
 *             <td>caseworker, caseworker-employment, caseworker-employment-englandwales</td>
 *             <td>Caseworker account for England/Wales cases</td>
 *         </tr>
 *         <tr>
 *             <td>scotland@hmcts.net</td>
 *             <td>caseworker, caseworker-employment, caseworker-employment-scotland</td>
 *             <td>Caseworker account for Scotland cases</td>
 *         </tr>
 *         <tr>
 *             <td>admin@hmcts.net</td>
 *             <td>caseworker, caseworker-employment, caseworker-employment-api</td>
 *             <td>Admin account</td>
 *         </tr>
 *         <tr>
 *             <td>superuser@etorganisation1.com</td>
 *             <td>caseworker-caa, pui-case-manager, pui-organisation-manager, pui-user-manager, pui-caa</td>
 *             <td>Solicitor Organisation Admin account</td>
 *         </tr>
 *         <tr>
 *             <td>solicitor1@etorganisation1.com</td>
 *             <td>caseworker-employment-legalrep-solicitor</td>
 *             <td>Solicitor account</td>
 *         </tr>
 *         <tr>
 *             <td>citizen@gmail.com</td>
 *             <td>citizen</td>
 *             <td>Citizen account</td>
 *         </tr>
 *     </tbody>
 * </table>
 * </p>
 */
@Component
@Slf4j
@SuppressWarnings({"unchecked", "PMD.UseUnderscoresInNumericLiterals"})
public class CftlibConfig implements CFTLibConfigurer {

    private static final String DEFAULT_LOCATION = "765324";

    @Value("${cftlib.import-ccd-defs-on-boot}")
    private boolean importCcdDefsOnBoot;

    @Value("${rse.lib.dump_definitions:false}")
    private boolean dumpDefinitions;

    @Override
    public void configure(CFTLib lib) throws IOException {
        createRoles(lib);
        createUsers(lib);
        createWaWiremockStubs();
        importCcdDefinitions(lib);
        if (!dumpDefinitions) {
            startDockerCompose();
        }
    }

    private void createRoles(CFTLib lib) {
        lib.createRoles(CASEWORKER_ROLES);
        lib.createRoles(ECM_CASEWORKER_ROLES);
        lib.createRoles(ECM_JUDGE_ROLES);
    }

    private void createUsers(CFTLib lib) {
        // Create importer user
        lib.createIdamUser(CCD_DOCKER_DEFAULT_EMAIL,
                CCD_IMPORT);

        // Create test users in the idam simulator.
        lib.createIdamUser(JUDGE_EW_EMAIL,
                CASEWORKER,
                CASEWORKER_EMPLOYMENT,
                CASEWORKER_EMPLOYMENT_ETJUDGE,
                CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
                Arrays.stream(ECM_JUDGE_ROLES).reduce((a, b) -> a + "," + b).orElse(""));

        lib.createIdamUser(JUDGE_SC_EMAIL,
                CASEWORKER,
                CASEWORKER_EMPLOYMENT,
                CASEWORKER_EMPLOYMENT_ETJUDGE,
                CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND);

        lib.createIdamUser(LEGALREP_EMAIL,
                CASEWORKER,
                CASEWORKER_EMPLOYMENT,
                CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR);

        lib.createIdamUser(ENGLANDWALES_EMAIL,
                CASEWORKER,
                CASEWORKER_EMPLOYMENT,
                CASEWORKER_EMPLOYMENT_ENGLANDWALES, TTL_PROFILE,
                Arrays.stream(ECM_CASEWORKER_ROLES).reduce((a, b) -> a + "," + b).orElse(""));

        lib.createIdamUser(SCOTLAND_EMAIL,
                CASEWORKER,
                CASEWORKER_EMPLOYMENT,
                CASEWORKER_EMPLOYMENT_SCOTLAND);

        lib.createIdamUser(ADMIN_EMAIL,
                CASEWORKER,
                CASEWORKER_EMPLOYMENT,
                CASEWORKER_EMPLOYMENT_API,
                CASEWORKER_WA,
                WA_TASK_CONFIGURATION, STAFF_ADMIN, CWD_ADMIN, CASEWORKER_CAA, CASEWORKER_APPROVER,
                Arrays.stream(ECM_CASEWORKER_ROLES).reduce((a, b) -> a + "," + b).orElse(""));

        lib.createIdamUser(SUPERUSER_EMAIL,
                CASEWORKER_CAA,
                PUI_CASE_MANAGER,
                PUI_ORGANISATION_MANAGER,
                PUI_USER_MANAGER,
                PUI_CAA);

        lib.createIdamUser("et.hearingCL001@justice.gov.uk",
                CASE_ALLOCATOR, CASEWORKER, CASEWORKER_EMPLOYMENT, CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                CASEWORKER_EMPLOYMENT_SCOTLAND, CASEWORKER_WA, WA_TASK_CONFIGURATION, CWD_USER,
                HEARING_CENTRE_ADMIN, HEARING_CENTRE_TEAM_LEADER, HEARING_MANAGER, HEARING_VIEWER,
                HMCTS_ADMIN, HMCTS_LEGAL_OPERATIONS, SENIOR_TRIBUNAL_CASEWORKER, SPECIFIC_ACCESS_APPROVER_ADMIN,
                SPECIFIC_ACCESS_APPROVER_LEGAL_OPS, TASK_SUPERVISOR, TRIBUNAL_CASEWORKER,
                EMPLOYMENT_TRIBUNAL_CASEWORKER, EMPLOYMENT_HEARING_CENTRE_ADMIN, EMPLOYMENT_SENIOR_TRIBUNAL_CASEWORKER,
                EMPLOYMENT_HEARING_CENTRE_TEAM_LEADER);

        // Dedicated LEGAL_OPERATIONS WA test user — can claim tribunal-caseworker tasks
        // in XUI.
        lib.createIdamUser(ET_LEGALOPS_EMAIL,
                CASE_ALLOCATOR, CASEWORKER, CASEWORKER_EMPLOYMENT, CASEWORKER_EMPLOYMENT_API,
                CASEWORKER_EMPLOYMENT_ENGLANDWALES, CASEWORKER_EMPLOYMENT_SCOTLAND, CASEWORKER_WA,
                WA_TASK_CONFIGURATION, CITIZEN, TASK_SUPERVISOR, TRIBUNAL_CASEWORKER, EMPLOYMENT_TRIBUNAL_CASEWORKER);

        lib.createIdamUser(SOLICITOR_1_EMAIL,
                CASEWORKER,
                CASEWORKER_EMPLOYMENT,
                CASEWORKER_CAA,
                PUI_CASE_MANAGER,
                PUI_ORGANISATION_MANAGER,
                PUI_USER_MANAGER,
                PUI_CAA,
                CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR,
                "caseworker-divorce-solicitor");

        lib.createIdamUser(SOLICITOR_2_EMAIL,
                CASEWORKER,
                CASEWORKER_EMPLOYMENT,
                CASEWORKER_CAA,
                PUI_CASE_MANAGER,
                PUI_ORGANISATION_MANAGER,
                PUI_USER_MANAGER,
                PUI_CAA,
                CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR,
                "caseworker-divorce-solicitor");

        // Claimant is a citizen
        lib.createIdamUser(CITIZEN_EMAIL, CITIZEN);

        // Respondent is a citizen
        lib.createIdamUser(RESPONDENT_EMAIL, CITIZEN);

        // Required by ccd-data-store-api
        lib.createIdamUser(IDAM_SYSTEM_USER_EMAIL, CASEWORKER);

        // Required by XUI for caseworker ref data calls (getUsersByServiceName).
        // XUI uses this system user to get tokens with elevated scopes (manage-user,
        // create-user, search-user) when calling rd-caseworker-ref-api routes.
        lib.createIdamUser("cwd_system@mailinator.com", CWD_SYSTEM_USER);

        // Required for Share a Case
        lib.createIdamUser(MCA_NOC_APPROVER_EMAIL, CASEWORKER, CASEWORKER_APPROVER, PRD_AAC_SYSTEM, PRD_ADMIN);
        lib.createIdamUser(MCA_SYSTEM_IDAM_ACC_EMAIL, CASEWORKER, CASEWORKER_CAA);
        lib.createIdamUser(ET_SYSTEM_EMAIL, CASEWORKER, CASEWORKER_EMPLOYMENT, CASEWORKER_EMPLOYMENT_API);
        lib.createIdamUser(ACAS_EMAIL, ACAS_API);

        lib.createIdamUser(ROLE_ASSIGNMENT_ADMIN_EMAIL,
                CASEWORKER, RAS_VALIDATION, CASEWORKER_WA, WA_TASK_CONFIGURATION);

        lib.createIdamUser(ET_CASEADMIN_EMAIL, CASEWORKER, CASEWORKER_EMPLOYMENT, CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                CASEWORKER_EMPLOYMENT_SCOTLAND, CASEWORKER_WA, WA_TASK_CONFIGURATION);

        lib.createIdamUser(WA_SYSTEM_USER_EMAIL,
                CASEWORKER, CASEWORKER_EMPLOYMENT, CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                CASEWORKER_EMPLOYMENT_SCOTLAND, CASEWORKER_EMPLOYMENT_API, CITIZEN, CASEWORKER_WA,
                WA_TASK_CONFIGURATION, CASEWORKER_EMPLOYMENT_ETJUDGE, TASK_SUPERVISOR);

        // Configure WA organisational role assignments. IDs are resolved dynamically so
        // this
        // works regardless of which machine the environment is running on.
        createWaRoleAssignments(lib);
    }

    // -----------------------------------------------------------------------
    // WA role assignments
    // -----------------------------------------------------------------------

    @SneakyThrows
    private void createWaRoleAssignments(CFTLib lib) {
        var assignments = new ArrayList<Map<String, Object>>();

        assignments.add(buildEntry(WA_SYSTEM_USER_EMAIL, List.of(
                role("case-allocator", "LEGAL_OPERATIONS", ENGLANDWALES_CASE_TYPE_ID),
                role("senior-tribunal-caseworker", "LEGAL_OPERATIONS", ENGLANDWALES_CASE_TYPE_ID),
                role("judge", "JUDICIAL", ENGLANDWALES_CASE_TYPE_ID),
                role("hearing-centre-team-leader", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("regional-centre-team-leader", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("ctsc-team-leader", "CTSC", ENGLANDWALES_CASE_TYPE_ID),
                role("case-allocator", "LEGAL_OPERATIONS", SCOTLAND_CASE_TYPE_ID),
                role("senior-tribunal-caseworker", "LEGAL_OPERATIONS", SCOTLAND_CASE_TYPE_ID),
                role("judge", "JUDICIAL", SCOTLAND_CASE_TYPE_ID),
                role("hearing-centre-team-leader", "ADMIN", SCOTLAND_CASE_TYPE_ID),
                role("regional-centre-team-leader", "ADMIN", SCOTLAND_CASE_TYPE_ID),
                role("ctsc-team-leader", "CTSC", SCOTLAND_CASE_TYPE_ID),
                role("case-allocator", "LEGAL_OPERATIONS", ENGLANDWALES_BULK_CASE_TYPE_ID),
                role("senior-tribunal-caseworker", "LEGAL_OPERATIONS", ENGLANDWALES_BULK_CASE_TYPE_ID),
                role("judge", "JUDICIAL", ENGLANDWALES_BULK_CASE_TYPE_ID),
                role("hearing-centre-team-leader", "ADMIN", ENGLANDWALES_BULK_CASE_TYPE_ID),
                role("regional-centre-team-leader", "ADMIN", ENGLANDWALES_BULK_CASE_TYPE_ID),
                role("ctsc-team-leader", "CTSC", ENGLANDWALES_BULK_CASE_TYPE_ID))));

        assignments.add(buildEntry(ADMIN_EMAIL, List.of(
                role("case-allocator", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("task-supervisor", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("hearing-centre-team-leader", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("case-allocator", "ADMIN", SCOTLAND_CASE_TYPE_ID),
                role("task-supervisor", "ADMIN", SCOTLAND_CASE_TYPE_ID),
                role("hearing-centre-team-leader", "ADMIN", SCOTLAND_CASE_TYPE_ID))));

        assignments.add(buildEntry(ET_CASEADMIN_EMAIL, List.of(
                role("case-allocator", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("task-supervisor", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("hearing-centre-admin", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("hearing-centre-team-leader", "ADMIN", ENGLANDWALES_CASE_TYPE_ID),
                role("case-allocator", "ADMIN", SCOTLAND_CASE_TYPE_ID),
                role("task-supervisor", "ADMIN", SCOTLAND_CASE_TYPE_ID),
                role("hearing-centre-admin", "ADMIN", SCOTLAND_CASE_TYPE_ID),
                role("hearing-centre-team-leader", "ADMIN", SCOTLAND_CASE_TYPE_ID))));

        // et.legalops — LEGAL_OPERATIONS WA user; log in as this user to see/claim
        // legal ops tasks.
        assignments.add(buildEntry(ET_LEGALOPS_EMAIL, List.of(
                role("tribunal-caseworker", "LEGAL_OPERATIONS", ENGLANDWALES_CASE_TYPE_ID),
                role("senior-tribunal-caseworker", "LEGAL_OPERATIONS", ENGLANDWALES_CASE_TYPE_ID),
                role("task-supervisor", "LEGAL_OPERATIONS", ENGLANDWALES_CASE_TYPE_ID),
                role("case-allocator", "LEGAL_OPERATIONS", ENGLANDWALES_CASE_TYPE_ID),
                role("tribunal-caseworker", "LEGAL_OPERATIONS", SCOTLAND_CASE_TYPE_ID),
                role("senior-tribunal-caseworker", "LEGAL_OPERATIONS", SCOTLAND_CASE_TYPE_ID),
                role("task-supervisor", "LEGAL_OPERATIONS", SCOTLAND_CASE_TYPE_ID),
                role("case-allocator", "LEGAL_OPERATIONS", SCOTLAND_CASE_TYPE_ID))));

        lib.configureRoleAssignments(new Gson().toJson(assignments));
    }

    @SneakyThrows
    private Map<String, Object> buildEntry(String email, List<Map<String, Object>> roleAssignments) {
        return Map.of(
                "id", lookupIdamUserId(email),
                "overrideAll", true,
                "roleAssignments", roleAssignments);
    }

    private Map<String, Object> role(String roleName, String roleCategory, String caseType) {
        return Map.of(
                "roleType", "ORGANISATION",
                "roleName", roleName,
                "grantType", "STANDARD",
                "roleCategory", roleCategory,
                "classification", "PUBLIC",
                "readOnly", false,
                "attributes", Map.of(
                        "caseType", caseType,
                        "jurisdiction", "EMPLOYMENT",
                        "primaryLocation", DEFAULT_LOCATION),
                "authorisations", List.of());
    }

    // -----------------------------------------------------------------------
    // WA Wiremock stubs
    // -----------------------------------------------------------------------

    /**
     * Generates Wiremock stubs for rd-caseworker-ref-api endpoints with UUIDs
     * resolved
     * dynamically from the IDAM simulator. Files are written to the Wiremock
     * bind-mount
     * path before {@code startDockerCompose()} runs so Wiremock picks them up on
     * startup.
     */
    @SneakyThrows
    private void createWaWiremockStubs() {
        var legalOpsId = lookupIdamUserId(ET_LEGALOPS_EMAIL);
        var adminId = lookupIdamUserId(ADMIN_EMAIL);
        var caseAdminId = lookupIdamUserId(ET_CASEADMIN_EMAIL);
        var filesDir = Path.of("build/resources/cftlib/compose/mocks/wiremock/__files/prd");
        Files.createDirectories(filesDir);

        var gson = new Gson();

        var usersByServiceName = List.of(
                buildServiceNameEntry(legalOpsId, ET_LEGALOPS_EMAIL, "ET", "LegalOps",
                        "LEGAL_OPERATIONS", true, true, true,
                        List.of(Map.of("role_id", "2", "role_name", "Legal Caseworker", "is_primary", true),
                                Map.of("role_id", "1", "role_name", "Senior Legal Caseworker", "is_primary", false))),
                buildServiceNameEntry(adminId, ADMIN_EMAIL, "ET", "Admin",
                        "CTSC", true, true, true,
                        List.of(Map.of("role_id", "10", "role_name", "CTSC Administrator", "is_primary", true),
                                Map.of("role_id", "3", "role_name", "Hearing Centre Team Leader", "is_primary",
                                        false))),
                buildServiceNameEntry(caseAdminId, ET_CASEADMIN_EMAIL, "ET", "CaseAdmin",
                        "CTSC", true, true, true,
                        List.of(Map.of("role_id", "4", "role_name", "Hearing Centre Administrator", "is_primary", true),
                                Map.of("role_id", "3", "role_name", "Hearing Centre Team Leader", "is_primary",
                                        false))));
        Files.writeString(filesDir.resolve("usersByServiceName.json"), gson.toJson(usersByServiceName));

        var caseworkerSearch = List.of(
                buildCaseworkerEntry(legalOpsId, ET_LEGALOPS_EMAIL, "ET", "LegalOps",
                        "LEGAL_OPERATIONS", true, true, true,
                        List.of(Map.of("role_id", "2", "role", "Legal Caseworker", "is_primary", true),
                                Map.of("role_id", "1", "role", "Senior Legal Caseworker", "is_primary", false))),
                buildCaseworkerEntry(adminId, ADMIN_EMAIL, "ET", "Admin",
                        "CTSC", true, true, true,
                        List.of(Map.of("role_id", "10", "role", "CTSC Administrator", "is_primary", true),
                                Map.of("role_id", "3", "role", "Hearing Centre Team Leader", "is_primary", false))),
                buildCaseworkerEntry(caseAdminId, ET_CASEADMIN_EMAIL, "ET", "CaseAdmin",
                        "CTSC", true, true, true,
                        List.of(Map.of("role_id", "4", "role", "Hearing Centre Administrator", "is_primary", true),
                                Map.of("role_id", "3", "role", "Hearing Centre Team Leader", "is_primary", false))));
        Files.writeString(filesDir.resolve("caseworkerProfileSearch.json"), gson.toJson(caseworkerSearch));

        log.info("Generated Wiremock caseworker stubs with IDAM-resolved UUIDs");
    }

    private Map<String, Object> buildServiceNameEntry(String id, String email, String firstName, String lastName,
            String userType, boolean taskSupervisor, boolean caseAllocator,
            boolean staffAdmin, List<Map<String, Object>> roles) {
        var staffProfile = new LinkedHashMap<String, Object>();
        staffProfile.put("id", id);
        staffProfile.put("first_name", firstName);
        staffProfile.put("last_name", lastName);
        staffProfile.put("email_id", email);
        staffProfile.put("region_id", 12);
        staffProfile.put("region", "National");
        staffProfile.put("base_location",
                List.of(Map.of("base_location_id", 36313, "location_name", "Leeds", "is_primary", true)));
        staffProfile.put("user_type_id", 1);
        staffProfile.put("user_type", userType);
        staffProfile.put("role", roles);
        staffProfile.put("work_area",
                List.of(Map.of("service_code", "BHA1", "area_of_work", "Employment Claims")));
        staffProfile.put("suspended", "false");
        staffProfile.put("case_allocator", String.valueOf(caseAllocator));
        staffProfile.put("task_supervisor", String.valueOf(taskSupervisor));
        staffProfile.put("staff_admin", String.valueOf(staffAdmin));
        return Map.of("ccd_service_name", "BHA1", "staff_profile", staffProfile);
    }

    private Map<String, Object> buildCaseworkerEntry(String id, String email, String firstName, String lastName,
            String userType, boolean taskSupervisor, boolean caseAllocator,
            boolean staffAdmin, List<Map<String, Object>> roles) {
        var entry = new LinkedHashMap<String, Object>();
        entry.put("id", id);
        entry.put("email_id", email);
        entry.put("first_name", firstName);
        entry.put("last_name", lastName);
        entry.put("suspended", false);
        entry.put("user_type", userType);
        entry.put("task_supervisor", taskSupervisor);
        entry.put("case_allocator", caseAllocator);
        entry.put("staff_admin", staffAdmin);
        entry.put("idam_roles", List.of("caseworker", "caseworker-employment", "caseworker-wa"));
        entry.put("up_idam_status", "ACTIVE");
        entry.put("roles", roles);
        entry.put("skills", List.of());
        entry.put("services",
                List.of(Map.of("service", "Employment Claims", "service_code", "BHA1")));
        entry.put("base_locations",
                List.of(Map.of("location_id", 36313, "location", "Leeds", "is_primary", true)));
        entry.put("region", "National");
        entry.put("region_id", 12);
        return entry;
    }

    // -----------------------------------------------------------------------
    // IDAM utility
    // -----------------------------------------------------------------------

    @SneakyThrows
    private String lookupIdamUserId(String email) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            var tokenBody = "grant_type=password"
                    + "&username=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                    + "&password=password"
                    + "&client_id=xuiwebapp"
                    + "&client_secret=AAAAAAAAAAAAAAAA"
                    + "&scope=openid+profile+roles"
                    + "&redirect_uri="
                    + URLEncoder.encode("http://localhost:3000/oauth2/callback", StandardCharsets.UTF_8);

            var tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5062/o/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenBody))
                    .build();

            var tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> tokenJson = (Map<String, Object>) new Gson().fromJson(tokenResponse.body(), Map.class);
            var accessToken = (String) tokenJson.get("access_token");

            var detailsRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5062/details"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            var detailsResponse = client.send(detailsRequest, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> detailsJson = (Map<String, Object>) new Gson().fromJson(detailsResponse.body(),
                    Map.class);
            return (String) detailsJson.get("id");
        }
    }

    private void importCcdDefinitions(CFTLib lib) throws IOException {
        if (importCcdDefsOnBoot) {
            importEnglandWales(lib);
            importScotland(lib);
            importAdmin(lib);
        }
    }

    private void importEnglandWales(CFTLib lib) throws IOException {
        importCcdDefinition(lib, ENGLANGWALES_CONFIG_FILE);
    }

    private void importScotland(CFTLib lib) throws IOException {
        importCcdDefinition(lib, SCOTLAND_CONFIG_FILE);
    }

    private void importAdmin(CFTLib lib) throws IOException {
        importCcdDefinition(lib, ADMIN_CONFIG_FILE);
    }

    private void importCcdDefinition(CFTLib lib, String file) throws IOException {
        Path definitionFile = resolveDefinitionFile(file);
        try {
            byte[] def = Files.readAllBytes(definitionFile);
            lib.importDefinition(def);
        } catch (Exception e) {
            log.error("Unable to import {} from {}", file, definitionFile, e);
            throw new IOException("Unable to import CCD definition " + definitionFile, e);
        }
    }

    private Path resolveDefinitionFile(String file) throws IOException {
        Path configuredPath = Path.of(file).normalize();
        if (Files.exists(configuredPath)) {
            return configuredPath.toAbsolutePath().normalize();
        }

        String relativePath = file.startsWith("./") ? file.substring(2) : file;
        Path currentDirectory = Path.of("").toAbsolutePath().normalize();
        while (currentDirectory != null) {
            Path candidate = currentDirectory.resolve(relativePath).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
            currentDirectory = currentDirectory.getParent();
        }

        throw new IOException(
                "Unable to locate CCD definition file '%s' from working directory '%s'"
                        .formatted(file, Path.of("").toAbsolutePath().normalize()));
    }

    private void startDockerCompose() {
        ControlPlane.waitForDB();
        DockerComposeProcessRunner.start();
    }
}
