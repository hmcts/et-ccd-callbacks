package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Object which contains data needed by Tornado to generate a document.
 */
@Data
@SuperBuilder
public class Et3ResponseDocument {
    @JsonProperty("accessKey")
    private String accessKey;
    @JsonProperty("templateName")
    private String templateName;
    @JsonProperty("outputName")
    private String outputName;
    @JsonProperty("data")
    private Et3ResponseData et3ResponseData;
}