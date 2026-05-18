package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class BundlesConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final EtUserRole regionalCaseworkerRole;
    private final boolean grantClaimantSubmissionToApi;
    private final String removeHearingBundlesDescription;

    protected BundlesConfig(
        EtUserRole regionalCaseworkerRole,
        boolean grantClaimantSubmissionToApi,
        String removeHearingBundlesDescription
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.grantClaimantSubmissionToApi = grantClaimantSubmissionToApi;
        this.removeHearingBundlesDescription = removeHearingBundlesDescription;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        Event.EventBuilder<T, EtUserRole, EtState> submitClaimantBundles = configBuilder
            .event("SUBMIT_CLAIMANT_BUNDLES")
            .forAllStates()
            .name("Submit hearing docs")
            .description("Submit hearing docs")
            .showCondition("caseType=\"dummy\"")
            .caseEventColumn("DisplayOrder", null)
            .blankCallbackUrls()
            .grant(Permission.CRUD, EtUserRole.CREATOR);

        if (grantClaimantSubmissionToApi) {
            submitClaimantBundles.grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
        }

        configBuilder.event("removeHearingBundles")
            .forAllStates()
            .name("Remove hearing documents")
            .description(removeHearingBundlesDescription)
            .showCondition(" ")
            .caseEventColumn("DisplayOrder", null)
            .aboutToStartCallbackUrl("")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/bundlesRespondent/removeHearingBundle")
            .fields()
            .pageWithCallbackUrl("1", "${ET_COS_URL}/bundlesRespondent/midPopulateRemoveHearingBundles")
            .pageLabel("Please specify the party whose hearing bundles are to be removed")
            .field(CaseData::getRemoveBundleDropDownSelectedParty)
            .mandatory()
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .done()
            .page("2")
            .pageLabel("Remove Hearing Bundle")
            .field(CaseData::getRemoveHearingBundleSelect)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getHearingBundleRemoveReason)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .done()
            .grant(Permission.CRUD, regionalCaseworkerRole, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }
}
