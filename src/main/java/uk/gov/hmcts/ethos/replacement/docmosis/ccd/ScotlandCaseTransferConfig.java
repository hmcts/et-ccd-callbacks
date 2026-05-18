package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@Component
public class ScotlandCaseTransferConfig extends CaseTransferConfig<ScotlandCaseData> {

    public ScotlandCaseTransferConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
            null,
            new DifferentCountryTransfer(
                "Case Transfer (England/Wales)",
                "Transfer case to England/Wales",
                33,
                "${ET_COS_URL}/caseTransfer/initTransferToEnglandWales",
                false
            ),
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
}
