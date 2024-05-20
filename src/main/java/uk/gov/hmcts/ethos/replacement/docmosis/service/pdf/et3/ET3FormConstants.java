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
    public static final String STRING_EMPTY = StringUtils.EMPTY;
    public static final String STRING_SPACE = StringUtils.SPACE;
    public static final String STRING_COMMA_WITH_SPACE = ", ";
    public static final String STRING_LINE_FEED = StringUtils.LF;
    public static final String YES_LOWERCASE = "yes";
    public static final String YES_CAPITALISED = "Yes";
    public static final String NO_LOWERCASE = "no";
    public static final String NO_CAPITALISED = "No";
    public static final String EMAIL_LOWERCASE = "email";
    public static final String POST_LOWERCASE = "post";
    public static final String VIDEO_HEARINGS = "Video hearings";
    public static final String PHONE_HEARINGS = "Phone hearings";
    public static final String EMAIL_CAPITALISED = "Email";
    public static final String POST_CAPITALISED = "Post";
    public static final String DATE_FORMAT_YYYY_MM_DD_DASH = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DD_MM_YYYY_DASH = "dd-MM-yyyy";

    // HEADER FIELDS
    public static final String TXT_PDF_HEADER_FIELD_CASE_NUMBER = "case number";
    public static final String TXT_PDF_HEADER_FIELD_DATE_RECEIVED = "date_received";
    public static final String TXT_PDF_HEADER_FIELD_RFT = "RTF";
    public static final String TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_EXISTS = "Additional document exists";
    public static final String TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_NOT_EXISTS = "No additional document";

    // SECTION 1 CLAIMANT NAME
    public static final String TXT_PDF_CLAIMANT_FIELD_NAME = "1.1 Claimant's name";

    // SECTION 2 RESPONDENT DETAILS
    public static final String CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS = "Miss";
    public static final String CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR = "Mr";
    public static final String CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS = "Mrs";
    public static final String CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS = "Ms";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_EMAIL = "2.5 Email";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_POST = "2.5 Post";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_PHONE = "2.10 phone";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_VIDEO = "2.10";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_NO =
            "2.9 more than one site - no";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES =
            "2.9 more than one site - yes";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MISS = "2.1 Miss";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MR = "2.1 Mr";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MRS = "2.1 Mrs";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MS = "2.1 Ms";
    public static final String CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_OTHER = "2.1 Other";
    public static final String TXT_PDF_RESPONDENT_FIELD_ADDRESS = "2.3 Respondent's address: number or name";
    public static final String TXT_PDF_RESPONDENT_FIELD_CONTACT_NAME = "2.2 name of contact";
    public static final String TXT_PDF_RESPONDENT_FIELD_DX = "2.3 dx number";
    public static final String TXT_PDF_RESPONDENT_FIELD_EMAIL = "2.6 email address";
    public static final String TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE =
            "2.13 employees employed at that site";
    public static final String TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_GREAT_BRITAIN = "2.11 number of employees";
    public static final String TXT_PDF_RESPONDENT_FIELD_MOBILE_NUMBER = "2.4 mobile number";
    public static final String TXT_PDF_RESPONDENT_FIELD_NAME = "2.2 name";
    public static final String TXT_PDF_RESPONDENT_FIELD_NUMBER = "2.3";
    public static final String TXT_PDF_RESPONDENT_FIELD_PHONE_NUMBER = "2.4 phone number";
    public static final String TXT_PDF_RESPONDENT_FIELD_POSTCODE = "2.3 Respondent's address: postcode";
    public static final String TXT_PDF_RESPONDENT_FIELD_TITLE_OTHER = "2.1 other specify";
    public static final String TXT_PDF_RESPONDENT_FIELD_TYPE = "2.4";

    // SECTION 3 ACAS DETAILS
    public static final String CHECKBOX_PDF_ACAS_FIELD_AGREEMENT_YES = "3.1 early conciliation details - yes";
    public static final String CHECKBOX_PDF_ACAS_FIELD_AGREEMENT_NO = "3.1 early conciliation details - no";
    public static final String TXT_PDF_ACAS_FIELD_AGREEMENT_NO_REASON = "3.1 If no, please explain why";

}
