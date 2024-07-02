package uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions;

import java.io.Serial;

public class VenueAddressReaderException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    public VenueAddressReaderException(String message) {
        super(message);
    }
}
