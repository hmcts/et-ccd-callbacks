package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.RespondentTellSomethingElseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.RespondentTellSomethingElseDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CLAIMANT_NOT_COMPLIED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONSIDER_A_DECISION_AFRESH;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONTACT_THE_TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_ORDER_OTHER_PARTY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_POSTPONE_A_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RECONSIDER_JUDGEMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_VARY_OR_REVOKE_AN_ORDER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.TSE_FILE_NAME;

public final class RespondentTellSomethingElseHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String RES_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";

    private RespondentTellSomethingElseHelper() {
    }

    public static String getDocumentRequest(CaseData caseData, String accessKey)
            throws JsonProcessingException {

        TSEApplicationTypeData selectedAppData = getSelectedApplicationType(caseData);
        GenericTseApplicationTypeItem lastApp = getCurrentGenericTseApplicationTypeItem(caseData);

        RespondentTellSomethingElseData data = RespondentTellSomethingElseData.builder()
            .resTseApplicant(RESPONDENT_TITLE)
            .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
            .resTseSelectApplication(defaultIfEmpty(caseData.getResTseSelectApplication(), null))
            .resTseApplicationDate(lastApp != null && StringUtils.isNotBlank(lastApp.getValue().getDate())
                ? lastApp.getValue().getDate() : UtilHelper.formatCurrentDate(LocalDate.now()))
            .resTseDocument(getDocumentName(selectedAppData))
            .resTseTextBox(getTextBoxDetails(selectedAppData))
            .resTseCopyToOtherPartyYesOrNo(defaultIfEmpty(caseData.getResTseCopyToOtherPartyYesOrNo(), null))
            .resTseCopyToOtherPartyTextArea(defaultIfEmpty(caseData.getResTseCopyToOtherPartyTextArea(), null))
            .build();

        RespondentTellSomethingElseDocument document = RespondentTellSomethingElseDocument.builder()
                .accessKey(accessKey)
                .outputName(TSE_FILE_NAME)
                .templateName(RES_TSE_TEMPLATE_NAME)
                .data(data).build();

        return OBJECT_MAPPER.writeValueAsString(document);

    }

    private static GenericTseApplicationTypeItem getCurrentGenericTseApplicationTypeItem(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return caseData.getGenericTseApplicationCollection()
            .get(caseData.getGenericTseApplicationCollection().size() - 1);
    }

    public static TSEApplicationTypeData getSelectedApplicationType(CaseData caseData) {
        return switch (caseData.getResTseSelectApplication()) {
            case TSE_APP_AMEND_RESPONSE -> new TSEApplicationTypeData(
                    caseData.getResTseDocument1(), caseData.getResTseTextBox1());
            case TSE_APP_CHANGE_PERSONAL_DETAILS -> new TSEApplicationTypeData(
                    caseData.getResTseDocument2(), caseData.getResTseTextBox2());
            case TSE_APP_CLAIMANT_NOT_COMPLIED -> new TSEApplicationTypeData(
                    caseData.getResTseDocument3(), caseData.getResTseTextBox3());
            case TSE_APP_CONSIDER_A_DECISION_AFRESH -> new TSEApplicationTypeData(
                    caseData.getResTseDocument4(), caseData.getResTseTextBox4());
            case TSE_APP_CONTACT_THE_TRIBUNAL -> new TSEApplicationTypeData(
                    caseData.getResTseDocument5(), caseData.getResTseTextBox5());
            case TSE_APP_ORDER_OTHER_PARTY -> new TSEApplicationTypeData(
                    caseData.getResTseDocument6(), caseData.getResTseTextBox6());
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE -> new TSEApplicationTypeData(
                    caseData.getResTseDocument7(), caseData.getResTseTextBox7());
            case TSE_APP_POSTPONE_A_HEARING -> new TSEApplicationTypeData(
                    caseData.getResTseDocument8(), caseData.getResTseTextBox8());
            case TSE_APP_RECONSIDER_JUDGEMENT -> new TSEApplicationTypeData(
                    caseData.getResTseDocument9(), caseData.getResTseTextBox9());
            case TSE_APP_RESTRICT_PUBLICITY -> new TSEApplicationTypeData(
                    caseData.getResTseDocument10(), caseData.getResTseTextBox10());
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM -> new TSEApplicationTypeData(
                    caseData.getResTseDocument11(), caseData.getResTseTextBox11());
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER -> new TSEApplicationTypeData(
                    caseData.getResTseDocument12(), caseData.getResTseTextBox12());
            default -> throw new IllegalArgumentException(String.format("Unexpected application type %s",
                    caseData.getResTseSelectApplication()));
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

}
