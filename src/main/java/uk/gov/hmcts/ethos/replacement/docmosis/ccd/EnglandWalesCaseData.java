package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public class EnglandWalesCaseData extends CaseData {

    @JsonProperty("flagLauncher")
    @CCD(label = "Flag Launcher", typeOverride = FieldType.FlagLauncher)
    private String flagLauncher;

    public String getFlagLauncher() {
        return flagLauncher;
    }

    public void setFlagLauncher(String flagLauncher) {
        this.flagLauncher = flagLauncher;
    }
}
