package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public class ScotlandCaseData extends CaseData implements HasFlagLauncher, HasLinkedCasesComponentLauncher {

    @JsonProperty("flagLauncher")
    @CCD(label = "Flag Launcher", typeOverride = FieldType.FlagLauncher, searchable = false)
    private String flagLauncher;

    @JsonProperty("LinkedCasesComponentLauncher")
    @CCD(
        label = "Component Launcher (for displaying Linked Cases data)",
        typeOverride = FieldType.ComponentLauncher,
        liveFrom = "10/02/2023",
        searchable = false
    )
    private String linkedCasesComponentLauncher;

    public String getFlagLauncher() {
        return flagLauncher;
    }

    public void setFlagLauncher(String flagLauncher) {
        this.flagLauncher = flagLauncher;
    }

    public String getLinkedCasesComponentLauncher() {
        return linkedCasesComponentLauncher;
    }

    public void setLinkedCasesComponentLauncher(String linkedCasesComponentLauncher) {
        this.linkedCasesComponentLauncher = linkedCasesComponentLauncher;
    }
}
