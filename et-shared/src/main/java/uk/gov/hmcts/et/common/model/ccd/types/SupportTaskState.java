package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupportTaskState {
    private String adminTaskCreated;
    private String adminTaskRequired;
    private String legalOfficerTaskCreated;
    private String legalOfficerTaskRequired;
    private String judgeTaskCreated;
    private String judgeTaskRequired;
    private String arrangeSupportTaskName;
}
