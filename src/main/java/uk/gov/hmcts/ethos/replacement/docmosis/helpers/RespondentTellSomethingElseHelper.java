package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseApplicationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseApplicationDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String RES_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";

    private static final Map<String, Function<CaseData, TSEApplicationTypeData>>
            APPLICATION_TYPE_DATA_MAP = new ConcurrentHashMap<>();

    static {
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_AMEND_RESPONSE, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument1(), caseData.getResTseTextBox1()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_CHANGE_PERSONAL_DETAILS, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument2(), caseData.getResTseTextBox2()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_CLAIMANT_NOT_COMPLIED, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument3(), caseData.getResTseTextBox3()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_CONSIDER_A_DECISION_AFRESH, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument4(), caseData.getResTseTextBox4()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_CONTACT_THE_TRIBUNAL, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument5(), caseData.getResTseTextBox5()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_ORDER_OTHER_PARTY, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument6(), caseData.getResTseTextBox6()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument7(), caseData.getResTseTextBox7()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_POSTPONE_A_HEARING, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument8(), caseData.getResTseTextBox8()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_RECONSIDER_JUDGEMENT, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument9(), caseData.getResTseTextBox9()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_RESTRICT_PUBLICITY, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument10(), caseData.getResTseTextBox10()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument11(), caseData.getResTseTextBox11()));
        APPLICATION_TYPE_DATA_MAP.put(TSE_APP_VARY_OR_REVOKE_AN_ORDER, caseData ->
                new TSEApplicationTypeData(caseData.getResTseDocument12(), caseData.getResTseTextBox12()));
    }

    private RespondentTellSomethingElseHelper() {
    }

    public static String getDocumentRequest(CaseData caseData, String accessKey)
            throws JsonProcessingException {

        TSEApplicationTypeData selectedAppData = getSelectedApplicationType(caseData);
        GenericTseApplicationTypeItem lastApp = getCurrentGenericTseApplicationTypeItem(caseData);

        TseApplicationData data = TseApplicationData.builder()
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

        TseApplicationDocument document = TseApplicationDocument.builder()
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
        String applicationType = caseData.getResTseSelectApplication();
        Function<CaseData, TSEApplicationTypeData> handler = APPLICATION_TYPE_DATA_MAP.get(applicationType);
        if (handler != null) {
            return handler.apply(caseData);
        } else {
            throw new IllegalArgumentException(String.format("Unexpected application type %s", applicationType));
        }
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
