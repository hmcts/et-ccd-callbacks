package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICSeekComments;
import uk.gov.hmcts.et.common.model.ccd.EtICUDLHearing;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule27;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule28;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.InitialConsiderationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.InitialConsiderationDocument;

import java.util.Optional;

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
            .hearingNotListed(caseData.getEtICHearingNotListedList())
            //cvp
            .cvpHearingType(Optional.ofNullable(caseData.getEtICHearingNotListedSeekComments())
                .map(EtICSeekComments::getEtICTypeOfCvpHearing).orElse(null))
            .cvpFinalDetails(Optional.ofNullable(caseData.getEtICHearingNotListedSeekComments())
                .map(EtICSeekComments::getEtICFinalHearingDetails).orElse(null))
            .cvpPreliminaryDetails(Optional.ofNullable(caseData.getEtICHearingNotListedSeekComments())
                .map(EtICSeekComments::getEtICPrelimHearingDetails).orElse(null))
            //preliminary
            .preliminaryHearingType(
                Optional.ofNullable(caseData.getEtICHearingNotListedListForPrelimHearing())
                    .map(EtICListForPreliminaryHearing::getEtICTypeOfPreliminaryHearing).orElse(null))
            .preliminaryHearingPurpose(Optional.ofNullable(caseData.getEtICHearingNotListedListForPrelimHearing())
                .map(EtICListForPreliminaryHearing::getEtICPurposeOfPreliminaryHearing).orElse(null))
            .preliminaryHearingNotice(Optional.ofNullable(caseData.getEtICHearingNotListedListForPrelimHearing())
                .map(EtICListForPreliminaryHearing::getEtICGiveDetailsOfHearingNotice).orElse(null))
            .preliminaryHearingLength(Optional.ofNullable(caseData.getEtICHearingNotListedListForPrelimHearing())
                .map(EtICListForPreliminaryHearing::getEtICLengthOfPrelimHearing).orElse(null))
            .finalHearingType(Optional.ofNullable(caseData.getEtICHearingNotListedListForFinalHearing())
                .map(EtICListForFinalHearing::getEtICTypeOfFinalHearing).orElse(null))
            .finalHearingLength(Optional.ofNullable(caseData.getEtICHearingNotListedListForFinalHearing())
                .map(EtICListForFinalHearing::getEtICLengthOfFinalHearing).orElse(null))
            //udl
            .udlSitAlone(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICEJSitAlone).orElse(null))
            .udlReasons(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLGiveReasons).orElse(null))
            .udlDisputeOnFacts(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLDisputeOnFacts).orElse(null))
            .udlLittleOrNoAgreement(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLLittleOrNoAgreement).orElse(null))
            .udlIssueOfLawArising(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLIssueOfLawArising).orElse(null))
            .udlViewsOfParties(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLViewsOfParties).orElse(null))
            .udlNoViewsExpressedByParties(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLNoViewsExpressedByParties).orElse(null))
            .udlConcurrentProceedings(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLConcurrentProceedings).orElse(null))
            .udlOther(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLOther).orElse(null))
            .udlHearingFormat(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLHearFormat).orElse(null))
            .udlCVPIssue(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLCVPIssue).orElse(null))
            .udlFinalF2FIssue(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                .map(EtICUDLHearing::getEtICUDLFinalF2FIssue).orElse(null))
            .otherDirections(caseData.getEtICHearingNotListedAnyOtherDirections())
            //further information
            .furtherInformation(caseData.getEtICFurtherInformation())
            .furtherInfoGiveDetails(caseData.getEtICFurtherInformationGiveDetails())
            .furtherInfoTimeToComply(caseData.getEtICFurtherInformationTimeToComply())
            .r27ClaimToBe(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                .map(EtInitialConsiderationRule27::getEtICRule27ClaimToBe).orElse(null))
            .r27WhichPart(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                .map(EtInitialConsiderationRule27::getEtICRule27WhichPart).orElse(null))
            .r27Direction(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                .map(EtInitialConsiderationRule27::getEtICRule27Direction).orElse(null))
            .r27DirectionReason(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                .map(EtInitialConsiderationRule27::getEtICRule27DirectionReason).orElse(null))
            .r27NumberOfDays(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                .map(EtInitialConsiderationRule27::getEtICRule27NumberOfDays).orElse(null))
            .r28ClaimToBe(Optional.ofNullable(caseData.getEtInitialConsiderationRule28())
                .map(EtInitialConsiderationRule28::getEtICRule28ClaimToBe).orElse(null))
            .r28WhichPart(Optional.ofNullable(caseData.getEtInitialConsiderationRule28())
                .map(EtInitialConsiderationRule28::getEtICRule28WhichPart).orElse(null))
            .r28DirectionReason(Optional.ofNullable(caseData.getEtInitialConsiderationRule28())
                .map(EtInitialConsiderationRule28::getEtICRule28DirectionReason).orElse(null))
            .r28NumberOfDays(Optional.ofNullable(caseData.getEtInitialConsiderationRule28())
                .map(EtInitialConsiderationRule28::getEtICRule28NumberOfDays).orElse(null))
            .furtherInfoAnyOtherDirections(caseData.getEtICFurtherInformationHearingAnyOtherDirections())
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



