package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GenericServiceUtils {

    private GenericServiceUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static void logException(String firstWord, String caseReferenceNumber, String errorMessage,
                                    String className, String methodName) {
        log.error("""
                *************EXCEPTION OCCURRED*************
                ERROR DESCRIPTION: {}
                CASE REFERENCE: {}
                ERROR MESSAGE: {}
                CLASS NAME: {}
                METHOD NAME: {}
                *****************END OF EXCEPTION MESSAGE***********************""",
                firstWord, caseReferenceNumber, errorMessage, className, methodName);
    }
}
