package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandEt3Config extends Et3Config<ScotlandCaseData> {

    public ScotlandEt3Config() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            true,
            true,
            44,
            "managingOffice !=\"Unassigned\"",
            true
        );
    }
}
