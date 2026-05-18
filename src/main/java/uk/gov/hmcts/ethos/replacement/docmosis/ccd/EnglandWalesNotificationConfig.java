package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesNotificationConfig extends NotificationConfig<EnglandWalesCaseData> {

    public EnglandWalesNotificationConfig() {
        super(1, false);
    }
}
