package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class InitialConsiderationData {

    @JsonProperty("case_number")
    private String caseNumber;

    @JsonProperty("issues_jurisdiction")
    private String issuesJurisdiction;

    @JsonProperty("can_proceed")
    private String canProceed;

    @JsonProperty("hearing_already_listed")
    private String hearingAlreadyListed;

    @JsonProperty("hearing_listed")
    private List<String> hearingListed;

    @JsonProperty("hearing_postpone")
    private String hearingPostpone;

    @JsonProperty("hearing_extend")
    private String hearingExtend;

    @JsonProperty("hearing_convert_final")
    private String hearingConvertFinal;

    @JsonProperty("hearing_convert_f2f")
    private String hearingConvertF2f;

    @JsonProperty("hearing_other")
    private String hearingOther;

    @JsonProperty("other_directions")
    private String otherDirections;
}
