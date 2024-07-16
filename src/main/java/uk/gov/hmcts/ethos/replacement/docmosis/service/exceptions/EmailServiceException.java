package uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions;

import java.io.Serial;

/**
 * Is thrown when email service failed to send email.
 */
public class EmailServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    /**
     * Creates a {@link EmailServiceException} with a message and a cause.
     * @param msg a message explaining why this exception is thrown
     * @param exception cause for the exception the cause (which is saved for later retrieval by the getCause() method).
     *        (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EmailServiceException(String msg, Throwable exception) {
        super(msg, exception);
    }
}
