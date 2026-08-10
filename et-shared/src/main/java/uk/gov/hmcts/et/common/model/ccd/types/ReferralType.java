package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralReplyTypeItem;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.DocumentUpload;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "referralDetails", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ReferralType {
    @CCD(label = "No")
    @JsonProperty("referralNumber")
    private String referralNumber;
    @CCD(label = "Hearing date")
    @JsonProperty("referralHearingDate")
    private String referralHearingDate;
    @CCD(label = "Referred to")
    @JsonProperty("referCaseTo")
    private String referCaseTo;
    @CCD(label = "Email address")
    @JsonProperty("referentEmail")
    private String referentEmail;
    @CCD(label = "Urgent")
    @JsonProperty("isUrgent")
    private String isUrgent;
    @CCD(label = "Subject")
    @JsonProperty("referralSubject")
    private String referralSubject;
    @CCD(label = "Referral subject")
    @JsonProperty("referralSubjectSpecify")
    private String referralSubjectSpecify;
    @CCD(label = "Details of the referral", typeOverride = FieldType.TextArea)
    @JsonProperty("referralDetails")
    private String referralDetails;
    @CCD(label = "Documents", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentUpload")
    @JsonProperty("referralDocument")
    private List<DocumentUpload> referralDocument;
    @CCD(label = "Recommended instructions", typeOverride = FieldType.TextArea)
    @JsonProperty("referralInstruction")
    private String referralInstruction;
    @CCD(label = "Referred by")
    @JsonProperty("referredBy")
    private String referredBy;
    @CCD(label = "Referral date")
    @JsonProperty("referralDate")
    private String referralDate;
    @CCD(label = "Status")
    @JsonProperty("referralStatus")
    private String referralStatus;

    @CCD(label = "General notes", typeOverride = FieldType.TextArea)
    @JsonProperty("closeReferralGeneralNotes")
    private String closeReferralGeneralNotes;
    @CCD(label = "Reply", typeOverride = FieldType.Collection, typeParameterOverride = "referralReply")
    @JsonProperty("referralReplyCollection")
    private List<ReferralReplyTypeItem> referralReplyCollection;
    @CCD(label = "Updates", typeOverride = FieldType.Collection, typeParameterOverride = "updateReferralDetails")
    @JsonProperty("updateReferralCollection")
    private ListTypeItem<UpdateReferralType> updateReferralCollection;
    @CCD(label = "Referral Document", categoryID = "C4", typeOverride = FieldType.Document)
    @JsonProperty("referralSummaryPdf")
    private UploadedDocumentType referralSummaryPdf;
}

