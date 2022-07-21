package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.List;

public class DocumentCollectionService {

    @Value("${document_management.ccdCaseDocument.url}")
    private String ccdCaseDocumentUrl;

    public void addDocumentCollection(CaseData caseData, DocumentInfo documentInfo, String documentFilename) {
        if (caseData.getDocumentCollection() == null) {
            List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
            caseData.setDocumentCollection(documentTypeItemList);
        }
        caseData.getDocumentCollection().add(
                createDocumentTypeItem(createDocumentPath(documentInfo), documentFilename));
    }

    private String createDocumentPath(DocumentInfo documentInfo) {
        return documentInfo.getUrl()
                .substring(documentInfo.getUrl().indexOf("/documents/"));
    }

    private DocumentTypeItem createDocumentTypeItem(String documentPath, String documentFilename) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(documentPath.replace("/documents/", "").replace("/binary", ""));
        documentTypeItem.setValue(createDocumentType(documentPath, documentFilename));
        return documentTypeItem;
    }

    private DocumentType createDocumentType(String documentPath, String documentFilename) {
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(null);
        documentType.setShortDescription(null);
        documentType.setUploadedDocument(createUploadedDocumentType(documentPath, documentFilename));
        return documentType;
    }

    private UploadedDocumentType createUploadedDocumentType(String documentPath, String documentFilename) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl(ccdCaseDocumentUrl + documentPath);
        uploadedDocumentType.setDocumentFilename(documentFilename);
        uploadedDocumentType.setDocumentUrl(ccdCaseDocumentUrl + documentPath.replace("/binary", ""));
        return uploadedDocumentType;
    }

}
