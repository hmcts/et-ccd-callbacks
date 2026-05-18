package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@Component
public class EnglandWalesCaseTransferConfig extends CaseTransferConfig<EnglandWalesCaseData> {

    public EnglandWalesCaseTransferConfig() {
        super(
            36,
            "Transfer case to another office (API/Multiples)",
            37,
            38,
            39,
            Permission.CRUD,
            false,
            47,
            true
        );
    }

    @Override
    protected EtUserRole regionalCaseworkerRole() {
        return EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES;
    }
}
