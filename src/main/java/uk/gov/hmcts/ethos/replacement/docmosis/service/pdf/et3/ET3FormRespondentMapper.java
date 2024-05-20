package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_POST;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_PHONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_VIDEO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MISS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MRS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.EMAIL_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.EMAIL_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.PHONE_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.POST_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.POST_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_CONTACT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_DX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_GREAT_BRITAIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_MOBILE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_POSTCODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_TITLE_OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.VIDEO_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfAddressField;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfCheckboxFieldWhenActualValueContainsExpectedValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfCheckboxFieldWhenExpectedValueEqualsActualValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfTextField;

public class ET3FormRespondentMapper {

    private ET3FormRespondentMapper() {
        // Add a private constructor to hide the implicit public one.
    }

    /**
     * Maps respondent values with PDF input fields.
     * @param respondentSumType respondent data selected by representative of respondent
     * @param pdfFields print fields that is created in ET3FormMapper
     */
    public static void mapRespondent(RespondentSumType respondentSumType,
                                     ConcurrentMap<String, Optional<String>> pdfFields) {
        // Setting Respondent Title
        String selectedTitle = respondentSumType.getEt3ResponseRespondentPreferredTitle();
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MR,
                YES_CAPITALISED,
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR,
                selectedTitle);
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MRS,
                YES_CAPITALISED,
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS,
                selectedTitle);
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MISS,
                YES_CAPITALISED,
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS,
                selectedTitle);
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MS,
                YES_CAPITALISED,
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS,
                selectedTitle);
        putOtherTitle(selectedTitle, pdfFields);
        // END OF SETTING RESPONDENT TITLE
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_NAME, respondentSumType.getResponseRespondentName());
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_NUMBER,
                respondentSumType.getEt3ResponseRespondentCompanyNumber());
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_TYPE,
                respondentSumType.getEt3ResponseRespondentEmployerType());
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_CONTACT_NAME,
                respondentSumType.getEt3ResponseRespondentContactName());
        putPdfAddressField(pdfFields,
                TXT_PDF_RESPONDENT_FIELD_ADDRESS, respondentSumType.getResponseRespondentAddress());
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_POSTCODE,
                respondentSumType.getResponseRespondentAddress().getPostCode());
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_DX, respondentSumType.getEt3ResponseDXAddress());
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_PHONE_NUMBER,
                respondentSumType.getResponseRespondentPhone1());
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_MOBILE_NUMBER,
                respondentSumType.getResponseRespondentPhone2());
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_EMAIL,
                EMAIL_LOWERCASE,
                EMAIL_CAPITALISED,
                respondentSumType.getResponseRespondentContactPreference());
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_POST,
                POST_LOWERCASE,
                POST_CAPITALISED,
                respondentSumType.getResponseRespondentContactPreference());
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_EMAIL, respondentSumType.getRespondentEmail());
        putPdfCheckboxFieldWhenActualValueContainsExpectedValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_VIDEO,
                YES_CAPITALISED,
                VIDEO_HEARINGS,
                respondentSumType.getEt3ResponseHearingRespondent());
        putPdfCheckboxFieldWhenActualValueContainsExpectedValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_PHONE,
                YES_CAPITALISED,
                PHONE_HEARINGS,
                respondentSumType.getEt3ResponseHearingRespondent());
        putPdfTextField(pdfFields,
                TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_GREAT_BRITAIN,
                respondentSumType.getEt3ResponseEmploymentCount());
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES,
                YES_LOWERCASE,
                YES_CAPITALISED,
                respondentSumType.getEt3ResponseMultipleSites());
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_NO,
                NO_LOWERCASE,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseMultipleSites());
        putPdfTextField(pdfFields,
                TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE,
                respondentSumType.getEt3ResponseSiteEmploymentCount());

    }

    private static void putOtherTitle(String selectedTitle,
                                      ConcurrentMap<String, Optional<String>> pdfFields) {
        if (isBlank(selectedTitle)
                || CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR.equalsIgnoreCase(selectedTitle)
                || CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS.equalsIgnoreCase(selectedTitle)
                || CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS.equalsIgnoreCase(selectedTitle)
                || CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS.equalsIgnoreCase(selectedTitle)) {
            putPdfTextField(pdfFields, CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_OTHER, STRING_EMPTY);
            putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_TITLE_OTHER, STRING_EMPTY);
            return;
        }
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_OTHER,
                YES_CAPITALISED,
                selectedTitle,
                selectedTitle);
        putPdfTextField(pdfFields, TXT_PDF_RESPONDENT_FIELD_TITLE_OTHER, selectedTitle);
    }

}
