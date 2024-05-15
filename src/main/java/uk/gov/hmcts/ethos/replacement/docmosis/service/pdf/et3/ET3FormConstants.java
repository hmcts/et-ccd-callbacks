package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import org.apache.commons.lang3.StringUtils;

/**
 *  Defines form fields and other form constants.
 */
public final class ET3FormConstants {

    private ET3FormConstants() {
        // Add a private constructor to hide the implicit public one.
    }

    // GENERIC CONSTANTS
    public static final String ET3_FORM_PDF_TEMPLATE = "ET3_0224.pdf";
    public static final String EMPTY_STRING = StringUtils.EMPTY;

    // HEADER FIELDS
    public static final String TXT_PDF_HEADER_FIELD_CASE_NUMBER = "case number";
    public static final String TXT_PDF_HEADER_FIELD_DATE_RECEIVED = "date_received";
    public static final String TXT_PDF_HEADER_FIELD_RFT = "RTF";
    public static final String TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_EXISTS = "Additional document exists";
    public static final String TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_NOT_EXISTS = "No additional document";

    // SECTION 1 CLAIMANT NAME
    public static final String TXT_PDF_CLAIMANT_FIELD_CLAIMANT_NAME = "1.1 Claimant's name";

    // SECTION 2 RESPONDENT DETAILS

}
