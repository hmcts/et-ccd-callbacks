package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

class ET3DocumentHelperTest {

    private static final String RESPONSE_STATUS_REJECTED = "Rejected";
    private static final String ENGLISH_ET3_FORM_NAME = "Test Company - ET3 Response.pdf";
    private static final String WELSH_ET3_FORM_NAME = "Test Company - ET3 Response - Welsh.pdf";
    private static final String ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE = "2.11";
    private static final String ET3_REJECTED_NOTIFICATION_DOCUMENT_TYPE = "2.12";

    @Test
    void theHasET3Document() {
        assertThat(ET3DocumentHelper.hasET3Document(null)).isFalse();

        RespondentSumTypeItem respondentSumTypeItemNullValue = new RespondentSumTypeItem();
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItemNullValue)).isFalse();

        RespondentSumTypeItem respondentSumTypeItem = ResourceLoader.fromString(
                TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class).getRespondentCollection().get(0);
        // All ET3 Documents Exists
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // Only ET3 English Form is Removed
        respondentSumTypeItem.getValue().setEt3Form(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms are Removed
        respondentSumTypeItem.getValue().setEt3FormWelsh(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms and Contest Claim Documents are Removed
        respondentSumTypeItem.getValue().setEt3ResponseContestClaimDocument(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms, Contest Claim and Response Respondent Support Documents are Removed
        respondentSumTypeItem.getValue().setEt3ResponseRespondentSupportDocument(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms, Contest Claim and Response Respondent Support Documents are Removed
        respondentSumTypeItem.getValue().setEt3ResponseRespondentSupportDocument(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms, Contest Claim, Response Respondent Support and Response Employer Claim
        // Documents are Removed
        respondentSumTypeItem.getValue().setEt3ResponseEmployerClaimDocument(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isFalse();
    }

    @Test
    @SneakyThrows
    void testAddET3NotificationDocumentsToDocumentCollection() {
        // Helpers to create DocumentTypeItem with given type and ID
        Function<String, DocumentTypeItem> createET3Item = type -> {
            UploadedDocumentType uploaded = UploadedDocumentType.builder()
                    .documentBinaryUrl("url-" + type)
                    .documentFilename("doc-" + type + ".pdf")
                    .documentUrl("http://localhost/doc-" + type)
                    .build();
            DocumentType docType = DocumentType.builder()
                    .typeOfDocument(type)
                    .uploadedDocument(uploaded)
                    .build();
            return DocumentTypeItem.builder()
                    .id(UUID.randomUUID().toString())
                    .value(docType)
                    .build();
        };
        // Create CaseData with accepted and rejected ET3 notification documents
        List<DocumentTypeItem> et3Docs = List.of(
                createET3Item.apply(ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE),
                createET3Item.apply("ET3_REJECTED_TYPE")
        );
        CaseData caseData = new CaseData();
        caseData.setEt3NotificationDocCollection(et3Docs);
        caseData.setDocumentCollection(new ArrayList<>());
        // Act
        ET3DocumentHelper.addET3NotificationDocumentsToDocumentCollection(caseData);
        // Assert
        List<DocumentTypeItem> result = caseData.getDocumentCollection();
        assertEquals(2, result.size());
        for (DocumentTypeItem item : result) {
            DocumentType value = item.getValue();
            assertNotNull(value);
            assertNotNull(value.getUploadedDocument());
            assertNotNull(value.getShortDescription());
            assertNotNull(value.getTopLevelDocuments());
            assertNotNull(value.getResponseClaimDocuments());
            assertNull(value.getTypeOfDocument()); // must be cleared
            assertTrue(value.getShortDescription().contains(value.getResponseClaimDocuments()));
        }
        // Edge case: if ET3 collection is empty, nothing should be added
        CaseData emptyCase = new CaseData();
        emptyCase.setEt3NotificationDocCollection(Collections.emptyList());
        ET3DocumentHelper.addET3NotificationDocumentsToDocumentCollection(emptyCase);
        assertNull(emptyCase.getDocumentCollection());
        // Edge case: if main collection is null, it should be initialized
        CaseData noMainCollection = new CaseData();
        noMainCollection.setEt3NotificationDocCollection(
                List.of(createET3Item.apply(ET3_REJECTED_NOTIFICATION_DOCUMENT_TYPE)));
        ET3DocumentHelper.addET3NotificationDocumentsToDocumentCollection(noMainCollection);
        assertNotNull(noMainCollection.getDocumentCollection());
        assertEquals(1, noMainCollection.getDocumentCollection().size());
    }

    @Test
    void theFindAllET3DocumentsOfRespondent() {
        assertThat(ET3DocumentHelper.findAllET3DocumentsOfRespondent(null)).isEmpty();

        RespondentSumTypeItem respondentSumTypeItem = ResourceLoader.fromString(
                TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class).getRespondentCollection().get(0);
        assertThat(ET3DocumentHelper.findAllET3DocumentsOfRespondent(respondentSumTypeItem)).hasSize(5);

        respondentSumTypeItem.getValue().setEt3ResponseContestClaimDocument(null);
        assertThat(ET3DocumentHelper.findAllET3DocumentsOfRespondent(respondentSumTypeItem)).hasSize(4);
    }

    @ParameterizedTest
    @MethodSource("provideDataForModifyDocumentCollectionForET3FormsTest")
    void theAddOrRemoveET3Documents(CaseData caseData) {
        ET3DocumentHelper.addOrRemoveET3Documents(caseData);
        if (ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE.equals(
                caseData.getEt3NotificationDocCollection().get(0).getValue().getTypeOfDocument())) {
            if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
                assertThat(caseData.getDocumentCollection()).hasSize(7);
            } else if (ObjectUtils.isEmpty(caseData.getRespondentCollection().get(0).getValue())) {
                assertThat(
                        caseData.getDocumentCollection().get(5).getValue().getUploadedDocument().getDocumentFilename())
                        .isEqualTo(ENGLISH_ET3_FORM_NAME);
                assertThat(
                        caseData.getDocumentCollection().get(6).getValue().getUploadedDocument().getDocumentFilename())
                        .isEqualTo(WELSH_ET3_FORM_NAME);
            } else if (RESPONSE_STATUS_REJECTED.equals(
                    caseData.getRespondentCollection().get(0).getValue().getResponseStatus())) {
                assertThat(caseData.getDocumentCollection()).hasSize(2);
            } else if (ACCEPTED_STATE.equals(
                    caseData.getRespondentCollection().get(0).getValue().getResponseStatus())) {
                assertThat(
                        caseData.getDocumentCollection().get(0).getValue().getUploadedDocument().getDocumentFilename())
                        .isEqualTo(ENGLISH_ET3_FORM_NAME);
                assertThat(
                        caseData.getDocumentCollection().get(1).getValue().getUploadedDocument().getDocumentFilename())
                        .isEqualTo(WELSH_ET3_FORM_NAME);
            }
        } else {
            assertThat(caseData.getDocumentCollection()).hasSize(2);
        }
    }

    private static Stream<Arguments> provideDataForModifyDocumentCollectionForET3FormsTest() {
        CaseData caseDataWithoutRespondentCollection =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataWithoutRespondentCollection.setRespondentCollection(null);

        CaseData caseDataRespondentValueNotExists =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataRespondentValueNotExists.getRespondentCollection().get(0).setValue(null);

        CaseData caseDataResponseNotAccepted =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataResponseNotAccepted.getRespondentCollection()
                .get(0).getValue().setResponseStatus(RESPONSE_STATUS_REJECTED);

        CaseData caseDataWithoutDocumentCollectionAndResponseAccepted =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataWithoutDocumentCollectionAndResponseAccepted.setDocumentCollection(null);
        caseDataWithoutDocumentCollectionAndResponseAccepted.getRespondentCollection()
                .get(0).getValue().setResponseStatus(ACCEPTED_STATE);

        CaseData caseDataET3NotificationDocCollectionAndResponseNotAccepted =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataET3NotificationDocCollectionAndResponseNotAccepted.getEt3NotificationDocCollection()
                .get(0).getValue().setTypeOfDocument("2.12");

        return Stream.of(Arguments.of(caseDataWithoutRespondentCollection),
                Arguments.of(caseDataRespondentValueNotExists),
                Arguments.of(caseDataResponseNotAccepted),
                Arguments.of(caseDataWithoutDocumentCollectionAndResponseAccepted),
                Arguments.of(caseDataET3NotificationDocCollectionAndResponseNotAccepted));
    }

    @Test
    void theHasInconsistentAcceptanceStatus() {
        // Null list
        assertTrue(ET3DocumentHelper.hasInconsistentAcceptanceStatus(null),
                "Should return false for null list");
        // Empty list
        assertTrue(ET3DocumentHelper.hasInconsistentAcceptanceStatus(List.of()),
                "Should return false for empty list");
        // First item's value is null
        assertTrue(ET3DocumentHelper.hasInconsistentAcceptanceStatus(
                List.of(DocumentTypeItem.builder().value(null).build())),
                "Should return false when first item's value is null");
        // First item's type is blank
        assertTrue(ET3DocumentHelper.hasInconsistentAcceptanceStatus(List.of(
                        DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument(" ").build()).build())),
                "Should return false when type is blank");
        // Single valid item
        assertFalse(ET3DocumentHelper.hasInconsistentAcceptanceStatus(List.of(
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.11").build()).build())),
                "Should return true for single valid item");
        // All items have the same type
        List<DocumentTypeItem> sameTypeItems = List.of(
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.11").build()).build(),
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.11").build()).build(),
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.11").build()).build()
        );
        assertFalse(ET3DocumentHelper.hasInconsistentAcceptanceStatus(sameTypeItems),
                "Should return true when all types match");
        // All items have different types than 2.11
        List<DocumentTypeItem> differentTypeItemsOf211 = List.of(
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.12").build()).build(),
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.13").build()).build(),
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.14").build()).build()
        );
        assertFalse(ET3DocumentHelper.hasInconsistentAcceptanceStatus(differentTypeItemsOf211),
                "Should return true when all types are different than 2.11");
        // Items with different types
        List<DocumentTypeItem> differentTypeItems = List.of(
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.11").build()).build(),
                DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.12").build()).build()
        );
        assertTrue(ET3DocumentHelper.hasInconsistentAcceptanceStatus(differentTypeItems),
                "Should return false when types differ");
    }

    @Test
    void theIsET3NotificationDocumentTypeResponseAccepted_AllScenarios() {
        // Null list
        assertFalse(ET3DocumentHelper.isET3NotificationDocumentTypeResponseAccepted(null),
                "Should return false for null list");

        // Empty list
        assertFalse(ET3DocumentHelper.isET3NotificationDocumentTypeResponseAccepted(List.of()),
                "Should return false for empty list");

        // First item's document type is blank
        DocumentTypeItem blankTypeItem = DocumentTypeItem.builder().value(null).build();
        assertFalse(ET3DocumentHelper.isET3NotificationDocumentTypeResponseAccepted(List.of(blankTypeItem)),
                "Should return false if document type is blank");

        // First item's document type is not "2.11"
        DocumentTypeItem wrongTypeItem = DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.12")
                .build()).build();
        assertFalse(ET3DocumentHelper.isET3NotificationDocumentTypeResponseAccepted(List.of(wrongTypeItem)),
                "Should return false if type is not 2.11");

        // First item's document type is "2.11"
        DocumentTypeItem acceptedItem = DocumentTypeItem.builder().value(DocumentType.builder().typeOfDocument("2.11")
                .build()).build();
        assertTrue(ET3DocumentHelper.isET3NotificationDocumentTypeResponseAccepted(List.of(acceptedItem)),
                "Should return true if type is 2.11");
    }

    @Test
    void theContainsNoRespondentWithResponse_Status_variousCases() {
        // Case 1: Empty list
        assertTrue(ET3DocumentHelper.containsNoRespondentWithResponseStatus(null),
                "Null list should return true");

        // Case 2: Empty list
        List<RespondentSumTypeItem> emptyList = Collections.emptyList();
        assertTrue(ET3DocumentHelper.containsNoRespondentWithResponseStatus(emptyList),
                "Empty list should return true");

        // Case 3: No accepted responses
        RespondentSumTypeItem respondentSumTypeItem1 = new RespondentSumTypeItem();
        respondentSumTypeItem1.setValue(RespondentSumType.builder().responseStatus("").build());
        RespondentSumTypeItem respondentSumTypeItem2 = new RespondentSumTypeItem();
        respondentSumTypeItem2.setValue(RespondentSumType.builder().responseStatus(null).build());
        List<RespondentSumTypeItem> noAccepted = List.of(respondentSumTypeItem1, respondentSumTypeItem2);
        assertTrue(ET3DocumentHelper.containsNoRespondentWithResponseStatus(noAccepted),
                "No response status found");

        // Case 4: One accepted response
        RespondentSumTypeItem respondentSumTypeItem3 = new RespondentSumTypeItem();
        respondentSumTypeItem3.setValue(RespondentSumType.builder().responseStatus("Not Accepted").build());
        List<RespondentSumTypeItem> oneAccepted = List.of(
                respondentSumTypeItem1, respondentSumTypeItem2, respondentSumTypeItem3);
        assertFalse(ET3DocumentHelper.containsNoRespondentWithResponseStatus(oneAccepted),
                "One response status as accepted should return false");

        // Case 5: All accepted responses
        RespondentSumTypeItem respondentSumTypeItem4 = new RespondentSumTypeItem();
        respondentSumTypeItem4.setValue(RespondentSumType.builder().responseStatus("Accepted").build());
        List<RespondentSumTypeItem> allAccepted = List.of(respondentSumTypeItem4, respondentSumTypeItem3);
        assertFalse(ET3DocumentHelper.containsNoRespondentWithResponseStatus(allAccepted),
                "All users have response status and should return false");
    }

    @Test
    void theAreET3DocumentsConsistentWithRespondentResponses() {
        // Helper to create RespondentSumTypeItem with a given status
        Function<String, RespondentSumTypeItem> respondentWithStatus = status -> {
            RespondentSumType respondent = RespondentSumType.builder().responseStatus(status).build();
            RespondentSumTypeItem item = new RespondentSumTypeItem();
            item.setValue(respondent);
            return item;
        };
        // Helper to create DocumentTypeItem with a given type
        Function<String, DocumentTypeItem> documentWithType = type -> {
            DocumentType document = DocumentType.builder().typeOfDocument(type).build();
            return DocumentTypeItem.builder().value(document).build();
        };
        // Constants
        String acceptedType = ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE;
        String rejectedType = "ET3_REJECTED_TYPE";
        // Case 1: Null inputs
        assertFalse(ET3DocumentHelper.areET3DocumentsConsistentWithRespondentResponses(null, null));
        // Case 2: Respondent has accepted status, but no accepted document
        assertFalse(ET3DocumentHelper.areET3DocumentsConsistentWithRespondentResponses(
                List.of(respondentWithStatus.apply(ACCEPTED_STATE)),
                List.of(documentWithType.apply(rejectedType))
        ));
        // Case 3: Respondent has accepted status, and accepted document exists
        assertTrue(ET3DocumentHelper.areET3DocumentsConsistentWithRespondentResponses(
                List.of(respondentWithStatus.apply(ACCEPTED_STATE)),
                List.of(documentWithType.apply(acceptedType))
        ));
        // Case 4: Respondent has rejected status, but only accepted document exists
        assertFalse(ET3DocumentHelper.areET3DocumentsConsistentWithRespondentResponses(
                List.of(respondentWithStatus.apply("REJECTED")),
                List.of(documentWithType.apply(acceptedType))
        ));
        // Case 5: Respondent has rejected status, and rejected document exists
        assertTrue(ET3DocumentHelper.areET3DocumentsConsistentWithRespondentResponses(
                List.of(respondentWithStatus.apply("REJECTED")),
                List.of(documentWithType.apply(rejectedType))
        ));
        // Case 6: Respondent has blank status
        assertFalse(ET3DocumentHelper.areET3DocumentsConsistentWithRespondentResponses(
                List.of(respondentWithStatus.apply("")),
                List.of(documentWithType.apply(acceptedType), documentWithType.apply(rejectedType))
        ));
        // Case 7: Multiple respondents — accepted and rejected — both document types present
        assertTrue(ET3DocumentHelper.areET3DocumentsConsistentWithRespondentResponses(
                List.of(
                        respondentWithStatus.apply(ACCEPTED_STATE),
                        respondentWithStatus.apply("REJECTED")
                ),
                List.of(
                        documentWithType.apply(acceptedType),
                        documentWithType.apply(rejectedType)
                )
        ));
    }
}
