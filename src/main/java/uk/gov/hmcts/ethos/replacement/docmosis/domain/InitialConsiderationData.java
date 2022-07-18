package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.util.List;

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

    // Eng Wales

    @JsonProperty("icReceiptET3FormIssues")
    private String icReceiptET3FormIssues;

    @JsonProperty("icRespondentsNameIdentityIssues")
    private String icRespondentsNameIdentityIssues;

    @JsonProperty("icJurisdictionCodeIssues")
    private String icJurisdictionCodeIssues;

    @JsonProperty("icApplicationIssues")
    private String icApplicationIssues;

    @JsonProperty("icEmployersContractClaimIssues")
    private String icEmployersContractClaimIssues;

    @JsonProperty("icClaimProspectIssues")
    private String icClaimProspectIssues;

    @JsonProperty("icListingIssues")
    private String icListingIssues;

    @JsonProperty("icDdaDisabilityIssues")
    private String icDdaDisabilityIssues;

    @JsonProperty("icOrderForFurtherInformation")
    private String icOrderForFurtherInformation;

    @JsonProperty("icOtherIssuesOrFinalOrders")
    private String icOtherIssuesOrFinalOrders;

}
