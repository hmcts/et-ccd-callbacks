package uk.gov.hmcts.et.common.model.hmc.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HearingResponse {
    @JsonProperty("hearingRequestID")
    @Size(max = 30)
    @NotNull
    private Long hearingRequestId;

    @JsonProperty("status")
    @Size(max = 100)
    @NotNull
    private String status;

    @JsonProperty("timeStamp")
    @NotNull
    private LocalDateTime timeStamp;

    @JsonProperty("versionNumber")
    @NotNull
    private Integer versionNumber;
}
