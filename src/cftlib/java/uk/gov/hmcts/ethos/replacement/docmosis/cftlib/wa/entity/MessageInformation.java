package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MessageInformation {
    @JsonProperty("CaseId")
    private String caseId;
    @JsonProperty("UserId")
    private String userId;
    @JsonProperty("EventId")
    private String eventId;
    @JsonProperty("CaseTypeId")
    private String caseTypeId;
    @JsonProperty("PreviousStateId")
    private String previousStateId;
    @JsonProperty("NewStateId")
    private String newStateId;
    @JsonProperty("AdditionalData")
    private Map<String, Object> additionalData;
    @JsonProperty("EventTimeStamp")
    private LocalDateTime eventTimeStamp;
    @JsonProperty("JurisdictionId")
    private String jurisdictionId;
    @JsonProperty("EventInstanceId")
    private int eventInstanceId;
}
