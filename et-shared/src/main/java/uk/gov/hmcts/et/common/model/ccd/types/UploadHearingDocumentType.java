package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.DocumentUpload;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HearingDocumentUpload", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UploadHearingDocumentType {
    @CCD(
            label = "Upload Hearing Documents",
            hint = "Upload a single PDF file containing the hearing documents",
            regex = ".pdf",
            categoryID = "C57",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload"
    )
    @JsonProperty("document")
    private DocumentUpload document;
    @CCD(
            label = "What are these hearing documents?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundleType"
    )
    @JsonProperty("type")
    private String type;
    @CCD(label = "Please specify", showCondition = "type=\"Other\"")
    @JsonProperty("typeOther")
    private String typeOther;
}
