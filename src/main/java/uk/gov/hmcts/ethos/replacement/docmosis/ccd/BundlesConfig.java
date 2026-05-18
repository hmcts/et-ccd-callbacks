package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class BundlesConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final boolean grantClaimantSubmissionToApi;

    protected BundlesConfig(boolean grantClaimantSubmissionToApi) {
        this.grantClaimantSubmissionToApi = grantClaimantSubmissionToApi;
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
    }
}
