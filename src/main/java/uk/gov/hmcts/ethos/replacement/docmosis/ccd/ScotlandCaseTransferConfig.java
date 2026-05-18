package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandCaseTransferConfig extends CaseTransferConfig<ScotlandCaseData> {

    public ScotlandCaseTransferConfig() {
        super(
            34,
            "Transfer case to another office (Multiples)",
            35,
            36,
            45,
            false
        );
    }
}
