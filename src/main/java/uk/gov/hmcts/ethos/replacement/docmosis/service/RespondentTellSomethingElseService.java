package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentTellSomethingElseService {
    private final EmailService emailService;
    private final UserService userService;

    @Value("${respondent.tse.template.id}")
    private String emailTemplateId;

    private static final String GIVE_DETAIL_MISSING = "Give Detail missing";

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

    private static final String VARIABLE_CONTENT_AMEND_RESPONSE =
        "<hr>Use this form to apply to amend the ET3 response."
        + "<br><br>The tribunal will decide if you can make the amendment by judging the fairness of the application. "
            + "This decision may be made at a hearing."
        + "<br><br>The tribunal also needs to know if you consider the amendment minor or substantial."
        + "<br><br>Providing details of why you want to amend the response and the importance of the amendment will "
            + "help the tribunal to decide your application more quickly."
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>what you want to amend in your response. "
            + "Be specific and refer to your ET3 response form if possible</li>"
        + "<li>if you consider it a minor or substantial amendment</li>"
        + "<li>why you want to make this amendment</li>"
        + "<li>why you are asking to make this amendment now</li>"
        + "<li>how this amendment will benefit you and how could it disadvantage you if not granted</li></ul>";
    private static final String VARIABLE_CONTENT_CHANGE_PERSONAL_DETAILS =
        "<hr>Use this form to apply to change details like the company address, email or telephone number. "
        + "<br><br>If you change the postal or email address, we’ll send any letters to the new address."
        + "<br><br>If you change the telephone number, "
            + "we’ll contact you using the new number if we have questions about your response."
        + "<h3>Details you can apply to change:</h3>"
        + "<ul><li>name</li>"
        + "<li>sex and preferred title</li>"
        + "<li>address</li>"
        + "<li>telephone number</li>"
        + "<li>email address</li></ul>";
    private static final String VARIABLE_CONTENT_CLAIMANT_NOT_COMPLIED =
        "<hr>"
        + "Use this form to tell us that the claimant has not complied with all or part of an order from the tribunal."
        + "<br><br>You should try to resolve your complaint with the claimant. "
            + "Only use this form if that is not possible."
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>which order has not been complied with</li>"
        + "<li>the date the tribunal issued the order</li>"
        + "<li>what the claimant has not done</li>"
        + "<li>what you want the tribunal to do next</li></ul>";
    private static final String VARIABLE_CONTENT_CONSIDER_A_DECISION_AFRESH =
        "<hr>"
        + "Use this form to have a judgment or order made by a legal officer considered afresh by an Employment Judge."
        + "<br><br>Considered afresh means that if you disagree with a judgment or order made by a legal officer "
            + "in this case, you can ask an Employment Judge to look at that decision again."
        + "<br><br>The judge will take the decision afresh on the basis of the same information as was before the "
            + "legal officer, unless either side chooses to supply additional information."
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>the decision you want considered afresh</li>"
        + "<li>the date the tribunal issued the decision</li>"
        + "<li>why you want the decision considered afresh</li></ul>";
    private static final String VARIABLE_CONTENT_CONTACT_THE_TRIBUNAL =
        "<hr>Tell or ask the tribunal about something relevant to this case."
        + "<h3>Do not use this form to:</h3>"
        + "<ul><li>seek legal advice from the tribunal. "
            + "The tribunal is an independent judicial body and cannot give legal advice</li>"
        + "<li>tell us about settlement offers or discussions, "
            + "which are private and confidential to the parties</li></ul>";
    private static final String VARIABLE_CONTENT_ORDER_OTHER_PARTY =
        "<hr>Use this form to ask the tribunal to order the claimant to do or provide something."
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>what you want the claimant to do</li>"
        + "<li>why it is relevant to your response</li>"
        + "<li>if you have already asked the claimant to do or provide this thing. "
            + "If you have, tell us when you asked and what their response was</li>"
        + "<li>if you have not asked the claimant to do or provide this thing yet, explain why</li></ul>";
    private static final String VARIABLE_CONTENT_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE =
        "<hr>You can ask the tribunal to order a witness to attend to give evidence."
        + "<br><br>The witness must be in Great Britain (England, Scotland and Wales). "
            + "This does not include Northern Ireland."
        + "<br><br>The tribunal can limit the number of witnesses to be called to give evidence on a particular "
            + "issue, especially if that issue is not central to the case."
        + "<br><br>The respondent may also be liable for the costs incurred by the witness’s attendance."
        + "<br><br>You should consider whether the evidence of this witness is likely to help your case."
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>the witness’s full name and address</li>"
        + "<li>why the attendance of this witness is necessary for a fair hearing. "
            + "The tribunal needs to understand why the evidence is relevant, and why there is no alternative way of "
            + "establishing the same points at the hearing without ordering a witness to attend</li>"
        + "<li>if you have asked this witness to attend voluntarily. "
            + "If you have, tell us when you asked and what their response was</li>"
        + "<li>if you have not asked the witness to attend yet, explain why</li></ul>"
        + "<br><br>If the order is granted, the witness will be ordered to attend on the first day of the hearing. "
            + "If you want them to attend on another day or days, tell us: "
        + "<ul><li>the dates apart from the first day of the hearing that you want the witness to attend</li>"
        + "<li>why their attendance is necessary on those dates</li></ul>"
        + "<br><br>If you want the witness to bring documents, tell us: "
        + "<ul><li>why these documents are relevant to the issues in this case</li>"
        + "<li>why an order to disclose the documents would not be enough</li>"
        + "<li>if you have already asked the witness or anyone else to provide these documents. "
            + "If you have, tell us when you asked and what their response was</li>"
        + "<li>if you have not asked the witness to provide the documents yet, explain why</li></ul>";
    private static final String VARIABLE_CONTENT_POSTPONE_A_HEARING =
        "<hr>Use this form to ask the tribunal to postpone a hearing to a later date. "
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>which hearing you want to postpone. "
            + "If the hearing you want to postpone is listed over multiple days, "
            + "tell us which day or days you are applying to postpone</li>"
        + "<li>the reason you want to postpone. "
            + "The tribunal will use this to decide whether to grant your application</li>"
        + "<li>any essential documents to support your application</li>"
        + "<li>weekday dates within the next 3 months that you, "
            + "your witnesses or representatives cannot attend a rescheduled hearing</li>"
        + "<li>the reason you cannot attend on those dates</li></ul>";
    private static final String VARIABLE_CONTENT_RECONSIDER_JUDGEMENT =
        "<hr>Use this form to have a judgment made by a tribunal reconsidered. "
        + "This may be a judgment made by a judge alone or by a judge sitting with non-legal members."
        + "<br><br>If reconsideration is necessary in the interests of justice you can ask the tribunal to look "
        + "at the judgment again."
        + "<br><br>Only a judgment which finally determines an issue in your case can be reconsidered. "
            + "This therefore excludes any decision that does not comprise a final determination of the claim, "
            + "or part of a claim. If you wish to ask for a case management order to be varied or revoked, "
            + "use the vary or revoke an order application."
        + "<br><br>You can only ask the tribunal to reconsider a judgment within 14 days of the date the judgment "
            + "was sent to you."
        + "<br><br>If you want the tribunal to reconsider a judgment that was sent to you over 14 days ago you "
            + "must explain why your application is late."
        + "<br><br>A reconsideration application does not affect the time limit for appealing to the Employment "
            + "Appeal Tribunal."
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>the judgment you want reconsidered </li>"
        + "<li>the date the tribunal issued the judgment</li>"
        + "<li>your reason for a late application if the judgment was sent over 14 days ago</li>"
        + "<li>why it is in the interests of justice to reconsider this judgment</li>"
        + "<li>if the tribunal should vary or revoke the judgment</li>"
        + "<li>any additional information or material which the tribunal does not already have to support your "
            + "application</li></ul>";
    private static final String VARIABLE_CONTENT_RESTRICT_PUBLICITY =
        "<hr>Use this form to apply to prevent or restrict publicity in this case."
        + "<br><br>It is an important principle that justice should normally be delivered in public."
        + "<br><br>However, the tribunal has the power to prevent or restrict the public disclosure of any aspect of "
            + "this case if necessary in the interests of justice or to protect the Convention rights of any person."
        + "<br><br>The tribunal also has certain other powers to sit in private or to restrict publicity in "
            + "accordance with sections 10A, 10B, 11 and 12 of the Employment Tribunals Act 1996."
        + "<br><br>The tribunal may issue an order:"
        + "<ul><li>that a hearing that would otherwise be in public be conducted, in whole or in part, in private</li>"
        + "<li>that the identities of specified parties, witnesses or other persons referred to in the proceedings "
            + "should not be disclosed to the public, by the use of anonymisation or otherwise</li>"
        + "<li>for measures preventing witnesses at a public hearing being identifiable by members of the public</li>"
        + "<li>restricting the reporting of the case in the media</li></ul>"
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>how the tribunal should prevent or restrict publicity in this case</li>"
        + "<li>why the restrictions you want are needed. The tribunal must make the least restrictive order "
            + "possible. Tell us why fewer restrictions would not be enough</li>"
        + "<li>any media interest in this case. Include names of media organisations or journalists that are "
            + "interested. The tribunal might seek their comments before making a decision</li></ul>";
    private static final String VARIABLE_CONTENT_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM =
        "<hr>You can request that the tribunal strike out all or parts of the claim."
        + "<br><br>If something is ‘struck out’ it is removed from the claim or response and cannot be relied upon."
        + "<br><br>The tribunal can strike out all or parts of the claimant’s claim on their own initiative or "
            + "after a request from the respondent."
        + "<br><br>A strike out request must be based on at least one of the grounds of Rule 37 of the Employment "
            + "Tribunals Rules of Procedure."
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>why you think the claim (or parts of it) should be struck out</li>"
        + "<li>which ground or grounds in Rule 37 you say applies in this case</li>"
        + "<li>if you are referring to numbered points or paragraphs in a claim, "
            + "include these numbers or other references</li></ul>";
    private static final String VARIABLE_CONTENT_VARY_OR_REVOKE_AN_ORDER =
        "<hr>Use this form to apply to vary or revoke an order the tribunal has issued."
        + "<br><br>Tribunal orders will not usually be varied or revoked unless there has been a material change "
        + "in circumstances since the order was made, which make the variation in the interests of justice."
        + "<br><br>The tribunal will consider your reasons for varying or revoking the order and any supporting "
            + "materials you provide then make a decision."
        + "<h3>Details to include in your application:</h3>"
        + "<ul><li>the order you want to vary or revoke </li>"
        + "<li>the date the tribunal issued the order</li>"
        + "<li>explain which part of the order you want to vary or revoke</li>"
        + "<li>how to vary the order</li>"
        + "<li>why it is in the interests of justice to vary or revoke this order. Tell us any relevant changes "
            + "of circumstance since the order was made and consider the requirements of the "
            + "‘overriding objective’ in Rule 2 of the Employment Tribunal Rules of Procedure</li></ul>";

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
     * This service return Variable Content according to selected application.
     * @param caseData CaseData with selected application value
     * @return Variable Content according to selected application
     */
    public String resTseSetVariableContent(CaseData caseData) {
        switch (caseData.getResTseSelectApplication()) {
            case SELECTED_APP_AMEND_RESPONSE:
                return VARIABLE_CONTENT_AMEND_RESPONSE;
            case SELECTED_APP_CHANGE_PERSONAL_DETAILS:
                return VARIABLE_CONTENT_CHANGE_PERSONAL_DETAILS;
            case SELECTED_APP_CLAIMANT_NOT_COMPLIED:
                return VARIABLE_CONTENT_CLAIMANT_NOT_COMPLIED;
            case SELECTED_APP_CONSIDER_A_DECISION_AFRESH:
                return VARIABLE_CONTENT_CONSIDER_A_DECISION_AFRESH;
            case SELECTED_APP_CONTACT_THE_TRIBUNAL:
                return VARIABLE_CONTENT_CONTACT_THE_TRIBUNAL;
            case SELECTED_APP_ORDER_OTHER_PARTY:
                return VARIABLE_CONTENT_ORDER_OTHER_PARTY;
            case SELECTED_APP_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE:
                return VARIABLE_CONTENT_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE;
            case SELECTED_APP_POSTPONE_A_HEARING:
                return VARIABLE_CONTENT_POSTPONE_A_HEARING;
            case SELECTED_APP_RECONSIDER_JUDGEMENT:
                return VARIABLE_CONTENT_RECONSIDER_JUDGEMENT;
            case SELECTED_APP_RESTRICT_PUBLICITY:
                return VARIABLE_CONTENT_RESTRICT_PUBLICITY;
            case SELECTED_APP_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM:
                return VARIABLE_CONTENT_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM;
            case SELECTED_APP_VARY_OR_REVOKE_AN_ORDER:
                return VARIABLE_CONTENT_VARY_OR_REVOKE_AN_ORDER;
            default:
                return "";
        }
    }

    public List<String> validateGiveDetail(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getResTseGiveDetails().isEmpty()) {
            errors.add(GIVE_DETAIL_MISSING);
        }
        return errors;
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
