package uk.gov.hmcts.ethos.replacement.docmosis.reports;

import java.io.Serial;

public class ReportException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
