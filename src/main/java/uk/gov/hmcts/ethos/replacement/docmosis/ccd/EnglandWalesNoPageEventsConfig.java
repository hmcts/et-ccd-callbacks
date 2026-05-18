package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesNoPageEventsConfig extends NoPageEventsConfig<EnglandWalesCaseData> {

    public EnglandWalesNoPageEventsConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            53,
            54,
            63
        );
    }
}
