package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.DocumentUpload;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "referralReply", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ReferralReplyType {
    //For Judge
    @CCD(label = "Reply to")
    @JsonProperty("directionTo")
    private String directionTo;
    @CCD(label = "Email address")
    @JsonProperty("replyToEmailAddress")
    private String replyToEmailAddress;
    @CCD(label = "Urgent")
    @JsonProperty("isUrgentReply")
    private String isUrgentReply;
    @CCD(label = "Directions", typeOverride = FieldType.TextArea)
    @JsonProperty("directionDetails")
    private String directionDetails;
    @CCD(label = "Documents", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentUpload")
    @JsonProperty("replyDocument")
    private List<DocumentUpload> replyDocument;
    @CCD(label = "General notes", typeOverride = FieldType.TextArea)
    @JsonProperty("replyGeneralNotes")
    private String replyGeneralNotes;
    @CCD(label = "Reply by")
    @JsonProperty("replyBy")
    private String replyBy;
    @CCD(label = "Date")
    @JsonProperty("replyDate")
    private String replyDate;

    // Work Allocation enablers
    @CCD(label = " ", showCondition = "replyDateTime = \"dummy\"")
    @JsonProperty("replyDateTime")
    private String replyDateTime;
    @CCD(label = " ", showCondition = "replyDateTime = \"dummy\"")
    @JsonProperty("referralSubject")
    private String referralSubject;
    @CCD(label = " ", showCondition = "replyDateTime = \"dummy\"")
    @JsonProperty("referralNumber")
    private String referralNumber;
}
