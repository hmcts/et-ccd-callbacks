package uk.gov.hmcts.ethos.replacement.docmosis.exceptions;

/**
 *   Triggered by when an IO exception is encountered when interacting with CCD.
 */
public class CcdInputOutputException extends RuntimeException {

    private static final long serialVersionUID = Long.MIN_VALUE;

    public CcdInputOutputException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
