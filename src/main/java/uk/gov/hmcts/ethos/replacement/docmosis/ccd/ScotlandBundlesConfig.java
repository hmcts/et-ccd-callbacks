package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandBundlesConfig extends BundlesConfig<ScotlandCaseData> {

    public ScotlandBundlesConfig() {
        super(true);
    }
}
