package uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.et.common.model.ccd.GenericTypeCaseDetails;
import uk.gov.hmcts.et.common.model.generic.GenericRequest;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreHearingDepositRequest extends GenericRequest {

    @JsonProperty("case_details")
    private GenericTypeCaseDetails<PreHearingDepositData> caseDetails;

}
