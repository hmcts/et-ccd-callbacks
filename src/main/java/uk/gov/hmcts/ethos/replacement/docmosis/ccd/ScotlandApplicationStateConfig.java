package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandApplicationStateConfig extends ApplicationStateConfig<ScotlandCaseData> {

    public ScotlandApplicationStateConfig() {
        super(44, false);
    }
}
