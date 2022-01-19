package uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class VenueAddress {
    @JsonProperty("venue")
    private String venue;

    @JsonProperty("address")
    private String address;
}
