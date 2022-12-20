package uk.gov.hmcts.ethos.replacement.docmosis.domain.admin;

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
    private AdminData data;

    public static ResponseEntity<CCDCallbackResponse> getCallbackRespEntityNoErrors(AdminData adminData) {

        return ResponseEntity.ok(builder()
                .data(adminData)
                .build());
    }

    public static ResponseEntity<CCDCallbackResponse> getCallbackRespEntityErrors(
            List<String> errors, AdminData adminData) {

        return ResponseEntity.ok(builder()
                .data(adminData)
                .errors(errors)
                .build());
    }

}
