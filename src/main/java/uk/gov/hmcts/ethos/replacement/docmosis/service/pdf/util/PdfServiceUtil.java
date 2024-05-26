package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;

@Slf4j
public class PdfServiceUtil {

    private PdfServiceUtil() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Logs exception according to given user inputs below. It enables us have a better log on azure cloud.
     * @param firstWord first wordings of the log.
     * @param caseReferenceNumber ethos case reference data
     * @param message exception message
     * @param className class name where the exception occurs
     * @param methodName method name where the exception occurs
     */
    public static void logException(String firstWord,
                                    String caseReferenceNumber,
                                    String message,
                                    String className,
                                    String methodName) {
        log.error("*************EXCEPTION OCCURED*************"
                + "\nERROR DESCRIPTION: " + (isNotBlank(firstWord) ? firstWord : STRING_EMPTY)
                + "\nCASE REFERENCE: " + (isNotBlank(caseReferenceNumber) ? caseReferenceNumber : STRING_EMPTY)
                + "\nERROR MESSAGE: " + (isNotBlank(message) ? message : STRING_EMPTY)
                + "\nCLASS NAME: " + (isNotBlank(className) ? className : STRING_EMPTY)
                + "\nMETHOD NAME: " + (isNotBlank(methodName) ? methodName : STRING_EMPTY)
                + "\n*****************END OF EXCEPTION MESSAGE***********************");
    }

}
