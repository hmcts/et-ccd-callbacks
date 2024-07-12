package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles;

import java.io.Serial;

public class ImportException extends Exception {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
