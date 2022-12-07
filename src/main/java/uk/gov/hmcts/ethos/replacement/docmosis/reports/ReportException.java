package uk.gov.hmcts.ethos.replacement.docmosis.reports;

@SuppressWarnings({"PMD.MissingSerialVersionUID"})
public class ReportException extends RuntimeException {
    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
