package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@Component
public class ScotlandBundlesConfig extends BundlesConfig<ScotlandCaseData> {

    public ScotlandBundlesConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            true,
            "Remove hearing documents",
            " ",
            Permission.CRUD
        );
    }
}
