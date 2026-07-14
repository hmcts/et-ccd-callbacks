package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.AdminCourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.VenueImport;

@JsonIgnoreProperties(ignoreUnknown = true)
@CCD(access = AdminAccess.class)
@Data
public class AdminData {
    @JsonProperty("name")
    @CCD(label = "Name")
    private String name;
    @JsonProperty("staffImportFile")
    @CCD(label = "Staff Import File")
    private ImportFile staffImportFile;
    @JsonProperty("venueImport")
    @CCD(label = "Venue Import")
    private VenueImport venueImport;
    // For adding judge
    @JsonProperty("tribunalOffice")
    @CCD(label = "Tribunal office", typeOverride = FieldType.FixedList,
        typeParameterOverride = "importOffice")
    private String tribunalOffice;
    @JsonProperty("judgeCode")
    @CCD(label = "Code")
    private String judgeCode;
    @JsonProperty("judgeName")
    @CCD(label = "Name")
    private String judgeName;
    @JsonProperty("employmentStatus")
    @CCD(label = "Employment status", typeOverride = FieldType.FixedList,
        typeParameterOverride = "fl_EmploymentStatus")
    private String employmentStatus;
    @JsonProperty("judgeSelectList")
    @CCD(label = "Select Judge", typeOverride = FieldType.DynamicList)
    private DynamicFixedListType judgeSelectList;

    // For adding fileLocation
    @JsonProperty("fileLocationCode")
    @CCD(label = "File Location Code")
    private String fileLocationCode;
    @JsonProperty("fileLocationName")
    @CCD(label = "File Location Name")
    private String fileLocationName;

    // For updating and deleting fileLocation
    @JsonProperty("fileLocationList")
    @CCD(label = "File Location List", typeOverride = FieldType.DynamicList)
    private DynamicFixedListType fileLocationList;

    @JsonProperty("adminCourtWorker")
    @CCD(label = "Admin Court Worker")
    private AdminCourtWorker adminCourtWorker;

    //updating and deleting court worker
    @JsonProperty("courtWorkerOffice")
    @CCD(label = "Tribunal Office", typeOverride = FieldType.FixedList,
        typeParameterOverride = "importOffice")
    private String courtWorkerOffice;
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
    @JsonProperty("courtWorkerSelectList")
    @CCD(label = "Select Court Worker", typeOverride = FieldType.DynamicList)
    private DynamicFixedListType courtWorkerSelectList;
    @JsonProperty("preHearingDepositImportFile")
    @CCD(label = "Pre-Hearing Deposit Import File")
    private ImportFile preHearingDepositImportFile;
}
