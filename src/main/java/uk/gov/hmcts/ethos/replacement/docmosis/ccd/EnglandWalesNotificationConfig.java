package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@Component
public class EnglandWalesNotificationConfig implements CCDConfig<EnglandWalesCaseData, EtState, EtUserRole> {

    @Override
    public void configure(ConfigBuilder<EnglandWalesCaseData, EtState, EtUserRole> configBuilder) {
        configBuilder.event("UPDATE_NOTIFICATION_STATE")
            .forAllStates()
            .name("Respond to a notification")
            .description("Respond to a notification")
            .displayOrder(71)
            .showCondition("caseType=\"dummy\"")
            .endButtonLabel("")
            .aboutToStartCallbackUrl("")
            .aboutToSubmitCallbackUrl("")
            .submittedCallbackUrl("")
            .omitLiveFrom()
            .omitPublish()
            .grant(Permission.CRUD, EtUserRole.CITIZEN);
    }
}
