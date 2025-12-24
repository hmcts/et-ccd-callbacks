package uk.gov.hmcts.ethos.replacement.docmosis.exceptions;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxService;

import java.io.Serial;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils.logException;

/**
 * Is thrown when an exception occurs whiles converting case data in pdf in {@link PdfBoxService}.
 * for example an IOException is encountered while loading pdf template
 */
@Slf4j
public class GenericServiceException extends Exception {
    @Serial
    private static final long serialVersionUID = 304268196018404976L;

    /**
     * Creates a {@link GenericServiceException} with an error message and the cause of the error and
     * logs exception with the values to make it easier to find in Azure logs.
     * @param message main message of the exception
     * @param cause main throwable cause of the exception
     * @param firstWord First wordings of the log
     * @param caseReferenceNumber reference number of the case
     * @param className class name where the exception occurred
     * @param methodName method of the exception occurred
     */
    public GenericServiceException(String message, Throwable cause, String firstWord, String caseReferenceNumber,
                                   String className, String methodName) {
        super(message, cause);
        logException(isNotBlank(firstWord) ? firstWord : STRING_EMPTY,
                isNotBlank(caseReferenceNumber) ? caseReferenceNumber : STRING_EMPTY,
                isNotBlank(message) ? message : STRING_EMPTY,
                isNotBlank(className) ? className : STRING_EMPTY,
                isNotBlank(methodName) ? methodName : STRING_EMPTY);
    }

}
