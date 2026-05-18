package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;

public abstract class CaseLinksConfig<T extends CaseData & HasLinkedCasesComponentLauncher>
    implements CCDConfig<T, EtState, EtUserRole> {

    private static final String LINKED_CASES_COMPONENT_LAUNCHER = "LinkedCasesComponentLauncher";
    private static final String LINKED_CASES_COMPONENT = "LinkedCases";

    private final TypedPropertyGetter<T, ListTypeItem<CaseLink>> caseLinks;
    private final TypedPropertyGetter<T, String> linkedCasesComponentLauncher;
    private final EtUserRole regionalCaseworkerRole;

    protected CaseLinksConfig(
        TypedPropertyGetter<T, ListTypeItem<CaseLink>> caseLinks,
        TypedPropertyGetter<T, String> linkedCasesComponentLauncher,
        EtUserRole regionalCaseworkerRole
    ) {
        this.caseLinks = caseLinks;
        this.linkedCasesComponentLauncher = linkedCasesComponentLauncher;
        this.regionalCaseworkerRole = regionalCaseworkerRole;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        caseLinkEvent(configBuilder, "createCaseLink", "Link cases", "To link related cases", 63)
            .endButtonLabel("Create Case Link")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/caseLinks/create/aboutToSubmit")
            .fields()
            .page("createCaseLink")
            .field(caseLinks)
            .componentLauncherBackingField(LINKED_CASES_COMPONENT_LAUNCHER)
            .done()
            .field(linkedCasesComponentLauncher)
            .componentLauncherCreate(LINKED_CASES_COMPONENT)
            .pageFieldDisplayOrder(1)
            .done()
            .done()
            .grant(Permission.CRU, regionalCaseworkerRole);

        caseLinkEvent(configBuilder, "maintainCaseLink", "Manage case links", "To maintain linked cases", 64)
            .showSummary()
            .endButtonLabel("Maintain Case Links")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/caseLinks/maintain/aboutToSubmit")
            .fields()
            .page("maintainCaseLink")
            .field(caseLinks)
            .componentLauncherBackingField(LINKED_CASES_COMPONENT_LAUNCHER)
            .done()
            .field(linkedCasesComponentLauncher)
            .componentLauncherUpdate(LINKED_CASES_COMPONENT)
            .pageFieldDisplayOrder(1)
            .done()
            .done()
            .grant(Permission.CRUD, regionalCaseworkerRole);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> caseLinkEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        String description,
        int displayOrder
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(description)
            .displayOrder(displayOrder)
            .blankCallbackUrls();
    }
}
