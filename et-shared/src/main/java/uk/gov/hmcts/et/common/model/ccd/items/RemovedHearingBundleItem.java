package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "RemovedHearingBundle", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class RemovedHearingBundleItem {
    @CCD(label = "Document Name", searchable = false)
    @JsonProperty("bundleName")
    private String bundleName;

    @CCD(label = "Removed Date", searchable = false)
    @JsonProperty("removedDateTime")
    private String removedDateTime;

    @CCD(label = "Reason for removal", searchable = false)
    @JsonProperty("removedReason")
    private String removedReason;
}
