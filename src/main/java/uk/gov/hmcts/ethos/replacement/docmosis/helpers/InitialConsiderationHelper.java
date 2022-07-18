package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.InitialConsiderationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.InitialConsiderationDocument;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

public class InitialConsiderationHelper {

    public String getDocumentRequest(CaseData caseData) throws JsonProcessingException {
        InitialConsiderationData data = InitialConsiderationData.builder()
            .caseNumber(nullCheck(caseData.getEthosCaseReference()))
            .issuesJurisdiction(nullCheck(caseData.getIcJurisdictionCodeIssues()))
            .hearingAlreadyListed(nullCheck(caseData.getEtICHearingAlreadyListed()))
            .hearingListed(caseData.getEtICHearingListed())
            .hearingPostpone(caseData.getEtICPostponeGiveDetails())
            .hearingConvertF2f(caseData.getEtICConvertF2fGiveDetails())
            //.hearingListed(caseData.getEtICHearingAlreadyListed())
            .build();

        InitialConsiderationDocument document = InitialConsiderationDocument.builder()
            .accessKey("")
            .outputName("")
            .templateName("")
            .data(data).build();

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(document);
    }

    public String getDocumentRequestEW(CaseData caseData) throws JsonProcessingException {
        InitialConsiderationData data = InitialConsiderationData.builder()
                .caseNumber(nullCheck(caseData.getEthosCaseReference()))
                .icReceiptET3FormIssues(nullCheck(caseData.getIcReceiptET3FormIssues()))
                .icRespondentsNameIdentityIssues(nullCheck(caseData.getIcRespondentsNameIdentityIssues()))
                .icJurisdictionCodeIssues(nullCheck(caseData.getIcJurisdictionCodeIssues()))
                .icApplicationIssues(nullCheck(caseData.getIcApplicationIssues()))
                .icEmployersContractClaimIssues(nullCheck(caseData.getIcEmployersContractClaimIssues()))
                .icClaimProspectIssues(nullCheck(caseData.getIcClaimProspectIssues()))
                .icListingIssues(nullCheck(caseData.getIcListingIssues()))
                .icDdaDisabilityIssues(nullCheck(caseData.getIcDdaDisabilityIssues()))
                .icOrderForFurtherInformation(nullCheck(caseData.getIcOrderForFurtherInformation()))
                .icOtherIssuesOrFinalOrders(nullCheck(caseData.getIcOtherIssuesOrFinalOrders()))
                .build();

        InitialConsiderationDocument document = InitialConsiderationDocument.builder()
                .accessKey("")
                .outputName("")
                .templateName("")
                .data(data).build();

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(document);
    }

}




