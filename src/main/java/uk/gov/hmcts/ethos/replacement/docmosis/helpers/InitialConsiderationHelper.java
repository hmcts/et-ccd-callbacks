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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@Slf4j
@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.ClassNamingConventions", "PMD.PrematureDeclaration"})
public final class InitialConsiderationHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String IC_OUTPUT_NAME = "Initial Consideration.pdf";
    private static final String IC_SUMMARY_EW_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02203.docx";
    private static final String IC_SUMMARY_SC_TEMPLATE_NAME = "EM-TRB-SCO-ENG-02204.docx";

    private InitialConsiderationHelper() {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * This method generates the data in a JSON format stored in a String which allows Tornado to process the
     * information.
     * @param caseData contains all the case data
     * @param accessKey contains the authentication token
     * @param caseTypeId contains Case Type ID for England Wales or Scotland
     * @return will either return a list which contains an error message if no respondents were found or will return an
     *      empty list showing that there were no errors
     * @throws JsonProcessingException if the JSON cannot be generated correctly, an error would be thrown. This could
     *      be due to an illegal character potentially existing in the data
     */
    public static String getDocumentRequest(CaseData caseData, String accessKey, String caseTypeId)
            throws JsonProcessingException {
        if (caseTypeId.equals(ENGLANDWALES_CASE_TYPE_ID)) {
            return getDocumentRequestEW(caseData, accessKey);
        } else {
            return getDocumentRequestSC(caseData, accessKey);
        }
    }

    private static String getDocumentRequestSC(CaseData caseData, String accessKey) throws JsonProcessingException {
        InitialConsiderationData data = InitialConsiderationData.builder()
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .issuesJurisdiction(defaultIfEmpty(caseData.getEtICJuridictionCodesInvalid(), null))
                .issuesJurCodesGiveDetails(defaultIfEmpty(caseData.getEtICInvalidDetails(), null))
                .canProceed(defaultIfEmpty(caseData.getEtICCanProceed(), null))
                .hearingAlreadyListed(defaultIfEmpty(caseData.getEtICHearingAlreadyListed(), null))
                .hearingListed(Optional.ofNullable(caseData.getEtICHearingListed()).orElse(null))
                .hearingPostpone(defaultIfEmpty(caseData.getEtICPostponeGiveDetails(), null))
                .hearingConvertF2f(defaultIfEmpty(caseData.getEtICConvertF2fGiveDetails(), null))
                .hearingConvertFinal(defaultIfEmpty(caseData.getEtICConvertPreliminaryGiveDetails(), null))
                .hearingExtend(defaultIfEmpty(caseData.getEtICExtendDurationGiveDetails(), null))
                .hearingOther(defaultIfEmpty(caseData.getEtICOtherGiveDetails(), null))
                .otherDirections(defaultIfEmpty(caseData.getEtICHearingAnyOtherDirections(), null))
                .hearingNotListed(Optional.ofNullable(caseData.getEtICHearingNotListedList()).orElse(null))
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
                //final
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
                .udlCheckComplianceOrders(Optional.ofNullable(caseData.getEtICHearingNotListedUDLHearing())
                        .map(EtIcudlHearing::getEtIcbuCheckComplianceOrders).orElse(null))
                .hearingNotListedOtherDirections(
                        defaultIfEmpty(caseData.getEtICHearingNotListedAnyOtherDirections(), null))
                //further information
                .furtherInformation(Optional.ofNullable(caseData.getEtICFurtherInformation()).orElse(null))
                .furtherInfoGiveDetails(defaultIfEmpty(caseData.getEtICFurtherInformationGiveDetails(), null))
                .furtherInfoTimeToComply(defaultIfEmpty(caseData.getEtICFurtherInformationTimeToComply(), null))
                .r27ClaimToBe(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                        .map(EtInitialConsiderationRule27::getEtICRule27ClaimToBe).orElse(null))
                .r27WhichPart(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                        .map(EtInitialConsiderationRule27::getEtICRule27WhichPart).orElse(null))
                .r27Direction(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                        .map(EtInitialConsiderationRule27::getEtICRule27Direction).orElse(null))
                .r27NoJurisdictionReason(Optional.ofNullable(caseData.getEtInitialConsiderationRule27())
                        .map(EtInitialConsiderationRule27::getEtICRule27NoJurisdictionReason).orElse(null))
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
                .furtherInfoAnyOtherDirections(
                        defaultIfEmpty(caseData.getEtICFurtherInformationHearingAnyOtherDirections(), null))
                .icDateCompleted(
                        defaultIfEmpty(caseData.getIcDateCompleted(),
                                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MMM/yyyy"))))
                .icCompletedBy(
                        defaultIfEmpty(caseData.getIcCompletedBy(), null))
                .build();
        InitialConsiderationDocument document = InitialConsiderationDocument.builder()
                .accessKey(accessKey)
                .outputName(IC_OUTPUT_NAME)
                .templateName(IC_SUMMARY_SC_TEMPLATE_NAME)
                .data(data).build();

        return OBJECT_MAPPER.writeValueAsString(document);
    }

    private static String getDocumentRequestEW(CaseData caseData, String accessKey) throws JsonProcessingException {
        InitialConsiderationData data = InitialConsiderationData.builder()
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .icReceiptET3FormIssues(defaultIfEmpty(caseData.getIcReceiptET3FormIssues(), null))
                .icRespondentsNameIdentityIssues(defaultIfEmpty(caseData.getIcRespondentsNameIdentityIssues(), null))
                .icJurisdictionCodeIssues(defaultIfEmpty(caseData.getIcJurisdictionCodeIssues(), null))
                .icApplicationIssues(defaultIfEmpty(caseData.getIcApplicationIssues(), null))
                .icEmployersContractClaimIssues(defaultIfEmpty(caseData.getIcEmployersContractClaimIssues(), null))
                .icClaimProspectIssues(defaultIfEmpty(caseData.getIcClaimProspectIssues(), null))
                .icListingIssues(defaultIfEmpty(caseData.getIcListingIssues(), null))
                .icDdaDisabilityIssues(defaultIfEmpty(caseData.getIcDdaDisabilityIssues(), null))
                .icOrderForFurtherInformation(defaultIfEmpty(caseData.getIcOrderForFurtherInformation(), null))
                .icOtherIssuesOrFinalOrders(defaultIfEmpty(caseData.getIcOtherIssuesOrFinalOrders(), null))
                .icDateCompleted(
                        defaultIfEmpty(caseData.getIcDateCompleted(), LocalDate.now().toString()))
                .icCompletedBy(
                        defaultIfEmpty(caseData.getIcCompletedBy(), null))
                .build();

        InitialConsiderationDocument document = InitialConsiderationDocument.builder()
                .accessKey(accessKey)
                .outputName(IC_OUTPUT_NAME)
                .templateName(IC_SUMMARY_EW_TEMPLATE_NAME)
                .data(data).build();

        return OBJECT_MAPPER.writeValueAsString(document);
    }
}