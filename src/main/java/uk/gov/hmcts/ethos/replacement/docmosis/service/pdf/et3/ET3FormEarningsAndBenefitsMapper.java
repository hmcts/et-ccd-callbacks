package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.ANNUALLY_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.ANNUALLY_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_ANNUALLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_MONTHLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_WEEKLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_ANNUALLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_MONTHLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_WEEKLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.MONTHLY_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.MONTHLY_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_NOT_CORRECT_INFORMATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_NOT_CORRECT_INFORMATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_WORK_HOURS_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.WEEKLY_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.WEEKLY_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormUtil.putConditionalPdfField;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormUtil.putPdfCheckboxFieldWhenExpectedValueEqualsActualValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormUtil.putPdfCheckboxFieldWhenOther;

public final class ET3FormEarningsAndBenefitsMapper {

    private ET3FormEarningsAndBenefitsMapper() {
        // Add a private constructor to hide the implicit public one.
    }

    /**
     * Maps pension and benefit values with PDF input fields.
     * @param respondentSumType respondent data selected by representative of respondent
     * @param pdfFields print fields that is created in ET3FormMapper
     */
    public static void mapEarningsAndBenefits(RespondentSumType respondentSumType,
                                              ConcurrentMap<String, Optional<String>> pdfFields) {
        // CLAIMANT WORK HOURS
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_YES,
                YES_LOWERCASE,
                YES_CAPITALISED,
                respondentSumType.getEt3ResponseClaimantWeeklyHours());
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NO,
                NO_LOWERCASE,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseClaimantWeeklyHours());
        putPdfCheckboxFieldWhenOther(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NOT_APPLICABLE,
                NO_LOWERCASE,
                List.of(NO_CAPITALISED, YES_CAPITALISED),
                respondentSumType.getEt3ResponseClaimantWeeklyHours());
        putConditionalPdfField(pdfFields,
                TXT_PDF_EARNINGS_BENEFITS_FIELD_WORK_HOURS_DETAILS,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                respondentSumType.getEt3ResponseClaimantCorrectHours());
        // END OF CLAIMANT WORK HOURS
        // EARNING DETAILS
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_YES,
                YES_LOWERCASE,
                YES_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect());
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NO,
                NO_LOWERCASE,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect());
        putPdfCheckboxFieldWhenOther(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NOT_APPLICABLE,
                NO_LOWERCASE,
                List.of(NO_CAPITALISED, YES_CAPITALISED),
                respondentSumType.getEt3ResponseEarningDetailsCorrect());
        putConditionalPdfField(pdfFields,
                TXT_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                respondentSumType.getEt3ResponsePayBeforeTax());
        putConditionalPdfField(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_WEEKLY,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                getPaymentFrequencyCheckboxValue(respondentSumType.getEt3ResponsePayFrequency(), WEEKLY_CAPITALISED));
        putConditionalPdfField(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_MONTHLY,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                getPaymentFrequencyCheckboxValue(respondentSumType.getEt3ResponsePayFrequency(), MONTHLY_CAPITALISED));
        putConditionalPdfField(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_ANNUALLY,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                getPaymentFrequencyCheckboxValue(respondentSumType.getEt3ResponsePayFrequency(), ANNUALLY_CAPITALISED));
        putConditionalPdfField(pdfFields,
                TXT_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                respondentSumType.getEt3ResponsePayTakehome());
        putConditionalPdfField(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_WEEKLY,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                getPaymentFrequencyCheckboxValue(respondentSumType.getEt3ResponsePayFrequency(), WEEKLY_CAPITALISED));
        putConditionalPdfField(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_MONTHLY,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                getPaymentFrequencyCheckboxValue(respondentSumType.getEt3ResponsePayFrequency(), MONTHLY_CAPITALISED));
        putConditionalPdfField(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_ANNUALLY,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                getPaymentFrequencyCheckboxValue(respondentSumType.getEt3ResponsePayFrequency(), ANNUALLY_CAPITALISED));
        // END OF EARNING DETAILS
        // NOTICE PERIOD DETAILS
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_YES,
                YES_LOWERCASE,
                YES_CAPITALISED,
                respondentSumType.getEt3ResponseIsNoticeCorrect());
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NO,
                NO_LOWERCASE,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseIsNoticeCorrect());
        putPdfCheckboxFieldWhenOther(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NOT_APPLICABLE,
                NO_LOWERCASE,
                List.of(NO_CAPITALISED, YES_CAPITALISED),
                respondentSumType.getEt3ResponseIsNoticeCorrect());
        putConditionalPdfField(pdfFields,
                TXT_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_NOT_CORRECT_INFORMATION,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseIsNoticeCorrect(),
                respondentSumType.getEt3ResponseCorrectNoticeDetails());
        // END OF NOTICE PERIOD DETAILS
        // PENSION & BENEFIT DETAILS
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_YES,
                YES_LOWERCASE,
                YES_CAPITALISED,
                respondentSumType.getEt3ResponseIsPensionCorrect());
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NO,
                NO_LOWERCASE,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseIsPensionCorrect());
        putPdfCheckboxFieldWhenOther(pdfFields,
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NOT_APPLICABLE,
                NO_LOWERCASE,
                List.of(NO_CAPITALISED, YES_CAPITALISED),
                respondentSumType.getEt3ResponseIsPensionCorrect());
        putConditionalPdfField(pdfFields,
                TXT_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_NOT_CORRECT_INFORMATION,
                NO_CAPITALISED,
                respondentSumType.getEt3ResponseIsPensionCorrect(),
                respondentSumType.getEt3ResponsePensionCorrectDetails());
    }

    private static String getPaymentFrequencyCheckboxValue(String payFrequency, String expectedPayFrequency) {
        if (!expectedPayFrequency.equalsIgnoreCase(payFrequency)) {
            return STRING_EMPTY;
        }
        return switch (payFrequency) {
            case WEEKLY_CAPITALISED -> WEEKLY_LOWERCASE;
            case MONTHLY_CAPITALISED -> MONTHLY_LOWERCASE;
            case ANNUALLY_CAPITALISED -> ANNUALLY_LOWERCASE;
            default -> STRING_EMPTY;
        };
    }
}
