package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormEarningsAndBenefitsMapper.mapEarningsAndBenefits;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_DUMMY_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_CORRECT_HOURS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_CORRECT_NOTICE_PERIOD;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_CORRECT_PENSION_AND_OTHER_BENEFITS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_PAY_BEFORE_TAX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_PAY_TAKE_HOME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCheckBoxNotApplicableValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCheckboxValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCorrectedCheckboxValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCorrectedDetailValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormUtil.cloneObject;

class ET3FormEarningsAndBenefitsMapperTest {

    private ConcurrentMap<String, Optional<String>> pdfFields;

    @BeforeEach
    void beforeEach() {

        pdfFields = new ConcurrentHashMap<>();
    }

    @ParameterizedTest
    @MethodSource("provideMapEarningsAndBenefitsTestData")
    void testMapClaimant(RespondentSumType respondentSumType) {
        mapEarningsAndBenefits(respondentSumType, pdfFields);
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                        List.of(NO_CAPITALISED, YES_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_WORK_HOURS_DETAILS))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponseClaimantCorrectHours(),
                        TEST_PDF_EARNINGS_BENEFITS_CORRECT_HOURS));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        List.of(NO_CAPITALISED, YES_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponsePayBeforeTax(),
                        TEST_PDF_EARNINGS_BENEFITS_PAY_BEFORE_TAX));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_WEEKLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), WEEKLY_CAPITALISED,
                        WEEKLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_MONTHLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), MONTHLY_CAPITALISED,
                        MONTHLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_ANNUALLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), ANNUALLY_CAPITALISED,
                        ANNUALLY_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponsePayTakehome(),
                        TEST_PDF_EARNINGS_BENEFITS_PAY_TAKE_HOME));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_WEEKLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), WEEKLY_CAPITALISED,
                        WEEKLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_MONTHLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), MONTHLY_CAPITALISED,
                        MONTHLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_ANNUALLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), ANNUALLY_CAPITALISED,
                        ANNUALLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                        List.of(NO_CAPITALISED, YES_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_NOT_CORRECT_INFORMATION))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponseCorrectNoticeDetails(),
                        TEST_PDF_EARNINGS_BENEFITS_CORRECT_NOTICE_PERIOD));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsPensionCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsPensionCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseIsPensionCorrect(),
                        List.of(NO_CAPITALISED, YES_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_NOT_CORRECT_INFORMATION))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseIsPensionCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponsePensionCorrectDetails(),
                        TEST_PDF_EARNINGS_BENEFITS_CORRECT_PENSION_AND_OTHER_BENEFITS));
    }

    private static Stream<RespondentSumType> provideMapEarningsAndBenefitsTestData() {
        CaseData caseData = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        RespondentSumType respondentSumTypeWithAllValues =
                caseData.getRespondentCollection().stream().filter(r -> caseData.getSubmitEt3Respondent()
                        .getSelectedLabel().equals(r.getValue().getRespondentName()))
                .toList().get(0).getValue();

        RespondentSumType respondentSumTypeClaimantWorkHoursYes = cloneObject(respondentSumTypeWithAllValues,
                RespondentSumType.class);
        respondentSumTypeClaimantWorkHoursYes.setEt3ResponseClaimantWeeklyHours(YES_CAPITALISED);

        RespondentSumType respondentSumTypeClaimantWorkHoursNotApplicableDummy =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeClaimantWorkHoursNotApplicableDummy.setEt3ResponseClaimantWeeklyHours(TEST_DUMMY_VALUE);

        RespondentSumType respondentSumTypeClaimantWorkHoursNotApplicableEmpty =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeClaimantWorkHoursNotApplicableEmpty.setEt3ResponseClaimantWeeklyHours(STRING_EMPTY);

        RespondentSumType respondentSumTypeClaimantWorkHoursNotApplicableNull =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeClaimantWorkHoursNotApplicableNull.setEt3ResponseClaimantWeeklyHours(null);

        RespondentSumType respondentSumTypeEarningDetailsCorrectYes = cloneObject(respondentSumTypeWithAllValues,
                RespondentSumType.class);
        respondentSumTypeEarningDetailsCorrectYes.setEt3ResponseEarningDetailsCorrect(YES_CAPITALISED);

        RespondentSumType respondentSumTypeEarningDetailsCorrectNotApplicableDummy =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeEarningDetailsCorrectNotApplicableDummy.setEt3ResponseEarningDetailsCorrect(TEST_DUMMY_VALUE);

        RespondentSumType respondentSumTypeEarningDetailsCorrectNotApplicableEmpty =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeEarningDetailsCorrectNotApplicableEmpty.setEt3ResponseEarningDetailsCorrect(STRING_EMPTY);

        RespondentSumType respondentSumTypeEarningDetailsCorrectNotApplicableNull =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeEarningDetailsCorrectNotApplicableNull.setEt3ResponseEarningDetailsCorrect(null);

        RespondentSumType respondentSumTypeEarningDetailsPayFrequencyWeekly =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeEarningDetailsPayFrequencyWeekly.setEt3ResponsePayFrequency(WEEKLY_CAPITALISED);

        RespondentSumType respondentSumTypeEarningDetailsPayFrequencyMonthly =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeEarningDetailsPayFrequencyMonthly.setEt3ResponsePayFrequency(MONTHLY_CAPITALISED);

        RespondentSumType respondentSumTypeEarningDetailsPayFrequencyAnnually =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeEarningDetailsPayFrequencyAnnually.setEt3ResponsePayFrequency(ANNUALLY_CAPITALISED);

        RespondentSumType respondentSumTypeIsNoticeCorrectYes =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeIsNoticeCorrectYes.setEt3ResponseIsNoticeCorrect(YES_CAPITALISED);

        RespondentSumType respondentSumTypeIsNoticeCorrectNotApplicableDummy =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeIsNoticeCorrectNotApplicableDummy.setEt3ResponseIsNoticeCorrect(TEST_DUMMY_VALUE);

        RespondentSumType respondentSumTypeIsNoticeCorrectNotApplicableEmpty =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeIsNoticeCorrectNotApplicableEmpty.setEt3ResponseIsNoticeCorrect(STRING_EMPTY);

        RespondentSumType respondentSumTypeIsNoticeCorrectNotApplicableNull =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeIsNoticeCorrectNotApplicableNull.setEt3ResponseIsNoticeCorrect(null);

        RespondentSumType respondentSumTypeIsPensionCorrectYes =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeIsPensionCorrectYes.setEt3ResponseIsPensionCorrect(YES_CAPITALISED);

        RespondentSumType respondentSumTypeIsPensionCorrectNotApplicableDummy =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeIsPensionCorrectNotApplicableDummy.setEt3ResponseIsPensionCorrect(TEST_DUMMY_VALUE);

        RespondentSumType respondentSumTypeIsPensionCorrectNotApplicableEmpty =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeIsPensionCorrectNotApplicableEmpty.setEt3ResponseIsPensionCorrect(STRING_EMPTY);

        RespondentSumType respondentSumTypeIsPensionCorrectNotApplicableNull =
                cloneObject(respondentSumTypeWithAllValues, RespondentSumType.class);
        respondentSumTypeIsPensionCorrectNotApplicableNull.setEt3ResponseIsPensionCorrect(null);

        return Stream.of(respondentSumTypeWithAllValues,
                respondentSumTypeClaimantWorkHoursYes,
                respondentSumTypeClaimantWorkHoursNotApplicableDummy,
                respondentSumTypeClaimantWorkHoursNotApplicableEmpty,
                respondentSumTypeClaimantWorkHoursNotApplicableNull,
                respondentSumTypeEarningDetailsCorrectYes,
                respondentSumTypeEarningDetailsCorrectNotApplicableDummy,
                respondentSumTypeEarningDetailsCorrectNotApplicableEmpty,
                respondentSumTypeEarningDetailsCorrectNotApplicableNull,
                respondentSumTypeEarningDetailsPayFrequencyWeekly,
                respondentSumTypeEarningDetailsPayFrequencyMonthly,
                respondentSumTypeEarningDetailsPayFrequencyAnnually,
                respondentSumTypeIsNoticeCorrectYes,
                respondentSumTypeIsNoticeCorrectNotApplicableDummy,
                respondentSumTypeIsNoticeCorrectNotApplicableEmpty,
                respondentSumTypeIsNoticeCorrectNotApplicableNull,
                respondentSumTypeIsPensionCorrectYes,
                respondentSumTypeIsPensionCorrectNotApplicableDummy,
                respondentSumTypeIsPensionCorrectNotApplicableEmpty,
                respondentSumTypeIsPensionCorrectNotApplicableNull
        );
    }

}
