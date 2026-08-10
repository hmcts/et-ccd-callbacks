package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;

import java.util.UUID;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Judgment", generate = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JudgementTypeItem {

    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private JudgementType value;

    public static JudgementTypeItem from(JudgementType judgementType) {
        JudgementTypeItem judgementTypeItem = new JudgementTypeItem();

        judgementTypeItem.setId(UUID.randomUUID().toString());
        judgementTypeItem.setValue(judgementType);

        return judgementTypeItem;
    }
}
