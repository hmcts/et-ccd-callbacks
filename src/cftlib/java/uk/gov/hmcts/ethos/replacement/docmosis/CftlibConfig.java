package uk.gov.hmcts.ethos.replacement.docmosis;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class CftlibConfig implements CFTLibConfigurer {
    @Override
    public void configure(CFTLib lib) throws Exception {
        // Create a test user in the idam simulator.
        lib.createIdamUser("a@b.com",
                "caseworker",
                "caseworker-employment",
                "caseworker-employment-englandwales");
        // Create our roles in CCD to allow our definition to import.
        lib.createRoles(
                "caseworker-employment",
                "caseworker-employment-api",
                "caseworker-employment-englandwales",
                "caseworker-employment-etjudge",
                "caseworker-employment-etjudge-englandwales"
        );
        // Import our definition.
        var def = Files.readAllBytes(
                Path.of("et-ccd-definitions-englandwales/definitions/xlsx/et-englandwales-ccd-config-cftlib.xlsx"));
        lib.importDefinition(def);
    }
}
