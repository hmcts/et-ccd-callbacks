package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.EnumSet;

public abstract class BundlesConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean grantClaimantSubmissionToApi;
    private final String removeHearingBundlesDescription;

    protected BundlesConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean grantClaimantSubmissionToApi,
        String removeHearingBundlesDescription
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
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

        configBuilder.event("uploadHearingDocuments")
            .forStateTransition(EtState.ACCEPTED, EnumSet.allOf(EtState.class))
            .name("Upload Hearing Documents")
            .description("Upload Hearing Documents")
            .caseEventColumn("DisplayOrder", null)
            .aboutToStartCallbackUrl("${ET_COS_URL}/uploadHearingDocuments/aboutToStart")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/uploadHearingDocuments/aboutToSubmit")
            .fields()
            .page("1")
            .field(CaseData::getUploadHearingDocumentsSelectPastOrFutureHearing)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("PageLabel", "Upload Hearing Documents")
            .done()
            .field(CaseData::getUploadHearingDocumentsSelectPastHearing)
            .mandatory()
            .showCondition("uploadHearingDocumentsSelectPastOrFutureHearing=\"Past\"")
            .showSummary()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getUploadHearingDocumentsSelectFutureHearing)
            .mandatory()
            .showCondition("uploadHearingDocumentsSelectPastOrFutureHearing=\"Future\"")
            .showSummary()
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .done()
            .field(CaseData::getUploadHearingDocumentType)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("PageFieldDisplayOrder", 3)
            .done()
            .field(CaseData::getUploadHearingDocumentsWhoseDocuments)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("PageFieldDisplayOrder", 4)
            .done()
            .field(CaseData::getUploadHearingDocumentsDateSubmitted)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("PageFieldDisplayOrder", 5)
            .done()
            .done()
            .grant(
                Permission.CRUD,
                regionalCaseworkerRole,
                regionalJudgeRole,
                EtUserRole.CASEWORKER_EMPLOYMENT_API
            )
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE);
    }
}
