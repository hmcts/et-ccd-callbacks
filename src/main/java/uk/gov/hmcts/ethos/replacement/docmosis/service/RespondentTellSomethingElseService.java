package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

@Service
public class RespondentTellSomethingElseService {

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

    private static final String VARIABLE_CONTENT_AMEND_RESPONSE = "Amend response";
    private static final String VARIABLE_CONTENT_CHANGE_PERSONAL_DETAILS = "Change personal details";
    private static final String VARIABLE_CONTENT_CLAIMANT_NOT_COMPLIED = "Claimant not complied";
    private static final String VARIABLE_CONTENT_CONSIDER_A_DECISION_AFRESH = "Consider a decision afresh";
    private static final String VARIABLE_CONTENT_CONTACT_THE_TRIBUNAL = "Contact the tribunal";
    private static final String VARIABLE_CONTENT_ORDER_OTHER_PARTY = "Order other party";
    private static final String VARIABLE_CONTENT_ORDER_A_WITNESS_TO_ATTEND_TO_GIVE_EVIDENCE =
            "Order a witness to attend to give evidence";
    private static final String VARIABLE_CONTENT_POSTPONE_A_HEARING = "Postpone a hearing";
    private static final String VARIABLE_CONTENT_RECONSIDER_JUDGEMENT = "Reconsider judgement";
    private static final String VARIABLE_CONTENT_RESTRICT_PUBLICITY = "Restrict publicity";
    private static final String VARIABLE_CONTENT_STRIKE_OUT_ALL_OR_PART_OF_A_CLAIM =
            "Strike out all or part of a claim";
    private static final String VARIABLE_CONTENT_VARY_OR_REVOKE_AN_ORDER = "Vary or revoke an order";

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

}
