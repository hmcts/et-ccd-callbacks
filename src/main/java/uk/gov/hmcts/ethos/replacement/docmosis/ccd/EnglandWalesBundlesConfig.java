package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesBundlesConfig extends BundlesConfig<EnglandWalesCaseData> {

    public EnglandWalesBundlesConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            false,
            "Remove Hearing Documents"
        );
    }
}
