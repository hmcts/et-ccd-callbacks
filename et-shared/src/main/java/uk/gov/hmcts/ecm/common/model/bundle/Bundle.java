package uk.gov.hmcts.ecm.common.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Jacksonized public record Bundle(
        BundleDetails value) {

}