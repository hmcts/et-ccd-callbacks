package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.generic.GenericCallbackResponse;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDCallbackResponse extends GenericCallbackResponse {

    private CaseData data;
    private Map<String, Object> dataClassification;
    private String securityClassification;
    private String errorMessageOverride;

    public CCDCallbackResponse(CaseData data) {
        this.data = data;
    }

    public String getConfirmationHeader() {
        return this.getConfirmation_header();
    }

    public String getConfirmationBody() {
        return this.getConfirmation_body();
    }
}
