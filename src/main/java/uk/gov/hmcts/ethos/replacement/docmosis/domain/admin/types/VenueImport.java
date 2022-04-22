package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VenueImport {
    @JsonProperty("venueImportFile")
    private ImportFile venueImportFile;
    @JsonProperty("venueImportOffice")
    private String venueImportOffice;
}
