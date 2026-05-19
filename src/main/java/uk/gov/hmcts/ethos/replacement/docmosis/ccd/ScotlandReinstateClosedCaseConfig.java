package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandReinstateClosedCaseConfig extends ReinstateClosedCaseConfig<ScotlandCaseData> {

    public ScotlandReinstateClosedCaseConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            39
        );
    }
}
