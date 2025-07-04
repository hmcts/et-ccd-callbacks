package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DocumentUtils {

    private DocumentUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Creates a new list of {@link GenericTypeItem} containing only the uploaded
     * document data from the provided document list.
     * <p>
     * For each item in the input list, this method:
     * <ul>
     *   <li>Creates a new {@link DocumentType} containing only the {@code uploadedDocument}
     *   field from the original item</li>
     *   <li>Preserves the original ID if present; otherwise generates a new UUID</li>
     *   <li>Preserves the binary URL of the uploaded document</li>
     * </ul>
     *
     * @param documentList the original list of {@link DocumentType} containing full document data
     * @return a list of {@link DocumentType} containing only uploaded document information
     */
    public static List<GenericTypeItem<DocumentType>> generateUploadedDocumentListFromDocumentList(
            List<GenericTypeItem<DocumentType>> documentList) {

        List<GenericTypeItem<DocumentType>> uploadedDocumentList = new ArrayList<>();
        documentList.forEach(doc -> {
            GenericTypeItem<DocumentType> genTypeItems = new GenericTypeItem<>();
            DocumentType docType = new DocumentType();
            docType.setUploadedDocument(doc.getValue().getUploadedDocument());
            docType.getUploadedDocument().setDocumentBinaryUrl(
                    doc.getValue().getUploadedDocument().getDocumentBinaryUrl());

            genTypeItems.setId(doc.getId() != null ? doc.getId() : UUID.randomUUID().toString());
            genTypeItems.setValue(docType);
            uploadedDocumentList.add(genTypeItems);
        });

        return uploadedDocumentList;
    }

    /**
     * Converts the given {@link UploadedDocumentType} to a {@link DocumentTypeItem} and sets its document levels.
     * These levels are used when displaying the {@link uk.gov.hmcts.et.common.model.ccd.CaseData}'s
     * DocumentCollection field.
     *
     * @param uploadedDocumentType the document to be converted into a {@link DocumentTypeItem}
     * @param topLevel the top-level category of the document (e.g., all ET3 documents have
     *                 "Response to a Claim" as the top level)
     * @param secondLevel the second-level type of the document (e.g., "ET3" or "ET3 Attachment")
     * @return a {@link DocumentTypeItem} created from the given {@link UploadedDocumentType}
     */
    public static DocumentTypeItem convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
            UploadedDocumentType uploadedDocumentType,
            String topLevel,
            String secondLevel) {
        if (ObjectUtils.isEmpty(uploadedDocumentType)
                || StringUtils.isBlank(topLevel)
                || StringUtils.isBlank(secondLevel)) {
            return null;
        }
        return DocumentHelper.createDocumentTypeItemFromTopLevel(uploadedDocumentType, topLevel, secondLevel);
    }

    /**
     * Updates the given {@link DocumentTypeItem} by setting the following fields.
     * <ul>
     *   <li>{@code dateOfCorrespondence} — set to the current date and time ({@code LocalDateTime.now()})</li>
     *   <li>{@code topLevelDocuments}</li>
     *   <li>{@code secondLevelDocuments}</li>
     *   <li>{@code documentTypeForDocument} — set to the existing document type or the value from
     *   {@code typeOfDocument}</li>
     * </ul>
     *
     * @param documentTypeItem the {@link DocumentTypeItem} to be updated
     * @param topLevel the top-level category of the document (e.g., "Response to a Claim" for ET3 documents)
     * @param secondLevel the specific type of the document (e.g., "ET3" or "ET3 Attachment")
     */
    public static void setDocumentTypeItemLevels(DocumentTypeItem documentTypeItem,
                                                 String topLevel,
                                                 String secondLevel) {
        if (ObjectUtils.isEmpty(documentTypeItem)
                || StringUtils.isBlank(topLevel)
                || StringUtils.isBlank(secondLevel)) {
            return;
        }
        documentTypeItem.getValue().setDateOfCorrespondence(LocalDate.now().toString());
        documentTypeItem.getValue().setTopLevelDocuments(topLevel);
        uk.gov.hmcts.ecm.common.helpers.DocumentHelper.setSecondLevelDocumentFromType(documentTypeItem.getValue(),
                secondLevel);
        uk.gov.hmcts.ecm.common.helpers.DocumentHelper.setDocumentTypeForDocument(documentTypeItem.getValue());
    }

    /**
     * Converts the given {@link UploadedDocumentType} to a {@link DocumentTypeItem} and adds it to the provided list,
     * after performing necessary null/empty checks.
     * <p>
     * If either {@code uploadedDocumentType} or {@code documentTypeItems} is null or empty,
     * the method exits without action.
     * Otherwise, it creates a {@link DocumentTypeItem} from the given {@link UploadedDocumentType},
     * sets its short description,
     * and appends it to the {@code documentTypeItems} list.
     *
     * @param documentTypeItems the list of {@link DocumentTypeItem}s to which the new item will be added
     * @param uploadedDocumentType the document to be converted to a {@link DocumentTypeItem}
     * @param topLevel the top-level category of the document (e.g., "Response to a Claim" for ET3 documents)
     * @param secondLevel the specific type of the document (e.g., "ET3" or "ET3 Attachment")
     * @param shortDescription a brief description to set for the converted document
     */
    public static void addUploadedDocumentTypeToDocumentTypeItems(List<DocumentTypeItem> documentTypeItems,
                                                                  UploadedDocumentType uploadedDocumentType,
                                                                  String topLevel,
                                                                  String secondLevel,
                                                                  String shortDescription) {
        if (documentTypeItems == null
                || ObjectUtils.isEmpty(uploadedDocumentType)
                || StringUtils.isBlank(topLevel)
                || StringUtils.isBlank(secondLevel)) {
            return;
        }
        DocumentTypeItem documentTypeItem = convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                uploadedDocumentType, topLevel, secondLevel);
        if (documentTypeItem != null && ObjectUtils.isNotEmpty(documentTypeItem.getValue())) {
            documentTypeItem.getValue().setShortDescription(shortDescription);
            documentTypeItems.add(documentTypeItem);
        }
    }

    /**
     * Adds the given {@link DocumentTypeItem} to the list of {@code documentTypeItems} only if it
     * does not already exist in the list based on either a matching ID or a matching binary document URL.
     * <p>
     * The method performs two checks before adding:
     * <ul>
     *     <li>If the list already contains a document with the same {@code id} as {@code documentTypeItem}</li>
     *     <li>If the list already contains a document with the same {@code documentBinaryUrl}</li>
     * </ul>
     * If either match is found, the document is not added.
     *
     * @param documentTypeItems the list of {@link DocumentTypeItem}s to check and possibly add to
     * @param documentTypeItem  the document to be conditionally added to the list
     */
    public static void addDocumentIfUnique(List<DocumentTypeItem> documentTypeItems,
                                           DocumentTypeItem documentTypeItem) {
        if (hasMatchingId(documentTypeItems, documentTypeItem)
                || hasMatchingBinaryUrl(documentTypeItems, documentTypeItem)) {
            return;
        }
        documentTypeItems.add(documentTypeItem);
    }

    /**
     * Determines whether a given {@link DocumentTypeItem} (target item) has a matching ID
     * with any item in the provided list of {@code documentTypeItems}. If a match is found,
     * the target item is updated with values from the matching item using {@code setDocumentNewValues}.
     *
     * <p>The method follows this logic:</p>
     * <ul>
     *   <li>If the {@code targetItem} is {@code null} or its ID is {@code null} or empty,
     *       the method returns {@code true}.</li>
     *   <li>If the {@code documentTypeItems} list is {@code null} or empty, it returns {@code false}.</li>
     *   <li>Iterates through each {@code DocumentTypeItem} in the list to find a matching ID.</li>
     *   <li>If a match is found, the {@code targetItem} is updated using {@code setDocumentNewValues},
     *       and the method returns {@code true}.</li>
     *   <li>If no match is found after the iteration, it returns {@code false}.</li>
     * </ul>
     *
     * @param documentTypeItems the list of existing {@link DocumentTypeItem} objects to search
     * @param targetItem the {@link DocumentTypeItem} whose ID is to be matched
     * @return {@code true} if a matching ID is found or the target item is {@code null}/empty;
     *         {@code false} otherwise
     */
    public static boolean hasMatchingId(List<DocumentTypeItem> documentTypeItems,
                                        DocumentTypeItem targetItem) {
        if (ObjectUtils.isEmpty(targetItem) || StringUtils.isEmpty(targetItem.getId())) {
            return true;
        }
        if (CollectionUtils.isEmpty(documentTypeItems)) {
            return false;
        }
        String targetItemId = targetItem.getId();
        for (DocumentTypeItem item : documentTypeItems) {
            if (ObjectUtils.isNotEmpty(item) && targetItemId.equals(item.getId())) {
                setDocumentNewValues(targetItem, item);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a given {@link DocumentTypeItem} (target item) has a matching binary URL
     * with any item in the provided list of {@code documentTypeItems}. If a match is found, the method
     * updates the {@code targetItem} with values from the matching item using {@code setDocumentNewValues}.
     *
     * <p>The method considers the following logic:</p>
     * <ul>
     *   <li>If {@code targetItem} or its binary URL is empty or null, the method returns {@code true}.</li>
     *   <li>If {@code documentTypeItems} is null or empty, it returns {@code false}.</li>
     *   <li>Iterates through each {@code DocumentTypeItem} in the list and compares its binary URL
     *       to that of the {@code targetItem}.</li>
     *   <li>If a match is found, {@code setDocumentNewValues} is invoked to update the target item
     *       and the method returns {@code true}.</li>
     *   <li>If no match is found, it returns {@code false}.</li>
     * </ul>
     *
     * @param documentTypeItems the list of existing {@link DocumentTypeItem} objects to search within
     * @param targetItem the {@link DocumentTypeItem} whose binary URL is to be matched
     * @return {@code true} if a match is found or if the target item is blank or lacks a binary URL;
     *         {@code false} otherwise
     */
    public static boolean hasMatchingBinaryUrl(List<DocumentTypeItem> documentTypeItems,
                                               DocumentTypeItem targetItem) {
        if (ObjectUtils.isEmpty(targetItem)
                || ObjectUtils.isEmpty(targetItem.getValue())
                || ObjectUtils.isEmpty(targetItem.getValue().getUploadedDocument())
                || StringUtils.isBlank(targetItem.getValue().getUploadedDocument().getDocumentBinaryUrl())) {
            return true;
        }
        if (CollectionUtils.isEmpty(documentTypeItems)) {
            return false;
        }
        String targetItemBinaryUrl = targetItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        for (DocumentTypeItem item : documentTypeItems) {
            if (ObjectUtils.isEmpty(item)
                    || ObjectUtils.isEmpty(item.getValue())
                    || ObjectUtils.isEmpty(item.getValue().getUploadedDocument())
                    || StringUtils.isBlank(item.getValue().getUploadedDocument().getDocumentBinaryUrl())) {
                continue;
            }
            if (targetItemBinaryUrl.equals(item.getValue().getUploadedDocument().getDocumentBinaryUrl())) {
                setDocumentNewValues(targetItem, item);
                return true;
            }
        }
        return false;
    }

    private static void setDocumentNewValues(DocumentTypeItem sourceDocumentTypeItem,
                                             DocumentTypeItem targetDocumentTypeItem) {
        if (ObjectUtils.isEmpty(sourceDocumentTypeItem) || ObjectUtils.isEmpty(targetDocumentTypeItem)
                || sourceDocumentTypeItem.getValue() == null
                || targetDocumentTypeItem.getValue() == null) {
            return;
        }
        DocumentType source = sourceDocumentTypeItem.getValue();
        DocumentType target = targetDocumentTypeItem.getValue();

        source.setTypeOfDocument(target.getTypeOfDocument());
        source.setShortDescription(target.getShortDescription());
        source.setDateOfCorrespondence(target.getDateOfCorrespondence());
        source.setTopLevelDocuments(target.getTopLevelDocuments());
        source.setDocumentType(target.getDocumentType());
        source.setUploadedDocument(target.getUploadedDocument());
        source.setResponseClaimDocuments(target.getResponseClaimDocuments());
        source.setStartingClaimDocuments(target.getStartingClaimDocuments());
        source.setInitialConsiderationDocuments(target.getInitialConsiderationDocuments());
        source.setCaseManagementDocuments(target.getCaseManagementDocuments());
        source.setWithdrawalSettledDocuments(target.getWithdrawalSettledDocuments());
        source.setHearingsDocuments(target.getHearingsDocuments());
        source.setJudgmentAndReasonsDocuments(target.getJudgmentAndReasonsDocuments());
        source.setReconsiderationDocuments(target.getReconsiderationDocuments());
        source.setMiscDocuments(target.getMiscDocuments());
    }

    /**
     * Removes all documents from the {@code primaryDocumentTypeItems} list that have an ID
     * matching any document in the {@code referenceDocumentTypeItems} list.
     * <p>
     * This method performs an in-place modification of {@code primaryDocumentTypeItems}, removing
     * any {@link DocumentTypeItem} whose ID is found in the list of reference items.
     * <p>
     * The comparison is based on the {@code id} field, and null IDs are safely ignored.
     *
     * @param primaryDocumentTypeItems   the list of documents to remove items from; must be modifiable
     * @param referenceDocumentTypeItems the list of documents providing IDs to match against
     */
    public static void removeDocumentsWithMatchingIDs(List<DocumentTypeItem> primaryDocumentTypeItems,
                                                      List<DocumentTypeItem> referenceDocumentTypeItems) {
        if (CollectionUtils.isEmpty(primaryDocumentTypeItems) || CollectionUtils.isEmpty(referenceDocumentTypeItems)) {
            return;
        }
        Set<String> referenceIds = referenceDocumentTypeItems.stream()
                .map(DocumentTypeItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        primaryDocumentTypeItems.removeIf(item -> referenceIds.contains(item.getId()));
    }

    /**
     * Removes documents from the {@code primaryDocumentTypeItems} list that have a matching
     * binary document URL with any document in the {@code referenceDocumentTypeItems} list.
     * <p>
     * The comparison is based on the {@code documentBinaryUrl} field of the {@link UploadedDocumentType}
     * inside each {@link DocumentTypeItem}. If a binary URL in the primary list matches a binary URL in the
     * reference list, the corresponding item is removed from the primary list.
     * <p>
     * Documents with {@code null} or blank binary URLs are safely ignored during comparison.
     * <p>
     * This method modifies the {@code primaryDocumentTypeItems} list in place.
     *
     * @param primaryDocumentTypeItems   the modifiable list of documents to remove items from
     * @param referenceDocumentTypeItems the list of documents providing binary URLs to match against
     */
    public static void removeDocumentsWithMatchingBinaryURLs(List<DocumentTypeItem> primaryDocumentTypeItems,
                                                             List<DocumentTypeItem> referenceDocumentTypeItems) {
        if (CollectionUtils.isEmpty(primaryDocumentTypeItems) || CollectionUtils.isEmpty(referenceDocumentTypeItems)) {
            return;
        }
        Set<String> referenceBinaryUrls = referenceDocumentTypeItems.stream()
                .map(DocumentTypeItem::getValue)
                .filter(Objects::nonNull)
                .map(DocumentType::getUploadedDocument)
                .filter(Objects::nonNull)
                .map(UploadedDocumentType::getDocumentBinaryUrl)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        primaryDocumentTypeItems.removeIf(item ->
                item != null
                        && item.getValue() != null
                        && item.getValue().getUploadedDocument() != null
                        && referenceBinaryUrls.contains(item.getValue().getUploadedDocument().getDocumentBinaryUrl())
        );
    }
}
