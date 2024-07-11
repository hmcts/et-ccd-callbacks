package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation;

import java.io.Serial;

public class SaveFileLocationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    public SaveFileLocationException(String message) {
        super(message);
    }

}
