package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;

@Component
public class EnglandWalesNotificationConfig extends NotificationConfig<EnglandWalesCaseData> {

    public EnglandWalesNotificationConfig() {
        super(
            1,
            false,
            EtUserRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
            EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
            false,
            false
        );
    }
}
