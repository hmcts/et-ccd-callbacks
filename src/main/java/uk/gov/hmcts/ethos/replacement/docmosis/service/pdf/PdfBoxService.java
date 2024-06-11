package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.EMPTY_BYTE_ARRAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.ET3_FORM_BYTE_ARRAY_CREATION_METHOD_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_DOCUMENT_CREATED_LOG_INFO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_OUTPUT_FILE_NAME_PREFIX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_CLASS_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_ERROR_NOT_ABLE_TO_MAP_CASE_DATA_TO_TEMPLATE_PDF;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_REQUIRED_FIELD_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_UNABLE_TO_PUT_FIELD_TO_PDF_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PUT_PDF_FIELD_METHOD_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.STREAM_CLOSURE_CLASS_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.UNABLE_TO_CLOSE_STREAM_FOR_PDF_TEMPLATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.UNABLE_TO_PROCESS_PDF_SOURCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CASE_DATA_NOT_FOUND_EXCEPTION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormMapper.mapEt3Form;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfBoxServiceUtil.logException;

/**
 * Service to support ET3 Response journey. Contains methods for generating and saving ET3 Response documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfBoxService {

    private final DocumentManagementService documentManagementService;
    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    /**
     * This method calls the ET3Mapper method to create the data to be passed through to dm store method
     * and then checks whether  it can reach the service.
     * @param caseData contains the data needed to generate the PDF
     * @param userToken contains the user authentication token
     * @param caseTypeId reference for which casetype the document is being uploaded to
     * @param documentName name of the document
     * @param pdfTemplate location of the pdf template to be mapped with case data
     * @return DocumentInfo which contains the URL and markup of the uploaded document
     * @throws IOException if the call to Tornado has failed, an exception will be thrown. This could be due to
    timeout or maybe a bad gateway.
     */
    public DocumentInfo generatePdfDocumentInfo(CaseData caseData, String userToken, String caseTypeId,
                                                String documentName, String pdfTemplate)
            throws IOException, PdfBoxServiceException {
        if (ObjectUtils.isEmpty(caseData)) {
            Throwable exception = new Throwable(PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY);
            throw new PdfBoxServiceException(PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY, exception,
                    PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY, CASE_DATA_NOT_FOUND_EXCEPTION_MESSAGE,
                    PDF_SERVICE_CLASS_NAME, GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME);
        }
        throwPdfServiceExceptionWhenStringValueIsEmpty(userToken,
                PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY,
                PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY, caseData.getEthosCaseReference());
        throwPdfServiceExceptionWhenStringValueIsEmpty(caseTypeId,
                PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY,
                PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY, caseData.getEthosCaseReference());
        throwPdfServiceExceptionWhenStringValueIsEmpty(documentName,
                PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY,
                PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY, caseData.getEthosCaseReference());
        throwPdfServiceExceptionWhenStringValueIsEmpty(pdfTemplate,
                PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY,
                PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY, caseData.getEthosCaseReference());
        try {
            byte[] bytes = convertCaseToPdfAsByteArray(caseData, pdfTemplate);
            String dmStoreDocumentName = generatePdfFileName(caseData, documentName);
            return createDocumentInfoFromBytes(userToken, bytes, dmStoreDocumentName, caseTypeId);
        } catch (IOException ioe) {
            logException(String.format(UNABLE_TO_PROCESS_PDF_SOURCE, pdfTemplate),
                    caseData.getEthosCaseReference(), ioe.getMessage(), PDF_SERVICE_CLASS_NAME,
                    GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME);
            throw ioe;
        }
    }

    private static void throwPdfServiceExceptionWhenStringValueIsEmpty(String stringValue,
                                                                       String exceptionMessage,
                                                                       String firstWord,
                                                                       String caseReferenceNumber)
            throws PdfBoxServiceException {
        if (StringUtils.isBlank(stringValue)) {
            Throwable throwable = new Throwable(PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_REQUIRED_FIELD_EMPTY);
            throw new PdfBoxServiceException(exceptionMessage, throwable, firstWord, caseReferenceNumber,
                    PDF_SERVICE_CLASS_NAME, GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME);
        }
    }

    private static String generatePdfFileName(CaseData caseData, String documentName) {
        return String.format(documentName, ObjectUtils.isEmpty(caseData.getSubmitEt3Respondent())
                || StringUtils.isBlank(caseData.getSubmitEt3Respondent().getSelectedLabel())
                ? PDF_OUTPUT_FILE_NAME_PREFIX : caseData.getSubmitEt3Respondent().getSelectedLabel());
    }

    private DocumentInfo createDocumentInfoFromBytes(String authToken, byte[] bytes, String documentName,
                                                    String caseTypeId) {
        URI documentSelfPath = uploadDocument(documentName, authToken, bytes, caseTypeId);
        log.info(String.format(PDF_DOCUMENT_CREATED_LOG_INFO, documentSelfPath.toString()));
        String downloadUrl = documentManagementService.generateDownloadableURL(documentSelfPath);
        String markup = documentManagementService.generateMarkupDocument(downloadUrl);
        return generateDocumentInfo(documentName, documentSelfPath, markup);
    }

    private URI uploadDocument(String documentName, String authToken, byte[] bytes, String caseTypeId) {
        return documentManagementService.uploadDocument(authToken, bytes, documentName,
                    APPLICATION_PDF_VALUE, caseTypeId);
    }

    private DocumentInfo generateDocumentInfo(String documentName, URI documentSelfPath, String markupURL) {
        return DocumentInfo.builder()
                .type(APPLICATION_PDF_VALUE)
                .description(documentName)
                .markUp(markupURL)
                .url(ccdGatewayBaseUrl + documentSelfPath.getRawPath() + "/binary")
                .build();
    }

    /**
     * Populates a pdf document with data stored in the case data parameter.
     *
     * @param caseData  {@link CaseData} object with information in which to populate the pdf with
     * @param pdfSource file name of the pdf template used to create the pdf
     * @return a byte array of the generated pdf file.
     * @throws IOException if there is an issue reading the pdf template
     */
    public byte[] convertCaseToPdfAsByteArray(CaseData caseData, String pdfSource) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream stream = ObjectUtils.isEmpty(cl) || StringUtils.isBlank(pdfSource) ? null
                : cl.getResourceAsStream(pdfSource);
        if (stream != null) {
            try (PDDocument pdfDocument = Loader.loadPDF(Objects.requireNonNull(stream.readAllBytes()))) {
                PDDocumentCatalog pdDocumentCatalog = pdfDocument.getDocumentCatalog();
                PDAcroForm pdfForm = pdDocumentCatalog.getAcroForm();

                for (Map.Entry<String, Optional<String>> entry : mapEt3Form(caseData).entrySet()) {
                    String entryKey = entry.getKey();
                    Optional<String> entryValue = entry.getValue();
                    if (entryValue.isPresent() && StringUtils.isNotBlank(entryValue.get())) {
                        putPdfField(caseData, pdfForm, entryValue, entryKey);
                    }
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                pdfDocument.save(byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            } catch (PdfBoxServiceException e) {
                logException(PDF_SERVICE_ERROR_NOT_ABLE_TO_MAP_CASE_DATA_TO_TEMPLATE_PDF,
                        caseData.getEthosCaseReference(), e.getMessage(), PDF_SERVICE_CLASS_NAME,
                        ET3_FORM_BYTE_ARRAY_CREATION_METHOD_NAME);
            } finally {
                safeClose(stream, caseData);
            }
        }
        safeClose(stream, caseData);
        return EMPTY_BYTE_ARRAY;
    }

    private static void putPdfField(CaseData caseData, PDAcroForm pdfForm, Optional<String> entryValue,
                                    String entryKey) {
        try {
            PDField pdfField = pdfForm.getField(entryKey);
            pdfField.setValue(entryValue.orElse(STRING_EMPTY));
        } catch (IOException ioe) {
            String fieldValue = ObjectUtils.isNotEmpty(entryValue) && entryValue.isPresent()
                    ? entryValue.get() : STRING_EMPTY;
            logException(String.format(PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_UNABLE_TO_PUT_FIELD_TO_PDF_FILE,
                            fieldValue, entryKey),
                    caseData.getEthosCaseReference(), ioe.getMessage(),
                    PDF_SERVICE_CLASS_NAME, PUT_PDF_FIELD_METHOD_NAME);
        }
    }

    private static void safeClose(InputStream is, CaseData caseData) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                logException(UNABLE_TO_CLOSE_STREAM_FOR_PDF_TEMPLATE, caseData.getEthosCaseReference(), e.getMessage(),
                        PDF_SERVICE_CLASS_NAME, STREAM_CLOSURE_CLASS_NAME);
            }
        }
    }
}
