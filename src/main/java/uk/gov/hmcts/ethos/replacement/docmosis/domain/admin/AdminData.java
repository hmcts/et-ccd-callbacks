package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AdminData {
    @JsonProperty("name")
    private String name;
    @JsonProperty("staffImportFile")
    private ImportFile staffImportFile;
}
