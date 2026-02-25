package uk.gov.hmcts.ecm.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimantHearingPreference {
    @JsonProperty("claimant_hearing_panel_preference")
    private String claimantHearingPanelPreference;
    @JsonProperty("claimant_hearing_panel_preference_why")
    private String claimantHearingPanelPreferenceWhy;
}
