package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@Component
public class EnglandWalesBundlesConfig extends BundlesConfig<EnglandWalesCaseData> {

    public EnglandWalesBundlesConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            false,
            "Remove Hearing Documents",
            "[STATE]!=\"AWAITING_SUBMISSION_TO_HMCTS\"",
            Permission.CRU
        );
    }
}
