package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@Data
public class InitialConsiderationDocument {

    @JsonProperty("accessKey")
    private String accessKey;

    @JsonProperty("templateName")
    private String templateName;

    @JsonProperty("outputName")
    private String outputName;

    @JsonProperty("data")
    private InitialConsiderationData data;
}
