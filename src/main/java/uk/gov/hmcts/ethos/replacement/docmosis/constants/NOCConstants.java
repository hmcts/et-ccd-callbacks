package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class NOCConstants {

    public static final String NOC_REQUEST = "nocRequest";
    public static final String EVENT_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";

    public static final String WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE =
        "Failed to send email to removed legal representative, case id: {}, error: {}";
    public static final String WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY =
        "Failed to send email to unrepresented party, case id: {}, error: {}";

    private NOCConstants() {
        // Final classes should not have a public or default constructor.
    }

}
