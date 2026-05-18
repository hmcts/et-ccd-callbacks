package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandCaseFlagsConfig extends CreateFlagConfig<ScotlandCaseData> {

    public ScotlandCaseFlagsConfig() {
        super(
            ScotlandCaseData::getCaseFlags,
            ScotlandCaseData::getFlagLauncher,
            ScotlandCaseData::getRespondentFlags,
            ScotlandCaseData::getClaimantFlags,
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND
        );
    }
}
