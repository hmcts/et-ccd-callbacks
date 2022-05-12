package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ClerkAdd;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.EmployeeMember;
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

    @JsonProperty("employeeMember")
    private EmployeeMember employeeMember;
    @JsonProperty("clerkAdd")
    private ClerkAdd clerkAdd;

}
