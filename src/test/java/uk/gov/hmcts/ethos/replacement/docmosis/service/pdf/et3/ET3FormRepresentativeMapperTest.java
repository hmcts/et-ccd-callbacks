package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ResourceLoader;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_REPRESENTATIVE_FIELD_COMMUNICATION_PREFERENCE_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_REPRESENTATIVE_FIELD_COMMUNICATION_PREFERENCE_POST;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_REPRESENTATIVE_FIELD_PHONE_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_REPRESENTATIVE_FIELD_VIDEO_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.EMAIL_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.EMAIL_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.POST_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.POST_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_MOBILE_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_ORGANISATION_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_POSTCODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_REFERENCE_FOR_CORRESPONDENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormRepresentativeMapper.mapRepresentative;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_DUMMY_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_MOBILE_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_ORGANISATION_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_POSTCODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_REFERENCE_FOR_CORRESPONDENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCheckboxValue;

class ET3FormRepresentativeMapperTest {

    private ConcurrentMap<String, Optional<String>> pdfFields;

    @BeforeEach
    void beforeEach() {

        pdfFields = new ConcurrentHashMap<>();
    }

    @ParameterizedTest
    @MethodSource("provideMapRepresentativeTestData")
    void testMapRepresentative(CaseData caseData, RespondentSumType respondentSumType) {
        mapRepresentative(caseData, respondentSumType, pdfFields);
        assumeTrue(ObjectUtils.isNotEmpty(caseData));
        assumeTrue(CollectionUtils.isNotEmpty(caseData.getRepCollection()));
        assumeTrue(ObjectUtils.isNotEmpty(caseData.getRepCollection().get(0)));
        assumeTrue(ObjectUtils.isNotEmpty(caseData.getRepCollection().get(0).getValue()));
        assumeTrue(!TEST_DUMMY_VALUE.equals(caseData.getRepCollection().get(0).getValue().getRespRepName()));
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_NAME)).contains(TEST_PDF_REPRESENTATIVE_NAME);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_ORGANISATION_NAME))
                .contains(TEST_PDF_REPRESENTATIVE_ORGANISATION_NAME);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_ADDRESS)).contains(TEST_PDF_REPRESENTATIVE_ADDRESS);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_POSTCODE)).contains(TEST_PDF_REPRESENTATIVE_POSTCODE);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_PHONE_NUMBER)).contains(isNotBlank(
                caseData.getRepCollection().get(0).getValue().getRepresentativePhoneNumber())
                ? TEST_PDF_REPRESENTATIVE_PHONE_NUMBER : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_MOBILE_PHONE_NUMBER)).contains(isNotBlank(
                caseData.getRepCollection().get(0).getValue().getRepresentativeMobileNumber())
                ? TEST_PDF_REPRESENTATIVE_MOBILE_PHONE_NUMBER : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_REFERENCE_FOR_CORRESPONDENCE)).contains(isNotBlank(
                caseData.getRepCollection().get(0).getValue().getRepresentativeReference())
                ? TEST_PDF_REPRESENTATIVE_REFERENCE_FOR_CORRESPONDENCE : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_REPRESENTATIVE_FIELD_COMMUNICATION_PREFERENCE_EMAIL)).contains(
                getCheckboxValue(caseData.getRepCollection().get(0).getValue().getRepresentativePreference(),
                        EMAIL_CAPITALISED, EMAIL_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_REPRESENTATIVE_FIELD_COMMUNICATION_PREFERENCE_POST)).contains(
                getCheckboxValue(caseData.getRepCollection().get(0).getValue().getRepresentativePreference(),
                        POST_CAPITALISED, POST_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_EMAIL_ADDRESS)).contains(isNotBlank(
                caseData.getRepCollection().get(0).getValue().getRepresentativeEmailAddress())
                ? TEST_PDF_REPRESENTATIVE_EMAIL_ADDRESS : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_REPRESENTATIVE_FIELD_VIDEO_HEARINGS)).contains(YES_CAPITALISED);
        assertThat(pdfFields.get(CHECKBOX_PDF_REPRESENTATIVE_FIELD_PHONE_HEARINGS)).contains(YES_CAPITALISED);
    }

    private static Stream<Arguments> provideMapRepresentativeTestData() {

        CaseData caseDataEmptyRepCollection = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataEmptyRepCollection.setRepCollection(null);

        CaseData caseDataEmptyWrongRep = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataEmptyWrongRep.getRepCollection().get(0).getValue().setRespRepName(TEST_DUMMY_VALUE);

        CaseData caseDataEmptyNonRep = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataEmptyNonRep.getRepCollection().get(0).setValue(null);

        CaseData caseDataRepresentativePhoneMobileNumbersExist =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataRepresentativePhoneMobileNumbersExist.getRepCollection().get(0).getValue()
                .setRepresentativePhoneNumber(TEST_PDF_REPRESENTATIVE_PHONE_NUMBER);
        caseDataRepresentativePhoneMobileNumbersExist.getRepCollection().get(0).getValue()
                .setRepresentativeMobileNumber(TEST_PDF_REPRESENTATIVE_MOBILE_PHONE_NUMBER);

        CaseData caseDataRepresentativeReferenceExist =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataRepresentativeReferenceExist.getRepCollection().get(0).getValue().setRepresentativeReference(
                TEST_PDF_REPRESENTATIVE_REFERENCE_FOR_CORRESPONDENCE);

        CaseData caseDataRepresentativeContactPreferenceEmail =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataRepresentativeContactPreferenceEmail.getRepCollection().get(0).getValue()
                .setRepresentativePreference(EMAIL_CAPITALISED);

        CaseData caseDataRepresentativeContactPreferencePost =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataRepresentativeContactPreferencePost.getRepCollection().get(0).getValue()
                .setRepresentativePreference(POST_CAPITALISED);
        CaseData caseData = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        RespondentSumType respondentSumTypeAllValues =
                caseData.getRespondentCollection().stream().filter(r -> caseData.getSubmitEt3Respondent()
                                .getSelectedLabel().equals(r.getValue().getRespondentName()))
                        .toList().get(0).getValue();

        return Stream.of(Arguments.of(null, respondentSumTypeAllValues),
                Arguments.of(caseDataEmptyRepCollection, respondentSumTypeAllValues),
                Arguments.of(caseDataEmptyWrongRep, respondentSumTypeAllValues),
                Arguments.of(caseDataEmptyNonRep, respondentSumTypeAllValues),
                Arguments.of(caseData, respondentSumTypeAllValues),
                Arguments.of(caseDataRepresentativePhoneMobileNumbersExist, respondentSumTypeAllValues),
                Arguments.of(caseDataRepresentativeReferenceExist, respondentSumTypeAllValues),
                Arguments.of(caseDataRepresentativeContactPreferenceEmail, respondentSumTypeAllValues),
                Arguments.of(caseDataRepresentativeContactPreferencePost, respondentSumTypeAllValues)
        );

    }

}
