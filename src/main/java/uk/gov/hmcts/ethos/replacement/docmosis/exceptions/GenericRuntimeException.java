package uk.gov.hmcts.ethos.replacement.docmosis.exceptions;

import java.io.Serial;

public class GenericRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 404268196018455872L;

    public GenericRuntimeException(GenericServiceException genericServiceException) {
        super(genericServiceException);
    }

}
