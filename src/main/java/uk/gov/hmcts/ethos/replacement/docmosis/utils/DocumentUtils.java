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
import java.util.Optional;
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
        if (ObjectUtils.isNotEmpty(documentTypeItem) && ObjectUtils.isNotEmpty(documentTypeItem.getValue())) {
            documentTypeItem.getValue().setShortDescription(shortDescription);
            documentTypeItems.add(documentTypeItem);
        }
    }

    /**
     * Checks whether the provided list of {@link DocumentTypeItem} contains a document with the given name.
     *
     * @param documentTypeItems the list of {@link DocumentTypeItem} to search.
     * @param binaryUrl the name of the document to look for.
     * @return {@code true} if a document with the specified name exists in the list; {@code false} otherwise.
     */
    public static boolean containsDocumentWithBinaryUrl(List<DocumentTypeItem> documentTypeItems, String binaryUrl) {
        if (CollectionUtils.isEmpty(documentTypeItems) || StringUtils.isBlank(binaryUrl)) {
            return false;
        }
        for (DocumentTypeItem documentTypeItem : documentTypeItems) {
            if (ObjectUtils.isNotEmpty(documentTypeItem.getValue())
                    && ObjectUtils.isNotEmpty(documentTypeItem.getValue().getUploadedDocument())
                    && StringUtils.isNotEmpty(documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl())
                    && documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl().equals(binaryUrl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a {@link DocumentTypeItem} to the given list of {@link DocumentTypeItem} only if:
     * <ul>
     *   <li>None of the following are null or empty:
     *     <ul>
     *       <li>{@code documentTypeItem}</li>
     *       <li>{@code documentTypeItem.getValue()}</li>
     *       <li>{@code documentTypeItem.getValue().getUploadedDocument()}</li>
     *       <li>{@code documentTypeItem.getValue().getUploadedDocument().getDocumentFilename()}</li>
     *     </ul>
     *   </li>
     *   <li>No existing item in {@code documentTypeItems} has the same document filename.</li>
     * </ul>
     * If the filename is unique, the item is added to the {@code documentTypeItems} list.
     *
     * @param documentTypeItems the list of existing {@link DocumentTypeItem} instances.
     * @param documentTypeItem the {@link DocumentTypeItem} to be conditionally added to {@code documentTypeItems}.
     */
    public static void addIfBinaryUrlNotExists(List<DocumentTypeItem> documentTypeItems,
                                               DocumentTypeItem documentTypeItem) {
        if (documentTypeItems == null
                || ObjectUtils.isEmpty(documentTypeItem)
                || ObjectUtils.isEmpty(documentTypeItem.getValue())
                || ObjectUtils.isEmpty(documentTypeItem.getValue().getUploadedDocument())
                || StringUtils.isEmpty(documentTypeItem.getValue().getUploadedDocument().getDocumentFilename())
                || containsDocumentWithBinaryUrl(documentTypeItems,
                documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl())) {
            return;
        }
        documentTypeItems.add(documentTypeItem);
    }

    /**
     * Removes from the primary list any {@link DocumentTypeItem} whose binary URL matches
     * one found in the reference list.
     * <p>
     * Each {@link DocumentTypeItem} is expected to contain a {@link DocumentType}, which in turn contains an
     * {@link UploadedDocumentType} that holds the binary URL. This method compares binary URLs between the two lists.
     * If any document in the primary list has a binary URL that also exists in the reference list, it will be removed
     * from the primary list.
     * <p>
     * The comparison is:
     * <ul>
     *   <li>Null-safe — any null intermediate object (e.g., value, document type, uploaded document) is ignored.</li>
     *   <li>Based on exact equality of non-null binary URLs.</li>
     * </ul>
     *
     * @param primaryDocumentTypeItems   the list of {@code DocumentTypeItem} objects to be modified;
     *                                   matching items will be removed
     * @param referenceDocumentTypeItems the list of {@code DocumentTypeItem} objects whose
     *                                   binary URLs are used for comparison
     */
    public static void removeDocumentsWithMatchingBinaryUrls(List<DocumentTypeItem> primaryDocumentTypeItems,
                                                             List<DocumentTypeItem> referenceDocumentTypeItems) {
        Set<String> referenceBinaryUrls = referenceDocumentTypeItems.stream()
                .map(item -> Optional.ofNullable(item)
                .map(DocumentTypeItem::getValue)
                .map(DocumentType::getUploadedDocument)
                .map(UploadedDocumentType::getDocumentBinaryUrl)
                .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        primaryDocumentTypeItems.removeIf(item -> {
            String url = Optional.ofNullable(item)
                    .map(DocumentTypeItem::getValue)
                    .map(DocumentType::getUploadedDocument)
                    .map(UploadedDocumentType::getDocumentBinaryUrl)
                    .orElse(null);
            return url != null && referenceBinaryUrls.contains(url);
        });
    }
}
