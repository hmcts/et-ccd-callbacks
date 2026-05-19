package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandTseResponseConfig extends TseResponseConfig<ScotlandCaseData> {

    public ScotlandTseResponseConfig() {
        super(false, true);
    }
}
