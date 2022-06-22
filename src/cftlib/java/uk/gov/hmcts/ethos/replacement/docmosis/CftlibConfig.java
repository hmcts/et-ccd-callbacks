package uk.gov.hmcts.ethos.replacement.docmosis;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftlibConfig implements CFTLibConfigurer {
  @Override
  public void configure(CFTLib lib) throws Exception {
    // Create a test user in the idam simulator.
    lib.createIdamUser("et.dev@hmcts.net",
      "caseworker",
      "caseworker-employment",
      "caseworker-employment-englandwales");
    // Create our roles in CCD to allow our definition to import.
    lib.createRoles(
            "caseworker-employment",
            "caseworker-employment-api",
            "caseworker-employment-englandwales",
            "caseworker-employment-etjudge",
            "caseworker-employment-etjudge-englandwales",
            "caseworker-employment-legalrep-solicitor",
            "citizen",
            "caseworker-ET-pcqextractor",
            "et-legal-rep"
    );
    // Import our definition.
    var def = Files.readAllBytes(Path.of("../et-ccd-definitions-englandwales/definitions/xlsx/et-englandwales-ccd-config-local.xlsx"));
    lib.importDefinition(def);
  }
}
