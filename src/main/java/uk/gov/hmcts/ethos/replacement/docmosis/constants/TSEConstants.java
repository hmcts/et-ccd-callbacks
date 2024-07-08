package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class TSEConstants {

    public static final String GIVE_DETAIL_MISSING = "Please upload a document or provide details in the text box.";
    public static final String CLAIMANT_TSE_WITHDRAW_CLAIM = "Withdraw all or part of claim";
    public static final String CLAIMANT_REP_TITLE = "Claimant Representative";
    public static final String MISSING_DETAILS = "Please upload a document or provide details in the text box.";
    public static final String APPLICATION_COMPLETE_RULE92_ANSWERED_NO = "<hr>"
            + "<h3>What happens next</h3>"
            + "<p>The tribunal will consider all correspondence and let you know what happens next.</p>"
            + "<hr>";
    public static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE = "<hr>"
                    + "<h3>What happens next</h3>"
                    + "<p>You must submit your application after copying the correspondence to the other party.</p>"
                    + "<p>To copy this correspondence to the other party, you must send it to them by post or email. "
                    + "You must include all supporting documents.</p>";
    public static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE = "<hr>"
                            + "<h3>What happens next</h3>"
                            + "<p>You have sent a copy of your application to the respondent. "
                            + "They will have until %s to respond.</p>"
                            + "<p>If they do respond, they are expected to copy their response to you.</p>"
                            + "<p>You may be asked to supply further information. "
                            + "The tribunal will consider all correspondence and let you know what happens next.</p>"
                            + "<hr>";

    private TSEConstants() {
        // Access through static methods
    }
}
