package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class Et1ReppedSectionsConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_LABEL = "PageLabel";

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        et1SectionThreeFields(et1SectionEvent(configBuilder, "et1SectionThree", "Claim Details", 57));
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et1SectionEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        int displayOrder
    ) {
        return configBuilder.event(eventId)
            .forStateTransition(
                EtState.AWAITING_SUBMISSION_TO_HMCTS,
                EtState.AWAITING_SUBMISSION_TO_HMCTS
            )
            .name(name)
            .description(name)
            .displayOrder(displayOrder)
            .showSummary()
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et1Repped/aboutToSubmitSection")
            .submittedCallbackUrl("${ET_COS_URL}/et1Repped/sectionCompleted")
            .endButtonLabel("Save as draft")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CLAIMANT_SOLICITOR);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et1SectionThreeFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .field("et1SectionThreePreamble")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("2")
            .pageLabel("Details of the claim")
            .field("et1SectionThreeDetailsPreamble")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt1SectionThreeDocumentUpload)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt1SectionThreeClaimDetails)
            .optional()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/et1Repped/validateGrounds")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("3")
            .pageLabel("What type of claim is this?")
            .field(CaseData::getEt1SectionThreeTypeOfClaim)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getDiscriminationTypesOfClaim)
            .mandatory()
            .showCondition("et1SectionThreeTypeOfClaim CONTAINS \"discrimination\"")
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPayTypesOfClaim)
            .mandatory()
            .showCondition("et1SectionThreeTypeOfClaim CONTAINS \"payRelated\"")
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getWhistleblowingYesNo)
            .optional()
            .showCondition("et1SectionThreeTypeOfClaim CONTAINS \"whistleBlowing\"")
            .showSummary()
            .caseEventColumn(
                "CallBackURLMidEvent",
                "${ET_COS_URL}/et1Repped/sectionThree/validateWhistleblowing"
            )
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getWhistleblowingRegulator)
            .mandatory()
            .showCondition("et1SectionThreeTypeOfClaim CONTAINS \"whistleBlowing\" AND whistleblowingYesNo = \"Yes\"")
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getOtherTypeOfClaimDetails)
            .mandatory()
            .showCondition("et1SectionThreeTypeOfClaim CONTAINS \"otherTypesOfClaims\"")
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("4")
            .pageLabel("What does the claimant want if their claim is successful? (Optional)")
            .field(CaseData::getClaimSuccessful)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getCompensationDetails)
            .optional()
            .showCondition("claimSuccessful CONTAINS \"compensation\"")
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTribunalRecommendationDetails)
            .optional()
            .showCondition("claimSuccessful CONTAINS \"tribunal\"")
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 4)
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("5")
            .pageLabel("Linked cases")
            .field("linkedCasesPreamble")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getLinkedCasesYesNo)
            .optional()
            .showSummary()
            .caseEventColumn(
                "CallBackURLMidEvent",
                "${ET_COS_URL}/et1Repped/sectionThree/validateLinkedCases"
            )
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getLinkedCasesDetails)
            .optional()
            .showCondition("linkedCasesYesNo CONTAINS \"Yes\"")
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }
}
