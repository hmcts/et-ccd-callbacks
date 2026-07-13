package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@JsonIgnoreProperties(ignoreUnknown = true)
@ComplexType(generate = false)
@Data
public class Document {
    @JsonProperty("document_url")
    private String url;
    @JsonProperty("document_filename")
    private String filename;
    @JsonProperty("document_binary_url")
    private String binaryUrl;
}
