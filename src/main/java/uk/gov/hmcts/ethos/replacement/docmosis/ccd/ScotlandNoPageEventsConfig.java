package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandNoPageEventsConfig extends NoPageEventsConfig<ScotlandCaseData> {

    public ScotlandNoPageEventsConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            51,
            52,
            59,
            true
        );
    }
}
