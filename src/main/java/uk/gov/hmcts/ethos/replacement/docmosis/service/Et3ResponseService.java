package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;

/**
 * Service to support ET3 Response journey. Contains methods for generating and saving ET3 Response documents.
 */
@Slf4j
@Service("et3ResponseService")
@RequiredArgsConstructor
public class Et3ResponseService {

    private final DocumentManagementService documentManagementService;
    private final TornadoService tornadoService;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    /**
     * This method calls the tornado service to generate the PDF for the ET3 Response journey.
     * @param caseData where the data is stored
     * @param userToken user authentication token
     * @param caseTypeId reference which caseType the document will be uploaded to
     * @return DocumentInfo which contains the URL and description of the document uploaded to DM Store
     */
    public DocumentInfo generateEt3ResponseDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken,
                caseTypeId, "ET3 Response.pdf");
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    /**
     * Saves the generated ET3 Response form document onto case data.
     * @param caseData where the data is stored
     */
    public void saveEt3ResponseDocument(CaseData caseData, DocumentInfo documentInfo) {
        UploadedDocumentType uploadedDocument = documentManagementService.addDocumentToDocumentField(documentInfo);

        DocumentTypeItem documentTypeItem = DocumentTypeItem.fromUploadedDocument(uploadedDocument);
        documentTypeItem.getValue().setTypeOfDocument("ET3");

        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }

        caseData.getDocumentCollection().add(documentTypeItem);
    }
}