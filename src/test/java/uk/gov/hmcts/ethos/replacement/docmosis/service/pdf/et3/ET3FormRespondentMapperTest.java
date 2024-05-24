package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.ResourceLoader;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormRespondentMapper.mapRespondent;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_CONTACT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_CONTACT_TYPE_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_DX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMPLOYEE_NUMBER_GREAT_BRITAIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_HEARING_TYPE_PHONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_HEARING_TYPE_VIDEO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_MOBILE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_POSTCODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfUtil.cloneObject;

class ET3FormRespondentMapperTest {

    private ConcurrentMap<String, Optional<String>> pdfFields;

    @BeforeEach
    void beforeEach() {
        pdfFields = new ConcurrentHashMap<>();
    }

    @ParameterizedTest
    @MethodSource("provideMapRespondentTestData")
    void testMapRespondent(RespondentSumType respondentSumType) {
        mapRespondent(respondentSumType, pdfFields);
        String selectedTitle = respondentSumType.getEt3ResponseRespondentPreferredTitle();
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MR)).contains(
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR.equals(selectedTitle)
                        ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MS)).contains(
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS.equals(selectedTitle)
                        ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MRS)).contains(
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS.equals(selectedTitle)
                        ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MISS)).contains(
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS.equals(selectedTitle)
                        ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_OTHER)).contains(isOtherTitle(selectedTitle)
                ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_TITLE_OTHER)).contains(isOtherTitle(selectedTitle)
                ? selectedTitle : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_NAME)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_NAME);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_NUMBER)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_NUMBER);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_TYPE)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_TYPE);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_CONTACT_NAME))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_CONTACT_NAME);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_POSTCODE))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_POSTCODE);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_DX)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_DX);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_PHONE_NUMBER))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_PHONE_NUMBER);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_MOBILE_NUMBER))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_MOBILE_NUMBER);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_EMAIL))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_CONTACT_TYPE_EMAIL);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_POST)).contains(STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_EMAIL)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMAIL);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_VIDEO))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_HEARING_TYPE_VIDEO);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_PHONE))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_HEARING_TYPE_PHONE);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_GREAT_BRITAIN))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMPLOYEE_NUMBER_GREAT_BRITAIN);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_NO))
                .contains(STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_ADDRESS))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_ADDRESS);

    }

    private static boolean isOtherTitle(String selectedTitle) {
        return isNotBlank(selectedTitle)
                && !CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR.equals(selectedTitle)
                && !CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS.equals(selectedTitle)
                && !CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS.equals(selectedTitle)
                && !CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS.equals(selectedTitle);
    }

    private static Stream<RespondentSumType> provideMapRespondentTestData() {
        CaseData caseData = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        RespondentSumType respondentSumType = caseData.getRespondentCollection().stream()
                .filter(r -> caseData.getSubmitEt3Respondent()
                        .getSelectedLabel().equals(r.getValue().getRespondentName()))
                .toList().get(0).getValue();
        RespondentSumType respondentSumTypeTitleMr = cloneObject(respondentSumType, RespondentSumType.class);
        respondentSumTypeTitleMr
                .setEt3ResponseRespondentPreferredTitle(CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR);
        RespondentSumType respondentSumTypeTitleMrs = cloneObject(respondentSumTypeTitleMr, RespondentSumType.class);
        respondentSumTypeTitleMrs
                .setEt3ResponseRespondentPreferredTitle(CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS);
        RespondentSumType respondentSumTypeTitleMs = cloneObject(respondentSumTypeTitleMr, RespondentSumType.class);
        respondentSumTypeTitleMs
                .setEt3ResponseRespondentPreferredTitle(CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS);
        RespondentSumType respondentSumTypeTitleMiss = cloneObject(respondentSumTypeTitleMr, RespondentSumType.class);
        respondentSumTypeTitleMiss
                .setEt3ResponseRespondentPreferredTitle(CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS);
        RespondentSumType respondentSumTypeTitleOther = cloneObject(respondentSumTypeTitleMr, RespondentSumType.class);
        respondentSumTypeTitleOther
                .setEt3ResponseRespondentPreferredTitle(CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_OTHER);
        return Stream.of(respondentSumType, respondentSumTypeTitleMr, respondentSumTypeTitleMrs,
                respondentSumTypeTitleMs, respondentSumTypeTitleMiss, respondentSumTypeTitleOther);
    }

}
