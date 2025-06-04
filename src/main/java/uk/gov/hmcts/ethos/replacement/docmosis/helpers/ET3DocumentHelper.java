package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CallbackObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_ACCEPTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_REJECTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_TO_A_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.addDocumentIfUnique;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.removeDocumentsWithMatchingBinaryURLs;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.removeDocumentsWithMatchingIDs;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.setDocumentTypeItemLevels;

public final class ET3DocumentHelper {

    private static final String ET3_FORM_ENGLISH_DESCRIPTION = "ET3 form English version";
    private static final String ET3_FORM_WELSH_DESCRIPTION = "ET3 form Welsh version";
    private static final String ET3_EMPLOYER_CONTEST_CLAIM_DOCUMENT = "ET3 employer contest claim document";
    private static final String ET3_RESPONDENT_SUPPORT_DOCUMENT = "ET3 respondent support document";
    private static final String ET3_RESPONDENT_CLAIM_DOCUMENT = "ET3 respondent claim document";
    private static final String ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_ENGLAND_WALES = "2.11";
    private static final String ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_SCOTLAND_LETTER_13 = "Letter 13";
    private static final String ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_SCOTLAND_LETTER_14 = "Letter 14";
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
                        addDocumentIfUnique(caseData.getDocumentCollection(), documentTypeItem);
                    }
                } else {
                    removeDocumentsWithMatchingIDs(caseData.getDocumentCollection(), documentTypeItems);
                    removeDocumentsWithMatchingBinaryURLs(caseData.getDocumentCollection(), documentTypeItems);
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
        return isAcceptedType(documentTypeItem.getValue().getTypeOfDocument());
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
     * Processes and adds ET3 notification documents from the {@code et3NotificationDocCollection}
     * into the main {@code documentCollection} of the provided {@link CaseData} object.
     * <p>
     * This method performs the following actions:
     * <ul>
     *     <li>Clones each document from the ET3 notification collection to avoid mutating the original items.</li>
     *     <li>Determines if the document type corresponds to an accepted or rejected ET3 response.</li>
     *     <li>Sets document classification levels and short descriptions based on the document type.</li>
     *     <li>Nullifies the original {@code typeOfDocument} after processing.</li>
     *     <li>Adds the document to the main collection if it doesn't already exist (by ID or binary URL).</li>
     *     <li>Sets document numbers for the updated collection.</li>
     * </ul>
     * If the {@code et3NotificationDocCollection} is empty or {@code null}, the method exits early.
     * If the {@code documentCollection} is {@code null}, it initializes it as a new list.
     *
     * @param caseData the {@link CaseData} object containing both ET3 notification and main document collections
     * @throws JsonProcessingException if cloning of a document fails during deep copy via JSON
     */
    public static void addET3NotificationDocumentsToDocumentCollection(CaseData caseData)
            throws JsonProcessingException {
        if (CollectionUtils.isEmpty(caseData.getEt3NotificationDocCollection())) {
            return;
        }
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        for (DocumentTypeItem item : caseData.getEt3NotificationDocCollection()) {
            DocumentTypeItem clonedItem = CallbackObjectUtils.cloneObject(item, DocumentTypeItem.class);
            DocumentType value = clonedItem.getValue();
            String typeOfDoc = value != null ? value.getTypeOfDocument() : null;
            if (StringUtils.isNotBlank(typeOfDoc)) {
                boolean isAccepted = ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_ENGLAND_WALES.equals(typeOfDoc);
                String status = isAccepted ? RESPONSE_ACCEPTED : RESPONSE_REJECTED;
                setDocumentTypeItemLevels(clonedItem, RESPONSE_TO_A_CLAIM, status);
                clonedItem.setId(UUID.randomUUID().toString());
                value.setShortDescription(String.format(ET3_NOTIFICATION_DOCUMENT_SHORT_DESCRIPTION, status));
                value.setTypeOfDocument(null);
                addDocumentIfUnique(caseData.getDocumentCollection(), clonedItem);
            }
        }
        DocumentHelper.setDocumentNumbers(caseData);
    }

    /**
     * Checks whether the list of document type items contains inconsistent acceptance statuses.
     * <p>
     * An "acceptance status" is considered consistent if all non-null document types in the list are either:
     * - All part of the accepted types (England & Wales, Scotland Letter 13, Scotland Letter 14), or
     * - All not part of the accepted types.
     * </p>
     *
     * @param documentTypeItems the list of document type items to check
     * @return {@code true} if the list has inconsistent acceptance statuses or contains invalid entries;
     *         {@code false} if all document types are consistently accepted or not accepted
     */
    public static boolean hasInconsistentAcceptanceStatus(List<DocumentTypeItem> documentTypeItems) {
        if (isFirstDocumentTypeInvalid(documentTypeItems)) {
            return true;
        }
        String firstType = documentTypeItems.get(0).getValue().getTypeOfDocument();
        boolean isFirstAcceptedType = isAcceptedType(firstType);
        for (int i = 1; i < documentTypeItems.size(); i++) {
            DocumentTypeItem item = documentTypeItems.get(i);
            if (ObjectUtils.isEmpty(item) || ObjectUtils.isEmpty(item.getValue())) {
                return true;
            }
            String currentType = item.getValue().getTypeOfDocument();
            boolean isCurrentAcceptedType = isAcceptedType(currentType);
            if (isFirstAcceptedType != isCurrentAcceptedType) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAcceptedType(String type) {
        return ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_ENGLAND_WALES.equals(type)
                || ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_SCOTLAND_LETTER_13.equals(type)
                || ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_SCOTLAND_LETTER_14.equals(type);
    }

    private static boolean isFirstDocumentTypeInvalid(List<DocumentTypeItem> documentTypeItems) {
        return CollectionUtils.isEmpty(documentTypeItems)
                || ObjectUtils.isEmpty(documentTypeItems.get(0))
                || ObjectUtils.isEmpty(documentTypeItems.get(0).getValue())
                || StringUtils.isBlank(documentTypeItems.get(0).getValue().getTypeOfDocument());
    }

    /**
     * Checks whether none of the respondents in the given list have a non-blank response status.
     * <p>
     * This method returns {@code true} if:
     * <ul>
     *     <li>The input list is {@code null}, or</li>
     *     <li>All respondents have a {@code null}, empty, or blank {@code responseStatus} field.</li>
     * </ul>
     *
     * @param respondentSumTypeItems the list of {@link RespondentSumTypeItem} to check; may be {@code null}
     * @return {@code true} if no respondent has a non-blank response status; {@code false} otherwise
     */
    public static boolean containsNoRespondentWithResponseStatus(List<RespondentSumTypeItem> respondentSumTypeItems) {
        return respondentSumTypeItems == null || respondentSumTypeItems.stream()
                .filter(Objects::nonNull)
                .map(RespondentSumTypeItem::getValue)
                .filter(Objects::nonNull)
                .noneMatch(respondent -> StringUtils.isNotBlank(respondent.getResponseStatus()));
    }

    /**
     * Determines whether the provided ET3 documents are consistent with the response statuses
     * of the given respondents.
     * <p>
     * The method evaluates whether at least one respondent with a non-blank response status
     * has a corresponding document type:
     * <ul>
     *     <li>If a respondent's response status is {@code ACCEPTED_STATE}, then there must be
     *         at least one document of type {@code ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE}.</li>
     *     <li>If the status is any other value, then there must be at least one document with a
     *         different type.</li>
     * </ul>
     * The method returns {@code true} as soon as one valid respondent-document match is found.
     * If the inputs are {@code null} or no valid match exists, it returns {@code false}.
     *
     * @param respondents the list of {@link RespondentSumTypeItem}s representing respondents
     *                    and their response statuses
     * @param documents   the list of {@link DocumentTypeItem}s representing ET3 documents uploaded to the case
     * @return {@code true} if any respondent with a valid response status has a matching document type;
     *         {@code false} otherwise
     */
    public static boolean areET3DocumentsConsistentWithRespondentResponses(
            List<RespondentSumTypeItem> respondents,
            List<DocumentTypeItem> documents) {
        if (respondents == null || documents == null) {
            return false;
        }
        boolean hasAcceptedDoc = documents.stream()
                .map(DocumentTypeItem::getValue)
                .filter(Objects::nonNull)
                .map(DocumentType::getTypeOfDocument)
                .anyMatch(ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_ENGLAND_WALES::equals);
        boolean hasRejectedDoc = documents.stream()
                .map(DocumentTypeItem::getValue)
                .filter(Objects::nonNull)
                .map(DocumentType::getTypeOfDocument)
                .anyMatch(docType -> !ET3_ACCEPTED_NOTIFICATION_DOCUMENT_TYPE_ENGLAND_WALES.equals(docType));
        for (RespondentSumTypeItem respondentSumTypeItem : respondents) {
            if (ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue())
                    && StringUtils.isNotBlank(respondentSumTypeItem.getValue().getResponseStatus())) {
                if (ACCEPTED_STATE.equals(respondentSumTypeItem.getValue().getResponseStatus())) {
                    if (hasAcceptedDoc) {
                        return true;
                    }
                } else {
                    if (hasRejectedDoc) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
