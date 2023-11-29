package uk.gov.hmcts.ethos.replacement.docmosis.config.interceptor;

import java.io.Serial;

public class UnauthorisedServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -8778329799233256308L;

    /**
     * Create an {@link UnauthorisedServiceException} with a cause and a message.
     * @param message exception message
     */
    public UnauthorisedServiceException(String message) {
        super(message);
    }
}
