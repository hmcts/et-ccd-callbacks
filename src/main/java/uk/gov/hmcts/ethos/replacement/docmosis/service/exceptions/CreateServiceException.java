package uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions;

import java.io.Serial;

/**
 * Is thrown when create service fails.
 */
public class CreateServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    /**
     * Creates a {@link CreateServiceException} with a message and a cause.
     * @param msg a message explaining why this exception is thrown
     * @param exception cause for the exception the cause (which is saved for later retrieval by the getCause() method).
     *        (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CreateServiceException(String msg, Throwable exception) {
        super(msg, exception);
    }
}
