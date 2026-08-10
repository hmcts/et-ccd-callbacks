package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.DocumentUpload;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HearingBundle", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class HearingBundleType {
    @CCD(label = "Have you agreed these documents with the other party?", searchable = false)
    @JsonProperty("agreedDocWith")
    private String agreedDocWith;
    @CCD(label = "Which documents are disputed", searchable = false)
    @JsonProperty("agreedDocWithBut")
    private String agreedDocWithBut;
    @CCD(label = "Why you've not been able to agree with the other party", searchable = false)
    @JsonProperty("agreedDocWithNo")
    private String agreedDocWithNo;
    @CCD(label = "Hearing", showCondition = "formattedSelectedHearing=\"dummy\"", searchable = false)
    @JsonProperty("hearing")
    private String hearing;
    @CCD(label = "Type", searchable = false)
    @JsonProperty("whatDocuments")
    private String whatDocuments;
    @CCD(label = "Type (other)", searchable = false)
    @JsonProperty("whatDocumentsOther")
    private String whatDocumentsOther;
    @CCD(label = "Whose hearing documents are you uploading?", searchable = false)
    @JsonProperty("whoseDocuments")
    private String whoseDocuments;
    @CCD(
            label = "Document",
            categoryID = "C57",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload"
    )
    @JsonProperty("uploadFile")
    private DocumentUpload uploadFile;
    @CCD(label = "Hearing", searchable = false)
    @JsonProperty("formattedSelectedHearing")
    private String formattedSelectedHearing;
    @CCD(label = "Uploaded date", searchable = false)
    @JsonProperty("uploadDateTime")
    private String uploadDateTime;
    // Submitted date is the date when the bundle was submitted to the tribunal and is different from the upload date.
    @CCD(label = "Submitted", searchable = false)
    @JsonProperty("submittedDate")
    private String submittedDate;
    @CCD(label = "Uploaded by", searchable = false)
    @JsonProperty("uploadedBy")
    private String uploadedBy;
}
