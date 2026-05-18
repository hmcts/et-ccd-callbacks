package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@Component
public class ScotlandCaseTransferConfig extends CaseTransferConfig<ScotlandCaseData> {

    public ScotlandCaseTransferConfig() {
        super(
            34,
            "Transfer case to another office (Multiples)",
            35,
            36,
            37,
            Permission.CRU,
            true,
            45,
            false
        );
    }

    @Override
    protected EtUserRole regionalCaseworkerRole() {
        return EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND;
    }
}
