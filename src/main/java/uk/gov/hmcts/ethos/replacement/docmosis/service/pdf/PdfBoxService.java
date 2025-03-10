package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.PdfServiceException;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;

import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.PDF_TYPE_ET3;
import static uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormConstants.ET3_FORM_CLIENT_TYPE_REPRESENTATIVE;
import static uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormConstants.SUBMIT_ET3;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.CASE_DATA_NOT_FOUND_EXCEPTION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_OUTPUT_FILE_NAME_PREFIX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_CLASS_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_REQUIRED_FIELD_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.UNABLE_TO_PROCESS_PDF_SOURCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtil.logException;

/**
 * Service to support ET3 Response journey. Contains methods for generating and saving ET3 Response documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfBoxService {

    private final TornadoService tornadoService;
    private final PdfService pdfService;

    @Value("${pdf.et3form}")
    public String et3EnglishPdfTemplateSource;

    /**
     * This method calls the ET3Mapper method to create the data to be passed through to dm store method
     * and then checks whether  it can reach the service.
     * @param caseData contains the data needed to generate the PDF
     * @param userToken contains the user authentication token
     * @param caseTypeId reference for which case type the document is being uploaded to
     * @param documentName name of the document
     * @param pdfTemplate location of the pdf template to be mapped with case data
     * @return DocumentInfo which contains the URL and markup of the uploaded document
     * @throws PdfServiceException if the call to Tornado has failed, an exception will be thrown. This could be due to
    timeout or maybe a bad gateway.
     */
    public DocumentInfo generatePdfDocumentInfo(CaseData caseData, String userToken, String caseTypeId,
                                                String documentName, String pdfTemplate)
            throws GenericServiceException, PdfServiceException {
        if (ObjectUtils.isEmpty(caseData)) {
            Throwable exception = new Throwable(PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY);
            throw new GenericServiceException(PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY, exception,
                    PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_CASE_DATA_EMPTY, CASE_DATA_NOT_FOUND_EXCEPTION_MESSAGE,
                    PDF_SERVICE_CLASS_NAME, GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME);
        }
        throwPdfServiceExceptionWhenStringValueIsEmpty(userToken,
                PDF_SERVICE_EXCEPTION_WHEN_USER_TOKEN_EMPTY, caseData.getEthosCaseReference());
        throwPdfServiceExceptionWhenStringValueIsEmpty(caseTypeId,
                PDF_SERVICE_EXCEPTION_WHEN_CASE_TYPE_ID_EMPTY, caseData.getEthosCaseReference());
        throwPdfServiceExceptionWhenStringValueIsEmpty(documentName,
                PDF_SERVICE_EXCEPTION_WHEN_DOCUMENT_NAME_EMPTY, caseData.getEthosCaseReference());
        throwPdfServiceExceptionWhenStringValueIsEmpty(pdfTemplate,
                PDF_SERVICE_EXCEPTION_WHEN_PDF_TEMPLATE_EMPTY, caseData.getEthosCaseReference());
        try {
            byte[] bytes = pdfService.convertCaseToPdf(caseData,
                    et3EnglishPdfTemplateSource, PDF_TYPE_ET3, ET3_FORM_CLIENT_TYPE_REPRESENTATIVE, SUBMIT_ET3);
            String dmStoreDocumentName = generatePdfFileName(caseData, documentName);
            log.info("Created PDF document binary data");
            return tornadoService.createDocumentInfoFromBytes(userToken, bytes, dmStoreDocumentName, caseTypeId);
        } catch (PdfServiceException pse) {
            logException(String.format(UNABLE_TO_PROCESS_PDF_SOURCE, pdfTemplate),
                    caseData.getEthosCaseReference(), pse.getMessage(), PDF_SERVICE_CLASS_NAME,
                    GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME);
            throw pse;
        }
    }

    private static void throwPdfServiceExceptionWhenStringValueIsEmpty(String stringValue,
                                                                       String exceptionMessage,
                                                                       String caseReferenceNumber)
            throws GenericServiceException {
        if (StringUtils.isBlank(stringValue)) {
            Throwable throwable = new Throwable(PDF_SERVICE_EXCEPTION_FIRST_WORD_WHEN_REQUIRED_FIELD_EMPTY);
            throw new GenericServiceException(exceptionMessage, throwable, exceptionMessage, caseReferenceNumber,
                    PDF_SERVICE_CLASS_NAME, GENERATE_PDF_DOCUMENT_INFO_SERVICE_NAME);
        }
    }

    private static String generatePdfFileName(CaseData caseData, String documentName) {
        return String.format(documentName, ObjectUtils.isEmpty(caseData.getSubmitEt3Respondent())
                || StringUtils.isBlank(caseData.getSubmitEt3Respondent().getSelectedLabel())
                ? PDF_OUTPUT_FILE_NAME_PREFIX : caseData.getSubmitEt3Respondent().getSelectedLabel());
    }
}
