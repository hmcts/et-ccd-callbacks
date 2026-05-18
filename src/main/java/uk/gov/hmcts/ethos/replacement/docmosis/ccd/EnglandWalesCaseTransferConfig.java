package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@Component
public class EnglandWalesCaseTransferConfig extends CaseTransferConfig<EnglandWalesCaseData> {

    public EnglandWalesCaseTransferConfig() {
        super(
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            new SameCountryTransfer(33),
            new DifferentCountryTransfer(
                "Case Transfer (Scotland)",
                "Transfer case to Scotland",
                35,
                "${ET_COS_URL}/caseTransfer/initTransferToScotland",
                true
            ),
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
}
