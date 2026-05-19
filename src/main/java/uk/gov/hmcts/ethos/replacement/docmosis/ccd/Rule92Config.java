package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.Set;

public abstract class Rule92Config<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final int claimantTseSubmitDisplayOrder;
    private final int claimantTseRespondDisplayOrder;
    private final boolean hideClaimantTseCollection;
    private final boolean grantTseEventsToApi;
    private final Integer respondentTseAllApplicationsDisplayOrder;
    private final int viewRespondentTseApplicationsDisplayOrder;
    private final boolean grantRespondentTseAllApplicationsToAcas;

    protected Rule92Config(
        int claimantTseSubmitDisplayOrder,
        int claimantTseRespondDisplayOrder,
        boolean hideClaimantTseCollection,
        boolean grantTseEventsToApi,
        Integer respondentTseAllApplicationsDisplayOrder,
        int viewRespondentTseApplicationsDisplayOrder,
        boolean grantRespondentTseAllApplicationsToAcas
    ) {
        this.claimantTseSubmitDisplayOrder = claimantTseSubmitDisplayOrder;
        this.claimantTseRespondDisplayOrder = claimantTseRespondDisplayOrder;
        this.hideClaimantTseCollection = hideClaimantTseCollection;
        this.grantTseEventsToApi = grantTseEventsToApi;
        this.respondentTseAllApplicationsDisplayOrder = respondentTseAllApplicationsDisplayOrder;
        this.viewRespondentTseApplicationsDisplayOrder = viewRespondentTseApplicationsDisplayOrder;
        this.grantRespondentTseAllApplicationsToAcas = grantRespondentTseAllApplicationsToAcas;
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

        grantClaimantTseViewAccess(
            allApplicationsEvent(
                configBuilder,
                "claimantTseAllApplications",
                null,
                "${ET_COS_URL}/claimantTSE/displayTable",
                CaseData::getClaimantTseAllApplicationsLabel,
                CaseData::getClaimantTseTableMarkUp,
                CaseData::getClaimantTseTableLabel,
                "claimantTseAllApplicationsLabel=\"dummy\""
            )
        );

        grantClaimantTseViewAccess(
            viewTseApplicationEvent(
                configBuilder,
                "viewClaimantTSEApplications",
                56,
                "${ET_COS_URL}/claimantTSE/viewApplicationsAboutToStart",
                "${ET_COS_URL}/claimantTSE/midPopulateChooseApplication",
                "${ET_COS_URL}/claimantTSE/midPopulateSelectedApplicationData"
            )
        );

        Event.EventBuilder<T, EtUserRole, EtState> respondentAllApplications = grantRespondentTseViewAccess(
            allApplicationsEvent(
                configBuilder,
                "respondentTseAllApplications",
                respondentTseAllApplicationsDisplayOrder,
                "${ET_COS_URL}/respondentTSE/displayTable",
                CaseData::getResTseAllApplicationsLabel,
                CaseData::getResTseTableMarkUp,
                CaseData::getResTseTableLabel,
                "resTseAllApplicationsLabel=\"dummy\""
            )
        );

        if (grantRespondentTseAllApplicationsToAcas) {
            respondentAllApplications.grant(Permission.R, EtUserRole.ET_ACAS_API);
        }

        grantRespondentTseViewAccess(
            viewTseApplicationEvent(
                configBuilder,
                "viewRespondentTSEApplications",
                viewRespondentTseApplicationsDisplayOrder,
                "${ET_COS_URL}/viewRespondentTSEApplications/aboutToStart",
                "${ET_COS_URL}/viewRespondentTSEApplications/midPopulateChooseApplication",
                "${ET_COS_URL}/viewRespondentTSEApplications/midPopulateSelectedApplicationData"
            )
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

    private Event.EventBuilder<T, EtUserRole, EtState> allApplicationsEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        Integer displayOrder,
        String aboutToStartCallbackUrl,
        TypedPropertyGetter<T, ?> headerLabel,
        TypedPropertyGetter<T, ?> tableMarkup,
        TypedPropertyGetter<T, ?> tableLabel,
        String tableShowCondition
    ) {
        Event.EventBuilder<T, EtUserRole, EtState> event = configBuilder.event(eventId)
            .forAllStates()
            .name("All applications")
            .description(" ")
            .showCondition("caseType =\"dummy\"")
            .caseEventColumn("ShowSummary", null)
            .aboutToStartCallbackUrl(aboutToStartCallbackUrl)
            .endButtonLabel("Close and return to case details");

        applyDisplayOrder(event, displayOrder);

        return event.fields()
            .page("1")
            .pageLabel(" ")
            .field(headerLabel)
            .readOnly()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(tableMarkup)
            .readOnly()
            .showCondition(tableShowCondition)
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(tableLabel)
            .readOnly()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> viewTseApplicationEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        int displayOrder,
        String aboutToStartCallbackUrl,
        String chooseApplicationCallbackUrl,
        String selectedApplicationCallbackUrl
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name("View application")
            .description(" ")
            .displayOrder(displayOrder)
            .showCondition("caseType=\"dummy\"")
            .caseEventColumn("ShowSummary", null)
            .aboutToStartCallbackUrl(aboutToStartCallbackUrl)
            .endButtonLabel("Close and return to case details")
            .fields()
            .pageWithCallbackUrl("1", chooseApplicationCallbackUrl)
            .pageLabel("")
            .field(CaseData::getTseViewApplicationOpenOrClosed)
            .mandatory()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .pageWithCallbackUrl("2", selectedApplicationCallbackUrl)
            .pageLabel("")
            .field(CaseData::getTseViewApplicationSelect)
            .mandatory()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .page("3")
            .field(CaseData::getTseApplicationSummaryAndResponsesMarkup)
            .readOnly()
            .showCondition("tseApplicationSummaryAndResponsesMarkupLabel=\"dummy\"")
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getTseApplicationSummaryAndResponsesMarkupLabel)
            .readOnly()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .done();
    }

    private void applyDisplayOrder(Event.EventBuilder<T, EtUserRole, EtState> event, Integer displayOrder) {
        if (displayOrder == null) {
            event.caseEventColumn("DisplayOrder", null);
        } else {
            event.displayOrder(displayOrder);
        }
    }

    private Event.EventBuilder<T, EtUserRole, EtState> grantClaimantTseViewAccess(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event
            .grant(Permission.D, EtUserRole.respondentSolicitors())
            .grant(Permission.CRUD, EtUserRole.CLAIMANT_SOLICITOR, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> grantRespondentTseViewAccess(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event
            .grant(Permission.CRUD, EtUserRole.respondentSolicitors())
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.D, EtUserRole.CLAIMANT_SOLICITOR);
    }

    private Set<Permission> waTaskPermissionForTseRespond() {
        return grantTseEventsToApi ? Permission.CRUD : Permission.CRU;
    }
}
