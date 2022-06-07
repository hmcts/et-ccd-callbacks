package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.AdminCourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.VenueImport;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AdminData {
    @JsonProperty("name")
    private String name;
    @JsonProperty("staffImportFile")
    private ImportFile staffImportFile;
    @JsonProperty("venueImport")
    private VenueImport venueImport;

    // For adding judge
    @JsonProperty("tribunalOffice")
    private String tribunalOffice;
    @JsonProperty("judgeCode")
    private String judgeCode;
    @JsonProperty("judgeName")
    private String judgeName;
    @JsonProperty("employmentStatus")
    private String employmentStatus;
    @JsonProperty("judgeSelectList")
    private DynamicFixedListType judgeSelectList;

    // For adding fileLocation
    @JsonProperty("fileLocationCode")
    private String fileLocationCode;
    @JsonProperty("fileLocationName")
    private String fileLocationName;

    // For updating fileLocation
    @JsonProperty("fileLocationList")
    private DynamicFixedListType fileLocationList;

    @JsonProperty("adminCourtWorker")
    private AdminCourtWorker adminCourtWorker;

    //updateCourtWorker
    @JsonProperty("updateCourtWorkerOffice")
    private String updateCourtWorkerOffice;
    @JsonProperty("updateCourtWorkerType")
    private String updateCourtWorkerType;
    @JsonProperty("updateCourtWorkerCode")
    private String updateCourtWorkerCode;
    @JsonProperty("updateCourtWorkerName")
    private String updateCourtWorkerName;
    @JsonProperty("updateCourtWorkerSelectList")
    private DynamicFixedListType updateCourtWorkerSelectList;

}
