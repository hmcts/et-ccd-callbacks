package uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions;

public class OfficeToAssignCaseToNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OfficeToAssignCaseToNotFoundException(String message) {
        super(message);
    }
}
