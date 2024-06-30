package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public class TSEConstants {
    private TSEConstants() {
        // Access through static methods
    }

    public static final String GIVE_DETAIL_MISSING = "Please upload a document or provide details in the text box.";
    public static final String CLAIMANT_TSE_WITHDRAW_CLAIM = "Withdraw all or part of claim";
    public static final String APPLICATION_COMPLETE_RULE92_ANSWERED_NO = "<hr>"
            + "<h3>What happens next</h3>"
            + "<p>The tribunal will consider all correspondence and let you know what happens next.</p>";

    public static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES = "<hr>"
            + "<h3>What happens next</h3>"
            + "<p>You have sent a copy of your application to the claimant. They will have until %s to respond.</p>"
            + "<p>If they do respond, they are expected to copy their response to you.</p>"
            + "<p>You may be asked to supply further information. "
            + "The tribunal will consider all correspondence and let you know what happens next.</p>";
}
