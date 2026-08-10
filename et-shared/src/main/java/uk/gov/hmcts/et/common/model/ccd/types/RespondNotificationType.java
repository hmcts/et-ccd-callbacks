package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.DocumentUpload;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "respondNotificationTypeCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class RespondNotificationType {
    @CCD(label = "Date")
    @JsonProperty("respondNotificationDate")
    private String respondNotificationDate;
    @CCD(label = "Response title")
    @JsonProperty("respondNotificationTitle")
    private String respondNotificationTitle;
    @CCD(label = "Additional information", typeOverride = FieldType.TextArea)
    @JsonProperty("respondNotificationAdditionalInfo")
    private String respondNotificationAdditionalInfo;
    @CCD(label = "Documents", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentUpload")
    @JsonProperty("respondNotificationUploadDocument")
    private List<DocumentUpload> respondNotificationUploadDocument;
    @CCD(label = "Is this a case management order or request?")
    @JsonProperty("respondNotificationCmoOrRequest")
    private String respondNotificationCmoOrRequest;
    @CCD(label = "Response due")
    @JsonProperty("respondNotificationResponseRequired")
    private String respondNotificationResponseRequired;
    @CCD(label = "Party or parties to respoond")
    @JsonProperty("respondNotificationWhoRespond")
    private String respondNotificationWhoRespond;
    @CCD(label = "Case management order made by")
    @JsonProperty("respondNotificationCaseManagementMadeBy")
    private String respondNotificationCaseManagementMadeBy;
    @CCD(label = "Request made by")
    @JsonProperty("respondNotificationRequestMadeBy")
    private String respondNotificationRequestMadeBy;
    @CCD(label = "Full name")
    @JsonProperty("respondNotificationFullName")
    private String respondNotificationFullName;
    @CCD(label = "Sent to")
    @JsonProperty("respondNotificationPartyToNotify")
    private String respondNotificationPartyToNotify;
    @CCD(label = "Updated date time")
    @JsonProperty("dateTime")
    private String dateTime;
    @CCD(label = " ", showCondition = "respondNotificationDate=\"dummy\"")
    private String state;
    @CCD(label = " ", showCondition = "state=\"dummy\"")
    private String isClaimantResponseDue;
}
