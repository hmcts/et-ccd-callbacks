package uk.gov.hmcts.ecm.common.model.servicebus.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMultiplesDataModel extends DataModelParent {

    /**
     * A single additional claimant. Retained for backward compatibility; the current flow sends one
     * message per multiple carrying every claimant in {@link #additionalClaimants} instead.
     */
    @JsonProperty("additionalClaimant")
    private AdditionalClaimant additionalClaimant;

    /**
     * All additional claimants for the multiple. A single {@code CreateUpdatesMsg} carries the full
     * list so that {@code CreateUpdatesQueueProcessor} can create every child case and the multiple
     * shell from one message.
     */
    @JsonProperty("additionalClaimants")
    private List<AdditionalClaimant> additionalClaimants;
}
