package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.ET3_FORM_SAMPLE_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.addPdfField;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.cloneObject;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.formatDate;

class PdfMapperUtilTest {

    private ConcurrentMap<String, Optional<String>> pdfFields;
    private CaseData caseData;

    @BeforeEach
    void beforeEach() {
        caseData = ResourceLoader.fromString(ET3_FORM_SAMPLE_CASE_DATA_FILE, CaseData.class);
        pdfFields = new ConcurrentHashMap<>();
    }

    @ParameterizedTest
    @CsvSource(value = {"2024/05/13:2024/05/13", "2024-05-13:13-05-2024", "null:null", "\"\":\"\""}, delimiter = ':')
    void testFormatDate(String dateToFormat, String formattedDate) {
        String actualValue = formatDate(dateToFormat);
        assertThat(actualValue).isEqualTo(formattedDate);
    }

    @ParameterizedTest
    @CsvSource(value = {"null:null", "\"\":null", "test_field:\"\"", "test_field:null", "test_field:test_value"},
            delimiter = ':')
    void testAddPdfFile(String fieldName, String value) {
        addPdfField(pdfFields, fieldName, value);
        assertThat(pdfFields.get(fieldName)).contains(value);
    }

    @Test
    void testCloneObject() {
        RespondentSumType clonedRespondent = cloneObject(caseData.getRespondentCollection().get(0).getValue(),
                RespondentSumType.class);
        assertThat(caseData.getRespondentCollection().get(0).getValue()).isEqualTo(clonedRespondent);
    }

}
