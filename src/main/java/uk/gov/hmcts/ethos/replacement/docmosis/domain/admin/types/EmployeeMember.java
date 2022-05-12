package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EmployeeMember {

    @JsonProperty("tribunalOffice")
    private String tribunalOffice;
    @JsonProperty("employeeMemberCode")
    private String employeeMemberCode;
    @JsonProperty("employeeMemberName")
    private String employeeMemberName;

}
