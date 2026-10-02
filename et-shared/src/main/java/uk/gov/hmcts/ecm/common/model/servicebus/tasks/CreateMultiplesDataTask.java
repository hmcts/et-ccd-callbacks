package uk.gov.hmcts.ecm.common.model.servicebus.tasks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMultiplesDataTask extends DataTaskParent {

    public CreateMultiplesDataTask(DataModelParent dataModelParent) {
        super(dataModelParent);
    }

    /**
     * No-op: case creation for additional claimants in a multiple is handled by
     * {@code CreateMultiplesService} in the service layer, not via the runTask pattern.
     */
    @Override
    public void run(SubmitEvent submitEvent) {
        // intentionally empty
    }
}
