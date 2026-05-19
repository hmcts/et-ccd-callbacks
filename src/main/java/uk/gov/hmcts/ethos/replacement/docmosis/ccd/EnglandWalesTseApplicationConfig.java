package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesTseApplicationConfig extends TseApplicationConfig<EnglandWalesCaseData> {

    public EnglandWalesTseApplicationConfig() {
        super(54, false);
    }
}
