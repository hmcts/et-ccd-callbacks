package uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions;

/**
 * Is thrown when verify token service fails to verify the JWT.
 */
@SuppressWarnings({"PMD.MissingSerialVersionUID"})
public class VerifyTokenServiceException extends RuntimeException {
    /**
     * Creates a {@link VerifyTokenServiceException} with only a message.
     * @param msg a message explaining why this exception is thrown
     */
    public VerifyTokenServiceException(String msg) {
        super(msg);
    }

    /**
     * Creates a {@link VerifyTokenServiceException} with a message and a cause.
     * @param msg a message explaining why this exception is thrown
     * @param exception cause for the exception the cause (which is saved for later retrieval by the getCause() method).
     *        (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public VerifyTokenServiceException(String msg, Throwable exception) {
        super(msg, exception);
    }
}
