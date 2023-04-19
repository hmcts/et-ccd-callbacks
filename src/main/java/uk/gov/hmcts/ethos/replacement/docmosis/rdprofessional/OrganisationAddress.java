package uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OrganisationAddress {
    @JsonProperty("addressLine1")
    private String addressLine1;
    @JsonProperty("addressLine2")
    private String addressLine2;
    @JsonProperty("addressLine3")
    private String addressLine3;
    @JsonProperty("townCity")
    private String townCity;
    @JsonProperty("county")
    private String county;
    @JsonProperty("country")
    private String country;
    @JsonProperty("postCode")
    private String postCode;
    // todo add dx array of something
}

