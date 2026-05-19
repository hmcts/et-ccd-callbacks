package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesInitialConsiderationConfig extends InitialConsiderationConfig<EnglandWalesCaseData> {

    public EnglandWalesInitialConsiderationConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            false
        );
    }
}
