package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_ACCEPTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_REJECTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_TO_A_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.addIfBinaryUrlNotExists;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.removeDocumentsWithMatchingBinaryUrls;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.setDocumentTypeItemLevels;

public final class ET3DocumentHelper {

    private static final String ET3_FORM_ENGLISH_DESCRIPTION = "ET3 form English version";
    private static final String ET3_FORM_WELSH_DESCRIPTION = "ET3 form Welsh version";
    private static final String ET3_EMPLOYER_CONTEST_CLAIM_DOCUMENT = "ET3 employer contest claim document";
    private static final String ET3_RESPONDENT_SUPPORT_DOCUMENT = "ET3 respondent support document";
    private static final String ET3_RESPONDENT_CLAIM_DOCUMENT = "ET3 respondent claim document";
    private static final String ET3_ACCEPTED_NOTIFICATION_DOCUMENT_ID = "2.11";
    private static final String ET3_NOTIFICATION_DOCUMENT_SHORT_DESCRIPTION =
            "ET3 response \"%s\" status document";

    private ET3DocumentHelper() {
        // Helper classes should not have a public or default constructor.
    }

    /**
     * Adds or removes ET3-related documents in the {@link CaseData#getDocumentCollection()} based on the response
     * status of each respondent in the {@link CaseData#getRespondentCollection()}.
     *
     * <p>
     *
     * - If the respondent's response status is {@code ACCEPTED_STATE}, all ET3 documents associated with that
     *   respondent are added to the document collection (if not already present).<br>
     * - If the response status is not accepted, any existing ET3 documents are removed
     * from the document collection.<br>
     * - If the document collection is initially null, it is initialized as an empty list.<br>
     *
     * <p>
     *
     * After processing all respondents, document numbers are updated accordingly using
     * {@link DocumentHelper#setDocumentNumbers(BaseCaseData)}.
     *
     * @param caseData the {@link CaseData} object containing respondents and the document collection to update
     */
    public static void addOrRemoveET3Documents(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return;
        }
        if (caseData.getDocumentCollection() == null) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue())) {
                List<DocumentTypeItem> documentTypeItems = findAllET3DocumentsOfRespondent(respondentSumTypeItem);
                if (isET3NotificationDocumentTypeResponseAccepted(caseData.getEt3NotificationDocCollection())
                        && ACCEPTED_STATE.equals(respondentSumTypeItem.getValue().getResponseStatus())) {
                    for (DocumentTypeItem documentTypeItem : documentTypeItems) {
                        addIfBinaryUrlNotExists(caseData.getDocumentCollection(), documentTypeItem);
                    }
                } else {
                    removeDocumentsWithMatchingBinaryUrls(caseData.getDocumentCollection(), documentTypeItems);
                }
            }
        }
        DocumentHelper.setDocumentNumbers(caseData);
    }

    /**
     * Determines whether the first {@link DocumentTypeItem} in the provided list represents
     * an accepted ET3 Notification document response.
     * <p>
     * The method checks if the list is non-empty, the first item and its nested properties
     * are not null or blank, and whether the document type is equal to {@code "2.11"},
     * which is considered the accepted ET3 response type.
     * <p>
     * Returns {@code false} if:
     * <ul>
     *   <li>The list is {@code null} or empty</li>
     *   <li>The first item is {@code null}</li>
     *   <li>The document type is blank or not equal to {@code "2.11"}</li>
     * </ul>
     *
     * @param documentTypeItems the list of {@code DocumentTypeItem} to evaluate
     * @return {@code true} if the first document's type is {@code "2.11"}; {@code false} otherwise
     */
    public static boolean isET3NotificationDocumentTypeResponseAccepted(List<DocumentTypeItem> documentTypeItems) {
        if (CollectionUtils.isEmpty(documentTypeItems)) {
            return false;
        }
        DocumentTypeItem documentTypeItem = documentTypeItems.get(0);
        if (ObjectUtils.isEmpty(documentTypeItem)
                || ObjectUtils.isEmpty(documentTypeItem.getValue())
                || StringUtils.isBlank(documentTypeItem.getValue().getTypeOfDocument())) {
            return false;
        }
        return ET3_ACCEPTED_NOTIFICATION_DOCUMENT_ID.equals(documentTypeItem.getValue().getTypeOfDocument());
    }

    /**
     * Retrieves all ET3-related documents associated with the given {@link RespondentSumTypeItem}.
     * <p>
     * This includes:
     * <ul>
     *   <li>The ET3 form in English, if available</li>
     *   <li>The ET3 form in Welsh, if available</li>
     *   <li>Any attachments related to the ET3 response (e.g., contesting the claim)</li>
     *   <li>Any respondent support documents related to the ET3 response</li>
     * </ul>
     * For each document, appropriate metadata such as document levels and short descriptions are set.
     *
     * @param respondentSumTypeItem the respondent item containing possible ET3 documents
     * @return a list of {@link DocumentTypeItem} objects representing all ET3-related documents for the respondent,
     *         or an empty list if none are found
     */
    public static List<DocumentTypeItem> findAllET3DocumentsOfRespondent(
            RespondentSumTypeItem respondentSumTypeItem) {
        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        if (hasET3Document(respondentSumTypeItem)) {
            RespondentSumType respondentSumType = respondentSumTypeItem.getValue();
            addUploadedDocumentTypeToDocumentTypeItems(documentTypeItems,
                    respondentSumType.getEt3Form(),
                    RESPONSE_TO_A_CLAIM,
                    ET3,
                    ET3_FORM_ENGLISH_DESCRIPTION);
            addUploadedDocumentTypeToDocumentTypeItems(documentTypeItems,
                    respondentSumType.getEt3FormWelsh(),
                    RESPONSE_TO_A_CLAIM,
                    ET3,
                    ET3_FORM_WELSH_DESCRIPTION);
            addUploadedDocumentTypeToDocumentTypeItems(documentTypeItems,
                    respondentSumType.getEt3ResponseEmployerClaimDocument(),
                    RESPONSE_TO_A_CLAIM,
                    ET3_ATTACHMENT,
                    ET3_RESPONDENT_CLAIM_DOCUMENT);
            if (CollectionUtils.isNotEmpty(respondentSumType.getEt3ResponseContestClaimDocument())) {
                for (DocumentTypeItem documentTypeItem : respondentSumType.getEt3ResponseContestClaimDocument()) {
                    setDocumentTypeItemLevels(documentTypeItem, RESPONSE_TO_A_CLAIM, ET3_ATTACHMENT);
                    documentTypeItem.getValue().setShortDescription(ET3_EMPLOYER_CONTEST_CLAIM_DOCUMENT);
                    documentTypeItems.add(documentTypeItem);
                }
            }
            addUploadedDocumentTypeToDocumentTypeItems(documentTypeItems,
                    respondentSumTypeItem.getValue().getEt3ResponseRespondentSupportDocument(),
                    RESPONSE_TO_A_CLAIM,
                    ET3_ATTACHMENT,
                    ET3_RESPONDENT_SUPPORT_DOCUMENT);
        }
        return documentTypeItems;
    }

    /**
     * Determines whether the given {@link RespondentSumTypeItem} contains any ET3-related documents.
     * <p>
     * This includes checks for the presence of:
     * <ul>
     *   <li>ET3 form (English)</li>
     *   <li>ET3 form (Welsh)</li>
     *   <li>Documents contesting the claim</li>
     *   <li>Respondent support documents</li>
     *   <li>Employer claim documents</li>
     * </ul>
     *
     * @param respondentSumTypeItem the respondent item to check for ET3-related documents
     * @return {@code true} if at least one ET3 document is present; {@code false} otherwise
     */
    public static boolean hasET3Document(RespondentSumTypeItem respondentSumTypeItem) {
        return ObjectUtils.isNotEmpty(respondentSumTypeItem)
                && ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue())
                && (ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue().getEt3Form())
                || ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue().getEt3FormWelsh())
                || CollectionUtils.isNotEmpty(respondentSumTypeItem.getValue().getEt3ResponseContestClaimDocument())
                || ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue().getEt3ResponseRespondentSupportDocument())
                || ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue().getEt3ResponseEmployerClaimDocument()));
    }

    /**
     * Updates the given list of {@link DocumentTypeItem} by synchronizing ET3 notification documents.
     * <p>
     * This method performs two main actions:
     * <ul>
     *     <li>Removes any existing documents from {@code documentTypeItems} that have a top-level document
     *         status of {@code "Response Accepted"} or {@code "Response Rejected"} and are not present
     *         in the provided {@code et3DocumentTypeItems} list (based on binary document URL).</li>
     *     <li>Adds new ET3 notification documents from {@code et3DocumentTypeItems} to {@code documentTypeItems}
     *         if they don't already exist (by binary URL), and sets their document levels and short descriptions
     *         based on whether the document type indicates acceptance or rejection.</li>
     * </ul>
     *
     * @param documentTypeItems      the list of existing {@link DocumentTypeItem}s to be updated;
     *                               items may be removed or added based on ET3 matching logic
     * @param et3DocumentTypeItems   the list of new ET3 notification {@link DocumentTypeItem}s
     *                               to merge into the collection
     */
    public static void updateET3NotificationDocumentsInCollection(List<DocumentTypeItem> documentTypeItems,
                                                                  List<DocumentTypeItem> et3DocumentTypeItems) {
        if (CollectionUtils.isEmpty(et3DocumentTypeItems)) {
            return;
        }

        // Remove existing items from documentTypeItems if they match RESPONSE_ACCEPTED/REJECTED
        // and are not present in the new ET3 list
        documentTypeItems.removeIf(existingItem -> {
            DocumentType value = existingItem.getValue();
            if (value == null) {
                return false;
            }

            String responseClaimDocuments = value.getResponseClaimDocuments();
            if (!RESPONSE_ACCEPTED.equals(responseClaimDocuments)
                    && !RESPONSE_REJECTED.equals(responseClaimDocuments)) {
                return false;
            }
            // Remove if no matching binary URL exists in the ET3 list
            String binaryUrl = value.getUploadedDocument() != null
                    ? value.getUploadedDocument().getDocumentBinaryUrl()
                    : null;
            return StringUtils.isNotBlank(binaryUrl)
                    && et3DocumentTypeItems.stream()
                    .map(DocumentTypeItem::getValue)
                    .filter(Objects::nonNull)
                    .map(DocumentType::getUploadedDocument)
                    .filter(Objects::nonNull)
                    .map(UploadedDocumentType::getDocumentBinaryUrl)
                    .noneMatch(binaryUrl::equals);
        });

        for (DocumentTypeItem item : et3DocumentTypeItems) {
            DocumentType value = item.getValue();
            String typeOfDoc = value != null ? value.getTypeOfDocument() : null;
            if (StringUtils.isNotBlank(typeOfDoc)) {
                boolean isAccepted = ET3_ACCEPTED_NOTIFICATION_DOCUMENT_ID.equals(typeOfDoc);
                String status = isAccepted ? RESPONSE_ACCEPTED : RESPONSE_REJECTED;
                setDocumentTypeItemLevels(item, RESPONSE_TO_A_CLAIM, status);
                value.setShortDescription(String.format(ET3_NOTIFICATION_DOCUMENT_SHORT_DESCRIPTION, status));
                value.setTypeOfDocument(ET3);
                addIfBinaryUrlNotExists(documentTypeItems, item);
            }
        }
    }

    /**
     * Checks whether the acceptance status of document types in the given list is inconsistent.
     * <p>
     * A document type of "2.11" is considered <strong>accepted</strong>, while any other type is considered
     * <strong>not accepted</strong>. The method returns:
     * <ul>
     *   <li>{@code true} if the list is {@code null}, empty, or the first item's type is blank
     *   (i.e., cannot determine acceptance).</li>
     *   <li>{@code true} if the list contains a mix of accepted ("2.11") and not accepted
     *   (any other) document types.</li>
     *   <li>{@code false} if all documents have the same acceptance status (all "2.11" or all not "2.11").</li>
     * </ul>
     *
     * @param documentTypeItems the list of {@code DocumentTypeItem} objects to evaluate
     * @return {@code true} if the acceptance status is inconsistent or the list is empty/invalid;
     *         {@code false} if all document types are uniformly accepted or not accepted
     */
    public static boolean hasInconsistentAcceptanceStatus(List<DocumentTypeItem> documentTypeItems) {
        if (isFirstDocumentTypeInvalid(documentTypeItems)) {
            return true;
        }
        String firstType = documentTypeItems.get(0).getValue().getTypeOfDocument();
        for (int i = 1; i < documentTypeItems.size(); i++) {
            DocumentTypeItem item = documentTypeItems.get(i);
            if (ObjectUtils.isEmpty(item) || ObjectUtils.isEmpty(item.getValue())
                    || ET3_ACCEPTED_NOTIFICATION_DOCUMENT_ID.equals(firstType)
                    && !ET3_ACCEPTED_NOTIFICATION_DOCUMENT_ID.equals(item.getValue().getTypeOfDocument())
                    || !ET3_ACCEPTED_NOTIFICATION_DOCUMENT_ID.equals(firstType)
                    && ET3_ACCEPTED_NOTIFICATION_DOCUMENT_ID.equals(item.getValue().getTypeOfDocument())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFirstDocumentTypeInvalid(List<DocumentTypeItem> documentTypeItems) {
        return CollectionUtils.isEmpty(documentTypeItems)
                || ObjectUtils.isEmpty(documentTypeItems.get(0))
                || ObjectUtils.isEmpty(documentTypeItems.get(0).getValue())
                || StringUtils.isBlank(documentTypeItems.get(0).getValue().getTypeOfDocument());
    }

    /**
     * Checks if the list of respondents contains at least one respondent
     * with an accepted response status.
     *
     * @param respondentSumTypeItems the list of {@code RespondentSumTypeItem} objects to check
     * @return {@code true} if at least one respondent has a response status equal to {@code ACCEPTED_STATE};
     *         {@code false} otherwise
     */
    public static boolean containsNoRespondentWithResponseStatus(List<RespondentSumTypeItem> respondentSumTypeItems) {
        if (respondentSumTypeItems == null) {
            return true;
        }
        return respondentSumTypeItems.stream()
                .filter(Objects::nonNull)
                .map(RespondentSumTypeItem::getValue)
                .filter(Objects::nonNull)
                .noneMatch(respondent -> StringUtils.isNotBlank(respondent.getResponseStatus()));
    }
}
