package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.Et3VettingType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Complex;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Mandatory;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Optional;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.ReadOnly;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.EventFieldSpec.field;

public abstract class Et3VettingConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean scotland;

    protected Et3VettingConfig(
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
        Event.EventBuilder<T, EtUserRole, EtState> event = configBuilder.event("et3Vetting")
            .forAllStates()
            .name("ET3 Processing")
            .description("ET3 Processing")
            .displayOrder(18)
            .showCondition("managingOffice !=\"Unassigned\"")
            .showSummary()
            .aboutToStartCallbackUrl("${ET_COS_URL}/et3Vetting/aboutToStart")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et3Vetting/aboutToSubmit")
            .submittedCallbackUrl("${ET_COS_URL}/et3Vetting/processingComplete")
            .caseEventColumn("PreConditionState(s)", "Accepted")
            .caseEventColumn("PostConditionState", "*")
            .caseEventColumn("Publish", "Y");

        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields = event.fields();
        fieldSpecs().forEach(spec -> addField(fields, spec));

        fields.done()
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        if (scotland) {
            event.grant(Permission.R, EtUserRole.ET_ACAS_API);
        }
    }

    private void addField(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        if ("respondentCollection".equals(spec.id())) {
            addRespondentCollection(fields, spec);
            return;
        }
        spec.addTo(fields).done();
    }

    private void addRespondentCollection(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        spec.addTo(fields)
            .complex(RespondentSumType.class)
            .field(RespondentSumType::getEt3Vetting)
            .optional()
            .caseEventFieldLabel(" ")
            .caseEventColumn("ID", "rule21")
            .caseEventColumn("FieldDisplayOrder", 1)
            .caseEventColumn("LiveFrom", null)
            .caseEventColumn("Publish", "Y")
            .complex()
            .field(Et3VettingType::getEt3ContractClaimSection7)
            .optional()
            .caseEventFieldLabel(" ")
            .caseEventColumn("ID", "rule21")
            .caseEventColumn("FieldDisplayOrder", 2)
            .caseEventColumn("LiveFrom", null)
            .caseEventColumn("Publish", "Y")
            .done()
            .field(Et3VettingType::getEt3IsThereAnEt3Response)
            .optional()
            .caseEventFieldLabel(" ")
            .caseEventColumn("ID", "rule21")
            .caseEventColumn("FieldDisplayOrder", 2)
            .caseEventColumn("LiveFrom", null)
            .caseEventColumn("Publish", "Y")
            .done()
            .done()
            .done();
    }

    @SuppressWarnings({"checkstyle:LineLength", "PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals"})
    private List<EventFieldSpec> fieldSpecs() {
        return List.of(
            nextListedDate(),
            field("respondentCollection", Complex, 2, 2, 2).show("et3ChooseRespondent=\"dummy\"").summary("N").pageColumn(1),
            field("horizontalLine", ReadOnly, 1, 1, 1).pageLabel("Before you start"),
            field("et3VettingBeforeYouStart", ReadOnly, 1, 1, 2).pageLabel("Before you start"),
            field("et3ChooseRespondent", Mandatory, 2, 2, 1).pageLabel("Select Respondent").mid("${ET_COS_URL}/et3Vetting/midPopulateRespondentEt3Response"),
            field("et3Date", ReadOnly, 3, 3, 1).show("et3DateLabel=\"dummy\"").pageLabel("Minimum required information").mid("${ET_COS_URL}/et3Vetting/midCalculateResponseInTime").pageColumn(1),
            field("et3DateLabel", ReadOnly, 3, 3, 2).pageColumn(1),
            field("et3IsThereAnEt3Response", Mandatory, 3, 3, 3).summary("Y"),
            field("et3NoEt3Response", Optional, 3, 3, 4).show("et3IsThereAnEt3Response=\"No\""),
            field("et3GeneralNotes", Optional, 3, 3, 5).pageLabel(""),
            field("et3DateCompanyHousePage", ReadOnly, 4, 4, 1).pageShow("et3IsThereAnEt3Response=\"No\"").pageLabel("Rule 22 referral").pageColumn(1),
            field("et3IsThereACompaniesHouseSearchDocument", Optional, 4, 4, 2).summary("Y"),
            field("et3CompanyHouseDocument", Mandatory, 4, 4, 3).show("et3IsThereACompaniesHouseSearchDocument=\"Yes\"").summary("Y"),
            field("et3GeneralNotesCompanyHouse", Optional, 4, 4, 4),
            field("et3DateIndividualInsolvency", ReadOnly, 5, 5, 1).pageShow("et3IsThereAnEt3Response=\"No\"").pageLabel("Rule 22 referral"),
            field("et3IsThereAnIndividualSearchDocument", Optional, 5, 5, 2).summary("Y"),
            field("et3IndividualInsolvencyDocument", Mandatory, 5, 5, 3).show("et3IsThereAnIndividualSearchDocument=\"Yes\"").summary("Y"),
            field("et3GeneralNotesIndividualInsolvency", Optional, 5, 5, 4),
            field("et3LegalIssue", Optional, 6, 6, 1).pageShow("et3IsThereAnEt3Response=\"No\"").pageLabel("Rule 22 referral").summary("Y"),
            field("et3LegalIssueGiveDetails", Mandatory, 6, 6, 2).show("et3LegalIssue=\"Yes\""),
            field("et3GeneralNotesLegalEntity", Optional, 6, 6, 3),
            field("et3ResponseInTimeDateLabel", ReadOnly, 7, 7, 1).pageShow("et3IsThereAnEt3Response=\"Yes\"").pageLabel("Minimum required information").mid("${ET_COS_URL}/et3Vetting/midRespondentNameAndAddressTable"),
            field("et3ResponseInTime", Mandatory, 7, 7, 2).summary("Y"),
            field("et3ResponseInTimeDetails", Optional, 7, 7, 3).show("et3ResponseInTime=\"No\"").summary("Y"),
            field("et3ContactDetailsLabel", ReadOnly, 8, 8, 1).pageShow("et3IsThereAnEt3Response=\"Yes\"").pageLabel("Minimum required information"),
            field("et3NameAddressRespondent", ReadOnly, 8, 8, 2).show("et3NameAddressRespondentLabel=\"dummy\""),
            field("et3NameAddressRespondentLabel", ReadOnly, 8, 8, 3),
            field("et3DoWeHaveRespondentsName", Mandatory, 8, 8, 4),
            field("et3GeneralNotesRespondentName", Optional, 8, 8, 5),
            field("et3ContactDetailsNameMismatchLabel", ReadOnly, 9, 9, 1).pageShow("et3IsThereAnEt3Response=\"Yes\" AND et3DoWeHaveRespondentsName=\"Yes\"").pageLabel("Minimum required information"),
            field("et3NameAddressRespondentNameMismatchLabel", ReadOnly, 9, 9, 3),
            field("et3DoesRespondentsNameMatch", Mandatory, 9, 9, 4).summary("Y"),
            field("et3RespondentNameMismatchDetails", Mandatory, 9, 9, 5).show("et3DoesRespondentsNameMatch=\"No\"").summary("Y").retainHidden(),
            field("et3GeneralNotesRespondentNameMatch", Optional, 9, 9, 6),
            field("et3ContactDetailsAddressLabel", ReadOnly, 10, 10, 1).pageShow("et3IsThereAnEt3Response=\"Yes\"").pageLabel("Minimum required information"),
            field("et3NameAddressRespondentAddressLabel", ReadOnly, 10, 10, 3),
            field("et3DoWeHaveRespondentsAddress", Mandatory, 10, 10, 4),
            field("et3GeneralNotesRespondentAddress", Optional, 10, 10, 5),
            field("et3ContactDetailsAddressMismatchLabel", ReadOnly, 11, 11, 1).pageShow("et3IsThereAnEt3Response=\"Yes\" AND et3DoWeHaveRespondentsAddress=\"Yes\"").pageLabel("Minimum required information"),
            field("et3NameAddressRespondentAddressMismatchLabel", ReadOnly, 11, 11, 3),
            field("et3DoesRespondentsAddressMatch", Mandatory, 11, 11, 4).summary("Y"),
            field("et3RespondentAddressMismatchDetails", Mandatory, 11, 11, 5).show("et3DoesRespondentsAddressMatch=\"No\"").summary("Y").retainHidden(),
            field("et3GeneralNotesAddressMatch", Optional, 11, 11, 6),
            field("et3ContestClaim", Mandatory, 12, 12, 1).pageShow("et3IsThereAnEt3Response=\"Yes\"").pageLabel("Minimum required information").summary("Y").pageColumn(1),
            field("et3ContestClaimGiveDetails", Optional, 12, 12, 2).show("et3ContestClaim=\"Yes\" OR et3ContestClaim=\"No\" OR et3ContestClaim=\"Unknown - no answer given\"").summary("Y").pageColumn(1).retainHidden(),
            field("et3GeneralNotesContestClaim", Optional, 12, 12, 3).pageColumn(1),
            field("et3ContractClaimSection7", Mandatory, 13, 13, 1).pageShow("et3IsThereAnEt3Response=\"Yes\"").pageLabel("Minimum required information").mid("${ET_COS_URL}/et3Vetting/midHearingListedTable").summary("Y").pageColumn(1),
            field("et3ContractClaimSection7Details", Optional, 13, 13, 2).show("et3ContractClaimSection7=\"Yes\"").summary("Y").pageColumn(1).retainHidden(),
            field("et3GeneralNotesContractClaimSection7", Optional, 13, 13, 3).pageColumn(1),
            field("et3HearingDetails", ReadOnly, 14, 14, 1).show("et3HearingDetailsLabel=\"dummy\"").pageShow("et3IsThereAnEt3Response=\"Yes\"").mid("${ET_COS_URL}/et3Vetting/midTransferApplicationTable").pageColumn(1),
            field("et3HearingDetailsLabel", ReadOnly, 14, 14, 2).pageLabel("Page Title").pageColumn(1),
            field("et3IsCaseListedForHearing", Mandatory, 14, 14, 3).summary("Y").pageColumn(1),
            field("et3IsCaseListedForHearingDetails", Optional, 14, 14, 4).show("et3IsCaseListedForHearing=\"No\"").pageColumn(1).retainHidden(),
            field("et3GeneralNotesCaseListed", Optional, 14, 14, 5).pageColumn(1),
            field("et3TribunalLocation", ReadOnly, 15, 15, 1).show("et3TribunalLocationLabel=\"dummy\"").pageShow("et3IsThereAnEt3Response=\"Yes\"").pageColumn(1).retainHidden(),
            field("et3TribunalLocationLabel", ReadOnly, 15, 15, 2).pageLabel("Page Title").pageColumn(1),
            field("et3IsThisLocationCorrect", Mandatory, 15, 15, 3).summary("Y").pageColumn(1),
            field("et3RegionalOffice", Mandatory, 15, 15, 4).show("et3IsThisLocationCorrect=\"No - suggest another location\"").pageColumn(1).retainHidden(),
            field("et3WhyWeShouldChangeTheOffice", Mandatory, 15, 15, 5).show("et3IsThisLocationCorrect=\"No - suggest another location\"").pageColumn(1).retainHidden(),
            field("et3GeneralNotesTransferApplication", Optional, 15, 15, 6).show("et3IsThisLocationCorrect=\"Yes\"").pageColumn(1),
            field("et3Rule26", Mandatory, 16, 16, 1).pageShow("et3IsThereAnEt3Response=\"Yes\"").pageLabel("Issues identified").summary("Y").pageColumn(1),
            field("et3Rule26Details", Mandatory, 16, 16, 2).show("et3Rule26=\"Yes\"").summary("Y").pageColumn(1).retainHidden(),
            field("et3SuggestedIssues", Optional, 16, 16, 3).summary("Y").pageColumn(1),
            field("et3SuggestedIssuesStrikeOut", Mandatory, 16, 16, 4).show(contains("Applications for strike out or deposit")).summary("Y").pageColumn(1).retainHidden(),
            field("et3SuggestedIssueInterpreters", Mandatory, 16, 16, 5).show(contains("Interpreters")).summary("Y").pageColumn(1).retainHidden(),
            field("et3SuggestedIssueJurisdictional", Mandatory, 16, 16, 6).show(contains("Jurisdictional issues")).summary("Y").pageColumn(1).retainHidden(),
            field("et3SuggestedIssueAdjustments", Mandatory, 16, 16, 7).show(contains("Request for adjustments")).summary("Y").pageColumn(1).retainHidden(),
            field("et3SuggestedIssueRule50", Mandatory, 16, 16, 8).show(contains("Rule 50")).summary("Y").pageColumn(1).retainHidden(),
            field("et3SuggestedIssueTimePoints", Mandatory, 16, 16, 9).show(contains("Time points")).summary("Y").pageColumn(1).retainHidden(),
            field("et3GeneralNotesRule26", Optional, 16, 16, 10).summary("Y").pageColumn(1),
            field("et3AdditionalInformation", Optional, 17, 17, 1).pageLabel("Final notes").summary("Y").pageColumn(1)
        );
    }

    private EventFieldSpec nextListedDate() {
        if (scotland) {
            return field("nextListedDate", Optional, 2, 2, 3)
                .show("et3ChooseRespondent=\"dummy\"")
                .summary("N")
                .pageColumn(1)
                .publishColumn();
        }
        return field("nextListedDate", Optional, 3, 3, 6)
            .show("et3DateLabel=\"dummy\"")
            .summary("N")
            .pageColumn(1)
            .publishColumn();
    }

    private String contains(String value) {
        return scotland ? "et3SuggestedIssues CONTAINS\"" + value + "\""
            : "et3SuggestedIssues CONTAINS \"" + value + "\"";
    }
}
