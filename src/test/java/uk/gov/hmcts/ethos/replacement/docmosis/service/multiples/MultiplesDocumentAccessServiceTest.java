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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        // Create a mock MultipleData object with an empty document collection
        MultipleData multipleData = mock(MultipleData.class);
        when(multipleData.getDocumentCollection()).thenReturn(new ArrayList<>());

        // Call the method to set multiple document collection
        multiplesDocumentAccessService.setMultipleDocumentCollection(multipleData);

        // Verify that the documentSelect is not set when document collection is empty
        verify(multipleData, Mockito.never()).setDocumentSelect(any());

        // Assert that the documentSelect is not set when document collection is empty
        assertNull(multipleData.getDocumentSelect());
    }

    @Test
    void testSetMultipleDocumentsToCorrectTab_Citizens() {
        multipleData.setDocumentAccess("Citizen");

        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");

        // Add document1 to the claimantDocumentCollection
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

        // Add document1 to the legalrepDocumentCollection
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

        // Add document1 to the legalrepDocumentCollection
        multipleData.getLegalrepDocumentCollection().add(document1);
        multipleData.getClaimantDocumentCollection().add(document1);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        assertEquals(1, multipleData.getLegalrepDocumentCollection().size());
        assertEquals(1, multipleData.getClaimantDocumentCollection().size());

        assertTrue(multipleData.getLegalrepDocumentCollection().contains(document1));
        assertTrue(multipleData.getClaimantDocumentCollection().contains(document1));
    }

    @Test
    public void testClaimantDocumentCollectionNullCheck() {

        // Set claimantDocumentCollection to null
        multipleData.setClaimantDocumentCollection(null);

        // Call the method that initializes claimantDocumentCollection if it is null
        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        // Assert that claimantDocumentCollection is not null and is an empty ArrayList
        assertNotNull(multipleData.getClaimantDocumentCollection());
        assertTrue(multipleData.getClaimantDocumentCollection().isEmpty());
    }

    @Test
    public void testLegalRepDocumentCollectionNullCheck() {

        // Set claimantDocumentCollection to null
        multipleData.setClaimantDocumentCollection(null);

        // Call the method that initializes claimantDocumentCollection if it is null
        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        // Assert that claimantDocumentCollection is not null and is an empty ArrayList
        assertNotNull(multipleData.getLegalrepDocumentCollection());
        assertTrue(multipleData.getLegalrepDocumentCollection().isEmpty());
    }

    @Test
    void testAddSelectedDocsToCollection() {
        // Create a list of selected documents
        List<DocumentTypeItem> selectedDocs = new ArrayList<>();
        DocumentTypeItem selectedDoc1 = new DocumentTypeItem();
        selectedDoc1.setId("1");
        selectedDocs.add(selectedDoc1);

        // Create a document collection with an existing document
        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        DocumentTypeItem existingDoc = new DocumentTypeItem();
        existingDoc.setId("2");
        documentCollection.add(existingDoc);

        // Call the method to add selected documents to the collection
        multiplesDocumentAccessService.addSelectedDocsToCollection(selectedDocs, documentCollection);

        // Verify that the selected document is added to the collection
        assertEquals(2, documentCollection.size()); // Expecting 2 documents in the collection
        assertEquals(selectedDoc1, documentCollection.get(1)); // Verify the selected document is added
    }
}
