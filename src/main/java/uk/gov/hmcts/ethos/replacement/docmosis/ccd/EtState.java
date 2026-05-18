package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

public enum EtState {
    DELETE("Delete"),
    SUBMITTED("Submitted"),
    AWAITING_SUBMISSION_TO_HMCTS("AWAITING_SUBMISSION_TO_HMCTS"),
    VETTED("Vetted"),
    ACCEPTED("Accepted"),
    CLOSED("Closed"),
    REJECTED("Rejected"),
    TRANSFERRED("Transferred"),
    SUBMITTED_REPORT("SubmittedReport"),
    OPEN("Open"),
    UPDATING("Updating"),
    ERROR("Error");

    private final String id;

    EtState(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
