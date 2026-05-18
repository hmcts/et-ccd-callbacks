package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class Rule92Config<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        hiddenStoredEvent(configBuilder, "STORE_CLAIMANT_TSE", "Store an application", "Store an application", "dummy")
            .grant(Permission.CRU, EtUserRole.CREATOR);

        hiddenStoredEvent(
            configBuilder,
            "STORE_CLAIMANT_TSE_RESPOND",
            "Store response to application",
            "Store a response to an application",
            "dummy"
        )
            .grant(Permission.CRU, EtUserRole.CREATOR);

        hiddenStoredEvent(
            configBuilder,
            "SUBMIT_STORED_CLAIMANT_TSE_RESPOND",
            "Submit stored response to TSE",
            "Submit stored a response to an application",
            "dummy"
        )
            .grant(Permission.CRUD, EtUserRole.CREATOR);

        hiddenStoredEvent(
            configBuilder,
            "STORE_PSE_RESPONSE",
            "Store notification response",
            "Store notification response",
            "Dummy"
        )
            .grant(Permission.CRU, EtUserRole.CREATOR);

        hiddenStoredEvent(
            configBuilder,
            "SUBMIT_STORED_PSE_RESPONSE",
            "Submit stored pse response",
            "Submit stored notification response",
            "Dummy"
        )
            .grant(Permission.CRUD, EtUserRole.CREATOR);

        hiddenStoredEvent(
            configBuilder,
            "STORE_RESPONDENT_TSE",
            "Store respondent application",
            "Store respondent application",
            "dummy"
        )
            .grant(Permission.CRU, EtUserRole.DEFENDANT);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> hiddenStoredEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        String description,
        String caseTypeValue
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(description)
            .showCondition("caseType=\"" + caseTypeValue + "\"")
            .caseEventColumn("DisplayOrder", null)
            .blankCallbackUrls();
    }
}
