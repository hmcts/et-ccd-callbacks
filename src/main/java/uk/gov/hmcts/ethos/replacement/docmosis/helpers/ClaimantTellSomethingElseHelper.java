package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseApplicationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseApplicationDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_AMEND_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_CONSIDER_DECISION_AFRESH;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_CONTACT_THE_TRIBUNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_ORDER_OTHER_PARTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_POSTPONE_A_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_RECONSIDER_JUDGMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.CLAIMANT_TSE_FILE_NAME;

public final class ClaimantTellSomethingElseHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String CLAIMANT_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";

    private ClaimantTellSomethingElseHelper() {
    }

    public static String getDocumentRequest(CaseData caseData, String accessKey)
            throws JsonProcessingException {

        TSEApplicationTypeData selectedAppData = getSelectedApplicationType(caseData);
        GenericTseApplicationTypeItem lastApp = getCurrentGenericTseApplicationTypeItem(caseData);

        TseApplicationData data = TseApplicationData.builder()
                .resTseApplicant(CLAIMANT_TITLE)
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .resTseSelectApplication(defaultIfEmpty(caseData.getClaimantTseSelectApplication(), null))
                .resTseApplicationDate(lastApp != null && StringUtils.isNotBlank(lastApp.getValue().getDate())
                        ? lastApp.getValue().getDate() : UtilHelper.formatCurrentDate(LocalDate.now()))
                .resTseDocument(getDocumentName(selectedAppData))
                .resTseTextBox(getTextBoxDetails(selectedAppData))
                .resTseCopyToOtherPartyYesOrNo(defaultIfEmpty(caseData.getClaimantTseRule92(), null))
                .resTseCopyToOtherPartyTextArea(defaultIfEmpty(caseData.getClaimantTseRule92AnsNoGiveDetails(), null))
                .build();

        TseApplicationDocument document = TseApplicationDocument.builder()
                .accessKey(accessKey)
                .outputName(CLAIMANT_TSE_FILE_NAME)
                .templateName(CLAIMANT_TSE_TEMPLATE_NAME)
                .data(data).build();

        return OBJECT_MAPPER.writeValueAsString(document);
    }

    public static TSEApplicationTypeData getSelectedApplicationType(CaseData caseData) {
        return switch (caseData.getClaimantTseSelectApplication()) {
            case CLAIMANT_TSE_AMEND_CLAIM -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument1(), caseData.getClaimantTseTextBox1());
            case CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument2(), caseData.getClaimantTseTextBox2());
            case CLAIMANT_TSE_CONSIDER_DECISION_AFRESH -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument3(), caseData.getClaimantTseTextBox3());
            case CLAIMANT_TSE_CONTACT_THE_TRIBUNAL -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument4(), caseData.getClaimantTseTextBox4());
            case CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument5(), caseData.getClaimantTseTextBox5());
            case CLAIMANT_TSE_ORDER_OTHER_PARTY -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument6(), caseData.getClaimantTseTextBox6());
            case CLAIMANT_TSE_POSTPONE_A_HEARING -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument7(), caseData.getClaimantTseTextBox7());
            case CLAIMANT_TSE_RECONSIDER_JUDGMENT -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument8(), caseData.getClaimantTseTextBox8());
            case CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument9(), caseData.getClaimantTseTextBox9());
            case CLAIMANT_TSE_RESTRICT_PUBLICITY -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument10(), caseData.getClaimantTseTextBox10());
            case CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument11(), caseData.getClaimantTseTextBox11());
            case CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument12(), caseData.getClaimantTseTextBox12());
            case CLAIMANT_TSE_WITHDRAW_CLAIM -> new TSEApplicationTypeData(
                    caseData.getClaimantTseDocument13(), caseData.getClaimantTseTextBox13());
            default -> throw new IllegalArgumentException(String.format("Unexpected application type %s",
                    caseData.getResTseSelectApplication()));
        };
    }

    public static String claimantSelectApplicationToType(String selectApplication) {
        return switch (selectApplication) {
            case CLAIMANT_TSE_AMEND_CLAIM -> "amend";
            case CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS -> "change-details";
            case CLAIMANT_TSE_CONSIDER_DECISION_AFRESH -> "reconsider-decision";
            case CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND -> "witness";
            case CLAIMANT_TSE_ORDER_OTHER_PARTY -> "respondent";
            case CLAIMANT_TSE_POSTPONE_A_HEARING -> "postpone";
            case CLAIMANT_TSE_RECONSIDER_JUDGMENT -> "reconsider-judgement";
            case CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED -> "non-compliance";
            case CLAIMANT_TSE_RESTRICT_PUBLICITY -> "publicity";
            case CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART -> "strike";
            case CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER -> "vary";
            case CLAIMANT_TSE_WITHDRAW_CLAIM -> "withdraw";
            case CLAIMANT_TSE_CONTACT_THE_TRIBUNAL -> "other";
            default ->
                    throw new IllegalArgumentException(String.format("Unexpected application type %s", selectApplication));
        };
    }

    private static String getDocumentName(TSEApplicationTypeData selectedAppData) {
        if (selectedAppData == null || selectedAppData.getUploadedTseDocument() == null) {
            return null;
        }
        return selectedAppData.getUploadedTseDocument().getDocumentFilename();
    }

    private static String getTextBoxDetails(TSEApplicationTypeData selectedAppData) {
        if (selectedAppData == null) {
            return "";
        }
        return selectedAppData.getSelectedTextBox();
    }

    private static GenericTseApplicationTypeItem getCurrentGenericTseApplicationTypeItem(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }
        return caseData.getGenericTseApplicationCollection()
                .get(caseData.getGenericTseApplicationCollection().size() - 1);
    }
}
