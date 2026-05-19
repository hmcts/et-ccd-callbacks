package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.Set;

public abstract class Rule92Config<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final int claimantTseSubmitDisplayOrder;
    private final int claimantTseRespondDisplayOrder;
    private final boolean hideClaimantTseCollection;
    private final boolean grantTseEventsToApi;

    protected Rule92Config(
        int claimantTseSubmitDisplayOrder,
        int claimantTseRespondDisplayOrder,
        boolean hideClaimantTseCollection,
        boolean grantTseEventsToApi
    ) {
        this.claimantTseSubmitDisplayOrder = claimantTseSubmitDisplayOrder;
        this.claimantTseRespondDisplayOrder = claimantTseRespondDisplayOrder;
        this.hideClaimantTseCollection = hideClaimantTseCollection;
        this.grantTseEventsToApi = grantTseEventsToApi;
    }

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

        submitTseEvent(
            configBuilder,
            "SUBMIT_CLAIMANT_TSE",
            "Create an application",
            claimantTseSubmitDisplayOrder,
            "${ET_COS_URL}/tseClaimant/aboutToSubmit",
            EtUserRole.CREATOR,
            Permission.CRUD,
            hideClaimantTseCollection
        );

        submitTseEvent(
            configBuilder,
            "SUBMIT_RESPONDENT_TSE",
            "Create an application",
            57,
            "${ET_COS_URL}/tseRespondent/aboutToSubmit",
            EtUserRole.DEFENDANT,
            Permission.CRUD,
            true
        );

        submitTseEvent(
            configBuilder,
            "CLAIMANT_TSE_RESPOND",
            "Respond to an application",
            claimantTseRespondDisplayOrder,
            "",
            EtUserRole.CREATOR,
            waTaskPermissionForTseRespond(),
            hideClaimantTseCollection
        );

        submitTseEvent(
            configBuilder,
            "RESPONDENT_TSE_RESPOND",
            "Respond to an application",
            58,
            "",
            EtUserRole.DEFENDANT,
            waTaskPermissionForTseRespond(),
            true
        );
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

    private Event.EventBuilder<T, EtUserRole, EtState> submitTseEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        int displayOrder,
        String aboutToSubmitCallbackUrl,
        EtUserRole citizenRole,
        Set<Permission> waTaskPermissions,
        boolean hideApplicationCollection
    ) {
        Event.EventBuilder<T, EtUserRole, EtState> event = tseFields(
            configBuilder.event(eventId)
                .forAllStates()
                .name(name)
                .description(name)
                .displayOrder(displayOrder)
                .showCondition("caseType=\"dummy\"")
                .publishToCamunda()
                .aboutToStartCallbackUrl("")
                .aboutToSubmitCallbackUrl(aboutToSubmitCallbackUrl)
                .submittedCallbackUrl(""),
            hideApplicationCollection
        )
            .grant(Permission.CRUD, citizenRole)
            .grant(waTaskPermissions, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        if (grantTseEventsToApi) {
            event.grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
        }

        return event;
    }

    private Event.EventBuilder<T, EtUserRole, EtState> tseFields(
        Event.EventBuilder<T, EtUserRole, EtState> event,
        boolean hideApplicationCollection
    ) {
        return event.fields()
            .page("1")
            .field(CaseData::getGenericTseApplicationCollection)
            .caseEventColumn("PageShowCondition", hideApplicationCollection
                ? "genericTseApplicationCollection.type=\"dummy\""
                : null)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn("PageColumnNumber", 1)
            .caseEventColumn("Publish", "Y")
            .done()
            .done();
    }

    private Set<Permission> waTaskPermissionForTseRespond() {
        return grantTseEventsToApi ? Permission.CRUD : Permission.CRU;
    }
}
