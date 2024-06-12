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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_DATES_FURTHER_INFO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_END_DATE_DAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_END_DATE_MONTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_END_DATE_YEAR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_START_DATE_DAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_START_DATE_MONTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_START_DATE_YEAR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormEmploymentMapper.mapEmployment;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_DUMMY_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_CORRECT_JOB_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_DATE_INFORMATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_END_DAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_END_MONTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_END_YEAR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_START_DAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_START_MONTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_START_YEAR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCheckBoxNotApplicableValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCheckboxValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCorrectedDetailValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormUtil.cloneObject;

class ET3FormEmploymentMapperTest {

    private ConcurrentMap<String, Optional<String>> pdfFields;

    @BeforeEach
    void beforeEach() {
        pdfFields = new ConcurrentHashMap<>();
    }

    @ParameterizedTest
    @MethodSource("provideMapEmploymentTestData")
    void testMapClaimant(RespondentSumType respondentSumType) {
        mapEmployment(respondentSumType, pdfFields);
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseAreDatesCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseAreDatesCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseAreDatesCorrect(),
                        List.of(YES_CAPITALISED, NO_CAPITALISED), NO_LOWERCASE));

        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_START_DATE_DAY))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentStartDate(), TEST_PDF_EMPLOYMENT_START_DAY));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_START_DATE_MONTH))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentStartDate(), TEST_PDF_EMPLOYMENT_START_MONTH));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_START_DATE_YEAR))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentStartDate(), TEST_PDF_EMPLOYMENT_START_YEAR));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_END_DATE_DAY))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentEndDate(), TEST_PDF_EMPLOYMENT_END_DAY));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_END_DATE_MONTH))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentEndDate(), TEST_PDF_EMPLOYMENT_END_MONTH));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_END_DATE_YEAR))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentEndDate(), TEST_PDF_EMPLOYMENT_END_YEAR));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_DATES_FURTHER_INFO))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponseEmploymentInformation(),
                        TEST_PDF_EMPLOYMENT_DATE_INFORMATION));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseContinuingEmployment(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseContinuingEmployment(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseContinuingEmployment(),
                        List.of(YES_CAPITALISED, NO_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsJobTitleCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsJobTitleCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseIsJobTitleCorrect(),
                        List.of(YES_CAPITALISED, NO_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_DETAILS))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseIsJobTitleCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseCorrectJobTitle(), TEST_PDF_EMPLOYMENT_CORRECT_JOB_TITLE));
    }

    private static Stream<RespondentSumType> provideMapEmploymentTestData() {
        CaseData caseData = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        RespondentSumType respondentSumTypeEmploymentDatesIncorrect =
                caseData.getRespondentCollection().stream().filter(r -> caseData.getSubmitEt3Respondent()
                        .getSelectedLabel().equals(r.getValue().getRespondentName()))
                .toList().get(0).getValue();

        RespondentSumType respondentSumTypeEmploymentDatesCorrect =
                cloneObject(respondentSumTypeEmploymentDatesIncorrect, RespondentSumType.class);
        respondentSumTypeEmploymentDatesCorrect.setEt3ResponseAreDatesCorrect(YES_CAPITALISED);
        respondentSumTypeEmploymentDatesCorrect.setEt3ResponseEmploymentStartDate(null);
        respondentSumTypeEmploymentDatesCorrect.setEt3ResponseEmploymentEndDate(null);
        respondentSumTypeEmploymentDatesCorrect.setEt3ResponseEmploymentInformation(null);

        RespondentSumType respondentSumTypeEmploymentDatesNotApplicableNull =
                cloneObject(respondentSumTypeEmploymentDatesCorrect, RespondentSumType.class);
        respondentSumTypeEmploymentDatesNotApplicableNull.setEt3ResponseAreDatesCorrect(null);
        respondentSumTypeEmploymentDatesNotApplicableNull.setEt3ResponseEmploymentStartDate(null);
        respondentSumTypeEmploymentDatesNotApplicableNull.setEt3ResponseEmploymentEndDate(null);
        respondentSumTypeEmploymentDatesNotApplicableNull.setEt3ResponseEmploymentInformation(null);

        RespondentSumType respondentSumTypeEmploymentDatesNotApplicableDummyValue =
                cloneObject(respondentSumTypeEmploymentDatesCorrect, RespondentSumType.class);
        respondentSumTypeEmploymentDatesNotApplicableDummyValue.setEt3ResponseAreDatesCorrect(TEST_DUMMY_VALUE);
        respondentSumTypeEmploymentDatesNotApplicableDummyValue.setEt3ResponseEmploymentStartDate(null);
        respondentSumTypeEmploymentDatesNotApplicableDummyValue.setEt3ResponseEmploymentEndDate(null);
        respondentSumTypeEmploymentDatesNotApplicableDummyValue.setEt3ResponseEmploymentInformation(null);

        RespondentSumType respondentSumTypeEmploymentContinues =
                cloneObject(respondentSumTypeEmploymentDatesIncorrect, RespondentSumType.class);
        respondentSumTypeEmploymentContinues.setEt3ResponseContinuingEmployment(YES_CAPITALISED);

        RespondentSumType respondentSumTypeEmploymentContinuesNotApplicableNull =
                cloneObject(respondentSumTypeEmploymentDatesIncorrect, RespondentSumType.class);
        respondentSumTypeEmploymentContinuesNotApplicableNull.setEt3ResponseContinuingEmployment(null);

        RespondentSumType respondentSumTypeEmploymentContinuesNotApplicableDummyValue =
                cloneObject(respondentSumTypeEmploymentDatesIncorrect, RespondentSumType.class);
        respondentSumTypeEmploymentContinuesNotApplicableDummyValue.setEt3ResponseContinuingEmployment(STRING_EMPTY);

        RespondentSumType respondentSumTypeJobTitleCorrect =
                cloneObject(respondentSumTypeEmploymentDatesIncorrect, RespondentSumType.class);
        respondentSumTypeJobTitleCorrect.setEt3ResponseIsJobTitleCorrect(YES_CAPITALISED);
        respondentSumTypeJobTitleCorrect.setEt3ResponseCorrectJobTitle(null);

        RespondentSumType respondentSumTypeJobTitleNotApplicableNull =
                cloneObject(respondentSumTypeEmploymentDatesIncorrect, RespondentSumType.class);
        respondentSumTypeJobTitleNotApplicableNull.setEt3ResponseIsJobTitleCorrect(null);
        respondentSumTypeJobTitleNotApplicableNull.setEt3ResponseCorrectJobTitle(null);

        RespondentSumType respondentSumTypeJobTitleNotApplicableEmptyString =
                cloneObject(respondentSumTypeEmploymentDatesIncorrect, RespondentSumType.class);
        respondentSumTypeJobTitleNotApplicableEmptyString.setEt3ResponseIsJobTitleCorrect(STRING_EMPTY);
        respondentSumTypeJobTitleNotApplicableEmptyString.setEt3ResponseCorrectJobTitle(null);

        return Stream.of(respondentSumTypeEmploymentDatesIncorrect,
                respondentSumTypeEmploymentDatesCorrect,
                respondentSumTypeEmploymentDatesNotApplicableNull,
                respondentSumTypeEmploymentDatesNotApplicableDummyValue,
                respondentSumTypeEmploymentContinues,
                respondentSumTypeEmploymentContinuesNotApplicableNull,
                respondentSumTypeEmploymentContinuesNotApplicableDummyValue,
                respondentSumTypeJobTitleCorrect,
                respondentSumTypeJobTitleNotApplicableNull,
                respondentSumTypeJobTitleNotApplicableEmptyString
        );
    }

}
