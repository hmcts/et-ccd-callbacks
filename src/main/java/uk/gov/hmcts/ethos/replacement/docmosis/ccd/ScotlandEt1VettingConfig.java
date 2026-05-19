package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandEt1VettingConfig extends Et1VettingConfig<ScotlandCaseData> {

    public ScotlandEt1VettingConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            true
        );
    }
}
