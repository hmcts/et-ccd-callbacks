package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormMapper;

/**
 * Service to support ET3 Response journey. Contains methods for generating and saving ET3 Response documents.
 */
@Slf4j
@Service("pdfService")
@RequiredArgsConstructor
public class PdfService {

    ET3FormMapper et3FormMapperService;

    public DocumentInfo generateDocumentByCaseData(CaseData caseData, String pdfSource) {
        DocumentInfo documentInfo = new DocumentInfo();
        return documentInfo;
    }

    public byte[] createET3FormByteArray(CaseData caseData, String pdfSource) {
        return new byte[] {};
    }
}
