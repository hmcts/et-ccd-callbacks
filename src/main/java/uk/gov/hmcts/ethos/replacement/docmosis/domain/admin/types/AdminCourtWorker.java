package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AdminCourtWorker {

    @JsonProperty("tribunalOffice")
    private String tribunalOffice;
    @JsonProperty("courtWorkerType")
    private String courtWorkerType;
    @JsonProperty("courtWorkerCode")
    private String courtWorkerCode;
    @JsonProperty("courtWorkerName")
    private String courtWorkerName;

    @JsonProperty("dynamicCourtWorkerList")
    private DynamicFixedListType dynamicCourtWorkerList;

}
