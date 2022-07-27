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
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.documentExistsOnCollection;

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

        if (CollectionUtils.isEmpty(caseData.getEt3ResponseDocumentCollection())) {
            caseData.setEt3ResponseDocumentCollection(new ArrayList<>());
        }

        documentTypeItemList.stream()
            .filter(d -> !documentExistsOnCollection(caseData.getEt3ResponseDocumentCollection(),
                d.getValue().getUploadedDocument().getDocumentBinaryUrl()))
            .forEach(documentToAdd -> saveFileToCollection(caseData, documentToAdd.getValue()));
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

        List<DocumentTypeItem> et3Documents = caseData.getEt3ResponseDocumentCollection();

        if (CollectionUtils.isEmpty(et3Documents)) {
            caseData.setEt3ResponseDocumentCollection(new ArrayList<>());
        } else if (documentExistsOnCollection(et3Documents, documentToAdd.getDocumentBinaryUrl())) {
            return;
        }

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(documentToAdd);

        saveFileToCollection(caseData, documentType);
    }

    /**
     * Saves a document to the ET3Response document collection.
     * @param caseData data for the current case.
     * @param documentType the document to save
     */
    private static void saveFileToCollection(CaseData caseData, DocumentType documentType) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(documentType);

        caseData.getEt3ResponseDocumentCollection().add(documentTypeItem);
    }
}
