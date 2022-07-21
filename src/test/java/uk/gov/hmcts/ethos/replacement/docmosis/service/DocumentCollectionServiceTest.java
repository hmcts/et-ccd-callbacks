package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DocumentCollectionServiceTest {

    private DocumentCollectionService documentCollectionService;

    private static final String IC_SUMMARY_FILENAME = "InitialConsideration.pdf";

    @BeforeEach
    void setUp() {
        documentCollectionService = new DocumentCollectionService();
    }

    @Test
    void addIcEwDocumentLink_NoDocumentCollection() {
        CaseData caseData = new CaseData();
        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setUrl("http://dm-store:8080/documents/9c6cc92e-1eea-430b-8583-a1e71508b2a1/binary");

        List<DocumentTypeItem> expectDocumentCollection = new ArrayList<>();
        expectDocumentCollection.add(createDocumentTypeItem("9c6cc92e-1eea-430b-8583-a1e71508b2a1"));

        documentCollectionService.addDocumentCollection(caseData, documentInfo, IC_SUMMARY_FILENAME);
        assertThat(caseData.getDocumentCollection())
                .isEqualTo(expectDocumentCollection);
    }

    @Test
    void addIcEwDocumentLink_HaveDocumentCollection() {
        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        documentCollection.add(createDocumentTypeItem("9c6cc92e-1eea-430b-8583-a1e71508b2a1"));
        CaseData caseData = new CaseData();
        caseData.setDocumentCollection(documentCollection);
        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setUrl("http://dm-store:8080/documents/1c67f047-72ee-48a5-a39c-441b5080b264/binary");

        List<DocumentTypeItem> expectDocumentCollection = new ArrayList<>();
        expectDocumentCollection.add(createDocumentTypeItem("9c6cc92e-1eea-430b-8583-a1e71508b2a1"));
        expectDocumentCollection.add(createDocumentTypeItem("1c67f047-72ee-48a5-a39c-441b5080b264"));

        documentCollectionService.addDocumentCollection(caseData, documentInfo, IC_SUMMARY_FILENAME);
        assertThat(caseData.getDocumentCollection())
                .isEqualTo(expectDocumentCollection);
    }

    private DocumentTypeItem createDocumentTypeItem(String docUrl) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("null/documents/" + docUrl + "/binary");
        uploadedDocumentType.setDocumentFilename(IC_SUMMARY_FILENAME);
        uploadedDocumentType.setDocumentUrl("null/documents/" + docUrl);

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);

        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(docUrl);
        documentTypeItem.setValue(documentType);

        return documentTypeItem;
    }

}