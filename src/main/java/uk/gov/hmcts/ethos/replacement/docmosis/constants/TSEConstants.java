package uk.gov.hmcts.ethos.replacement.docmosis.constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TSEConstants {

    public static final String GIVE_DETAIL_MISSING = "Please upload a document or provide details in the text box.";
    public static final String CLAIMANT_TSE_AMEND_CLAIM = "Amend claim";
    public static final String CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS = "Change personal details";
    public static final String CLAIMANT_TSE_CONSIDER_DECISION_AFRESH = "Consider decision afresh";
    public static final String CLAIMANT_TSE_CONTACT_THE_TRIBUNAL = "Contact the tribunal";
    public static final String CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND = "Order a witness to attend to give evidence";
    public static final String CLAIMANT_TSE_ORDER_OTHER_PARTY = "Order other party";
    public static final String CLAIMANT_TSE_POSTPONE_A_HEARING = "Postpone a hearing";
    public static final String CLAIMANT_TSE_RECONSIDER_JUDGMENT = "Reconsider judgment";
    public static final String CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED = "Respondent not complied";
    public static final String CLAIMANT_TSE_RESTRICT_PUBLICITY = "Restrict publicity";
    public static final String CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART = "Strike out all or part of the response";
    public static final String CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER = "Vary or revoke an order";
    public static final String CLAIMANT_TSE_WITHDRAW_CLAIM = "Withdraw all or part of claim";
    public static final String CLAIMANT_REP_TITLE = "Claimant Representative";
    public static final String APPLICATION_COMPLETE_RULE92_ANSWERED_NO = "<hr>"
            + "<h2>What happens next</h2>"
            + "<p>The tribunal will consider all correspondence and let you know what happens next.</p>"
            + "<h3>%s</h3>";
    public static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE = "<hr>"
                    + "<h2>Copy this correspondence to the other party</h2>"
                    + "<p>You have submitted your application to the tribunal. "
                    + "You must now copy the correspondence to the other party.</p>"
                    + "<p>To copy this correspondence to the other party, you must send it to them by post or email. "
                    + "You must include all supporting documents.</p>"
                    + "<h3>%s</h3>";
    public static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE = "<hr>"
                            + "<h2>What happens next</h2>"
                            + "<p>You have sent a copy of your application to the respondent. "
                            + "They will have until %s to respond.</p>"
                            + "<p>If they do respond, they are expected to copy their response to you.</p>"
                            + "<p>You may be asked to supply further information. "
                            + "The tribunal will consider all correspondence and let you know what happens next.</p>"
                            + "<h3>%s</h3>";
    public static final Map<String, String> APPLICATION_TYPE_MAP = new ConcurrentHashMap<>();

    static {
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_AMEND_CLAIM, "amend");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, "change-details");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH, "reconsider-decision");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND, "witness");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_ORDER_OTHER_PARTY, "respondent");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_POSTPONE_A_HEARING, "postpone");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RECONSIDER_JUDGMENT, "reconsider-judgement");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED, "non-compliance");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RESTRICT_PUBLICITY, "publicity");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART, "strike");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER, "vary");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_WITHDRAW_CLAIM, "withdraw");
        TSEConstants.APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL, "other");
    }

    private TSEConstants() {
        // Access through static methods
    }
}
