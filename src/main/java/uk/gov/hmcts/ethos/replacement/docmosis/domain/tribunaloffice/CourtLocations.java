package uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CourtLocations {

    @JsonProperty("name")
    private String name;

    @JsonProperty("region")
    private String region;

    @JsonProperty("epimmsId")
    private String epimmsId;

    @JsonProperty("regionId")
    private String regionId;
}
