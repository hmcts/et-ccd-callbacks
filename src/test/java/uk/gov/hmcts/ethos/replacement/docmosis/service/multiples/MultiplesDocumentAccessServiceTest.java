package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicMultiSelectListType;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void testSetMultipleDocumentsToCorrectTab_Citizens() {
        multipleData.setDocumentAccess("Citizen");

        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");

        // Add document1 to the legalrepDocumentCollection
        multipleData.getClaimantDocumentCollection().add(document1);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        // Verify legalrepDocumentCollection contains selected document
        assertEquals(1, multipleData.getClaimantDocumentCollection().size());
    }

    @Test
    void testSetMultipleDocumentsToCorrectTab_LegalRep() {
        multipleData.setDocumentAccess("Legal rep/respondents");

        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");

        // Add document1 to the legalrepDocumentCollection
        multipleData.getLegalrepDocumentCollection().add(document1);

        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);

        // Verify legalrepDocumentCollection contains selected document
        assertEquals(1, multipleData.getLegalrepDocumentCollection().size());
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

        // Verify legalrepDocumentCollection contains selected document
        assertEquals(1, multipleData.getLegalrepDocumentCollection().size());

        // Verify citizenDocumentCollection also contains the selected document
        assertEquals(1, multipleData.getClaimantDocumentCollection().size());
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
}
