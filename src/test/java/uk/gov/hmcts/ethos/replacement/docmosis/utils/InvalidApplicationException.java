package uk.gov.hmcts.ethos.replacement.docmosis.utils;

class InvalidApplicationException extends RuntimeException {
    private static final long serialVersionUID = -4434020881691857246L;

    InvalidApplicationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
