package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandAllocateHearingConfig extends AllocateHearingConfig<ScotlandCaseData> {

    public ScotlandAllocateHearingConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            true,
            true,
            null
        );
    }
}
