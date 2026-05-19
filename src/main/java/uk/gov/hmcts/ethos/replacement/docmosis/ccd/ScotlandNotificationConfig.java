package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class ScotlandNotificationConfig extends NotificationConfig<ScotlandCaseData> {

    public ScotlandNotificationConfig() {
        super(
            2,
            true,
            EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND
        );
    }
}
