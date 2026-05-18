package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandNotificationConfig extends NotificationConfig<ScotlandCaseData> {

    public ScotlandNotificationConfig() {
        super(2, true);
    }
}
