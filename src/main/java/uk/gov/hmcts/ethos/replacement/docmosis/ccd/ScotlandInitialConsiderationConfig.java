package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandInitialConsiderationConfig extends InitialConsiderationConfig<ScotlandCaseData> {

    public ScotlandInitialConsiderationConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            true
        );
    }
}
