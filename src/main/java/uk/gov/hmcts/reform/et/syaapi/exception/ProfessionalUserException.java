package uk.gov.hmcts.reform.et.syaapi.exception;

import java.io.Serial;

/**
 * Exception thrown when a professional user (legal representative) attempts to self-assign a case.
 * Professional users should use MyHMCTS instead of the citizen portal.
 */
public class ProfessionalUserException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ProfessionalUserException with the specified detail message.
     *
     * @param message the detail message
     */
    public ProfessionalUserException(String message) {
        super(message);
    }

    /**
     * Constructs a new ProfessionalUserException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the root cause
     */
    public ProfessionalUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
