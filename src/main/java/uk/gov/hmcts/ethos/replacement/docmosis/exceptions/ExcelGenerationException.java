package uk.gov.hmcts.ethos.replacement.docmosis.exceptions;

import java.io.Serial;

/**
 *   Triggered when an IO exception is encountered when generating the Multiples excel doc.
 */
public class ExcelGenerationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    public ExcelGenerationException(String message, Throwable err) {
        super(message, err);
    }
}
