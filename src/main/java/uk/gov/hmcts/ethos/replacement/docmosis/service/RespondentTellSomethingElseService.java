package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentTseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTseType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

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

    @Value("${respondent.tse.template.id}")
    private String emailTemplateId;

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
    public void sendRespondentApplicationEmail(CaseDetails caseDetails, String userToken) {
        String legalRepEmail = userService.getUserDetails(userToken).getEmail();

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
                    customisedText = String.format(RULE92_ANSWERED_YES_GROUP_A, caseData.getResTseSelectApplication());
                    break;
                case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                case SELECTED_APP_RECONSIDER_JUDGEMENT:
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
        if (CollectionUtils.isEmpty(caseData.getResTseCollection())) {
            caseData.setResTseCollection(new ArrayList<>());
        }

        RespondentTseType respondentTseType = new RespondentTseType();

        assignDataToFieldsFromApplicationType(respondentTseType, caseData);
        respondentTseType.setResTseSelectApplication(caseData.getResTseSelectApplication());
        respondentTseType.setResTseCopyToOtherPartyYesOrNo(caseData.getResTseCopyToOtherPartyYesOrNo());
        respondentTseType.setResTseCopyToOtherPartyTextArea(caseData.getResTseCopyToOtherPartyTextArea());

        RespondentTseTypeItem respondentTseTypeItem = new RespondentTseTypeItem();
        respondentTseTypeItem.setId(UUID.randomUUID().toString());
        respondentTseTypeItem.setValue(respondentTseType);

        List<RespondentTseTypeItem> respondentTseCollection = caseData.getResTseCollection();
        respondentTseCollection.add(respondentTseTypeItem);
        caseData.setResTseCollection(respondentTseCollection);

        clearRespondentTseDataFromCaseData(caseData);
    }

    private void assignDataToFieldsFromApplicationType(RespondentTseType respondentTseType, CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox1());
                respondentTseType.setResTseDocument(caseData.getResTseDocument1());
                break;
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox2());
                respondentTseType.setResTseDocument(caseData.getResTseDocument2());
                caseData.setResTseTextBox2(null);
                caseData.setResTseDocument2(null);
                break;
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox3());
                respondentTseType.setResTseDocument(caseData.getResTseDocument3());
                caseData.setResTseTextBox3(null);
                caseData.setResTseDocument3(null);
                break;
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox4());
                respondentTseType.setResTseDocument(caseData.getResTseDocument4());
                caseData.setResTseTextBox4(null);
                caseData.setResTseDocument4(null);
                break;
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox5());
                respondentTseType.setResTseDocument(caseData.getResTseDocument5());
                caseData.setResTseTextBox5(null);
                caseData.setResTseDocument5(null);
                break;
            case SELECTED_APP_ORDER_OTHER_PARTY:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox6());
                respondentTseType.setResTseDocument(caseData.getResTseDocument6());
                caseData.setResTseTextBox6(null);
                caseData.setResTseDocument6(null);
                break;
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox7());
                respondentTseType.setResTseDocument(caseData.getResTseDocument7());
                caseData.setResTseTextBox7(null);
                caseData.setResTseDocument7(null);
                break;
            case SELECTED_APP_POSTPONE_A_HEARING:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox8());
                respondentTseType.setResTseDocument(caseData.getResTseDocument8());
                caseData.setResTseTextBox8(null);
                caseData.setResTseDocument8(null);
                break;
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox9());
                respondentTseType.setResTseDocument(caseData.getResTseDocument9());
                caseData.setResTseTextBox9(null);
                caseData.setResTseDocument9(null);
                break;
            case SELECTED_APP_RESTRICT_PUBLICITY:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox10());
                respondentTseType.setResTseDocument(caseData.getResTseDocument10());
                caseData.setResTseTextBox10(null);
                caseData.setResTseDocument10(null);
                break;
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox11());
                respondentTseType.setResTseDocument(caseData.getResTseDocument11());
                caseData.setResTseTextBox11(null);
                caseData.setResTseDocument11(null);
                break;
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                respondentTseType.setResTseTextBox(caseData.getResTseTextBox12());
                respondentTseType.setResTseDocument(caseData.getResTseDocument12());
                caseData.setResTseTextBox12(null);
                caseData.setResTseDocument12(null);
                break;
            default:
                break;
        }
    }

    private void clearRespondentTseDataFromCaseData(CaseData caseData) {
        caseData.setResTseSelectApplication(null);
        caseData.setResTseCopyToOtherPartyYesOrNo(null);
        caseData.setResTseCopyToOtherPartyTextArea(null);
    }
}
