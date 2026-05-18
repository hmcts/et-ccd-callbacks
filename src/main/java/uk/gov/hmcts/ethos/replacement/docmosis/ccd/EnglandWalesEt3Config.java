package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesEt3Config extends Et3Config<EnglandWalesCaseData> {

    public EnglandWalesEt3Config() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            false
        );
    }
}
