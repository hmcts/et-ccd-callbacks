package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "genericTseDetails", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@Data
@NoArgsConstructor
public class GenericTseApplicationType {

    @CCD(label = "No")
    @JsonProperty("number")
    private String number; // Unique, incremented value for each application
    @CCD(label = "Type")
    @JsonProperty("type")
    private String type; // Amend details
    @CCD(label = "Applicant")
    @JsonProperty("applicant")
    private String applicant; // Either Respondent or Claimant
    @CCD(label = "Applicant id", showCondition = "applicant=\"dummy\"")
    @JsonProperty("applicantIdamId")
    private String applicantIdamId;
    @CCD(label = "Application date")
    @JsonProperty("date")
    private String date;
    @CCD(label = "Supporting material", categoryID = "C4", typeOverride = FieldType.Document)
    @JsonProperty("documentUpload")
    private UploadedDocumentType documentUpload;
    @CCD(label = "What do you want to tell or ask the tribunal?", typeOverride = FieldType.TextArea)
    @JsonProperty("details")
    private String details;
    @CCD(label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?")
    @JsonProperty("copyToOtherPartyYesOrNo")
    private String copyToOtherPartyYesOrNo;
    @CCD(
            label = "Details of why you do not want to inform the other party",
            showCondition = "copyToOtherPartyYesOrNo=\"No\"",
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("copyToOtherPartyText")
    private String copyToOtherPartyText;
    @CCD(label = "Response due")
    @JsonProperty("dueDate")
    private String dueDate;
    @CCD(label = "Number of responses")
    @JsonProperty("responsesCount")
    private String responsesCount;
    @CCD(label = "Status")
    @JsonProperty("status")
    private String status;
    @CCD(label = "General notes")
    @JsonProperty("closeApplicationNotes")
    private String closeApplicationNotes;
    @CCD(label = "Application State", showCondition = "status=\"dummy\"")
    @JsonProperty("applicationState")
    private String applicationState;
    @CCD(
            label = "Respondent application State",
            showCondition = "status=\"dummy\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "tseStatus"
    )
    @JsonProperty("respondentState")
    private List<TseStatusTypeItem> respondentState;
    // If there are tribunal requests/orders that required a response from Respondent
    @CCD(label = "If there are tribunal requests/orders that required a response from Respondent")
    @JsonProperty("respondentResponseRequired")
    private String respondentResponseRequired;
    // If there are tribunal requests/orders that required a response from Claimant
    @CCD(label = "If there are tribunal requests/orders that required a response from Claimant")
    @JsonProperty("claimantResponseRequired")
    private String claimantResponseRequired;

    @CCD(label = "Responses", typeOverride = FieldType.Collection, typeParameterOverride = "tseReply")
    @JsonProperty("respondCollection")
    private List<TseRespondTypeItem> respondCollection;
    @CCD(
            label = "Responses",
            showCondition = "date=\"dummy\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "tseReply"
    )
    @JsonProperty("respondStoredCollection")
    private List<TseRespondTypeItem> respondStoredCollection;

    @CCD(label = "Record a decision", typeOverride = FieldType.Collection, typeParameterOverride = "tseAdminDecision")
    @JsonProperty("adminDecision")
    private List<TseAdminRecordDecisionTypeItem> adminDecision;

}
