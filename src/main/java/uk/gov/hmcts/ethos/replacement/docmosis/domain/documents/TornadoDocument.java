package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TornadoDocument<T> {
    @JsonProperty("accessKey")
    private String accessKey;
    @JsonProperty("templateName")
    private String templateName;
    @JsonProperty("outputName")
    private String outputName;
    @JsonProperty("data")
    private T data;
}
