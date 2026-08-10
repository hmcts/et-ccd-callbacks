package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ET3Vetting", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Et3VettingType {
    @CCD(
            label = "Select the respondent you are processing",
            showCondition = "et3ChooseRespondent!=\"\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    @JsonProperty("et3ChooseRespondent")
    private DynamicFixedListType et3ChooseRespondent;
    // ET3 Response Page
    @CCD(
            label = "Is there an ET3 response?",
            showCondition = "et3IsThereAnEt3Response!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3IsThereAnEt3Response")
    private String et3IsThereAnEt3Response;
    @CCD(
            label = "Give details",
            showCondition = "et3NoEt3Response!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3NoEt3Response")
    private String et3NoEt3Response;
    @CCD(
            label = "General Notes",
            showCondition = "et3GeneralNotes!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotes")
    private String et3GeneralNotes;
    // ET3 Company House search document page
    @CCD(
            label = "Is there a Companies House search document?",
            showCondition = "et3IsThereACompaniesHouseSearchDocument!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3IsThereACompaniesHouseSearchDocument")
    private String et3IsThereACompaniesHouseSearchDocument;
    @CCD(
            label = "Upload the Companies House search document",
            showCondition = "et3CompanyHouseDocument!=\"\"",
            categoryID = "C18",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("et3CompanyHouseDocument")
    private UploadedDocumentType et3CompanyHouseDocument;
    @CCD(
            label = "General Notes",
            showCondition = "et3GeneralNotesCompanyHouse!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesCompanyHouse")
    private String et3GeneralNotesCompanyHouse;
    // ET3 Individual insolvency search document page
    @CCD(
            label = "Is there an individual insolvency search document?",
            showCondition = "et3IsThereAnIndividualSearchDocument!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3IsThereAnIndividualSearchDocument")
    private String et3IsThereAnIndividualSearchDocument;
    @CCD(
            label = "Upload the individual insolvency search document",
            showCondition = "et3IndividualInsolvencyDocument!=\"\"",
            categoryID = "C18",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("et3IndividualInsolvencyDocument")
    private UploadedDocumentType et3IndividualInsolvencyDocument;
    @CCD(
            label = "General Notes",
            showCondition = "et3GeneralNotesIndividualInsolvency!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesIndividualInsolvency")
    private String et3GeneralNotesIndividualInsolvency;
    // ET3 Legal issue page
    @CCD(
            label = "Is there an issue with whether the respondent is a legal entity?",
            showCondition = "et3LegalIssue!=\"\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_respondent_legal_entity"
    )
    @JsonProperty("et3LegalIssue")
    private String et3LegalIssue;
    @CCD(
            label = "Give details",
            showCondition = "et3LegalIssueGiveDetails!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3LegalIssueGiveDetails")
    private String et3LegalIssueGiveDetails;
    @CCD(
            label = "Give details",
            showCondition = "et3GeneralNotesLegalEntity!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesLegalEntity")
    private String et3GeneralNotesLegalEntity;
    // ET3 Response in time page
    @CCD(
            label = "Did we receive the ET3 response in time?",
            showCondition = "et3ResponseInTime!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3ResponseInTime")
    private String et3ResponseInTime;
    @CCD(
            label = "Give details",
            showCondition = "et3ResponseInTimeDetails!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponseInTimeDetails")
    private String et3ResponseInTimeDetails;
    // ET3 Respondents Name page
    @CCD(
            label = "Do we have the respondent's name?",
            showCondition = "et3DoWeHaveRespondentsName!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3DoWeHaveRespondentsName")
    private String et3DoWeHaveRespondentsName;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesRespondentName!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesRespondentName")
    private String et3GeneralNotesRespondentName;
    @CCD(
            label = "Does the respondent's name match?",
            showCondition = "et3DoesRespondentsNameMatch!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3DoesRespondentsNameMatch")
    private String et3DoesRespondentsNameMatch;
    @CCD(
            label = "Give details",
            showCondition = "et3RespondentNameMismatchDetails!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3RespondentNameMismatchDetails")
    private String et3RespondentNameMismatchDetails;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesRespondentNameMatch!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesRespondentNameMatch")
    private String et3GeneralNotesRespondentNameMatch;
    // ET3 Respondents Address page
    @CCD(
            label = "Do we have the respondent's address?",
            showCondition = "et3DoWeHaveRespondentsAddress!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3DoWeHaveRespondentsAddress")
    private String et3DoWeHaveRespondentsAddress;
    @CCD(
            label = "Does the respondent's address match?",
            showCondition = "et3DoesRespondentsAddressMatch!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3DoesRespondentsAddressMatch")
    private String et3DoesRespondentsAddressMatch;
    @CCD(
            label = "Give details",
            showCondition = "et3RespondentAddressMismatchDetails!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3RespondentAddressMismatchDetails")
    private String et3RespondentAddressMismatchDetails;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesRespondentAddress!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesRespondentAddress")
    private String et3GeneralNotesRespondentAddress;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesAddressMatch!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesAddressMatch")
    private String et3GeneralNotesAddressMatch;
    // ET3 Case Listed Page
    @CCD(
            label = "Is the case listed for hearing?",
            showCondition = "et3IsCaseListedForHearing!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3IsCaseListedForHearing")
    private String et3IsCaseListedForHearing;
    @CCD(
            label = "Give details",
            showCondition = "et3IsCaseListedForHearingDetails!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3IsCaseListedForHearingDetails")
    private String et3IsCaseListedForHearingDetails;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesCaseListed!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesCaseListed")
    private String et3GeneralNotesCaseListed;
    // ET3 Transfer Application
    @CCD(
            label = "Is this location correct?",
            showCondition = "et3IsThisLocationCorrect!=\"\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_et3_tribunal_location_change"
    )
    @JsonProperty("et3IsThisLocationCorrect")
    private String et3IsThisLocationCorrect;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesTransferApplication!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesTransferApplication")
    private String et3GeneralNotesTransferApplication;
    @CCD(
            label = "England & Wales regional office",
            showCondition = "et3RegionalOffice!=\"\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice"
    )
    @JsonProperty("et3RegionalOffice")
    private String et3RegionalOffice;
    @CCD(
            label = "Why should we change the office?",
            showCondition = "et3WhyWeShouldChangeTheOffice!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3WhyWeShouldChangeTheOffice")
    private String et3WhyWeShouldChangeTheOffice;
    // ET3 Resist the claim
    @CCD(
            label = "Does the respondent wish to contest any part of the claim?",
            showCondition = "et3ContestClaim!=\"\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_contest_claim_status"
    )
    @JsonProperty("et3ContestClaim")
    private String et3ContestClaim;
    @CCD(
            label = "Give details",
            showCondition = "et3ContestClaimGiveDetails!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ContestClaimGiveDetails")
    private String et3ContestClaimGiveDetails;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesContestClaim!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesContestClaim")
    private String et3GeneralNotesContestClaim;
    // ET3 Contract claim section 7
    @CCD(
            label = "Is there an Employer's Contract Claim in section 7 of the ET3 response?",
            showCondition = "et3ContractClaimSection7!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3ContractClaimSection7")
    private String et3ContractClaimSection7;
    @CCD(
            label = "Give details",
            showCondition = "et3ContractClaimSection7Details!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ContractClaimSection7Details")
    private String et3ContractClaimSection7Details;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesContractClaimSection7!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesContractClaimSection7")
    private String et3GeneralNotesContractClaimSection7;
    // ET3 suggested issues
    @CCD(
            label = "Are there any issues identified for the judge's initial consideration - prospects of claim / response arguable? (Rule 27)",
            showCondition = "et3Rule26!=\"\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3Rule26")
    private String et3Rule26;
    @CCD(
            label = "Give details",
            showCondition = "et3Rule26Details!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3Rule26Details")
    private String et3Rule26Details;
    @CCD(
            label = "Are there any other suggested orders, directions or issues?",
            showCondition = "et3IsThereAnEt3Response=\"dummy\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_et3_suggested_issues"
    )
    @JsonProperty("et3SuggestedIssues")
    private List<String> et3SuggestedIssues;
    @CCD(
            label = "Applications for strike out or deposit",
            showCondition = "et3SuggestedIssuesStrikeOut!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3SuggestedIssuesStrikeOut")
    private String et3SuggestedIssuesStrikeOut;
    @CCD(
            label = "Interpreters",
            showCondition = "et3SuggestedIssueInterpreters!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3SuggestedIssueInterpreters")
    private String et3SuggestedIssueInterpreters;
    @CCD(
            label = "Jurisdictional issues",
            showCondition = "et3SuggestedIssueJurisdictional!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3SuggestedIssueJurisdictional")
    private String et3SuggestedIssueJurisdictional;
    @CCD(
            label = "Request for adjustments",
            showCondition = "et3SuggestedIssueAdjustments!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3SuggestedIssueAdjustments")
    private String et3SuggestedIssueAdjustments;
    @CCD(
            label = "Rule 49",
            showCondition = "et3SuggestedIssueRule50!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3SuggestedIssueRule50")
    private String et3SuggestedIssueRule50;
    @CCD(
            label = "Time points",
            showCondition = "et3SuggestedIssueTimePoints!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3SuggestedIssueTimePoints")
    private String et3SuggestedIssueTimePoints;
    @CCD(
            label = "General notes",
            showCondition = "et3GeneralNotesRule26!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3GeneralNotesRule26")
    private String et3GeneralNotesRule26;
    // ET3 Final notes
    @CCD(
            label = "Additional information",
            showCondition = "et3AdditionalInformation!=\"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3AdditionalInformation")
    private String et3AdditionalInformation;
    @CCD(label = "ET3 Processing Document", categoryID = "C72", searchable = false, typeOverride = FieldType.Document)
    @JsonProperty("et3VettingDocument")
    private UploadedDocumentType et3VettingDocument;
}
