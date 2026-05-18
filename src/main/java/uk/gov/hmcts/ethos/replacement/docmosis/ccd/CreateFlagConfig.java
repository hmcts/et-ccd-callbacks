package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;

public abstract class CreateFlagConfig<T extends CaseData & HasFlagLauncher>
    implements CCDConfig<T, EtState, EtUserRole> {

    private final TypedPropertyGetter<T, CaseFlagsType> caseFlags;
    private final TypedPropertyGetter<T, String> flagLauncher;
    private final TypedPropertyGetter<T, CaseFlagsType> respondentFlags;
    private final TypedPropertyGetter<T, CaseFlagsType> claimantFlags;
    private final EtUserRole regionalCaseworkerRole;

    protected CreateFlagConfig(
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
        configBuilder.event("createFlag")
            .forAllStates()
            .name("Create a case flag")
            .description("Create Flag")
            .showSummary()
            .caseEventColumn("DisplayOrder", null)
            .caseEventColumn("EventEnablingCondition", "")
            .blankCallbackUrls()
            .fields()
            .page("1")
            .field(caseFlags)
            .optional()
            .showCondition("flagLauncher=\"hidden\"")
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("RetainHiddenValue", "Yes")
            .done()
            .field(flagLauncher)
            .optional()
            .displayContextParameter("#ARGUMENT(CREATE)")
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(respondentFlags)
            .optional()
            .showCondition("flagLauncher=\"hidden\"")
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("RetainHiddenValue", "Yes")
            .done()
            .field(claimantFlags)
            .optional()
            .showCondition("flagLauncher=\"hidden\"")
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("RetainHiddenValue", "Yes")
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, regionalCaseworkerRole);
    }
}
