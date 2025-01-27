package uk.gov.hmcts.ethos.replacement.docmosis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.ControlPlane;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

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
public class CftlibConfig implements CFTLibConfigurer {

    @Value("${cftlib.import-ccd-defs-on-boot}")
    private boolean importCcdDefsOnBoot;

    @Value("${cftlib.englandwales-ccd-config-path}")
    private String englandWalesCcdConfigPath;

    @Value("${cftlib.scotland-ccd-config-path}")
    private String scotlandCcdConfigPath;

    @Value("${cftlib.admin-ccd-config-path}")
    private String adminCcdConfigPath;

    @Override
    public void configure(CFTLib lib) throws IOException, URISyntaxException {
        createRoles(lib);
        createUsers(lib);
        importCcdDefinitions(lib);
        startDockerCompose();
    }

    private void createRoles(CFTLib lib) {
        lib.createRoles(CASEWORKER_ROLES);
        lib.createRoles(ECM_CASEWORKER_ROLES);
        lib.createRoles(ECM_JUDGE_ROLES);
    }

    private void createUsers(CFTLib lib) throws IOException, URISyntaxException {
        // Create importer user
        lib.createIdamUser(CCD_DOCKER_DEFAULT_EMAIL,
            CCD_IMPORT);

        String fileName = "roleAssignment.json";
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource(fileName)).toURI())));

        lib.configureRoleAssignments(json);

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

        lib.createIdamUser("et-case-officer@fake.hmcts.net",
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

        // Claimant is a citizen
        lib.createIdamUser(CITIZEN_EMAIL, CITIZEN);

        // Respondent is a citizen
        lib.createIdamUser(RESPONDENT_EMAIL, CITIZEN);

        // Required by ccd-data-store-api
        lib.createIdamUser(IDAM_SYSTEM_USER_EMAIL, CASEWORKER);

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
    }

    private void importCcdDefinitions(CFTLib lib) {
        if (importCcdDefsOnBoot) {
            importEnglandWales(lib);
            importScotland(lib);
            importAdmin(lib);
        }
    }

    private void importEnglandWales(CFTLib lib) {
        String file = englandWalesCcdConfigPath + ENGLANGWALES_CONFIG_FILE;
        importCcdDefinition(lib, file);
    }

    private void importScotland(CFTLib lib) {
        String file = scotlandCcdConfigPath + SCOTLAND_CONFIG_FILE;
        importCcdDefinition(lib, file);
    }

    private void importAdmin(CFTLib lib) {
        String file = adminCcdConfigPath + ADMIN_CONFIG_FILE;
        importCcdDefinition(lib, file);
    }

    private void importCcdDefinition(CFTLib lib, String file) {
        try {
            byte[] def = Files.readAllBytes(Path.of(file));
            lib.importDefinition(def);
        } catch (IOException e) {
            log.error("Unable to import {},", file);
        }
    }

    private void startDockerCompose() {
        ControlPlane.waitForDB();
        DockerComposeProcessRunner.start();
    }
}
