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

    @Override
    protected int addAmendJurisdictionDisplayOrder() {
        return 18;
    }

    @Override
    protected int adrDocumentsDisplayOrder() {
        return 57;
    }

    @Override
    protected int piiDocumentsDisplayOrder() {
        return 58;
    }

    @Override
    protected int appealDocumentsDisplayOrder() {
        return 59;
    }
}
