package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesRule92Config extends Rule92Config<EnglandWalesCaseData> {

    public EnglandWalesRule92Config() {
        super(57, 58, true, false);
    }
}
