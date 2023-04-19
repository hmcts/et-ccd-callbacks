package uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

//todo tidy
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationsResponse {
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "organisationIdentifier")
    private String organisationIdentifier;
    @JsonProperty(value = "contactInformation")
    private List<OrganisationAddress> contactInformation;
}
