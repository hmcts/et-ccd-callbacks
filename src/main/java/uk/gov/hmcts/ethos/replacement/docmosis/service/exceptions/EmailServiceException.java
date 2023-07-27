package uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions;

/**
 * Is thrown when email service failed to send email.
 */
public class EmailServiceException extends RuntimeException {
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
