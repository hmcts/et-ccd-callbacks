package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "pseStatus", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class PseStatusType {

    @CCD(label = "User Idam Id")
    @JsonProperty("userIdamId")
    private String userIdamId;
    @CCD(label = "Notification state")
    @JsonProperty("notificationState")
    private String notificationState;
    @CCD(label = "Updated date time")
    @JsonProperty("dateTime")
    private String dateTime;

}
