package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesCaseTransferConfig extends CaseTransferConfig<EnglandWalesCaseData> {

    public EnglandWalesCaseTransferConfig() {
        super(
            36,
            "Transfer case to another office (API/Multiples)",
            37,
            38,
            47,
            true
        );
    }
}
