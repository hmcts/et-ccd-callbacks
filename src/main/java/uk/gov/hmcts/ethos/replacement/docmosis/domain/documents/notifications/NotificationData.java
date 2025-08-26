package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.List;

@SuperBuilder
@Builder
public class NotificationData {
    @JsonProperty("ethosCaseReference")
    private final String ethosCaseReference;
    @JsonProperty("notificationNumber")
    private final String notificationNumber;
    @JsonProperty("notificationTitle")
    private final String notificationTitle;
    @JsonProperty("notificationSubject")
    private final String notificationSubject;
    @JsonProperty("dateSent")
    private final String dateSent;
    @JsonProperty("partyToNotify")
    private final String partyToNotify;
    @JsonProperty("additionalInformation")
    private final String additionalInformation;
    @JsonProperty("isHearingSubject")
    private final String isHearingSubject;
    @JsonProperty("hearing")
    private final String hearing;
    @JsonProperty("areThereLetters")
    private final String areThereLetters;
    @JsonProperty("documents")
    private final List<GenericTypeItem<DocumentType>> documents;
    @JsonProperty("isCmoSubject")
    private final String isCmoSubject;
    @JsonProperty("cmoOrRequest")
    private final String cmoOrRequest;
    @JsonProperty("cmoRequestMadeBy")
    private final String cmoRequestMadeBy;
    @JsonProperty("cmoRequestResponseRequired")
    private final String cmoRequestResponseRequired;
    @JsonProperty("isJudgmentSubject")
    private final String isJudgmentSubject;
    @JsonProperty("judgmentName")
    private final String judgmentName;
    @JsonProperty("judgmentDecision")
    private final String judgmentDecision;
    @JsonProperty("isEccSubject")
    private final String isEccSubject;
    @JsonProperty("eccType")
    private final String eccType;
    @JsonProperty("eccResponseRequired")
    private final String eccResponseRequired;
    @JsonProperty("areThereResponses")
    private final String areThereResponses;
    @JsonProperty("responses")
    private final List<ResponsePartyData> responses;
    @JsonProperty("areThereTribunalResponses")
    private final String areThereTribunalResponses;
    @JsonProperty("tribunalResponses")
    private final List<ResponseAdminData> tribunalResponses;
}
