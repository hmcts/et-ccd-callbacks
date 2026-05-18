package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesApplicationStateConfig extends ApplicationStateConfig<EnglandWalesCaseData> {

    public EnglandWalesApplicationStateConfig() {
        super(59, true);
    }
}
