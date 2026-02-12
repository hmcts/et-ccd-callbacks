package uk.gov.hmcts.ecm.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DigitalCaseFileType {
    @JsonProperty("uploadedDocument")
    private UploadedDocumentType uploadedDocument;
    @JsonProperty("dateGenerated")
    private String dateGenerated;
    @JsonProperty("status")
    private String status;
    @JsonProperty("error")
    private String error;
}
