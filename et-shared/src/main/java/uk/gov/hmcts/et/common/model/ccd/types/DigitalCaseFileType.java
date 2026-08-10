package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DigitalCaseFile", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DigitalCaseFileType {
    @CCD(label = "Digital Case File", categoryID = "C69", typeOverride = FieldType.Document)
    @JsonProperty("uploadedDocument")
    private UploadedDocumentType uploadedDocument;
    @CCD(label = "Status")
    @JsonProperty("status")
    private String status;
    @CCD(label = "Date Generated", typeOverride = FieldType.Date)
    @JsonProperty("dateGenerated")
    private String dateGenerated;
    @CCD(label = "Error")
    @JsonProperty("error")
    private String error;
}
