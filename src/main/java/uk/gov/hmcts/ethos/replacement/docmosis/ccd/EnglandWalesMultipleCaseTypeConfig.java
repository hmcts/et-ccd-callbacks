package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesMultipleCaseTypeConfig extends MultipleCaseTypeConfig<EnglandWalesMultipleData> {

    public EnglandWalesMultipleCaseTypeConfig() {
        super(
            "ET_EnglandWales_Multiple",
            "Eng/Wales - Multiples",
            "England/Wales - Multiples",
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES
        );
    }
}
