package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.commons.util.StringUtils.isBlank;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_COMMA_WITH_SPACE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_LINE_FEED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_SPACE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ACTUAL_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ADDRESS_COUNTRY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ADDRESS_COUNTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ADDRESS_LINE_1;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ADDRESS_LINE_2;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ADDRESS_LINE_3;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ADDRESS_POST_TOWN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_CHECK_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_EQUAL_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_EXPECTED_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_FIELD_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.cloneObject;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.formatDate;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putConditionalPdfField;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfAddressField;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfCheckboxFieldWhenActualValueContainsExpectedValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfCheckboxFieldWhenExpectedValueEqualsActualValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfCheckboxFieldWhenOther;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.putPdfTextField;

class PdfMapperUtilTest {

    private ConcurrentMap<String, Optional<String>> pdfFields;
    private RespondentSumType respondentSumType;
    private CaseData caseData;

    @BeforeEach
    void beforeEach() {
        caseData = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        pdfFields = new ConcurrentHashMap<>();
    }

    @ParameterizedTest
    @CsvSource(value = {"2024/05/13:2024/05/13:yyyy-MM-dd:dd-MM-yyyy", "2024-05-13:13-05-2024:yyyy-MM-dd:dd-MM-yyyy",
                        "null:null:yyyy-MM-dd:dd-MM-yyyy", "\"\":\"\":yyyy-MM-dd:dd-MM-yyyy",
                        "2024-05-20:20:yyyy-MM-dd:dd", "2024-05-20:05:yyyy-MM-dd:MM",
                        "2024-05-20:2024:yyyy-MM-dd:yyyy"}, delimiter = ':')
    void testFormatDate(String dateToFormat, String formattedDate, String existingDateFormat, String conversionFormat) {
        String actualValue = formatDate(dateToFormat, existingDateFormat, conversionFormat);
        assertThat(actualValue).isEqualTo(formattedDate);
    }

    @ParameterizedTest
    @CsvSource(value = {"null:null", "'':null", "'':''", "test_field:''", "test_field:null",
                        "test_field:test_value", "'':test_value"}, delimiter = ':', nullValues = {"null"})
    void testPutPdfTextField(String fieldName, String value) {
        putPdfTextField(pdfFields, fieldName, value);
        assumeTrue(isNotBlank(fieldName));
        assertThat(pdfFields.get(fieldName)).contains(isBlank(value) ? STRING_EMPTY : value);
    }

    @ParameterizedTest
    @CsvSource(value = {"null:null:null:null", "'':null:null:null", "'':'':'':''", "test_field:'':'':''",
                        "test_field:null:null:null", "test_field:expected_value:actual_value:put_value",
                        "test_field:equal_value:equal_value:put_value", "test_field:expected_value:'':put_value",
                        "'':test_value:equal_value:equal_value:put_value"},
            delimiter = ':', nullValues = {"null"})
    void testPutConditionalPdfTextField(String fieldName, String expectedValue, String actualValue, String valueToPut) {
        putConditionalPdfField(pdfFields, fieldName, expectedValue, actualValue, valueToPut);
        assumeTrue(isNotBlank(fieldName));
        assertThat(pdfFields.get(fieldName)).contains(isBlank(valueToPut) || isBlank(fieldName)
                || isBlank(expectedValue) || isBlank(actualValue)
                || !expectedValue.equalsIgnoreCase(actualValue) ? STRING_EMPTY : valueToPut);
    }

    @ParameterizedTest
    @CsvSource(value = {"null:null:null:null", "'':'':'':'':''", "testField:'':'':''", "testField:checkValue:'':''",
                        "testField:null:null:null", "testField:checkValue:null:null",
                        "testField:checkValue:expectedValue:null", "testField:checkValue:expectedValue:actualValue",
                        "testField:checkValue:equalValue:equalValue"},
            delimiter = ':', nullValues = {"null"})
    void testPutPdfCheckboxFieldWhenExpectedValueEqualsActualValue(String fieldName,
                                                                   String checkValue,
                                                                   String expectedValue,
                                                                   String actualValue) {
        putPdfCheckboxFieldWhenExpectedValueEqualsActualValue(
                pdfFields, fieldName, checkValue, expectedValue, actualValue);
        assumeTrue(isNotBlank(fieldName));
        assertThat(pdfFields.get(fieldName)).contains(isBlank(checkValue) || isBlank(expectedValue)
                || isBlank(actualValue) || !expectedValue.equalsIgnoreCase(actualValue) ? STRING_EMPTY : checkValue);
    }

    @ParameterizedTest
    @MethodSource("providePutPdfCheckboxFieldWhenExpectedValueContainsActualValueData")
    void testPutPdfCheckboxFieldWhenExpectedValueContainsActualValue(String fieldName,
                                                                   String checkValue,
                                                                   String expectedValue,
                                                                   List<String> actualValue) {
        putPdfCheckboxFieldWhenActualValueContainsExpectedValue(
                pdfFields, fieldName, checkValue, expectedValue, actualValue);
        assumeTrue(isNotBlank(fieldName));
        assertThat(pdfFields.get(fieldName)).contains(isBlank(checkValue) || isBlank(expectedValue)
                || isEmpty(actualValue) || !actualValue.contains(expectedValue) ? STRING_EMPTY : checkValue);
    }

    @ParameterizedTest
    @MethodSource("providePutPdfCheckboxFieldWhenOtherData")
    void testPutPdfCheckboxFieldWhenOther(String fieldName,
                                          String checkValue,
                                          List<String> expectedValueList,
                                          String actualValue) {
        putPdfCheckboxFieldWhenOther(pdfFields, fieldName, checkValue, expectedValueList, actualValue);
        assumeTrue(isNotBlank(fieldName));
        assertThat(pdfFields.get(fieldName)).contains(isBlank(checkValue) || isEmpty(expectedValueList)
                || isBlank(actualValue) || expectedValueList.contains(actualValue) ? STRING_EMPTY : checkValue);
    }

    @ParameterizedTest
    @MethodSource("providePutPdfAddressFieldData")
    void testPutPdfAddressField(Address address, String expectedAddress) {
        putPdfAddressField(pdfFields, TXT_PDF_RESPONDENT_FIELD_ADDRESS, address);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_ADDRESS)).contains(expectedAddress);
    }

    @Test
    void testCloneObject() {
        RespondentSumType clonedRespondent = cloneObject(respondentSumType, RespondentSumType.class);
        assertThat(caseData.getRespondentCollection().get(0).getValue()).isEqualTo(clonedRespondent);
    }

    private static Stream<Arguments> providePutPdfCheckboxFieldWhenExpectedValueContainsActualValueData() {
        return Stream.of(Arguments.of(null, null, null, null),
                Arguments.of(STRING_EMPTY, STRING_EMPTY, STRING_EMPTY, List.of(STRING_EMPTY)),
                Arguments.of(TEST_FIELD_NAME, STRING_EMPTY, STRING_EMPTY, List.of(STRING_EMPTY)),
                Arguments.of(TEST_FIELD_NAME, TEST_CHECK_VALUE, STRING_EMPTY, List.of(STRING_EMPTY)),
                Arguments.of(TEST_FIELD_NAME, TEST_CHECK_VALUE, TEST_EXPECTED_VALUE, List.of(STRING_EMPTY)),
                Arguments.of(TEST_FIELD_NAME, null, null, null),
                Arguments.of(TEST_FIELD_NAME, TEST_CHECK_VALUE, null, null),
                Arguments.of(TEST_FIELD_NAME, TEST_CHECK_VALUE, TEST_EXPECTED_VALUE, null),
                Arguments.of(TEST_FIELD_NAME, TEST_CHECK_VALUE, TEST_EXPECTED_VALUE, null),
                Arguments.of(TEST_FIELD_NAME, TEST_CHECK_VALUE, TEST_EXPECTED_VALUE,
                        List.of(TEST_ACTUAL_VALUE)),
                Arguments.of(TEST_FIELD_NAME, TEST_CHECK_VALUE, TEST_EXPECTED_VALUE,
                        List.of(TEST_ACTUAL_VALUE)),
                Arguments.of(TEST_FIELD_NAME, TEST_CHECK_VALUE, TEST_EQUAL_VALUE,
                        List.of(TEST_EQUAL_VALUE))
        );
    }

    private static Stream<Arguments> providePutPdfAddressFieldData() {
        Address addressWithEmptyValues = createAddressObject(STRING_EMPTY,
                STRING_EMPTY, STRING_EMPTY, STRING_EMPTY, STRING_EMPTY, STRING_EMPTY);
        Address addressWithNullValues = createAddressObject(null,
                null, null, null, null, null);
        Address addressWithAllValues = createAddressObject(TEST_ADDRESS_LINE_1, TEST_ADDRESS_LINE_2,
            TEST_ADDRESS_LINE_3, TEST_ADDRESS_POST_TOWN, TEST_ADDRESS_COUNTY, TEST_ADDRESS_COUNTRY);
        Address addressWithoutLine1 = createAddressObject(null, TEST_ADDRESS_LINE_2,
                TEST_ADDRESS_LINE_3, TEST_ADDRESS_POST_TOWN, TEST_ADDRESS_COUNTY, TEST_ADDRESS_COUNTRY);
        Address addressWithoutLine1Line2 = createAddressObject(null, null,
                TEST_ADDRESS_LINE_3, TEST_ADDRESS_POST_TOWN, TEST_ADDRESS_COUNTY, TEST_ADDRESS_COUNTRY);
        Address addressWithoutLine1Line2Line3 = createAddressObject(null, null,
                null, TEST_ADDRESS_POST_TOWN, TEST_ADDRESS_COUNTY, TEST_ADDRESS_COUNTRY);
        Address addressWithoutLine1Line2Line3PostTown = createAddressObject(null, null,
                null, null, TEST_ADDRESS_COUNTY, TEST_ADDRESS_COUNTRY);
        Address addressWithoutLine1Line2Line3PostTownCounty = createAddressObject(null, null,
                null, null, null, TEST_ADDRESS_COUNTRY);
        return Stream.of(Arguments.of(null, STRING_EMPTY),
                Arguments.of(addressWithEmptyValues, STRING_EMPTY),
                Arguments.of(addressWithNullValues, STRING_EMPTY),
                Arguments.of(addressWithAllValues, TEST_ADDRESS_LINE_1 + STRING_SPACE + TEST_ADDRESS_LINE_2
                        + STRING_SPACE + TEST_ADDRESS_LINE_3 + STRING_LINE_FEED + TEST_ADDRESS_POST_TOWN
                        + STRING_COMMA_WITH_SPACE + TEST_ADDRESS_COUNTY + STRING_LINE_FEED + TEST_ADDRESS_COUNTRY),
                Arguments.of(addressWithoutLine1, TEST_ADDRESS_LINE_2 + STRING_SPACE + TEST_ADDRESS_LINE_3
                        + STRING_LINE_FEED + TEST_ADDRESS_POST_TOWN + STRING_COMMA_WITH_SPACE + TEST_ADDRESS_COUNTY
                        + STRING_LINE_FEED + TEST_ADDRESS_COUNTRY),
                Arguments.of(addressWithoutLine1Line2, TEST_ADDRESS_LINE_3 + STRING_LINE_FEED
                        + TEST_ADDRESS_POST_TOWN + STRING_COMMA_WITH_SPACE + TEST_ADDRESS_COUNTY
                        + STRING_LINE_FEED + TEST_ADDRESS_COUNTRY),
                Arguments.of(addressWithoutLine1Line2Line3, TEST_ADDRESS_POST_TOWN + STRING_COMMA_WITH_SPACE
                        + TEST_ADDRESS_COUNTY + STRING_LINE_FEED + TEST_ADDRESS_COUNTRY),
                Arguments.of(addressWithoutLine1Line2Line3PostTown, TEST_ADDRESS_COUNTY + STRING_LINE_FEED
                        + TEST_ADDRESS_COUNTRY),
                Arguments.of(addressWithoutLine1Line2Line3PostTownCounty, TEST_ADDRESS_COUNTRY)
        );
    }

    private static Address createAddressObject(String addressLine1, String addressLine2, String addressLine3,
                                               String postTown, String county, String country) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        address.setPostTown(postTown);
        address.setCounty(county);
        address.setCountry(country);
        return address;
    }

    private static Stream<Arguments> providePutPdfCheckboxFieldWhenOtherData() {
        return Stream.of(Arguments.of(null, null, null, null),
                            Arguments.of(STRING_EMPTY, STRING_EMPTY, List.of(), STRING_EMPTY),
                            Arguments.of(STRING_EMPTY, STRING_EMPTY, List.of(STRING_EMPTY), STRING_EMPTY),
                            Arguments.of(STRING_EMPTY, STRING_EMPTY, List.of(STRING_EMPTY), STRING_EMPTY),
                            Arguments.of(TEST_FIELD_NAME, STRING_EMPTY, null, STRING_EMPTY),
                            Arguments.of(TEST_FIELD_NAME, YES_LOWERCASE,
                                    List.of(YES_CAPITALISED, NO_CAPITALISED), YES_CAPITALISED),
                            Arguments.of(TEST_FIELD_NAME, NO_LOWERCASE,
                                    List.of(YES_CAPITALISED, NO_CAPITALISED), NO_CAPITALISED)
        );
    }
}
