package uk.gov.hmcts.ethos.replacement.docmosis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public void configure(CFTLib lib) {
        createRoles(lib);
        createUsers(lib);
        importCcdDefinitions(lib);
    }

    private void createRoles(CFTLib lib) {
        lib.createRoles(
                "caseworker-employment",
                "caseworker-employment-api",
                "caseworker-employment-englandwales",
                "caseworker-employment-scotland",
                "caseworker-employment-etjudge",
                "caseworker-employment-etjudge-englandwales",
                "caseworker-employment-etjudge-scotland",
                "citizen",
                "caseworker-employment-legalrep-solicitor",
                "caseworker-et-pcqextractor");
    }

    private void createUsers(CFTLib lib) {
        // Create importer user
        lib.createIdamUser("ccd.docker.default@hmcts.net",
                "ccd-import");

        // Create test users in the idam simulator.
        lib.createIdamUser("englandwales@hmcts.net",
                "caseworker",
                "caseworker-employment",
                "caseworker-employment-englandwales");

        lib.createIdamUser("scotland@hmcts.net",
                "caseworker",
                "caseworker-employment",
                "caseworker-employment-scotland");

        lib.createIdamUser("admin@hmcts.net",
                "caseworker",
                "caseworker-employment",
                "caseworker-employment-api");
    }

    private void importCcdDefinitions(CFTLib lib) {
        if (importCcdDefsOnBoot) {
            importEnglandWales(lib);
            importScotland(lib);
            importAdmin(lib);
        }
    }

    private void importEnglandWales(CFTLib lib) {
        String file = englandWalesCcdConfigPath + "/definitions/xlsx/et-englandwales-ccd-config-cftlib.xlsx";
        importCcdDefinition(lib, file);
    }

    private void importScotland(CFTLib lib) {
        String file = scotlandCcdConfigPath + "/definitions/xlsx/et-scotland-ccd-config-cftlib.xlsx";
        importCcdDefinition(lib, file);
    }

    private void importAdmin(CFTLib lib) {
        String file = adminCcdConfigPath + "/definitions/xlsx/et-admin-ccd-config-cftlib.xlsx";
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
}
