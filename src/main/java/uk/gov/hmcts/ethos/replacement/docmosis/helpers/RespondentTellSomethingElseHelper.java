package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.RespondentTellSomethingElseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.RespondentTellSomethingElseDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentTSEApplicationTypeData;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

public class RespondentTellSomethingElseHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String RES_TSE_FILE_NAME = "resTse.pdf";
    private static final String RES_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";

    private static final String SELECTED_APP_AMEND_RESPONSE = "Amend response";
    private static final String SELECTED_APP_CHANGE_PERSONAL_DETAILS = "Change personal details";
    private static final String SELECTED_APP_CLAIMANT_NOT_COMPLIED = "Claimant not complied";
    private static final String SELECTED_APP_CONSIDER_A_DECISION_AFRESH = "Consider a decision afresh";
    private static final String SELECTED_APP_CONTACT_THE_TRIBUNAL = "Contact the tribunal";
    private static final String SELECTED_APP_ORDER_OTHER_PARTY = "Order other party";
    private static final String SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE =
            "Order a witness to attend to give evidence";
    private static final String SELECTED_APP_POSTPONE_A_HEARING = "Postpone a hearing";
    private static final String SELECTED_APP_RECONSIDER_JUDGEMENT = "Reconsider judgement";
    private static final String SELECTED_APP_RESTRICT_PUBLICITY = "Restrict publicity";
    private static final String SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM = "Strike out all or part of a claim";
    private static final String SELECTED_APP_VARY_OR_REVOKE_AN_ORDER = "Vary or revoke an order";

    private RespondentTellSomethingElseHelper() {
    }

    public static String getDocumentRequest(CaseData caseData, String accessKey)
            throws JsonProcessingException {

        RespondentTSEApplicationTypeData selectedAppData = getSelectedAppAppType(caseData);

        RespondentTellSomethingElseData data = RespondentTellSomethingElseData.builder()
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .resTseSelectApplication(defaultIfEmpty(caseData.getResTseSelectApplication(), null))
                .resTseDocument(getDocumentName(selectedAppData))
                .resTseTextBox(getTextBoxDetails(selectedAppData))
                .build();

        RespondentTellSomethingElseDocument document = RespondentTellSomethingElseDocument.builder()
                .accessKey(accessKey)
                .outputName(RES_TSE_FILE_NAME)
                .templateName(RES_TSE_TEMPLATE_NAME)
                .data(data).build();

        return OBJECT_MAPPER.writeValueAsString(document);

    }

    public static RespondentTSEApplicationTypeData getSelectedAppAppType(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument1(), caseData.getResTseTextBox1());
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument2(), caseData.getResTseTextBox2());
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument3(), caseData.getResTseTextBox3());
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument4(), caseData.getResTseTextBox4());
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument5(), caseData.getResTseTextBox5());
            case SELECTED_APP_ORDER_OTHER_PARTY:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument6(), caseData.getResTseTextBox6());
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument7(), caseData.getResTseTextBox7());
            case SELECTED_APP_POSTPONE_A_HEARING:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument8(), caseData.getResTseTextBox8());
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument9(), caseData.getResTseTextBox9());
            case SELECTED_APP_RESTRICT_PUBLICITY:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument10(), caseData.getResTseTextBox10());
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument11(), caseData.getResTseTextBox11());
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                return new RespondentTSEApplicationTypeData(
                        caseData.getResTseDocument12(), caseData.getResTseTextBox12());
            default:
                return null;
        }
    }

    private static String getDocumentName(RespondentTSEApplicationTypeData selectedAppData) {
        if (selectedAppData != null && selectedAppData.getResTseDocument() != null) {
            return selectedAppData.getResTseDocument().getDocumentFilename();
        } else {
            return null;
        }
    }

    private static String getTextBoxDetails(RespondentTSEApplicationTypeData selectedAppData) {
        if (selectedAppData != null) {
            return selectedAppData.getSelectedTextBox();
        } else {
            return null;
        }
    }

}
