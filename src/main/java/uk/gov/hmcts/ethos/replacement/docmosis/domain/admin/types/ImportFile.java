package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ImportFile {

    @JsonProperty("file")
    private Document file;
    @JsonProperty("user")
    private String user;
    @JsonProperty("lastImported")
    private String lastImported;
}
