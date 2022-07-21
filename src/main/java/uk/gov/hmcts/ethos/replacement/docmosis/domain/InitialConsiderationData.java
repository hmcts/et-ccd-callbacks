package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import java.util.List;

@SuperBuilder
@Data
public class InitialConsiderationData {

    @JsonProperty("caseNumber")
    private String caseNumber;

    @JsonProperty("issuesJurisdiction")
    private String issuesJurisdiction;

    @JsonProperty("canProceed")
    private String canProceed;

    @JsonProperty("hearingAlreadyListed")
    private String hearingAlreadyListed;

    @JsonProperty("hearingListed")
    private List<String> hearingListed;

    @JsonProperty("hearingPostpone")
    private String hearingPostpone;

    @JsonProperty("hearingExtend")
    private String hearingExtend;

    @JsonProperty("hearingConvertFinal")
    private String hearingConvertFinal;

    @JsonProperty("hearingConvertF2f")
    private String hearingConvertF2f;

    @JsonProperty("hearingOther")
    private String hearingOther;

    @JsonProperty("otherDirections")
    private String otherDirections;

    @JsonProperty("hearingNotListed")
    private List<String> hearingNotListed;


    void testCCD(CaseData caseData){
        caseData.getEtICHearingNotListedList();

    }

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
