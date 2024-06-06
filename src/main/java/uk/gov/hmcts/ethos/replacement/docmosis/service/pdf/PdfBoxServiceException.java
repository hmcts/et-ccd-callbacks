package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf;

import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;

/**
 * Is thrown when an exception occurs whiles converting case data in pdf in {@link PdfBoxService}.
 * for example an IOException is encountered while loading pdf template
 */
@Slf4j
public class PdfBoxServiceException extends Exception {
    @Serial
    private static final long serialVersionUID = 304268196018404976L;

    /**
     * Creates a {@link PdfBoxServiceException} with an error message and the cause of the error and
     * logs exception with the values to make it easier to find in Azure logs.
     * @param message main message of the exception
     * @param cause main throwable cause of the exception
     * @param firstWord First wordings of the log
     * @param caseReferenceNumber reference number of the case
     * @param className class name where the exception occurred
     * @param methodName method of the exception occurred
     */
    public PdfBoxServiceException(String message, Throwable cause, String firstWord, String caseReferenceNumber,
                                  String className, String methodName) {
        super(message, cause);
        log.error("*************EXCEPTION OCCURED*************"
                + "\nERROR DESCRIPTION: " + (isNotBlank(firstWord) ? firstWord : STRING_EMPTY)
                + "\nCASE REFERENCE: " + (isNotBlank(caseReferenceNumber) ? caseReferenceNumber : STRING_EMPTY)
                + "\nERROR MESSAGE: " + (isNotBlank(message) ? message : STRING_EMPTY)
                + "\nCLASS NAME: " + (isNotBlank(className) ? className : STRING_EMPTY)
                + "\nMETHOD NAME: " + (isNotBlank(methodName) ? methodName : STRING_EMPTY)
                + "\n*****************END OF EXCEPTION MESSAGE***********************");
    }

}
