package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesAllocateHearingConfig extends AllocateHearingConfig<EnglandWalesCaseData> {

    public EnglandWalesAllocateHearingConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            false,
            false
        );
    }
}
