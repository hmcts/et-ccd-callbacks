package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.InitialConsiderationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.InitialConsiderationDocument;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Slf4j
public class InitialConsiderationHelper {

    private static final String IC_SUMMARY_FILENAME = "InitialConsideration.pdf";
    private static final String IC_SUMMARY_EW_TEMPLATE_NAME = "TEST-RET-2017.docx";
    private static final String IC_SUMMARY_SC_TEMPLATE_NAME = "InitialConsideration.docx";

    private InitialConsiderationHelper() {
    }

    public static String getDocumentRequestSC(CaseData caseData, String accessKey) throws JsonProcessingException {
        InitialConsiderationData data = InitialConsiderationData.builder()
            .caseNumber(nullCheck(caseData.getEthosCaseReference()))
            .issuesJurisdiction(nullCheck(caseData.getEtICJuridictionCodesInvalid()))
            .canProceed(nullCheck(caseData.getEtICCanProceed()))
            .hearingAlreadyListed(nullCheck(caseData.getEtICHearingAlreadyListed()))
            .hearingListed(caseData.getEtICHearingListed())
            .hearingPostpone(caseData.getEtICPostponeGiveDetails())
            .hearingConvertF2f(caseData.getEtICConvertF2fGiveDetails())
            .hearingConvertFinal(caseData.getEtICConvertPreliminaryGiveDetails())
            .hearingExtend(caseData.getEtICExtendDurationGiveDetails())
            .hearingOther(caseData.getEtICExtendDurationGiveDetails())
            .otherDirections(caseData.getEtICHearingAnyOtherDirections())
            .build();

        InitialConsiderationDocument document = InitialConsiderationDocument.builder()
            .accessKey(accessKey)
            .outputName(IC_SUMMARY_FILENAME)
            .templateName(IC_SUMMARY_SC_TEMPLATE_NAME)
            .data(data).build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        log.info(mapper.writeValueAsString(data));

        return mapper.writeValueAsString(document);
    }

    public static String getDocumentRequestEW(CaseData caseData, String accessKey) throws JsonProcessingException {
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
            .accessKey(accessKey)
            .outputName(IC_SUMMARY_FILENAME)
            .templateName(IC_SUMMARY_EW_TEMPLATE_NAME)
            .data(data).build();

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(document);
    }
}



