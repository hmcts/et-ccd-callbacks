package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.CcdRoleConfig;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.util.BeftaUtils;

import java.util.List;
import java.util.Locale;

/**
 * Configuration class for setting up high-level data in the CCD definition store and to help automate definition
 * file uploads through Jenkins.
 */
@Slf4j
public class HighLevelDataSetupConfiguration extends DataLoaderToDefinitionStore {

    private final CcdEnvironment environment;
    public static final String PUBLIC = "PUBLIC";
    private static final CcdRoleConfig[] CCD_ROLES = {
        new CcdRoleConfig("citizen", PUBLIC),
        new CcdRoleConfig("caseworker", PUBLIC),
        new CcdRoleConfig("caseworker-employment", PUBLIC),
        new CcdRoleConfig("caseworker-employment-englandwales", PUBLIC),
        new CcdRoleConfig("caseworker-employment-scotland", PUBLIC),
        new CcdRoleConfig("caseworker-employment-etjudge", PUBLIC),
        new CcdRoleConfig("caseworker-employment-etjudge-englandwales", PUBLIC),
        new CcdRoleConfig("caseworker-employment-etjudge-scotland", PUBLIC),
        new CcdRoleConfig("caseworker-employment-api", PUBLIC),
        new CcdRoleConfig("caseworker-employment-legalrep-solicitor", PUBLIC),
        new CcdRoleConfig("caseworker-wa-task-configuration", "RESTRICTED"),
        new CcdRoleConfig("et-acas-api", PUBLIC),
        new CcdRoleConfig("caseworker-ras-validation", PUBLIC),
        new CcdRoleConfig("GS_profile", PUBLIC),
        new CcdRoleConfig("TTL_profile", PUBLIC),
        new CcdRoleConfig("caseworker-wa", PUBLIC),
        new CcdRoleConfig("caseworker-wa-task-officer", PUBLIC),
        new CcdRoleConfig("caseworker-approver", PUBLIC),
        new CcdRoleConfig("caseworker-caa", PUBLIC),
        new CcdRoleConfig("caseworker-et-pcqextractor", PUBLIC),
    };

    public HighLevelDataSetupConfiguration(CcdEnvironment dataSetupEnvironment) {
        super(dataSetupEnvironment);
        environment = dataSetupEnvironment;
    }

    public static void main(String[] args) throws Throwable {
        DataLoaderToDefinitionStore.main(HighLevelDataSetupConfiguration.class, args);
    }

    @Override
    public void addCcdRoles() {
        for (CcdRoleConfig roleConfig : CCD_ROLES) {
            try {
                log.info("\n\nAdding CCD Role {}.", roleConfig);
                addCcdRole(roleConfig);
                log.info("\n\nAdded CCD Role {}.", roleConfig);
            } catch (Exception e) {
                log.error("\n\nCouldn't add CCD Role {} - Exception: {}.\n\n", roleConfig, e.getMessage());
                if (!shouldTolerateDataSetupFailure()) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void createRoleAssignments() {
        // Do not create role assignments.
        BeftaUtils.defaultLog("Will NOT create role assignments!");
    }

    @Override
    protected boolean shouldTolerateDataSetupFailure() {
        return true;
    }

    @Override
    protected List<String> getAllDefinitionFilesToLoadAt(String definitionsPath) {
        String env = environment.name().toLowerCase(Locale.UK);

        return List.of(
            "ccd-definitions/dist/%s/et-admin-ccd-config-%s.xlsx".formatted(env, env),
            "ccd-definitions/dist/%s/et-englandwales-ccd-config-%s.xlsx".formatted(env, env),
            "ccd-definitions/dist/%s/et-scotland-ccd-config-%s.xlsx".formatted(env, env)
        );
    }
}
