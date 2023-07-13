package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.RespondentTellSomethingElseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.RespondentTellSomethingElseDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentTSEApplicationTypeData;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.tuple.ImmutablePair.of;
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

        RespondentTSEApplicationTypeData selectedAppData = getSelectedApplicationType(caseData);

        RespondentTellSomethingElseData data = RespondentTellSomethingElseData.builder()
                .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
                .resTseSelectApplication(defaultIfEmpty(caseData.getResTseSelectApplication(), null))
                .resTseDocument(getDocumentName(selectedAppData))
                .resTseTextBox(getTextBoxDetails(selectedAppData))
                .build();

        RespondentTellSomethingElseDocument document = RespondentTellSomethingElseDocument.builder()
                .accessKey(accessKey)
                .outputName(TSE_FILE_NAME)
                .templateName(RES_TSE_TEMPLATE_NAME)
                .data(data).build();

        return OBJECT_MAPPER.writeValueAsString(document);

    }

    public static RespondentTSEApplicationTypeData getSelectedApplicationType(CaseData caseData) {
        String resTseSelectApplication = caseData.getResTseSelectApplication();
        var pair = switch (resTseSelectApplication) {
            case TSE_APP_AMEND_RESPONSE -> of(caseData.getResTseDocument1(),
                caseData.getResTseTextBox1());
            case TSE_APP_CHANGE_PERSONAL_DETAILS -> of(caseData.getResTseDocument2(), caseData.getResTseTextBox2());
            case TSE_APP_CLAIMANT_NOT_COMPLIED -> of(caseData.getResTseDocument3(), caseData.getResTseTextBox3());
            case TSE_APP_CONSIDER_A_DECISION_AFRESH -> of(caseData.getResTseDocument4(), caseData.getResTseTextBox4());
            case TSE_APP_CONTACT_THE_TRIBUNAL -> of(caseData.getResTseDocument5(), caseData.getResTseTextBox5());
            case TSE_APP_ORDER_OTHER_PARTY -> of(caseData.getResTseDocument6(), caseData.getResTseTextBox6());
            case TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE ->
                of(caseData.getResTseDocument7(), caseData.getResTseTextBox7());
            case TSE_APP_POSTPONE_A_HEARING -> of(caseData.getResTseDocument8(), caseData.getResTseTextBox8());
            case TSE_APP_RECONSIDER_JUDGEMENT -> of(caseData.getResTseDocument9(), caseData.getResTseTextBox9());
            case TSE_APP_RESTRICT_PUBLICITY -> of(caseData.getResTseDocument10(), caseData.getResTseTextBox10());
            case TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM ->
                of(caseData.getResTseDocument11(), caseData.getResTseTextBox11());
            case TSE_APP_VARY_OR_REVOKE_AN_ORDER -> of(caseData.getResTseDocument12(), caseData.getResTseTextBox12());
            default -> throw new IllegalArgumentException(String.format("Unexpected application type %s",
                resTseSelectApplication));
        };
        return new RespondentTSEApplicationTypeData(pair.left, pair.right);
    }

    private static String getDocumentName(RespondentTSEApplicationTypeData selectedAppData) {
        if (selectedAppData == null || selectedAppData.getResTseDocument() == null) {
            return null;
        }

        return selectedAppData.getResTseDocument().getDocumentFilename();
    }

    private static String getTextBoxDetails(RespondentTSEApplicationTypeData selectedAppData) {
        if (selectedAppData == null) {
            return "";
        }

        return selectedAppData.getSelectedTextBox();
    }

}
