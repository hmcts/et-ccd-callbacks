package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

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
                RespondentTellSomethingElseHelper.buildPersonalisation(
                    caseDetails,
                    customisedText,
                    caseData.getResTseSelectApplication()
                ));
        }
    }

}
