package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesCaseFlagsConfig extends CaseFlagsConfig<EnglandWalesCaseData> {

    public EnglandWalesCaseFlagsConfig() {
        super(
            EnglandWalesCaseData::getCaseFlags,
            EnglandWalesCaseData::getFlagLauncher,
            EnglandWalesCaseData::getRespondentFlags,
            EnglandWalesCaseData::getClaimantFlags,
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES
        );
    }
}
