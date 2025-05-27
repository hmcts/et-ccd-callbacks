package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseApplicationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseApplicationDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEApplicationTypeData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper.getRespondentRepresentative;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getRespondentSelectedApplicationType;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.CLAIMANT_TSE_FILE_NAME;

public final class ClaimantTellSomethingElseHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String CLAIMANT_TSE_TEMPLATE_NAME = "EM-TRB-EGW-ENG-02822.docx";
    private static final String EMPTY_TABLE_MESSAGE = "There are no applications to view";
    private static final String TABLE_COLUMNS_MARKDOWN = """
                        | No | Application type | Applicant | Application date | Response due | Number of responses |
                        | Status |
                        |:---------|:---------|:---------|:---------|:---------|:---------|:---------|
                        %s
                        """;
    private static final String TABLE_ROW_MARKDOWN = "|%s|%s|%s|%s|%s|%s|%s|\r\n";
    private static final Map<String, Function<CaseData, TSEApplicationTypeData>>
            APPLICATION_TYPE_DATA_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> APPLICATION_TYPE_MAP = new ConcurrentHashMap<>();

    static {
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_AMEND_CLAIM, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument1(), caseData.getClaimantTseTextBox1()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument2(), caseData.getClaimantTseTextBox2()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument3(), caseData.getClaimantTseTextBox3()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument4(), caseData.getClaimantTseTextBox4()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument5(), caseData.getClaimantTseTextBox5()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_ORDER_OTHER_PARTY, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument6(), caseData.getClaimantTseTextBox6()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_POSTPONE_A_HEARING, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument7(), caseData.getClaimantTseTextBox7()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_RECONSIDER_JUDGMENT, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument8(), caseData.getClaimantTseTextBox8()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument9(), caseData.getClaimantTseTextBox9()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_RESTRICT_PUBLICITY, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument10(), caseData.getClaimantTseTextBox10()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument11(), caseData.getClaimantTseTextBox11()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument12(), caseData.getClaimantTseTextBox12()));
        APPLICATION_TYPE_DATA_MAP.put(CLAIMANT_TSE_WITHDRAW_CLAIM, caseData ->
                new TSEApplicationTypeData(caseData.getClaimantTseDocument13(), caseData.getClaimantTseTextBox13()));
    }

    static {
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_AMEND_CLAIM, "amend");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, "change-details");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH, "reconsider-decision");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND, "witness");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_ORDER_OTHER_PARTY, "respondent");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_POSTPONE_A_HEARING, "postpone");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RECONSIDER_JUDGMENT, "reconsider-judgement");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED, "non-compliance");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RESTRICT_PUBLICITY, "publicity");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART, "strike");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER, "vary");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_WITHDRAW_CLAIM, "withdraw");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL, "other");
    }

    private ClaimantTellSomethingElseHelper() {
    }

    public static TSEApplicationTypeData getSelectedApplicationType(CaseData caseData) {
        String applicationType = caseData.getClaimantTseSelectApplication();
        Function<CaseData, TSEApplicationTypeData> handler = APPLICATION_TYPE_DATA_MAP.get(applicationType);
        if (handler != null) {
            return handler.apply(caseData);
        } else {
            throw new IllegalArgumentException(String.format("Unexpected application type %s", applicationType));
        }
    }

    public static String claimantSelectApplicationToType(String selectApplication) {
        String type = APPLICATION_TYPE_MAP.get(selectApplication);
        if (type != null) {
            return type;
        } else {
            throw new IllegalArgumentException(String.format("Unexpected application type %s", selectApplication));
        }
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
        if (isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }
        return caseData.getGenericTseApplicationCollection()
                .get(caseData.getGenericTseApplicationCollection().size() - 1);
    }

    public static String generateClaimantRepApplicationMarkdown(CaseData caseData) {
        List<GenericTseApplicationTypeItem> genericApplicationList = caseData.getGenericTseApplicationCollection();
        if (isEmpty(genericApplicationList)) {
            return EMPTY_TABLE_MESSAGE;
        }

        AtomicInteger applicationNumber = new AtomicInteger(1);

        String tableRows = genericApplicationList.stream()
                .filter(TseViewApplicationHelper::applicationsSharedWithClaimant)
                .map(o -> formatRow(o, applicationNumber))
                .collect(Collectors.joining());

        return String.format(TABLE_COLUMNS_MARKDOWN, tableRows);
    }

    private static String formatRow(GenericTseApplicationTypeItem genericTseApplicationTypeItem, AtomicInteger count) {
        GenericTseApplicationType value = genericTseApplicationTypeItem.getValue();
        int responses = value.getRespondCollection() == null ? 0 : value.getRespondCollection().size();
        String status = Optional.ofNullable(value.getStatus()).orElse(OPEN_STATE);

        return String.format(TABLE_ROW_MARKDOWN, count.getAndIncrement(), value.getType(), value.getApplicant(),
                value.getDate(), value.getDueDate(), responses, status);
    }

    /**
     * Retrieves a list of email addresses for respondents and their representatives from the given case data.
     *
     * @param caseData the case data containing respondent and representative information
     * @return a mapping of email addresses and respondent ids for respondents and their representatives
     */
    public static Map<String, String> getRespondentsAndRepsEmailAddresses(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
        Map<String, String> emailAddressesMap = new ConcurrentHashMap<>();

        respondentCollection.forEach(respondentSumTypeItem -> {
            RespondentSumType respondent = respondentSumTypeItem.getValue();
            String responseEmail = respondent.getResponseRespondentEmail();
            String respondentEmail = respondent.getRespondentEmail();

            if (StringUtils.isNotBlank(responseEmail)) {
                emailAddressesMap.put(responseEmail, respondentSumTypeItem.getId());
            } else if (StringUtils.isNotBlank(respondentEmail)) {
                emailAddressesMap.put(respondentEmail, respondentSumTypeItem.getId());
            }

            RepresentedTypeR representative = getRespondentRepresentative(caseData, respondent);
            if (representative != null && StringUtils.isNotBlank(representative.getRepresentativeEmailAddress())) {
                emailAddressesMap.put(representative.getRepresentativeEmailAddress(), "");
            }
        });

        return emailAddressesMap;
    }

    public static String getApplicantType(CaseData caseData) {
        GenericTseApplicationType selectedApplicationType = getRespondentSelectedApplicationType(caseData);
        return selectedApplicationType.getApplicant();
    }
}
