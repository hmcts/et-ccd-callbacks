package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_TO_A_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtil.addDocumentIfNotExists;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtil.setDocumentTypeItemLevels;

public final class ET3DocumentHelper {

    private static final String ET3_FORM_ENGLISH_DESCRIPTION = "ET3 form English version";
    private static final String ET3_FORM_WELSH_DESCRIPTION = "ET3 form Welsh version";
    private static final String ET3_EMPLOYER_CONTEST_CLAIM_DOCUMENT = "ET3 employer contest claim document";
    private static final String ET3_RESPONDENT_SUPPORT_DOCUMENT = "ET3 respondent support document";

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
        if (isEmpty(caseData.getRespondentCollection())) {
            return;
        }
        if (caseData.getDocumentCollection() == null) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue())) {
                if (ACCEPTED_STATE.equals(respondentSumTypeItem.getValue().getResponseStatus())) {
                    List<DocumentTypeItem> documentTypeItems =
                            findAllET3DocumentsOfRespondent(respondentSumTypeItem);
                    for (DocumentTypeItem documentTypeItem : documentTypeItems) {
                        addDocumentIfNotExists(caseData.getDocumentCollection(), documentTypeItem);
                    }
                } else {
                    removeET3DocumentsFromDocumentCollection(caseData.getDocumentCollection());
                }
            }
        }
        DocumentHelper.setDocumentNumbers(caseData);
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
     * Removes ET3-related documents from the given list of {@link DocumentTypeItem}.
     * <p>
     * First, the method checks if the {@code documentTypeItems} list is empty. If not, it iterates through the list and
     * removes any document whose {@code topLevelDocuments} field has the value "Response to a claim",
     * indicating it is an ET3 document.
     *
     * @param documentTypeItems the list of documents to filter, removing any ET3-related items
     */
    public static void removeET3DocumentsFromDocumentCollection(List<DocumentTypeItem> documentTypeItems) {
        if (CollectionUtils.isEmpty(documentTypeItems)) {
            return;
        }
        documentTypeItems.removeIf(documentTypeItem -> ObjectUtils.isNotEmpty(documentTypeItem.getValue())
                && ObjectUtils.isNotEmpty(documentTypeItem.getValue().getUploadedDocument())
                && RESPONSE_TO_A_CLAIM.equals(documentTypeItem.getValue().getTopLevelDocuments()));
    }

}
