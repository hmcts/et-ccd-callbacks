package uk.gov.hmcts.ecm.common.model.servicebus.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMultiplesDataModel extends DataModelParent {

    /**
     * The single claimant whose details should override the lead case when creating the new case.
     * One message (and therefore one model instance) is sent per additional claimant,
     * regardless of whether the claimants were entered manually or via spreadsheet upload.
     */
    @JsonProperty("additionalClaimant")
    private AdditionalClaimant additionalClaimant;
}
