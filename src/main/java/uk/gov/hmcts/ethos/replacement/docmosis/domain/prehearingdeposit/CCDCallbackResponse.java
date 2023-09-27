package uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.generic.GenericCallbackResponse;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDCallbackResponse extends GenericCallbackResponse {
    private PreHearingDepositData data;

    public static ResponseEntity<CCDCallbackResponse> getCallbackRespEntityNoErrors(
            PreHearingDepositData preHearingDepositData) {

        return ResponseEntity.ok(builder()
                .data(preHearingDepositData)
                .build());
    }

    public static ResponseEntity<CCDCallbackResponse> getCallbackRespEntityErrors(
            List<String> errors, PreHearingDepositData preHearingDepositData) {

        return ResponseEntity.ok(builder()
                .data(preHearingDepositData)
                .errors(errors)
                .build());
    }

}
