package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;

public abstract class CaseFlagsConfig<T extends CaseData & HasFlagLauncher>
    implements CCDConfig<T, EtState, EtUserRole> {

    private final TypedPropertyGetter<T, CaseFlagsType> caseFlags;
    private final TypedPropertyGetter<T, String> flagLauncher;
    private final TypedPropertyGetter<T, CaseFlagsType> respondentFlags;
    private final TypedPropertyGetter<T, CaseFlagsType> claimantFlags;
    private final EtUserRole regionalCaseworkerRole;

    protected CaseFlagsConfig(
        TypedPropertyGetter<T, CaseFlagsType> caseFlags,
        TypedPropertyGetter<T, String> flagLauncher,
        TypedPropertyGetter<T, CaseFlagsType> respondentFlags,
        TypedPropertyGetter<T, CaseFlagsType> claimantFlags,
        EtUserRole regionalCaseworkerRole
    ) {
        this.caseFlags = caseFlags;
        this.flagLauncher = flagLauncher;
        this.respondentFlags = respondentFlags;
        this.claimantFlags = claimantFlags;
        this.regionalCaseworkerRole = regionalCaseworkerRole;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        caseFlagEvent(configBuilder, "createFlag", "Create a case flag", "Create Flag")
            .fields()
            .page("1")
            .field(caseFlags)
            .caseFlagBackingField()
            .done()
            .field(flagLauncher)
            .flagLauncherCreate()
            .done()
            .field(respondentFlags)
            .caseFlagBackingField()
            .done()
            .field(claimantFlags)
            .caseFlagBackingField()
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, regionalCaseworkerRole);

        caseFlagEvent(configBuilder, "manageFlags", "Manage case flags", "Manage Flags")
            .fields()
            .page("1")
            .field(caseFlags)
            .caseFlagBackingField()
            .pageFieldDisplayOrder(7)
            .done()
            .field(claimantFlags)
            .caseFlagBackingField()
            .pageFieldDisplayOrder(4)
            .done()
            .field(flagLauncher)
            .flagLauncherUpdate()
            .pageFieldDisplayOrder(6)
            .done()
            .field(respondentFlags)
            .caseFlagBackingField()
            .pageFieldDisplayOrder(5)
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, regionalCaseworkerRole);

        migrationEvent(configBuilder, "migrateCaseFlags", "Migrate Case Flags")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/case-flags-migration/about-to-submit")
            .submittedCallbackUrl("")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        migrationEvent(configBuilder, "rollbackCaseFlags", "Rollback Case Flags")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/case-flags-rollback/about-to-submit")
            .submittedCallbackUrl("")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> caseFlagEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        String description
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(description)
            .showSummary()
            .caseEventColumn("DisplayOrder", null)
            .caseEventColumn("EventEnablingCondition", "")
            .blankCallbackUrls();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> migrationEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .caseEventColumn("DisplayOrder", null);
    }
}
