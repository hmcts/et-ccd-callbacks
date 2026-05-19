package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesReferralConfig extends ReferralConfig<EnglandWalesCaseData> {

    public EnglandWalesReferralConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            48,
            "Create Referral",
            12,
            13,
            51
        );
    }
}
