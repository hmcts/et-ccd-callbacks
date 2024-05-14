package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MultiplesDocumentAccessServiceTest {

    private MultiplesDocumentAccessService multiplesDocumentAccessService;

    private MultipleData multipleData;

    @BeforeEach
    void setUp() {
        multiplesDocumentAccessService = new MultiplesDocumentAccessService();
        multipleData = new MultipleData();
        multipleData.setDocumentCollection(new ArrayList<>());
        multipleData.setClaimantDocumentCollection(new ArrayList<>());
        multipleData.setLegalrepDocumentCollection(new ArrayList<>());

    }

    @Test
    void testSetMultipleDocumentCollection() {
        multipleData = new MultipleData();
        multipleData.setDocumentCollection(new ArrayList<>());

        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename("random.pdf");

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);

        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setValue(documentType);
        document1.setId("1");

        multipleData.getDocumentCollection().add(document1);

        multiplesDocumentAccessService.setMultipleDocumentCollection(multipleData);

        assertEquals(1, multipleData.getDocumentCollection().size());
        assertTrue(multipleData.getDocumentCollection().contains(document1));
    }

    @Test
    void testSetMultipleDocumentCollectionWhenDocumentCollectionIsEmpty() {

        MultipleData multipleData = mock(MultipleData.class);
        when(multipleData.getDocumentCollection()).thenReturn(new ArrayList<>());

        multiplesDocumentAccessService.setMultipleDocumentCollection(multipleData);

        verify(multipleData, Mockito.never()).setDocumentSelect(any());

        assertNull(multipleData.getDocumentSelect());
    }

    @Test
    void testSetMultipleDocumentsToCorrectTab_Citizens() {
        multipleData.setDocumentAccess("Citizen");

        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");

        multipleData.getClaimantDocumentCollection().add(document1);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        assertEquals(1, multipleData.getClaimantDocumentCollection().size());
        assertTrue(multipleData.getClaimantDocumentCollection().contains(document1));
    }

    @Test
    void testSetMultipleDocumentsToCorrectTab_LegalRep() {
        multipleData.setDocumentAccess("Legal rep/respondents");

        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");

        multipleData.getLegalrepDocumentCollection().add(document1);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        assertEquals(1, multipleData.getLegalrepDocumentCollection().size());
        assertTrue(multipleData.getLegalrepDocumentCollection().contains(document1));
    }

    @Test
    void testSetMultipleDocumentsToCorrectTab_BothLegalRepAndCitizen() {
        multipleData.setDocumentAccess("Both legal rep and citizen");

        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");

        DocumentTypeItem document2 = new DocumentTypeItem();
        document2.setId("2");

        multipleData.getLegalrepDocumentCollection().add(document1);
        multipleData.getClaimantDocumentCollection().add(document1);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        assertEquals(1, multipleData.getLegalrepDocumentCollection().size());
        assertEquals(1, multipleData.getClaimantDocumentCollection().size());

        assertTrue(multipleData.getLegalrepDocumentCollection().contains(document1));
        assertTrue(multipleData.getClaimantDocumentCollection().contains(document1));
    }

    @Test
     void testClaimantDocumentCollectionNullCheck() {

        multipleData.setClaimantDocumentCollection(null);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        assertNotNull(multipleData.getClaimantDocumentCollection());
        assertTrue(multipleData.getClaimantDocumentCollection().isEmpty());
    }

    @Test
    void testLegalRepDocumentCollectionNullCheck() {

        multipleData.setClaimantDocumentCollection(null);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        assertNotNull(multipleData.getLegalrepDocumentCollection());
        assertTrue(multipleData.getLegalrepDocumentCollection().isEmpty());
    }

    @Test
    void testAddSelectedDocsToCollection() {

        List<DocumentTypeItem> selectedDocs = new ArrayList<>();
        DocumentTypeItem selectedDoc1 = new DocumentTypeItem();
        selectedDoc1.setId("1");
        selectedDocs.add(selectedDoc1);

        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        DocumentTypeItem existingDoc = new DocumentTypeItem();
        existingDoc.setId("2");
        documentCollection.add(existingDoc);

        multiplesDocumentAccessService.addSelectedDocsToCollection(selectedDocs, documentCollection);

        assertEquals(2, documentCollection.size());
        assertEquals(selectedDoc1, documentCollection.get(1));
    }
}
