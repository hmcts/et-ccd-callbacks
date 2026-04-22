package uk.gov.hmcts.reform.et.syaapi.exception;

import java.io.Serial;

/**
 * Triggered when invalid data is provided for case role modification.
 */
public class CaseUserRoleValidationException extends ManageCaseRoleException {

    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    /**
     * Creates a {@link CaseUserRoleValidationException} with a message.
     * @param message the exception message
     */
    public CaseUserRoleValidationException(String message) {
        super(message);
    }

    /**
     * Creates a {@link CaseUserRoleValidationException} with a message and cause.
     * @param message the exception message
     * @param cause the underlying cause
     */
    public CaseUserRoleValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
