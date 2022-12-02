package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentTellSomethingElseService {
    private final EmailService emailService;
    private final UserService userService;

    @Value("${respondent.tse.template.id}")
    private String emailTemplateId;

    private static final String NO = "I do not want to copy";
    private static final String rule92AnsweredNoText = "You have said that you do not want to copy this correspondence "
        + "to the other party. \n \n"
        + "The tribunal will consider all correspondence and let you know what happens next.";
    private static final String rule92AnsweredYesGroupA = "The other party will be notified that any objections to "
        + "your %s application should be sent to the tribunal as soon as possible, and in any event "
        + "within 7 days.";
    private static final String rule92AnsweredYesGroupB = "The other party is not expected to respond to this "
        + "application.\n \nHowever, they have been notified that any objections to your %s application should be "
        + "sent to the tribunal as soon as possible, and in any event within 7 days.";

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
            customisedText = rule92AnsweredNoText;
        } else {
            switch (caseData.getResTseSelectApplication()) {
                case "Amend response":
                case "Strike out all or part of a claim":
                case "Contact the tribunal":
                case "Postpone a hearing":
                case "Vary or revoke an order":
                case "Order other party":
                case "Claimant not complied":
                case "Restrict publicity":
                    customisedText = String.format(rule92AnsweredYesGroupA, caseData.getResTseSelectApplication());
                    break;
                case "Change personal details":
                case "Consider a decision afresh":
                case "Reconsider judgement":
                    customisedText = String.format(rule92AnsweredYesGroupB, caseData.getResTseSelectApplication());
                    break;
                case "Order a witness to attend to give evidence":
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
