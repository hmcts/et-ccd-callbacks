package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3CaseDetailsLinksStatuses;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3HubLinksStatuses;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.DocumentUpload;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Respondent", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondentSumType {
    @CCD(
            label = "Response",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ResponseStatus"
    )
    @JsonProperty("response_status")
    private String responseStatus;
    @CCD(
            label = "Is the claim resisted?",
            showCondition = "response_status=\"Accepted\" AND responseReceived =\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("responseToClaim")
    private String responseToClaim;
    @CCD(
            label = "Reason for the rejection",
            showCondition = "response_status=\"Rejected\" AND responseReceived =\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "msl_Response"
    )
    @JsonProperty("rejection_reason")
    private String rejectionReason;
    @CCD(
            label = "Reason for the rejection",
            showCondition = "rejection_reason=\"Other\" AND responseReceived =\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("rejection_reason_other")
    private String rejectionReasonOther;
    @CCD(
            label = "Response received outside of time allowed?",
            showCondition = "response_status=\"Not accepted\" AND responseReceived =\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("responseOutOfTime")
    private String responseOutOfTime;
    @CCD(
            label = "Not on prescribed form?",
            showCondition = "response_status=\"Not accepted\" AND responseReceived =\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("responseNotOnPrescribedForm")
    private String responseNotOnPrescribedForm;
    @CCD(
            label = "Required information missing?",
            showCondition = "response_status=\"Not accepted\" AND responseReceived =\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("responseRequiredInfoAbsent")
    private String responseRequiredInfoAbsent;
    @CCD(label = "Notes", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("responseNotes")
    private String responseNotes;
    @CCD(
            label = "Date referred to Judge",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.Date
    )
    @JsonProperty("response_referred_to_judge")
    private String responseReferredToJudge;
    @CCD(
            label = "Date returned from Judge",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.Date
    )
    @JsonProperty("response_returned_from_judge")
    private String responseReturnedFromJudge;
    @CCD(label = "Name of respondent", typeOverride = FieldType.TextArea)
    @JsonProperty("respondent_name")
    private String respondentName;
    @CCD(
            label = "Is there an ECC?",
            showCondition = "responseReceived=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("respondentEcc")
    private String respondentEcc;
    @CCD(
            label = "Has a reply to the ECC been received?",
            showCondition = "respondentEcc=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("respondentEccReply")
    private String respondentEccReply;
    @CCD(label = " ", showCondition = "respondentEccReply=\"dummy\"", retainHiddenValue = true)
    @JsonProperty("respondentEccReplyCount")
    private String respondentEccReplyCount; // for WA Tasks

    @CCD(
            label = "Hearing panel preference",
            showCondition = "responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_HearingPanelPreference"
    )
    @JsonProperty("respondent_hearing_panel_preference")
    private String respondentHearingPanelPreference;
    @CCD(
            label = "Hearing panel preference reason",
            showCondition = "respondent_hearing_panel_preference=\"Judge\" OR respondent_hearing_panel_preference=\"Panel\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("respondent_hearing_panel_preference_reason")
    private String respondentHearingPanelPreferenceReason;

    @CCD(
            label = "Type of respondent",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondentType"
    )
    @JsonProperty("respondentType")
    private String respondentType;
    @CCD(label = "Organisation or business name", showCondition = "respondentType=\"Organisation\"")
    @JsonProperty("respondentOrganisation")
    private String respondentOrganisation;
    @CCD(label = "Respondent First Name", showCondition = "respondentType=\"Individual\"")
    @JsonProperty("respondentFirstName")
    private String respondentFirstName;
    @CCD(label = "Respondent Last Name", showCondition = "respondentType=\"Individual\"")
    @JsonProperty("respondentLastName")
    private String respondentLastName;
    @CCD(
            label = "Is there an ACAS Certificate number?",
            showCondition = "responseStruckOut !=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("respondent_ACAS_question")
    private String respondentAcasQuestion;
    @CCD(label = "ACAS Certificate Number", showCondition = "respondent_ACAS_question=\"Yes\"", searchable = false)
    @JsonProperty("respondent_ACAS")
    private String respondentAcas;
    @CCD(
            label = "What are the reasons for not having an ACAS Certificate number?",
            showCondition = "respondent_ACAS_question=\"No\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ACAS"
    )
    @JsonProperty("respondent_ACAS_no")
    private String respondentAcasNo;
    @CCD(
            label = "Respondent Address",
            showCondition = "responseStruckOut !=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.AddressUK
    )
    @JsonProperty("respondent_address")
    private Address respondentAddress;
    @CCD(
            label = "Phone number",
            showCondition = "responseStruckOut !=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.PhoneUK
    )
    @JsonProperty("respondent_phone1")
    private String respondentPhone1;
    @CCD(
            label = "Alternative number",
            showCondition = "responseStruckOut !=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.PhoneUK
    )
    @JsonProperty("respondent_phone2")
    private String respondentPhone2;
    @CCD(
            label = "Email address",
            showCondition = "responseStruckOut !=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.Email
    )
    @JsonProperty("respondent_email")
    private String respondentEmail;
    @CCD(
            label = "Contact preference",
            showCondition = "responseStruckOut !=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ContactPreference"
    )
    @JsonProperty("respondent_contact_preference")
    private String respondentContactPreference;
    @CCD(
            label = "Response Struck Out",
            showCondition = "responseReceived=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("responseStruckOut")
    private String responseStruckOut;
    @CCD(
            label = "Struck Out Date",
            showCondition = "responseStruckOut=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("responseStruckOutDate")
    private String responseStruckOutDate;
    @CCD(
            label = "Judge's only consent",
            showCondition = "responseStruckOut=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("responseStruckOutChairman")
    private String responseStruckOutChairman;
    @CCD(
            label = "Why struck out",
            showCondition = "responseStruckOut=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_et3Struckout"
    )
    @JsonProperty("responseStruckOutReason")
    private String responseStruckOutReason;
    @CCD(
            label = "Respondent Address (from the ET3 form)",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    @JsonProperty("responseRespondentAddress")
    private Address responseRespondentAddress;
    @CCD(
            label = "Phone number (from the ET3 form)",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.PhoneUK
    )
    @JsonProperty("responseRespondentPhone1")
    private String responseRespondentPhone1;
    @CCD(
            label = "Alternative number (from the ET3 form)",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.PhoneUK
    )
    @JsonProperty("responseRespondentPhone2")
    private String responseRespondentPhone2;
    @CCD(
            label = "Email address (from the ET3 form)",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.Email
    )
    @JsonProperty("responseRespondentEmail")
    private String responseRespondentEmail;
    @CCD(
            label = "Contact preference (from the ET3 form)",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ContactPreference"
    )
    @JsonProperty("responseRespondentContactPreference")
    private String responseRespondentContactPreference;

    @CCD(label = "Has the ET3 form been received?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("responseReceived")
    private String responseReceived;
    @CCD(
            label = "Response received date",
            showCondition = "responseReceived=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("responseReceivedDate")
    private String responseReceivedDate;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", retainHiddenValue = true)
    @JsonProperty("responseReceivedCount")
    private String responseReceivedCount; // for WA Tasks

    @CCD(label = "Title", showCondition = "responseReceived=\"Yes\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("responseRespondentNameQuestion")
    private String responseRespondentNameQuestion;
    @CCD(label = "Title", showCondition = "responseReceived=\"Yes\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("responseRespondentName")
    private String responseRespondentName;
    @CCD(
            label = "Is the claim against this Respondent continuing? ",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("responseContinue")
    private String responseContinue;
    @CCD(ignore = true)
    @JsonProperty("responseCounterClaim")
    private String responseCounterClaim;
    @CCD(label = "Reference", showCondition = "responseReceived=\"Yes\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("responseReference")
    private String responseReference;
    @CCD(label = "Has there been a request for an extension?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("extensionRequested")
    private String extensionRequested;
    @CCD(
            label = "Has the request for extension been granted?",
            showCondition = "extensionRequested=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("extensionGranted")
    private String extensionGranted;
    @CCD(
            label = "Enter the extension date",
            showCondition = "extensionGranted=\"Yes\" AND extensionRequested=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("extensionDate")
    private String extensionDate;
    @CCD(
            label = "Has the ET3 form been resubmitted?",
            showCondition = "extensionDate=\"dummy\"",
            searchable = false,
            retainHiddenValue = true
    )
    @JsonProperty("extensionResubmitted")
    private String extensionResubmitted;
    @CCD(label = "ET3 vetting", showCondition = "et3VettingCompleted=\"Yes\"", searchable = false)
    @JsonProperty("et3Vetting")
    private Et3VettingType et3Vetting;
    @CCD(
            label = "et3VettingCompleted",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3VettingCompleted")
    private String et3VettingCompleted;

    // ET3 Response
    @CCD(
            label = "Is this the correct claimant for the claim you're responding to?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3ResponseIsClaimantNameCorrect")
    private String et3ResponseIsClaimantNameCorrect;
    @CCD(
            label = "What is the correct name of the claimant?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false
    )
    @JsonProperty("et3ResponseClaimantNameCorrection")
    private String et3ResponseClaimantNameCorrection;
    @CCD(
            label = "Enter the company number if applicable",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false
    )
    @JsonProperty("et3ResponseRespondentCompanyNumber")
    private String et3ResponseRespondentCompanyNumber;
    @CCD(
            label = "What type of employer is the respondent?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_employer_type"
    )
    @JsonProperty("et3ResponseRespondentEmployerType")
    private String et3ResponseRespondentEmployerType;
    @CCD(
            label = "If individual, what is their preferred title?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false
    )
    @JsonProperty("et3ResponseRespondentPreferredTitle")
    private String et3ResponseRespondentPreferredTitle;
    @CCD(
            label = "Name of contact at respondent's address if not you as the representative",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false
    )
    @JsonProperty("et3ResponseRespondentContactName")
    private String et3ResponseRespondentContactName;
    @CCD(label = "DX address (if known)", showCondition = "response_referred_to_judge=\"Dummy\"", searchable = false)
    @JsonProperty("et3ResponseDXAddress")
    private String et3ResponseDXAddress;
    @CCD(
            label = "Provide a reason why you have selected post",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false
    )
    @JsonProperty("et3ResponseContactReason")
    private String et3ResponseContactReason;
    @CCD(
            label = "Which types of hearing can you, as the representative, attend?",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et3_hearing_type"
    )
    @JsonProperty("et3ResponseHearingRepresentative")
    private List<String> et3ResponseHearingRepresentative;
    @CCD(
            label = "Which types of hearing can the respondent attend?",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et3_hearing_type"
    )
    @JsonProperty("et3ResponseHearingRespondent")
    private List<String> et3ResponseHearingRespondent;
    @CCD(
            label = "How many people does the respondent employ in Great Britain?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            min = 0,
            typeOverride = FieldType.Number
    )
    @JsonProperty("et3ResponseEmploymentCount")
    private String et3ResponseEmploymentCount;
    @CCD(
            label = "Does the respondent have more than one site in Great Britain?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3ResponseMultipleSites")
    private String et3ResponseMultipleSites;
    @CCD(
            label = "How many people are employed at the place where the claimant worked?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            min = 0,
            typeOverride = FieldType.Number
    )
    @JsonProperty("et3ResponseSiteEmploymentCount")
    private String et3ResponseSiteEmploymentCount;
    @CCD(
            label = "Do you agree with the details given by the claimant about early conciliation with Acas?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3ResponseAcasAgree")
    private String et3ResponseAcasAgree;
    @CCD(
            label = "Why do you disagree with the Acas conciliation details given?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponseAcasAgreeReason")
    private String et3ResponseAcasAgreeReason;
    @CCD(
            label = "Are the dates of employment given by the claimant correct?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable"
    )
    @JsonProperty("et3ResponseAreDatesCorrect")
    private String et3ResponseAreDatesCorrect;
    @CCD(
            label = "Enter the employment start date",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.Date
    )
    @JsonProperty("et3ResponseEmploymentStartDate")
    private String et3ResponseEmploymentStartDate;
    @CCD(
            label = "Enter employment end date",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.Date
    )
    @JsonProperty("et3ResponseEmploymentEndDate")
    private String et3ResponseEmploymentEndDate;
    @CCD(
            label = "Do you want to provide any further information about the claimant's employment dates?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponseEmploymentInformation")
    private String et3ResponseEmploymentInformation;
    @CCD(
            label = "Is the claimant's employment with the respondent continuing?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable"
    )
    @JsonProperty("et3ResponseContinuingEmployment")
    private String et3ResponseContinuingEmployment;
    @CCD(
            label = "Is the claimant's description of their job or job title correct?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable"
    )
    @JsonProperty("et3ResponseIsJobTitleCorrect")
    private String et3ResponseIsJobTitleCorrect;
    @CCD(
            label = "What is or was the claimant's correct job title?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponseCorrectJobTitle")
    private String et3ResponseCorrectJobTitle;
    @CCD(
            label = "Are the claimant's total weekly work hours correct?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable"
    )
    @JsonProperty("et3ResponseClaimantWeeklyHours")
    private String et3ResponseClaimantWeeklyHours;
    @CCD(
            label = "What are the claimant's correct total weekly work hours?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.Number
    )
    @JsonProperty("et3ResponseClaimantCorrectHours")
    private String et3ResponseClaimantCorrectHours;
    @CCD(
            label = "Are the earnings details given by the claimant correct?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable"
    )
    @JsonProperty("et3ResponseEarningDetailsCorrect")
    private String et3ResponseEarningDetailsCorrect;
    @CCD(
            label = "How often was the claimant paid?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_et3_pay_frequency"
    )
    @JsonProperty("et3ResponsePayFrequency")
    private String et3ResponsePayFrequency;
    @CCD(
            label = "Enter the claimant's pay BEFORE tax",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("et3ResponsePayBeforeTax")
    private String et3ResponsePayBeforeTax;
    @CCD(
            label = "Enter the claimant's normal take-home pay",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("et3ResponsePayTakehome")
    private String et3ResponsePayTakehome;
    @CCD(
            label = "Is the information given by the claimant correct about their notice?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable"
    )
    @JsonProperty("et3ResponseIsNoticeCorrect")
    private String et3ResponseIsNoticeCorrect;
    @CCD(
            label = "What are the claimant's correct notice details?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponseCorrectNoticeDetails")
    private String et3ResponseCorrectNoticeDetails;
    @CCD(
            label = "Are the details about pension and other benefits correct?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable"
    )
    @JsonProperty("et3ResponseIsPensionCorrect")
    private String et3ResponseIsPensionCorrect;
    @CCD(
            label = "What are the correct pension and benefit details?",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponsePensionCorrectDetails")
    private String et3ResponsePensionCorrectDetails;
    @CCD(
            label = "Does the respondent contest the claim?",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_contest_claim"
    )
    @JsonProperty("et3ResponseRespondentContestClaim")
    private String et3ResponseRespondentContestClaim;
    @CCD(
            label = "Upload a document to your response",
            showCondition = "et3ResponseRespondentContestClaim=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload"
    )
    @JsonProperty("et3ResponseContestClaimDocument")
    private List<DocumentTypeItem> et3ResponseContestClaimDocument;
    @CCD(
            label = "Use this text box for any accompanying information",
            showCondition = "et3ResponseRespondentContestClaim=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponseContestClaimDetails")
    private String et3ResponseContestClaimDetails;
    @CCD(
            label = "Does the respondent wish to make an Employer's Contract Claim?",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("et3ResponseEmployerClaim")
    private String et3ResponseEmployerClaim;
    @CCD(
            label = "Provide the background and details of your Employer's Contract Claim",
            showCondition = "et3ResponseEmployerClaim=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponseEmployerClaimDetails")
    private String et3ResponseEmployerClaimDetails;
    @CCD(
            label = "Add a document",
            showCondition = "et3ResponseEmployerClaim=\"Yes\" AND responseReceived=\"Yes\"",
            categoryID = "C19",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload"
    )
    @JsonProperty("et3ResponseEmployerClaimDocument")
    private UploadedDocumentType et3ResponseEmployerClaimDocument;
    @CCD(
            label = "In the respondent party - are you aware of any physical, mental or learning disability or health conditions which requires support?",
            showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_yes_no_not_sure_yet"
    )
    @JsonProperty("et3ResponseRespondentSupportNeeded")
    private String et3ResponseRespondentSupportNeeded;
    @CCD(
            label = "Use this text box or upload the requirements in a document",
            showCondition = "et3ResponseRespondentSupportNeeded=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("et3ResponseRespondentSupportDetails")
    private String et3ResponseRespondentSupportDetails;
    @CCD(
            label = "Add document",
            showCondition = "et3ResponseRespondentSupportNeeded=\"Yes\"",
            categoryID = "C19",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload"
    )
    @JsonProperty("et3ResponseRespondentSupportDocument")
    private UploadedDocumentType et3ResponseRespondentSupportDocument;
    @CCD(
            label = "ET3 Form",
            showCondition = "responseReceived=\"Yes\"",
            categoryID = "C18",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload"
    )
    @JsonProperty("et3Form")
    private DocumentUpload et3Form;
    @CCD(
            label = "ET3 Form",
            categoryID = "C18",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload"
    )
    @JsonProperty("et3FormWelsh")
    private DocumentUpload et3FormWelsh;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", retainHiddenValue = true)
    @JsonProperty("et3NotificationAcceptedDate")
    private String et3NotificationAcceptedDate;

    @CCD(
            label = "Section complete",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("personalDetailsSection")
    private String personalDetailsSection;
    @CCD(
            label = "Section complete",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("employmentDetailsSection")
    private String employmentDetailsSection;
    @CCD(
            label = "Section complete",
            showCondition = "response_referred_to_judge=\"Dummy\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("claimDetailsSection")
    private String claimDetailsSection;

    //ET3 fields
    @CCD(label = "Respondent idam id", showCondition = "responseReceived=\"Dummy\"", searchable = false)
    @JsonProperty("idamId")
    private String idamId;
    @CCD(label = "ET3 case details links statuses", showCondition = "responseReceived=\"Dummy\"", searchable = false)
    @JsonProperty("et3CaseDetailsLinksStatuses")
    private ET3CaseDetailsLinksStatuses et3CaseDetailsLinksStatuses;
    @CCD(label = "ET3 hub links statuses", showCondition = "responseReceived=\"Dummy\"", searchable = false)
    @JsonProperty("et3HubLinksStatuses")
    private ET3HubLinksStatuses et3HubLinksStatuses;
    @CCD(
            label = "Language Preference",
            showCondition = "responseReceived=\"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_languages"
    )
    @JsonProperty("et3ResponseLanguagePreference")
    private String et3ResponseLanguagePreference;
    @CCD(ignore = true)
    @JsonProperty("et3ResponseHearingRespondentNoDetails")
    private String et3ResponseHearingRespondentNoDetails;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("et3Status")
    private String et3Status;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("et3IsRespondentAddressCorrect")
    private String et3IsRespondentAddressCorrect;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("contactDetailsSection")
    private String contactDetailsSection;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("employerDetailsSection")
    private String employerDetailsSection;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("conciliationAndEmployeeDetailsSection")
    private String conciliationAndEmployeeDetailsSection;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("payPensionBenefitDetailsSection")
    private String payPensionBenefitDetailsSection;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("contestClaimSection")
    private String contestClaimSection;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("employersContractClaimSection")
    private String employersContractClaimSection;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("representativeRemoved")
    private String representativeRemoved;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("represented")
    private String represented;
    @CCD(label = " ", showCondition = "responseReceived=\"dummy\"", searchable = false, retainHiddenValue = true)
    @JsonProperty("representativeId")
    private String representativeId;
}
