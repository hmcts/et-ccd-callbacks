package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class Et1ReppedSectionsConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_LABEL = "PageLabel";

    private final boolean includeLanguageFields;

    protected Et1ReppedSectionsConfig(boolean includeLanguageFields) {
        this.includeLanguageFields = includeLanguageFields;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        et1SectionOneFields(et1SectionEvent(
            configBuilder,
            "et1SectionOne",
            "ET1 - Claimant details",
            "Claimant details",
            55
        ));
        et1SectionThreeFields(et1SectionEvent(configBuilder, "et1SectionThree", "Claim Details", "Claim Details", 57));
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et1SectionEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        String description,
        int displayOrder
    ) {
        return configBuilder.event(eventId)
            .forStateTransition(
                EtState.AWAITING_SUBMISSION_TO_HMCTS,
                EtState.AWAITING_SUBMISSION_TO_HMCTS
            )
            .name(name)
            .description(description)
            .displayOrder(displayOrder)
            .showSummary()
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et1Repped/aboutToSubmitSection")
            .submittedCallbackUrl("${ET_COS_URL}/et1Repped/sectionCompleted")
            .endButtonLabel("Save as draft")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CLAIMANT_SOLICITOR);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et1SectionOneFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var fields = event.fields()
            .page("1")
            .pageLabel("Make a claim to an employment tribunal")
            .field("et1Section1PreambleLabel")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .pageLabel("Claimant details")
            .field(CaseData::getClaimantFirstName)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getClaimantLastName)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getClaimantDateOfBirth)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("3")
            .pageLabel("Claimant details")
            .field(CaseData::getClaimantSex)
            .optional()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/et1Repped/sectionOne/validateClaimantSex")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getClaimantPreferredTitle)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("4")
            .pageLabel("Claimant contact address")
            .field(CaseData::getClaimantContactAddress)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("5")
            .pageLabel("Hearing format")
            .field("hearingFormatPreamble")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getRepresentativeAttendHearing)
            .optional()
            .showSummary()
            .caseEventColumn(
                "CallBackURLMidEvent",
                "${ET_COS_URL}/et1Repped/sectionOne/validateHearingPreferences"
            )
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();

        if (includeLanguageFields) {
            fields
                .field(CaseData::getHearingContactLanguage)
                .optional()
                .showSummary()
                .caseEventColumn(PAGE_LABEL, null)
                .caseEventColumn(PAGE_COLUMN_NUMBER, null)
                .done();
        }

        fields
            .field(CaseData::getClaimantAttendHearing)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();

        if (includeLanguageFields) {
            fields
                .field(CaseData::getClaimantHearingContactLanguage)
                .optional()
                .showSummary()
                .caseEventColumn(PAGE_LABEL, null)
                .caseEventColumn(PAGE_COLUMN_NUMBER, null)
                .done();
        }

        fields
            .page("6")
            .pageLabel("In the claimant party - are you aware of any physical, mental or learning disability or health "
                           + "condition which requires support?")
            .field(CaseData::getClaimantSupportQuestion)
            .optional()
            .showSummary()
            .caseEventColumn(
                "CallBackURLMidEvent",
                "${ET_COS_URL}/et1Repped/sectionOne/validateClaimantSupport"
            )
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getClaimantSupportQuestionReason)
            .optional()
            .showCondition("claimantSupportQuestion CONTAINS \"Yes\"")
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("7")
            .pageLabel("Your information (as the representative)")
            .field("representativeInformationPreamble")
            .readOnly()
            .caseEventColumn(
                "CallBackURLMidEvent",
                "${ET_COS_URL}/et1Repped/sectionOne/validateRepresentativeInformation"
            )
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getRepresentativeContactPreference)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getContactPreferencePostReason)
            .optional()
            .showCondition("representativeContactPreference CONTAINS \"Post\"")
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();

        if (includeLanguageFields) {
            fields
                .field(CaseData::getContactLanguageQuestion)
                .optional()
                .showSummary()
                .caseEventColumn(PAGE_LABEL, null)
                .caseEventColumn(PAGE_COLUMN_NUMBER, null)
                .done();
        }

        return fields
            .field(CaseData::getRepresentativeAddress)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 5)
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getRepresentativePhoneNumber)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 6)
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getRepresentativeReferenceNumber)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 7)
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
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
