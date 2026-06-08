package uk.gov.hmcts.ethos.replacement.docmosis.exceptions;

import java.io.Serial;

public class UnAuthorisedServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2047737262969335485L;

    public UnAuthorisedServiceException(String message) {
        super(message);
    }
}
