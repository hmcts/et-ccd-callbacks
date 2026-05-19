package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesTseResponseConfig extends TseResponseConfig<EnglandWalesCaseData> {

    public EnglandWalesTseResponseConfig() {
        super(true, false);
    }
}
