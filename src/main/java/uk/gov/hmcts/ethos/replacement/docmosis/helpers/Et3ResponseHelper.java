package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * ET3 Response Helper provides methods to assist with the ET3 Response Form event.
 */
@Slf4j
public class Et3ResponseHelper {

    private static final String CLAIMANT_NAME_TABLE = "<pre> ET1 claimant name&#09&#09&#09&#09 %s</pre><hr>";
    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";

    private Et3ResponseHelper() {
        // Access through static methods
    }

    /**
     * Formats the name of the claimant for display on the Claimant name correct page.
     * @param caseData data for the current case.
     * @return Name ready for presentation on web.
     */
    public static String formatClaimantNameForHtml(CaseData caseData) {
        return String.format(CLAIMANT_NAME_TABLE, caseData.getClaimant());
    }

    /**
     * Validates that the employment start date is in the past and not after 
     * the employment end date if both dates are provided.
     * @param caseData data for the current case.
     * @return List of validation errors encountered.
     */
    public static List<String> validateEmploymentDates(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();
        String startDateStr = caseData.getEt3ResponseEmploymentStartDate();
        String endDateStr = caseData.getEt3ResponseEmploymentEndDate();

        if (isNullOrEmpty(startDateStr)) {
            return errors;
        }

        LocalDate startDate = LocalDate.parse(startDateStr);

        if (startDate.isAfter(LocalDate.now())) {
            errors.add(START_DATE_MUST_BE_IN_THE_PAST);
        }

        if (isNullOrEmpty(endDateStr)) {
            return errors;
        }

        LocalDate endDate = LocalDate.parse(endDateStr);

        if (startDate.isAfter(endDate)) {
            errors.add(END_DATE_MUST_BE_AFTER_THE_START_DATE);
        }

        return errors;
    }

    /**
     * Adds a group of documents to the ET3 response collection so that it can be displayed on the document tab.
     * @param caseData data for the current case.
     * @param documentTypeItemList the documents to be added to the document tab.
     */
    public static void addDocuments(CaseData caseData, List<DocumentTypeItem> documentTypeItemList) {
        if (CollectionUtils.isEmpty(documentTypeItemList)) {
            return;
        }

        if (caseData.getEt3ResponseDocumentCollection() == null) {
            caseData.setEt3ResponseDocumentCollection(new ArrayList<>());
        }

        for (DocumentTypeItem documentTypeItem : documentTypeItemList) {
            if (documentExistsOnCollection(
                caseData.getEt3ResponseDocumentCollection(),
                documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl())
            ) {
                continue;
            }

            DocumentType documentType = documentTypeItem.getValue();

            DocumentTypeItem documentItem = new DocumentTypeItem();
            documentItem.setId(UUID.randomUUID().toString());
            documentItem.setValue(documentType);

            caseData.getEt3ResponseDocumentCollection().add(documentTypeItem);
        }
    }

    /**
     * Adds a document to the ET3 response collection so that it can be displayed on the document tab.
     * @param caseData data for the current case.
     * @param documentToAdd the document to be added to the document tab.
     */
    public static void addDocument(CaseData caseData, UploadedDocumentType documentToAdd) {
        if (documentToAdd == null) {
            return;
        }

        if (caseData.getEt3ResponseDocumentCollection() == null) {
            caseData.setEt3ResponseDocumentCollection(new ArrayList<>());
        } else if (
            documentExistsOnCollection(caseData.getEt3ResponseDocumentCollection(),
                documentToAdd.getDocumentBinaryUrl())
        ) {
            return;
        }

        DocumentType documentType = new DocumentType();

        documentType.setTypeOfDocument("Other");
        documentType.setUploadedDocument(documentToAdd);
        documentType.setShortDescription("Uploaded with ET3 response");

        DocumentTypeItem documentItem = new DocumentTypeItem();

        documentItem.setId(UUID.randomUUID().toString());
        documentItem.setValue(documentType);

        caseData.getEt3ResponseDocumentCollection().add(documentItem);
    }

    /**
     * Checks if a document collection contains a document based on the URL of the document.
     * @param collectionToCheck a document collection to check.
     * @param documentUrlToFind the url of the document to be found.
     * @return true if found, false if not found or the collection passed is null.
     */
    private static boolean documentExistsOnCollection(List<DocumentTypeItem> collectionToCheck,
                                                      String documentUrlToFind) {
        if (collectionToCheck == null) {
            return false;
        }

        return collectionToCheck
            .stream()
            .anyMatch(
                d -> d.getValue()
                    .getUploadedDocument()
                    .getDocumentBinaryUrl()
                    .equals(documentUrlToFind)
            );
    }
}
