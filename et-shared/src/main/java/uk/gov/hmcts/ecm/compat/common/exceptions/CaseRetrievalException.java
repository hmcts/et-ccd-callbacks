package uk.gov.hmcts.ecm.compat.common.exceptions;

public class CaseRetrievalException extends RuntimeException {
    public CaseRetrievalException(String message) {
        super(message);
    }

    public CaseRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
