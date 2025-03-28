package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf;

/**
 *  Defines PDF Service Constants.
 */
public final class PdfBoxServiceConstants {

    public static final String PDF_SERVICE_CLASS_NAME = "PdfService";
    public static final String UNABLE_TO_PROCESS_PDF_SOURCE = "Unable to process pdf template file %s";
    public static final String GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME = "generatePdfDocumentInfo";
    public static final String ET3_RESPONSE_PDF_FILE_NAME = "%s-ET3_Response.pdf";
    public static final String PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY =
            "To create PDF file case data should not be empty";
    public static final String PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_REQUIRED_FIELD_EMPTY =
            "One of the required parameters was empty. You can find details in the logs";
    public static final String PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY =
            "To create PDF file user token should not be empty";
    public static final String PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY =
            "To create PDF file case type id should not be empty";
    public static final String PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY =
            "To create PDF file document name should not be empty";
    public static final String PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY =
            "To create PDF file pdf template should not be empty";
    public static final char CHARACTER_DOT = '.';
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final String STRING_ZERO = "0";
    public static final String CURRENCY_DECIMAL_ZERO_WITH_DOT = ".00";
    public static final String PDF_OUTPUT_FILE_NAME_PREFIX = "ET3_Form_Respondent_Document";
    public static final String CASE_DATA_NOT_FOUND_EXCEPTION_MESSAGE = "Case Data is null or empty";

    private PdfBoxServiceConstants() {
        // Add a private constructor to hide the implicit public one.
    }
}
