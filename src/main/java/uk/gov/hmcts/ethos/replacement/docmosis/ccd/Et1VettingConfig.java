package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.VettingJurisdictionCodesType;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Mandatory;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Optional;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.ReadOnly;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.EventFieldSpec.field;

public abstract class Et1VettingConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean scotland;

    protected Et1VettingConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean scotland
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.scotland = scotland;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        Event.EventBuilder<T, EtUserRole, EtState> event = configBuilder.event("et1Vetting")
            .forAllStates()
            .name("ET1 case vetting")
            .description("Vetting a case")
            .displayOrder(8)
            .showCondition("managingOffice !=\"Unassigned\"")
            .showSummary()
            .aboutToStartCallbackUrl("${ET_COS_URL}/initialiseEt1Vetting")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et1VettingAboutToSubmit")
            .submittedCallbackUrl("${ET_COS_URL}/finishEt1Vetting")
            .caseEventColumn("PreConditionState(s)", "Submitted;Rejected;Vetted")
            .caseEventColumn("PostConditionState", "Vetted");

        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields = event.fields();
        fieldSpecs().forEach(spec -> addField(fields, spec));

        fields.done()
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private void addField(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        if ("vettingJurisdictionCodeCollection".equals(spec.id())) {
            addJurisdictionCodeCollection(fields, spec);
            return;
        }
        spec.addTo(fields).done();
    }

    private void addJurisdictionCodeCollection(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        spec.addTo(fields)
            .complex(VettingJurisdictionCodesType.class)
            .field(VettingJurisdictionCodesType::getEt1VettingJurCodeList)
            .optional()
            .caseEventFieldLabel("Jurisdiction code")
            .caseEventColumn("ID", "JurisdictionCode")
            .caseEventColumn("FieldDisplayOrder", 1)
            .caseEventColumn("LiveFrom", null)
            .done()
            .done();
    }

    @SuppressWarnings({"checkstyle:LineLength", "PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals"})
    private List<EventFieldSpec> fieldSpecs() {
        return List.of(
            field("horizontalLine", ReadOnly, 1, 1, 1).pageLabel("Before you start"),
            field("et1VettingBeforeYouStart", ReadOnly, 1, 1, 2).show("et1VettingBeforeYouStartLabel=\"dummy\""),
            field("et1VettingBeforeYouStartLabel", ReadOnly, 1, 1, 3),
            field("et1VettingContactDetailsLabel", ReadOnly, 2, 2, 1).pageLabel("Minimum required information"),
            field("et1VettingClaimantDetailsMarkUp", ReadOnly, 2, 2, 2).show("et1VettingContactDetailsLabel=\"dummy\""),
            field("et1VettingClaimantDetailsLabel", ReadOnly, 2, 2, 3),
            field("et1VettingRespondentDetailsMarkUp", ReadOnly, 2, 2, 4).show("et1VettingContactDetailsLabel=\"dummy\""),
            field("et1VettingRespondentDetailsLabel", ReadOnly, 2, 2, 5),
            field("et1VettingCanServeClaimYesOrNo", Mandatory, 2, 2, 6).summary("Y"),
            field("et1VettingCanServeClaimNoReason", Mandatory, 2, 2, 7).show("et1VettingCanServeClaimYesOrNo=\"No\"").summary("Y"),
            field("et1VettingCanServeClaimGeneralNote", Optional, 2, 2, 8).summary("Y"),
            field("et1VettingClaimantDetailsLabel2", ReadOnly, 3, 3, 1).pageLabel("Minimum required information"),
            field("et1VettingRespondentAcasDetails1", ReadOnly, 3, 3, 2).show("et1VettingContactDetailsLabel=\"dummy\""),
            field("et1VettingRespondentAcasDetailsLabel1", ReadOnly, 3, 3, 3),
            field("et1VettingAcasCertIsYesOrNo1", Mandatory, 3, 3, 4).summary("Y"),
            field("et1VettingAcasCertExemptYesOrNo1", Mandatory, 3, 3, 5).show("et1VettingAcasCertIsYesOrNo1=\"No\"").summary("Y"),
            field("et1VettingRespondentAcasDetails2", ReadOnly, 3, 3, 6).show("et1VettingContactDetailsLabel=\"dummy\""),
            field("et1VettingRespondentAcasDetailsLabel2", ReadOnly, 3, 3, 7).show("et1VettingRespondentAcasDetails2!=\"\""),
            field("et1VettingAcasCertIsYesOrNo2", Mandatory, 3, 3, 8).show("et1VettingRespondentAcasDetails2!=\"\"").summary("Y"),
            field("et1VettingAcasCertExemptYesOrNo2", Mandatory, 3, 3, 9).show("et1VettingRespondentAcasDetails2!=\"\" AND et1VettingAcasCertIsYesOrNo2=\"No\"").summary("Y"),
            field("et1VettingRespondentAcasDetails3", ReadOnly, 3, 3, 10).show("et1VettingContactDetailsLabel=\"dummy\""),
            field("et1VettingRespondentAcasDetailsLabel3", ReadOnly, 3, 3, 11).show("et1VettingRespondentAcasDetails3!=\"\""),
            field("et1VettingAcasCertIsYesOrNo3", Mandatory, 3, 3, 12).show("et1VettingRespondentAcasDetails3!=\"\"").summary("Y"),
            field("et1VettingAcasCertExemptYesOrNo3", Mandatory, 3, 3, 13).show("et1VettingRespondentAcasDetails3!=\"\" AND et1VettingAcasCertIsYesOrNo3=\"No\"").summary("Y"),
            field("et1VettingRespondentAcasDetails4", ReadOnly, 3, 3, 14).show("et1VettingContactDetailsLabel=\"dummy\""),
            field("et1VettingRespondentAcasDetailsLabel4", ReadOnly, 3, 3, 15).show("et1VettingRespondentAcasDetails4!=\"\""),
            field("et1VettingAcasCertIsYesOrNo4", Mandatory, 3, 3, 16).show("et1VettingRespondentAcasDetails4!=\"\"").summary("Y"),
            field("et1VettingAcasCertExemptYesOrNo4", Mandatory, 3, 3, 17).show("et1VettingRespondentAcasDetails4!=\"\" AND et1VettingAcasCertIsYesOrNo4=\"No\"").summary("Y"),
            field("et1VettingRespondentAcasDetails5", ReadOnly, 3, 3, 18).show("et1VettingContactDetailsLabel=\"dummy\""),
            field("et1VettingRespondentAcasDetailsLabel5", ReadOnly, 3, 3, 19).show("et1VettingRespondentAcasDetails5!=\"\""),
            field("et1VettingAcasCertIsYesOrNo5", Mandatory, 3, 3, 20).show("et1VettingRespondentAcasDetails5!=\"\"").summary("Y"),
            field("et1VettingAcasCertExemptYesOrNo5", Mandatory, 3, 3, 21).show("et1VettingRespondentAcasDetails5!=\"\" AND et1VettingAcasCertIsYesOrNo5=\"No\"").summary("Y"),
            field("et1VettingRespondentAcasDetails6", ReadOnly, 3, 3, 22).show("et1VettingContactDetailsLabel=\"dummy\""),
            field("et1VettingRespondentAcasDetailsLabel6", ReadOnly, 3, 3, 23).show("et1VettingRespondentAcasDetails6!=\"\""),
            field("et1VettingAcasCertIsYesOrNo6", Mandatory, 3, 3, 24).show("et1VettingRespondentAcasDetails6!=\"\"").summary("Y"),
            field("et1VettingAcasCertExemptYesOrNo6", Mandatory, 3, 3, 25).show("et1VettingRespondentAcasDetails6!=\"\" AND et1VettingAcasCertIsYesOrNo6=\"No\"").summary("Y"),
            field("horizontalLine4", ReadOnly, 3, 3, 26),
            field("et1VettingAcasCertGeneralNote", Optional, 3, 3, 27).summary("Y"),
            horizontalLine2(),
            field("substantiveDefectsList", Optional, 4, 4, 2).summary("Y"),
            field("rule121aTextArea", Mandatory, 4, 4, 3).show("substantiveDefectsList CONTAINS \"rule121a\"").summary("Y"),
            field("rule121bTextArea", Mandatory, 4, 4, 4).show("substantiveDefectsList CONTAINS \"rule121b\"").summary("Y"),
            field("rule121cTextArea", Mandatory, 4, 4, 5).show("substantiveDefectsList CONTAINS \"rule121c\"").summary("Y"),
            field("rule121dTextArea", Mandatory, 4, 4, 6).show("substantiveDefectsList CONTAINS \"rule121d\"").summary("Y"),
            field("rule121daTextArea", Mandatory, 4, 4, 7).show("substantiveDefectsList CONTAINS \"rule121 da\"").summary("Y"),
            field("rule121eTextArea", Mandatory, 4, 4, 8).show("substantiveDefectsList CONTAINS \"rule121e\"").summary("Y"),
            field("rule121fTextArea", Mandatory, 4, 4, 9).show("substantiveDefectsList CONTAINS \"rule121f\"").summary("Y"),
            field("horizontalLine3", ReadOnly, 4, 4, 10),
            field("et1SubstantiveDefectsGeneralNotes", Optional, 4, 4, 11).summary("Y"),
            field("existingJurisdictionCodes", ReadOnly, 5, 5, 1).show("et1SubstantiveDefectsGeneralNotes=\"dummy\"").pageLabel("Case details"),
            field("existingJurisdictionCodesLabel", ReadOnly, 5, 5, 2),
            field("areTheseCodesCorrect", Mandatory, 5, 5, 3).summary("Y"),
            field("codesCorrectGiveDetails", Mandatory, 5, 5, 4).show("areTheseCodesCorrect=\"No\"").summary("Y"),
            field("vettingJurisdictionCodeCollection", Optional, 5, 5, 5).summary("Y"),
            field("et1JurisdictionCodeGeneralNotes", Optional, 5, 5, 6).mid("${ET_COS_URL}/jurisdictionCodes").summary("Y"),
            field("trackType", ReadOnly, 5, 5, 7).show("areTheseCodesCorrect=\"dummy\""),
            field("trackAllocation", ReadOnly, 6, 6, 1).show("isTrackAllocationCorrect=\"dummy\"").pageLabel("Case details"),
            field("trackAllocationLabel", ReadOnly, 6, 6, 2),
            field("isTrackAllocationCorrect", Mandatory, 6, 6, 3).summary("Y"),
            field("suggestAnotherTrack", Mandatory, 6, 6, 4).show("isTrackAllocationCorrect=\"No\"").summary("Y"),
            field("whyChangeTrackAllocation", Mandatory, 6, 6, 5).show("suggestAnotherTrack !=\"\" AND isTrackAllocationCorrect=\"No\"").summary("Y"),
            field("trackAllocationGeneralNotes", Optional, 6, 6, 6).summary("Y"),
            field("tribunalAndOfficeLocation", Mandatory, 7, 7, 1).show("horizontalLine=\"dummy\"").pageLabel("Case details").summary("Y"),
            field("tribunalAndOfficeLocationLabel", ReadOnly, 7, 7, 2),
            field("isLocationCorrect", Mandatory, 7, 7, 3).summary("Y"),
            field("regionalOffice", Mandatory, 7, 7, 4).show("horizontalLine=\"dummy\"").summary("Y"),
            field("regionalOfficeLabel", ReadOnly, 7, 7, 5).show("isLocationCorrect=\"No\""),
            field("regionalOfficeList", Mandatory, 7, 7, 6).show("isLocationCorrect=\"No\"").summary("Y"),
            field("whyChangeOffice", Mandatory, 7, 7, 7).show("isLocationCorrect=\"No\"").summary("Y"),
            field("et1LocationGeneralNotes", Optional, 7, 7, 8).mid("${ET_COS_URL}/et1HearingVenue").summary("Y"),
            field("et1AddressDetails", ReadOnly, 8, 8, 1).show("horizontalLine=\"dummy\"").pageLabel("Case details"),
            field("et1AddressDetailsLabel", ReadOnly, 8, 8, 2),
            field("et1SuggestHearingVenue", Mandatory, 8, 8, 3).summary("Y"),
            field("et1TribunalRegion", ReadOnly, 8, 8, 4).show("horizontalLine=\"dummy\""),
            field("et1TribunalRegionLabel", ReadOnly, 8, 8, 5).show("et1SuggestHearingVenue=\"Yes\""),
            field("et1HearingVenues", Mandatory, 8, 8, 6).show("et1SuggestHearingVenue=\"Yes\"").summary("Y"),
            field("et1HearingVenueGeneralNotes", Optional, 8, 8, 7).summary("Y"),
            field("et1GovOrMajorQuestion", Mandatory, 9, 9, 1).pageLabel("Further questions").summary("Y"),
            field("et1ReasonableAdjustmentsQuestion", Mandatory, 9, 9, 2).summary("Y"),
            field("et1ReasonableAdjustmentsTextArea", Mandatory, 9, 9, 3).show("et1ReasonableAdjustmentsQuestion=\"Yes\"").summary("Y"),
            field("et1VideoHearingQuestion", Mandatory, 9, 9, 4).summary("Y"),
            field("et1VideoHearingTextArea", Mandatory, 9, 9, 5).show("et1VideoHearingQuestion=\"No\"").summary("Y"),
            field("et1FurtherQuestionsGeneralNotes", Optional, 9, 9, 6).summary("Y"),
            field("et1JudgeReferralLine1", ReadOnly, 10, 10, 1).pageLabel("Possible referral to a judge or legal officer"),
            field("referralToJudgeOrLOList", Optional, 10, 10, 2).summary("Y"),
            field("aClaimOfInterimReliefTextArea", Mandatory, 10, 10, 3).show("referralToJudgeOrLOList CONTAINS \"aClaimOfInterimRelief\"").summary("Y"),
            field("aStatutoryAppealTextArea", Mandatory, 10, 10, 4).show("referralToJudgeOrLOList CONTAINS \"aStatutoryAppeal\"").summary("Y"),
            field("anAllegationOfCommissionOfSexualOffenceTextArea", Mandatory, 10, 10, 5).show("referralToJudgeOrLOList CONTAINS \"anAllegationOfCommissionOfSexualOffence\"").summary("Y"),
            field("insolvencyTextArea", Mandatory, 10, 10, 6).show("referralToJudgeOrLOList CONTAINS \"insolvency\"").summary("Y"),
            field("jurisdictionsUnclearTextArea", Mandatory, 10, 10, 7).show("referralToJudgeOrLOList CONTAINS \"jurisdictionsUnclear\"").summary("Y"),
            field("lengthOfServiceTextArea", Mandatory, 10, 10, 8).show("referralToJudgeOrLOList CONTAINS \"lengthOfService\"").summary("Y"),
            field("potentiallyLinkedCasesInTheEcmTextArea", Mandatory, 10, 10, 9).show("referralToJudgeOrLOList CONTAINS \"potentiallyLinkedCasesInTheEcm\"").summary("Y"),
            field("rule50IssuesTextArea", Mandatory, 10, 10, 10).show("referralToJudgeOrLOList CONTAINS \"rule50Issues\"").summary("Y"),
            field("anotherReasonForJudicialReferralTextArea", Mandatory, 10, 10, 11).show("referralToJudgeOrLOList CONTAINS \"anotherReasonForJudicialReferral\"").summary("Y"),
            field("et1JudgeReferralLine2", ReadOnly, 10, 10, 12),
            field("et1JudgeReferralGeneralNotes", Optional, 10, 10, 13).summary("Y"),
            field("et1REJOrVPReferralLine1", ReadOnly, 11, 11, 1).pageLabel("Possible referral to Regional Employment Judge or Vice-President"),
            field("referralToREJOrVPList", Optional, 11, 11, 2).summary("Y"),
            field("vexatiousLitigantOrderTextArea", Mandatory, 11, 11, 3).show("referralToREJOrVPList CONTAINS \"vexatiousLitigantOrder\"").summary("Y"),
            field("aNationalSecurityIssueTextArea", Mandatory, 11, 11, 4).show("referralToREJOrVPList CONTAINS \"aNationalSecurityIssue\"").summary("Y"),
            field("nationalMultipleOrPresidentialOrderTextArea", Mandatory, 11, 11, 5).show("referralToREJOrVPList CONTAINS \"nationalMultipleOrPresidentialOrder\"").summary("Y"),
            field("transferToOtherRegionTextArea", Mandatory, 11, 11, 6).show("referralToREJOrVPList CONTAINS \"transferToOtherRegion\"").summary("Y"),
            field("serviceAbroadTextArea", Mandatory, 11, 11, 7).show("referralToREJOrVPList CONTAINS \"serviceAbroad\"").summary("Y"),
            field("aSensitiveIssueTextArea", Mandatory, 11, 11, 8).show("referralToREJOrVPList CONTAINS \"aSensitiveIssue\"").summary("Y"),
            field("anyPotentialConflictTextArea", Mandatory, 11, 11, 9).show("referralToREJOrVPList CONTAINS \"anyPotentialConflict\"").summary("Y"),
            field("anotherReasonREJOrVPTextArea", Mandatory, 11, 11, 10).show("referralToREJOrVPList CONTAINS \"anotherReasonREJOrVP\"").summary("Y"),
            field("et1REJOrVPReferralLine2", ReadOnly, 11, 11, 11),
            field("et1REJOrVPReferralGeneralNotes", Optional, 11, 11, 12).summary("Y"),
            field("et1OtherReferralLine1", ReadOnly, 12, 12, 1).pageLabel("Other factors"),
            field("otherReferralList", Optional, 12, 12, 2).summary("Y"),
            field("claimOutOfTimeTextArea", Mandatory, 12, 12, 3).show("otherReferralList CONTAINS \"claimOutOfTime\"").summary("Y"),
            field("multipleClaimTextArea", Mandatory, 12, 12, 4).show("otherReferralList CONTAINS \"multipleClaim\"").summary("Y"),
            field("employmentStatusIssuesTextArea", Mandatory, 12, 12, 5).show("otherReferralList CONTAINS \"employmentStatusIssues\"").summary("Y"),
            field("pidJurisdictionRegulatorTextArea", Mandatory, 12, 12, 6).show("otherReferralList CONTAINS \"pidJurisdictionRegulator\"").summary("Y"),
            field("videoHearingPreferenceTextArea", Mandatory, 12, 12, 7).show("otherReferralList CONTAINS \"videoHearingPreference\"").summary("Y"),
            field("rule50IssuesForOtherReferralTextArea", Mandatory, 12, 12, 8).show("otherReferralList CONTAINS \"rule50IssuesOtherFactors\"").summary("Y"),
            field("anotherReasonForOtherReferralTextArea", Mandatory, 12, 12, 9).show("otherReferralList CONTAINS \"otherRelevantFactors\"").summary("Y"),
            field("et1OtherReferralLine2", ReadOnly, 12, 12, 10),
            field("et1OtherReferralGeneralNotes", Optional, 12, 12, 11).summary("Y"),
            field("et1VettingAdditionalInformationTextArea", Optional, 13, 13, 1).pageLabel("Final notes").summary("Y")
        );
    }

    private EventFieldSpec horizontalLine2() {
        EventFieldSpec field = field("horizontalLine2", ReadOnly, 4, 4, 1);
        return scotland ? field.pageLabel("Case details") : field;
    }
}
