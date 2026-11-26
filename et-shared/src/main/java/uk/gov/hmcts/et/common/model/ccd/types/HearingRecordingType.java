package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A recording of one session of a hearing day. Stored on each day of a hearing (DateListed) and
 * edited through the Hearing Details event (HearingDetails).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingRecordingType {
    @JsonProperty("hearingRecordingDocument")
    private UploadedDocumentType hearingRecordingDocument;
    @JsonProperty("hearingRecordingDescription")
    private String hearingRecordingDescription;
    @JsonProperty("hearingRecordingStartTime")
    private String hearingRecordingStartTime;
}
