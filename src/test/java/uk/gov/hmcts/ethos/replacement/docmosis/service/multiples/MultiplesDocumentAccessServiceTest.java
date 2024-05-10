package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicMultiSelectListType;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MultiplesDocumentAccessServiceTest {

    private MultiplesDocumentAccessService multiplesDocumentAccessService;
    private MultipleData multipleData;

    @Mock
    private DynamicMultiSelectListType dynamicMultiSelectList;

    @BeforeEach
    void setUp() {
        multiplesDocumentAccessService = new MultiplesDocumentAccessService();
        MockitoAnnotations.initMocks(this);

        multipleData = new MultipleData();
        multipleData.setDocumentCollection(new ArrayList<>());
        multipleData.setDocumentSelect(dynamicMultiSelectList);
        multipleData.setClaimantDocumentCollection(new ArrayList<>());
        multipleData.setLegalrepDocumentCollection(new ArrayList<>());

    }

    @Test
    void testSetMultipleDocumentCollectionWhenDocumentCollectionIsEmpty() {
        // Create a mock MultipleData object with an empty document collection
        MultipleData multipleData = Mockito.mock(MultipleData.class);
        when(multipleData.getDocumentCollection()).thenReturn(new ArrayList<>());

        // Call the method to set multiple document collection
        multiplesDocumentAccessService.setMultipleDocumentCollection(multipleData);

        // Verify that the documentSelect is not set when document collection is empty
        verify(multipleData, Mockito.never()).setDocumentSelect(Mockito.any());

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
    void testSetMultipleDocumentsToCorrectTab_Default() {
        multipleData.setDocumentAccess("None (clear access)");

        // Add test data to document collection
        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");
        DocumentTypeItem document2 = new DocumentTypeItem();
        document2.setId("2");
        multipleData.getDocumentCollection().add(document1);
        multipleData.getDocumentCollection().add(document2);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        // Verify claimantDocumentCollection and legalrepDocumentCollection do not contain selected documents
        assertEquals(0, multipleData.getClaimantDocumentCollection().size());
        assertEquals(0, multipleData.getLegalrepDocumentCollection().size());
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
