package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public final class LoggingUtil {

    private LoggingUtil() {
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
                + "\nERROR DESCRIPTION: " + (isNotBlank(firstWord) ? firstWord : StringUtils.EMPTY)
                + "\nCASE REFERENCE: " + (isNotBlank(caseReferenceNumber) ? caseReferenceNumber : StringUtils.EMPTY)
                + "\nERROR MESSAGE: " + (isNotBlank(message) ? message : StringUtils.EMPTY)
                + "\nCLASS NAME: " + (isNotBlank(className) ? className : StringUtils.EMPTY)
                + "\nMETHOD NAME: " + (isNotBlank(methodName) ? methodName : StringUtils.EMPTY)
                + "\n*****************END OF EXCEPTION MESSAGE***********************");
    }
}
