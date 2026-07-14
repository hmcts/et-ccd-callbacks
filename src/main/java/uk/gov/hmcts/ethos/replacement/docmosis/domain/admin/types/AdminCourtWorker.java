package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AdminCourtWorker {

    @JsonProperty("tribunalOffice")
    @CCD(label = "Tribunal Office", typeOverride = FieldType.FixedList,
        typeParameterOverride = "importOffice")
    private String tribunalOffice;
    @JsonProperty("courtWorkerType")
    @CCD(label = "Court Worker Type", typeOverride = FieldType.FixedList,
        typeParameterOverride = "fl_CourtWorker")
    private String courtWorkerType;
    @JsonProperty("courtWorkerCode")
    @CCD(label = "Court Worker Code")
    private String courtWorkerCode;
    @JsonProperty("courtWorkerName")
    @CCD(label = "Court Worker Name")
    private String courtWorkerName;

}
