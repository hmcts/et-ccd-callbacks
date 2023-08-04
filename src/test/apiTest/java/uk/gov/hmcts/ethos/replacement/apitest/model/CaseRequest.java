package uk.gov.hmcts.ethos.replacement.apitest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@Builder
@Jacksonized
public class CaseRequest {
    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("case_type_id")
    private String caseTypeId;

    @JsonProperty("post_code")
    private String postCode;

    @JsonProperty("case_data")
    private Map<String, Object> caseData;
}
