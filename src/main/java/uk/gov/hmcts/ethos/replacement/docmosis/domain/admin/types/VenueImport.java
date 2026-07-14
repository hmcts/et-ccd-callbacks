package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VenueImport {
    @JsonProperty("venueImportFile")
    @CCD(label = "Venue Import File")
    private ImportFile venueImportFile;
    @JsonProperty("venueImportOffice")
    @CCD(label = "Tribunal Office", typeOverride = FieldType.FixedList,
        typeParameterOverride = "importOffice")
    private String venueImportOffice;
}
