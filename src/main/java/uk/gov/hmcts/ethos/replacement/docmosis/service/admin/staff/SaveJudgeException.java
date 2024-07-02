package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import java.io.Serial;

public class SaveJudgeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    public SaveJudgeException(String message) {
        super(message);
    }
}
