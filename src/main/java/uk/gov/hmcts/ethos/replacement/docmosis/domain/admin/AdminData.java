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

    // For updating and deleting fileLocation
    @JsonProperty("fileLocationList")
    private DynamicFixedListType fileLocationList;

    @JsonProperty("adminCourtWorker")
    private AdminCourtWorker adminCourtWorker;

    //updating and deleting court worker
    @JsonProperty("courtWorkerOffice")
    private String courtWorkerOffice;
    @JsonProperty("courtWorkerType")
    private String courtWorkerType;
    @JsonProperty("courtWorkerCode")
    private String courtWorkerCode;
    @JsonProperty("courtWorkerName")
    private String courtWorkerName;
    @JsonProperty("courtWorkerSelectList")
    private DynamicFixedListType courtWorkerSelectList;

    //Ref data fixes
    @JsonProperty("hearingDateType")
    private String hearingDateType;
    @JsonProperty("listingDate")
    private String listingDate;
    @JsonProperty("listingDateFrom")
    private String listingDateFrom;
    @JsonProperty("listingDateTo")
    private String listingDateTo;
    @JsonProperty("listingVenue")
    private String listingVenue;
    @JsonProperty("refDataType")
    private String refDataType;
}
