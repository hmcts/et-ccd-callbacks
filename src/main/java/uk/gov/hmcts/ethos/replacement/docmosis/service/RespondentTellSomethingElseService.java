package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentTellSomethingElseService {
    private final EmailService emailService;
    private final UserService userService;
    private final TornadoService tornadoService;
    private final DocumentManagementService documentManagementService;

    @Value("${respondent.tse.template.id}")
    private String emailTemplateId;

    private static final String APPLICANT_CLAIMANT = "Claimant";

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

    private static final String GIVE_DETAIL_MISSING = "Use the text box or file upload to give details.";
    private static final String NO = "I do not want to copy";
    private static final String RULE92_ANSWERED_NO = "You have said that you do not want to copy this correspondence "
        + "to the other party. \n \n"
        + "The tribunal will consider all correspondence and let you know what happens next.";
    private static final String RULE92_ANSWERED_YES_GROUP_A = "The other party will be notified that any objections to "
        + "your %s application should be sent to the tribunal as soon as possible, and in any event "
        + "within 7 days.";
    private static final String RULE92_ANSWERED_YES_GROUP_B = "The other party is not expected to respond to this "
        + "application.\n \nHowever, they have been notified that any objections to your %s application should be "
        + "sent to the tribunal as soon as possible, and in any event within 7 days.";
    private static final String DOC_GEN_ERROR = "Failed to generate document for case id: %s";
    private static final String RES_TSE_FILE_NAME = "resTse.pdf";

    /**
     * Validate Give Details (free text box) or file upload is mandatory.
     * @param caseData in which the case details are extracted from
     * @return errors Error message
     */
    public List<String> validateGiveDetails(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (Boolean.TRUE.equals(checkSelectedAppGiveDetailsIsBlank(caseData))) {
            errors.add(GIVE_DETAIL_MISSING);
        }
        return errors;
    }

    private Boolean checkSelectedAppGiveDetailsIsBlank(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument1(), caseData.getResTseTextBox1());
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument2(), caseData.getResTseTextBox2());
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument3(), caseData.getResTseTextBox3());
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument4(), caseData.getResTseTextBox4());
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument5(), caseData.getResTseTextBox5());
            case SELECTED_APP_ORDER_OTHER_PARTY:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument6(), caseData.getResTseTextBox6());
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument7(), caseData.getResTseTextBox7());
            case SELECTED_APP_POSTPONE_A_HEARING:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument8(), caseData.getResTseTextBox8());
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument9(), caseData.getResTseTextBox9());
            case SELECTED_APP_RESTRICT_PUBLICITY:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument10(), caseData.getResTseTextBox10());
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument11(), caseData.getResTseTextBox11());
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                return checkGiveDetailsIsBlank(caseData.getResTseDocument12(), caseData.getResTseTextBox12());
            default:
                return true;
        }
    }

    private Boolean checkGiveDetailsIsBlank(UploadedDocumentType document, String textBox) {
        return document == null && isNullOrEmpty(textBox);
    }

    /**
     * Uses {@link EmailService} to generate an email to Respondent.
     * Uses {@link UserService} to get Respondents email address.
     * @param caseDetails in which the case details are extracted from
     * @param userToken jwt used for authorization
     */
    public void sendAcknowledgeEmailAndGeneratePdf(CaseDetails caseDetails, String userToken) {
        String legalRepEmail = userService.getUserDetails(userToken).getEmail();
        UploadedDocumentType resTseCyaPdfDocument;

        CaseData caseData = caseDetails.getCaseData();
        String customisedText = null;

        if (NO.equals(caseData.getResTseCopyToOtherPartyYesOrNo())) {
            customisedText = RULE92_ANSWERED_NO;
        } else {
            switch (caseData.getResTseSelectApplication()) {
                case SELECTED_APP_AMEND_RESPONSE:
                case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                case SELECTED_APP_POSTPONE_A_HEARING:
                case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                case SELECTED_APP_ORDER_OTHER_PARTY:
                case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                case SELECTED_APP_RESTRICT_PUBLICITY:
                    resTseCyaPdfDocument = generateCyaPdfDocument(caseData, userToken, caseDetails.getCaseTypeId());
                    customisedText = String.format(RULE92_ANSWERED_YES_GROUP_A, caseData.getResTseSelectApplication());
                    break;
                case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                case SELECTED_APP_RECONSIDER_JUDGEMENT:
                    resTseCyaPdfDocument = generateCyaPdfDocument(caseData, userToken, caseDetails.getCaseTypeId());
                    customisedText = String.format(RULE92_ANSWERED_YES_GROUP_B, caseData.getResTseSelectApplication());
                    break;
                case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                    // No need to send email for Group C
                    break;
                default:
                    break;
            }
        }

        if (customisedText != null) {
            emailService.sendEmail(
                emailTemplateId,
                legalRepEmail,
                buildPersonalisation(
                    caseDetails,
                    customisedText,
                    caseData.getResTseSelectApplication()
                ));
        }
    }

    private UploadedDocumentType generateCyaPdfDocument(CaseData caseData, String userToken, String caseTypeId) {
        DocumentInfo documentInfo = generateDocument(caseData, userToken, caseTypeId);
        return documentManagementService.addDocumentToDocumentField(documentInfo);
    }

    private DocumentInfo generateDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken, caseTypeId, RES_TSE_FILE_NAME);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOC_GEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    public Map<String, String> buildPersonalisation(CaseDetails detail,
                                                           String customisedText,
                                                           String applicationType) {
        CaseData caseData = detail.getCaseData();
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("claimant", caseData.getClaimant());
        personalisation.put("respondents", getRespondentNames(caseData));
        personalisation.put("customisedText", customisedText);
        personalisation.put("shortText", applicationType);
        personalisation.put("caseId", detail.getCaseId());
        return personalisation;
    }

    /**
     * Creates a new Respondent TSE collection if it doesn't exist.
     * Create a new element in the list and assign the TSE data from CaseData to it.
     * At last, clears the existing TSE data from CaseData to ensure fields will be empty when user
     * starts a new application in the same case.
     * @param caseData contains all the case data
     */
    public void createRespondentApplication(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            caseData.setGenericTseApplicationCollection(new ArrayList<>());
        }

        GenericTseApplicationType respondentTseType = new GenericTseApplicationType();

        respondentTseType.setDate(Helper.getCurrentDate());
        respondentTseType.setNumber(String.valueOf(getNextApplicationNumber(caseData)));
        respondentTseType.setApplicant(APPLICANT_CLAIMANT);
        assignDataToFieldsFromApplicationType(respondentTseType, caseData);
        respondentTseType.setType(caseData.getResTseSelectApplication());
        respondentTseType.setCopyToOtherPartyYesOrNo(caseData.getResTseCopyToOtherPartyYesOrNo());
        respondentTseType.setCopyToOtherPartyText(caseData.getResTseCopyToOtherPartyTextArea());

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(respondentTseType);

        List<GenericTseApplicationTypeItem> tseApplicationCollection = caseData.getGenericTseApplicationCollection();
        tseApplicationCollection.add(tseApplicationTypeItem);
        caseData.setGenericTseApplicationCollection(tseApplicationCollection);

        clearRespondentTseDataFromCaseData(caseData);
    }

    private void assignDataToFieldsFromApplicationType(GenericTseApplicationType respondentTseType, CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                respondentTseType.setDetails(caseData.getResTseTextBox1());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument1());
                break;
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                respondentTseType.setDetails(caseData.getResTseTextBox2());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument2());
                break;
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                respondentTseType.setDetails(caseData.getResTseTextBox3());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument3());
                break;
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                respondentTseType.setDetails(caseData.getResTseTextBox4());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument4());
                break;
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                respondentTseType.setDetails(caseData.getResTseTextBox5());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument5());
                break;
            case SELECTED_APP_ORDER_OTHER_PARTY:
                respondentTseType.setDetails(caseData.getResTseTextBox6());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument6());
                break;
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                respondentTseType.setDetails(caseData.getResTseTextBox7());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument7());
                break;
            case SELECTED_APP_POSTPONE_A_HEARING:
                respondentTseType.setDetails(caseData.getResTseTextBox8());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument8());
                break;
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                respondentTseType.setDetails(caseData.getResTseTextBox9());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument9());
                break;
            case SELECTED_APP_RESTRICT_PUBLICITY:
                respondentTseType.setDetails(caseData.getResTseTextBox10());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument10());
                break;
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                respondentTseType.setDetails(caseData.getResTseTextBox11());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument11());
                break;
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                respondentTseType.setDetails(caseData.getResTseTextBox12());
                respondentTseType.setDocumentUpload(caseData.getResTseDocument12());
                break;
            default:
                break;
        }
    }

    private void clearRespondentTseDataFromCaseData(CaseData caseData) {
        caseData.setResTseSelectApplication(null);
        caseData.setResTseCopyToOtherPartyYesOrNo(null);
        caseData.setResTseCopyToOtherPartyTextArea(null);

        caseData.setResTseTextBox1(null);
        caseData.setResTseTextBox2(null);
        caseData.setResTseTextBox3(null);
        caseData.setResTseTextBox4(null);
        caseData.setResTseTextBox5(null);
        caseData.setResTseTextBox6(null);
        caseData.setResTseTextBox7(null);
        caseData.setResTseTextBox8(null);
        caseData.setResTseTextBox9(null);
        caseData.setResTseTextBox10(null);
        caseData.setResTseTextBox11(null);
        caseData.setResTseTextBox12(null);

        caseData.setResTseDocument1(null);
        caseData.setResTseDocument2(null);
        caseData.setResTseDocument3(null);
        caseData.setResTseDocument4(null);
        caseData.setResTseDocument5(null);
        caseData.setResTseDocument6(null);
        caseData.setResTseDocument7(null);
        caseData.setResTseDocument8(null);
        caseData.setResTseDocument9(null);
        caseData.setResTseDocument10(null);
        caseData.setResTseDocument11(null);
        caseData.setResTseDocument12(null);
    }

    /**
     * Gets the number a new TSE application should be labelled as.
     * @param caseData contains all the case data
     */
    public static int getNextApplicationNumber(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return 1;
        }
        return caseData.getGenericTseApplicationCollection().size() + 1;
    }

}
