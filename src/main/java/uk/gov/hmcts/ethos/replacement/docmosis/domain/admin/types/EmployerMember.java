package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EmployerMember {

    @JsonProperty("tribunalOffice")
    private String tribunalOffice;
    @JsonProperty("employerMemberCode")
    private String employerMemberCode;
    @JsonProperty("employerMemberName")
    private String employerMemberName;

}
