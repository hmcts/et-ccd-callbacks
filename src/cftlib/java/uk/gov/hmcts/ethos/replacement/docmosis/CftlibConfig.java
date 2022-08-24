package uk.gov.hmcts.ethos.replacement.docmosis;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftlibConfig implements CFTLibConfigurer {
    @Override
    public void configure(CFTLib lib) throws Exception {
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

        // Create our roles in CCD to allow our definition to import.
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
                "caseworker-et-pcqextractor"
        );
    }
}
