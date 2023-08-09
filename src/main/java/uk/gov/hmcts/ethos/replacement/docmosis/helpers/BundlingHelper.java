package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BundlingHelper {

    private BundlingHelper() {
        // access through static methods
    }

    /**
     * Add the stitched document to the digital case file field.
     * @param caseData data
     */
    public static void addBundleToDocumentCollection(CaseData caseData) {
        Optional<Bundle> stitchedBundle = caseData.getCaseBundles().stream()
                .filter(bundle -> bundle.getValue().getStitchedDocument() != null)
                .findFirst();
        if (stitchedBundle.isEmpty()) {
            return;
        }

        DocumentLink documentLink = stitchedBundle.get().getValue().getStitchedDocument();
        DocumentTypeItem documentTypeItem = createTribunalCaseFile(documentLink);
        List<DocumentTypeItem> newDocCollection = new ArrayList<>(List.of(documentTypeItem));
        caseData.setDigitalCaseFile(newDocCollection);
    }

    @NotNull
    private static DocumentTypeItem createTribunalCaseFile(DocumentLink documentLink) {
        UploadedDocumentType uploadedDocumentType = UploadedDocumentType.builder()
                .documentFilename(documentLink.documentFilename)
                .documentUrl(documentLink.documentUrl)
                .documentBinaryUrl(documentLink.documentBinaryUrl)
                .build();
        DocumentTypeItem documentTypeItem = DocumentHelper.createDocumentTypeItem(
                uploadedDocumentType,
                null);
        documentTypeItem.getValue().setDateOfCorrespondence(LocalDate.now().toString());
        return documentTypeItem;
    }
}
