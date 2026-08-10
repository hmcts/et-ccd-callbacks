package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.DocumentUpload;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "tseReply", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class TseRespondType {

    @CCD(label = "Response date")
    @JsonProperty("date")
    private String date;
    @CCD(label = "Response from")
    @JsonProperty("from")
    private String from;

    // Respondent / Claimant Reply
    @CCD(label = "Response from Idam id")
    @JsonProperty("fromIdamId")
    private String fromIdamId;
    @CCD(label = "What's your response to the claimant's application", typeOverride = FieldType.TextArea)
    @JsonProperty("response")
    private String response;
    @CCD(label = " ", showCondition = "date=\"dummy\"")
    @JsonProperty("hasSupportingMaterial")
    private String hasSupportingMaterial;
    @CCD(label = "Supporting material", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentUpload")
    @JsonProperty("supportingMaterial")
    private List<GenericTypeItem<DocumentUpload>> supportingMaterial;
    @CCD(label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?")
    @JsonProperty("copyToOtherParty")
    private String copyToOtherParty;
    @CCD(label = "Give details", typeOverride = FieldType.TextArea)
    @JsonProperty("copyNoGiveDetails")
    private String copyNoGiveDetails;
    @CCD(label = " ", showCondition = "from=\"dummy\"", categoryID = "C4", typeOverride = FieldType.Document)
    @JsonProperty("summaryPdf")
    private UploadedDocumentType summaryPdf;

    // Admin Reply
    @CCD(label = "Enter response title")
    @JsonProperty("enterResponseTitle")
    private String enterResponseTitle;
    @CCD(label = "Additional information", typeOverride = FieldType.TextArea)
    @JsonProperty("additionalInformation")
    private String additionalInformation;
    @CCD(label = "Add document", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentUpload")
    @JsonProperty("addDocument")
    private List<GenericTypeItem<DocumentUpload>> addDocument;
    @CCD(label = "Is this a case management order or request?")
    @JsonProperty("isCmoOrRequest")
    private String isCmoOrRequest;
    @CCD(label = "Case management order made by")
    @JsonProperty("cmoMadeBy")
    private String cmoMadeBy;
    @CCD(label = "Request made by")
    @JsonProperty("requestMadeBy")
    private String requestMadeBy;
    @CCD(label = "Enter their full name")
    @JsonProperty("madeByFullName")
    private String madeByFullName;
    @CCD(label = "Is a response to the tribunal required?")
    @JsonProperty("isResponseRequired")
    private String isResponseRequired;
    @CCD(label = "Select the party or parties who must respond")
    @JsonProperty("selectPartyRespond")
    private String selectPartyRespond;
    @CCD(label = "Select the party or parties to notify")
    @JsonProperty("selectPartyNotify")
    private String selectPartyNotify;
    @CCD(label = "Viewed by the claimant")
    @JsonProperty("viewedByClaimant")
    private String viewedByClaimant;

    // Work Allocation enablers
    @CCD(label = " ", showCondition = "dateTime = \"dummy\"")
    @JsonProperty("dateTime")
    private String dateTime;
    @CCD(label = " ", showCondition = "applicationType = \"dummy\"")
    @JsonProperty("applicationType")
    private String applicationType;
}
