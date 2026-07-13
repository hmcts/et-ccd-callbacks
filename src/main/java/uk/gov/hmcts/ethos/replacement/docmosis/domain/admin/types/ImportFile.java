package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@ComplexType(generate = true)
@Data
public class ImportFile {

    @JsonProperty("file")
    @CCD(label = "File", regex = ".xlsx")
    private Document file;
    @JsonProperty("user")
    @CCD(label = "User")
    private String user;
    @JsonProperty("lastImported")
    @CCD(label = "Last Imported", typeOverride = FieldType.DateTime)
    private String lastImported;
}
