package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICSeekComments;
import uk.gov.hmcts.et.common.model.ccd.EtIcudlHearing;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule27;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule28;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.InitialConsiderationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.InitialConsiderationDocument;

import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
public class InitialConsiderationHelper {

    private static final String IC_OUTPUT_NAME = "Initial Consideration.pdf";
    private static final String IC_SUMMARY_EW_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02203.docx";
    private static final String IC_SUMMARY_SC_TEMPLATE_NAME = "EM-TRB-SCO-ENG-02204.docx";

    private InitialConsiderationHelper() {
    }

    public static String getDocumentRequestSC(CaseData caseData, String accessKey) throws JsonProcessingException {
        InitialConsiderationData data = InitialConsiderationData.builder()
                .caseNumber(nullChecker(caseData.getEthosCaseReference()))
                .issuesJurisdiction(nullChecker(caseData.getEtICJuridictionCodesInvalid()))
                .canProceed(nullChecker(caseData.getEtICCanProceed()))
                .hearingAlreadyListed(nullChecker(caseData.getEtICHearingAlreadyListed()))
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
                        .map(EtIcudlHearing::getEtIcejSitAlone).orElse(null))
                .udlReasons(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlGiveReasons).orElse(null))
                .udlDisputeOnFacts(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlDisputeOnFacts).orElse(null))
                .udlLittleOrNoAgreement(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlLittleOrNoAgreement).orElse(null))
                .udlIssueOfLawArising(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlIssueOfLawArising).orElse(null))
                .udlViewsOfParties(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlViewsOfParties).orElse(null))
                .udlNoViewsExpressedByParties(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlNoViewsExpressedByParties).orElse(null))
                .udlConcurrentProceedings(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlConcurrentProceedings).orElse(null))
                .udlOther(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlOther).orElse(null))
                .udlHearingFormat(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlHearFormat).orElse(null))
                .udlCVPIssue(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlCvpIssue).orElse(null))
                .udlFinalF2FIssue(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcudlFinalF2FIssue).orElse(null))
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
                .outputName(IC_OUTPUT_NAME)
                .templateName(IC_SUMMARY_SC_TEMPLATE_NAME)
                .data(data).build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        log.info(mapper.writeValueAsString(data));

        return mapper.writeValueAsString(document);
    }

    public static String getDocumentRequestEW(CaseData caseData, String accessKey) throws JsonProcessingException {
        InitialConsiderationData data = InitialConsiderationData.builder()
                .caseNumber(nullChecker(caseData.getEthosCaseReference()))
                .icReceiptET3FormIssues(nullChecker(caseData.getIcReceiptET3FormIssues()))
                .icRespondentsNameIdentityIssues(nullChecker(caseData.getIcRespondentsNameIdentityIssues()))
                .icJurisdictionCodeIssues(nullChecker(caseData.getIcJurisdictionCodeIssues()))
                .icApplicationIssues(nullChecker(caseData.getIcApplicationIssues()))
                .icEmployersContractClaimIssues(nullChecker(caseData.getIcEmployersContractClaimIssues()))
                .icClaimProspectIssues(nullChecker(caseData.getIcClaimProspectIssues()))
                .icListingIssues(nullChecker(caseData.getIcListingIssues()))
                .icDdaDisabilityIssues(nullChecker(caseData.getIcDdaDisabilityIssues()))
                .icOrderForFurtherInformation(nullChecker(caseData.getIcOrderForFurtherInformation()))
                .icOtherIssuesOrFinalOrders(nullChecker(caseData.getIcOtherIssuesOrFinalOrders()))
                .build();

        InitialConsiderationDocument document = InitialConsiderationDocument.builder()
                .accessKey(accessKey)
                .outputName(IC_OUTPUT_NAME)
                .templateName(IC_SUMMARY_EW_TEMPLATE_NAME)
                .data(data).build();

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(document);
    }

    private static String nullChecker(String value) {
        return isNullOrEmpty(value)
                ? null
                : value;
    }
}