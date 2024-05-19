package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.ResourceLoader;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_DATE_RECEIVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_RFT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_NOT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormHeaderMapper.mapHeader;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_HEADER_VALUE_CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_HEADER_VALUE_DATE_RECEIVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfMapperUtil.cloneObject;

class ET3FormHeaderMapperTest {

    private ConcurrentMap<String, Optional<String>> pdfFields;

    @BeforeEach
    void beforeEach() {
        pdfFields = new ConcurrentHashMap<>();
    }

    @ParameterizedTest
    @MethodSource("provideMapHeaderTestData")
    void testMapHeader(CaseData caseData, RespondentSumType respondentSumType) {
        mapHeader(caseData, respondentSumType, pdfFields);
        assertThat(pdfFields.get(TXT_PDF_HEADER_FIELD_CASE_NUMBER)).contains(TEST_PDF_HEADER_VALUE_CASE_NUMBER);
        assertThat(pdfFields.get(TXT_PDF_HEADER_FIELD_DATE_RECEIVED)).contains(TEST_PDF_HEADER_VALUE_DATE_RECEIVED);
        if (ObjectUtils.isEmpty(respondentSumType.getEt3ResponseRespondentSupportDocument())
                && ObjectUtils.isEmpty(respondentSumType.getEt3ResponseEmployerClaimDocument())
                && CollectionUtils.isEmpty(respondentSumType.getEt3ResponseContestClaimDocument())) {
            assertThat(pdfFields.get(TXT_PDF_HEADER_FIELD_RFT))
                    .contains(TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_NOT_EXISTS);
        } else {
            assertThat(pdfFields.get(TXT_PDF_HEADER_FIELD_RFT))
                    .contains(TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_EXISTS);
        }
    }

    private static Stream<Arguments> provideMapHeaderTestData() {
        CaseData caseData = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        RespondentSumType respondentSumTypeWithDocuments = caseData.getRespondentCollection().stream()
                .filter(r -> caseData.getSubmitEt3Respondent()
                        .getSelectedLabel().equals(r.getValue().getRespondentName()))
                .toList().get(0).getValue();
        RespondentSumType respondentSumTypeWithoutDocuments = cloneObject(respondentSumTypeWithDocuments,
                RespondentSumType.class);
        respondentSumTypeWithoutDocuments.setEt3ResponseRespondentSupportDocument(null);
        respondentSumTypeWithoutDocuments.setEt3ResponseEmployerClaimDocument(null);
        respondentSumTypeWithoutDocuments.setEt3ResponseContestClaimDocument(null);
        return Stream.of(Arguments.of(caseData, respondentSumTypeWithDocuments),
                Arguments.of(caseData, respondentSumTypeWithoutDocuments));
    }

}
