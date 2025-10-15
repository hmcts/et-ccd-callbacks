package uk.gov.hmcts.ethos.replacement.docmosis.exceptions;

import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.util.Arrays;

@Slf4j
public class CallbacksRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    public CallbacksRuntimeException(Exception exception) {
        super(exception);
        String errorMessage = "************ CallbacksRuntimeException ************"
                + "Error occurred while modifying case role: " + exception.getMessage()
                + "\nStack trace: " + Arrays.toString(exception.getStackTrace())
                + "***************************************************";
        log.error(errorMessage);
    }
}
