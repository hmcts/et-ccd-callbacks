package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClerkAdd {
    @JsonProperty("tribunalOffice")
    private String tribunalOffice;
    @JsonProperty("clerkCode")
    private String clerkCode;
    @JsonProperty("clerkName")
    private String clerkName;
}
