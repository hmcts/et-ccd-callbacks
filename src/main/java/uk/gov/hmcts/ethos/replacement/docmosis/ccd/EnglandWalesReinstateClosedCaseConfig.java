package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesReinstateClosedCaseConfig extends ReinstateClosedCaseConfig<EnglandWalesCaseData> {

    public EnglandWalesReinstateClosedCaseConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            41
        );
    }
}
