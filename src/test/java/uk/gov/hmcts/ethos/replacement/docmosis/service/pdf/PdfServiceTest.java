package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ResourceLoader;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceConstants.PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceTestConstants.TEST_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceTestConstants.TEST_DOCUMENT_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceTestConstants.TEST_DOCUMENT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceTestConstants.TEST_DOCUMENT_URL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceTestConstants.TEST_PDF_TEMPLATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfServiceTestConstants.TEST_USER_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

class PdfServiceTest {

    PdfService pdfService;
    DocumentManagementService documentManagementService;

    @BeforeEach
    public void setUp() throws IOException {
        mockDocumentManagement();
        pdfService = new PdfService(documentManagementService);
    }

    private void mockDocumentManagement() {
        documentManagementService = mock(DocumentManagementService.class);
        String documentUrl = TEST_DOCUMENT_URL;
        URI uri = URI.create(documentUrl);
        when(documentManagementService.uploadDocument(anyString(), any(byte[].class),
                anyString(), anyString(), anyString())).thenReturn(uri);
        when(documentManagementService.generateDownloadableURL(uri)).thenReturn(documentUrl);
        when(documentManagementService.generateMarkupDocument(anyString())).thenReturn(TEST_DOCUMENT_MARKUP);
    }

    @ParameterizedTest
    @MethodSource("provideGeneratePdfDocumentInfoTestData")
    @SneakyThrows
    void testGeneratePdfDocumentInfo(
            CaseData caseData, String userToken, String caseTypeId, String documentName, String pdfTemplate) {
        if (ObjectUtils.isEmpty(caseData)) {
            assertThatThrownBy(() -> pdfService.generatePdfDocumentInfo(caseData, userToken, caseTypeId, documentName,
                    pdfTemplate)).hasMessage(PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY);
            return;
        }
        if (isExceptionThrownForEmptyStringByMessage(userToken, PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY,
                caseData, userToken, caseTypeId, documentName, pdfTemplate)) {
            return;
        }
        if (isExceptionThrownForEmptyStringByMessage(caseTypeId, PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY,
                caseData, userToken, caseTypeId, documentName, pdfTemplate)) {
            return;
        }
        if (isExceptionThrownForEmptyStringByMessage(documentName, PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY,
                caseData, userToken, caseTypeId, documentName, pdfTemplate)) {
            return;
        }
        if (isExceptionThrownForEmptyStringByMessage(pdfTemplate, PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY,
                caseData, userToken, caseTypeId, documentName, pdfTemplate)) {
            return;
        }
        DocumentInfo documentInfo = pdfService.generatePdfDocumentInfo(
                caseData, userToken, caseTypeId, documentName, pdfTemplate);

        assertThat(documentInfo.getType()).isEqualTo(APPLICATION_PDF_VALUE);
        assertThat(documentInfo.getUrl()).contains(TEST_DOCUMENT_URL);
        assertThat(documentInfo.getDescription()).isEqualTo(TEST_DOCUMENT_NAME);
        assertThat(documentInfo.getMarkUp()).isEqualTo(TEST_DOCUMENT_MARKUP);
    }

    private boolean isExceptionThrownForEmptyStringByMessage(String stringValue, String message, CaseData caseData,
                                                       String userToken, String caseTypeId, String documentName,
                                                       String pdfTemplate) {
        if (StringUtils.isBlank(stringValue)) {
            assertThatThrownBy(() -> pdfService
                    .generatePdfDocumentInfo(caseData, userToken, caseTypeId, documentName, pdfTemplate))
                    .hasMessage(message);
            return true;
        }
        return false;
    }

    private static Stream<Arguments> provideGeneratePdfDocumentInfoTestData() {
        CaseData caseData = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        return Stream.of(Arguments.of(
                caseData, TEST_USER_TOKEN, TEST_CASE_TYPE_ID, TEST_DOCUMENT_NAME, TEST_PDF_TEMPLATE),
                Arguments.of(
                        null, TEST_USER_TOKEN, TEST_CASE_TYPE_ID, TEST_DOCUMENT_NAME, TEST_PDF_TEMPLATE),
                Arguments.of(
                        caseData, null, TEST_CASE_TYPE_ID, TEST_DOCUMENT_NAME, TEST_PDF_TEMPLATE),
                Arguments.of(
                        caseData, TEST_USER_TOKEN, null, TEST_DOCUMENT_NAME, TEST_PDF_TEMPLATE),
                Arguments.of(
                        caseData, TEST_USER_TOKEN, TEST_CASE_TYPE_ID, null, TEST_PDF_TEMPLATE),
                Arguments.of(
                        caseData, TEST_USER_TOKEN, TEST_CASE_TYPE_ID, TEST_DOCUMENT_NAME, null));
    }

}
