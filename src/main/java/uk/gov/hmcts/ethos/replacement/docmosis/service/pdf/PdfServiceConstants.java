package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf;

/**
 *  Defines PDF Service Constants.
 */
public final class PdfServiceConstants {

    public static final String PDF_SERVICE_ERROR_NOT_ABLE_TO_MAP_CASE_DATA_TO_TEMPLATE_PDF =
            "Exception occurred while mapping case data to template PDF";
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[]{};

    public static final String PDF_SERVICE_CLASS_NAME = "PdfService";
    public static final String ET3_FORM_BYTE_ARRAY_CREATION_METHOD_NAME = "createET3FormByteArray";
    public static final String PUT_PDF_FIELD_METHOD_NAME = "putPdfField";
    public static final String PDF_SERVICE_PARSING_ERROR = "Error while parsing PDF file for entry key \"%s\"";
    public static final String STREAM_CLOSURE_CLASS_NAME = "safeClose";
    public static final String UNABLE_TO_CLOSE_STREAM_FOR_PDF_TEMPLATE =
            "Unable to close input stream for the template PDF file";
    public static final String UNABLE_TO_PROCESS_PDF_SOURCE = "Unable to process pdf template file %s";
    public static final String GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME = "generatePdfDocumentInfo";
    public static final String ET3_RESPONSE_PDF_FILE_NAME = "%s-ET3_Response.pdf";
    public static final String PDF_DOCUMENT_CREATED_LOG_INFO = "URI documentSelfPath uploaded and created: %s";
    public static final String PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY =
            "To create PDF file case data should not be empty";
    public static final String PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY =
            "To create PDF file user token should not be empty";
    public static final String PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY =
            "To create PDF file case type id should not be empty";
    public static final String PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY =
            "To create PDF file document name should not be empty";
    public static final String PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY =
            "To create PDF file pdf template should not be empty";

    private PdfServiceConstants() {
        // Add a private constructor to hide the implicit public one.
    }
}
