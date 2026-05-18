package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesSingleFieldEventsConfig extends SingleFieldEventsConfig<EnglandWalesCaseData> {

    public EnglandWalesSingleFieldEventsConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            52,
            "Add telephone note"
        );
    }
}
