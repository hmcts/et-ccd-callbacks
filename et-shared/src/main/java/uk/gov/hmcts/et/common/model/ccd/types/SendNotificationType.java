package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseStatusTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "sendNotificationCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class SendNotificationType {

    @CCD(label = "No")
    @JsonProperty("number")
    private String number; // Unique, incremented value for each application
    @CCD(label = "Date sent")
    @JsonProperty("date")
    private String date;

    @CCD(label = "Notification")
    @JsonProperty("sendNotificationTitle")
    private String sendNotificationTitle;
    @CCD(label = " ", showCondition = "date=\"dummy\"")
    @JsonProperty("sendNotificationLetter")
    private String sendNotificationLetter;
    @CCD(label = "Documents", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentUpload")
    @JsonProperty("sendNotificationUploadDocument")
    private List<DocumentTypeItem> sendNotificationUploadDocument;
    @CCD(
            label = " ",
            showCondition = "date=\"dummy\"",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_sendNotificationSubject"
    )
    @JsonProperty("sendNotificationSubject")
    private List<String> sendNotificationSubject;
    @CCD(label = "Additional information", typeOverride = FieldType.TextArea)
    @JsonProperty("sendNotificationAdditionalInfo")
    private String sendNotificationAdditionalInfo;
    @CCD(label = "To party")
    @JsonProperty("sendNotificationNotify")
    private String sendNotificationNotify;
    @CCD(label = "Hearing", typeOverride = FieldType.DynamicList)
    @JsonProperty("sendNotificationSelectHearing")
    private DynamicFixedListType sendNotificationSelectHearing;
    @CCD(label = "Case management order or request")
    @JsonProperty("sendNotificationCaseManagement")
    private String sendNotificationCaseManagement;
    @CCD(label = "Response due")
    @JsonProperty("sendNotificationResponseTribunal")
    private String sendNotificationResponseTribunal;
    @CCD(label = "Case management order made by")
    @JsonProperty("sendNotificationWhoCaseOrder")
    private String sendNotificationWhoCaseOrder;
    @CCD(label = "Party or parties to respond")
    @JsonProperty("sendNotificationSelectParties")
    private String sendNotificationSelectParties;
    @CCD(label = "Full name")
    @JsonProperty("sendNotificationFullName")
    private String sendNotificationFullName;
    @CCD(label = "Full name")
    @JsonProperty("sendNotificationFullName2")
    private String sendNotificationFullName2;
    @CCD(label = "Decision", showCondition = "date=\"dummy\"")
    @JsonProperty("sendNotificationDecision")
    private String sendNotificationDecision;
    @CCD(label = "Details", showCondition = "date=\"dummy\"", typeOverride = FieldType.TextArea)
    @JsonProperty("sendNotificationDetails")
    private String sendNotificationDetails;
    @CCD(label = "Request made by")
    @JsonProperty("sendNotificationRequestMadeBy")
    private String sendNotificationRequestMadeBy;
    @CCD(label = " ", showCondition = "date=\"dummy\"")
    @JsonProperty("sendNotificationEccQuestion")
    private String sendNotificationEccQuestion;
    @CCD(label = "Judgment made by")
    @JsonProperty("sendNotificationWhoMadeJudgement")
    private String sendNotificationWhoMadeJudgement;
    @CCD(label = "Responses", typeOverride = FieldType.Collection, typeParameterOverride = "pseRespondCollection")
    @JsonProperty("respondCollection")
    private List<PseResponseTypeItem> respondCollection;
    @CCD(
            label = "Responses",
            showCondition = "date=\"dummy\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "pseRespondCollection"
    )
    @JsonProperty("respondStoredCollection")
    private List<PseResponseTypeItem> respondStoredCollection;
    @CCD(
            label = "Responses",
            showCondition = "date=\"dummy\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "pseRespondCollection"
    )
    @JsonProperty("respondentRespondStoredCollection")
    private List<PseResponseTypeItem> respondentRespondStoredCollection;
    @CCD(
            label = "Tribunal Responses",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "respondNotificationTypeCollection"
    )
    @JsonProperty("respondNotificationTypeCollection")
    private List<GenericTypeItem<RespondNotificationType>> respondNotificationTypeCollection;
    @CCD(label = "Notification State", showCondition = "number=\"dummy\"")
    @JsonProperty("notificationState")
    private String notificationState;
    @CCD(
            label = "Respondent notification State",
            showCondition = "number=\"dummy\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "pseStatus"
    )
    @JsonProperty("respondentState")
    private List<PseStatusTypeItem> respondentState;
    @CCD(label = "Subject")
    @JsonProperty("sendNotificationSubjectString")
    private String sendNotificationSubjectString;
    @CCD(label = "Response due")
    @JsonProperty("sendNotificationResponseTribunalTable")
    private String sendNotificationResponseTribunalTable;
    @CCD(label = "Number of responses")
    @JsonProperty("sendNotificationResponsesCount")
    private String sendNotificationResponsesCount;
    @CCD(label = "Sent by")
    @JsonProperty("sendNotificationSentBy")
    private String sendNotificationSentBy;
    @CCD(label = "Multiple ref", showCondition = "sendNotificationNotify=\"dummy\"")
    @JsonProperty("notificationSentFrom")
    private String notificationSentFrom;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "To", showCondition = "sendNotificationNotify=\"Lead case\"")
  private String sendNotificationNotifyLeadCase;
  @CCD(label = "To", showCondition = "sendNotificationNotify=\"Lead and sub cases\"")
  private String sendNotificationNotifyAll;
  @CCD(label = "To", showCondition = "sendNotificationNotify=\"Selected cases\"")
  private String sendNotificationNotifySelected;
  @CCD(label = "sendNotificationAnotherLetter")
  private String sendNotificationAnotherLetter;
  // ==== end synthesised definition-only fields ====
}
