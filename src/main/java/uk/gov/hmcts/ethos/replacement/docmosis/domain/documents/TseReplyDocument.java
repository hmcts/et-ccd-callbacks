package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Object which contains data needed by Tornado to generate a document.
 */
@SuperBuilder
@Data
@SuppressWarnings({"PMD.LinguisticNaming", "PMD.TooManyFields"})
public class TseReplyDocument {
    @JsonProperty("accessKey")
    private String accessKey;
    @JsonProperty("templateName")
    private String templateName;
    @JsonProperty("outputName")
    private String outputName;
    @JsonProperty("data")
    private TseReplyData data;
}
