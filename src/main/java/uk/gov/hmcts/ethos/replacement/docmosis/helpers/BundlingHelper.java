package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;

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
                .filter(bundle -> bundle.value().getStitchedDocument() != null)
                .findFirst();
        if (stitchedBundle.isEmpty()) {
            return;
        }

        DocumentLink documentLink = stitchedBundle.get().value().getStitchedDocument();
        if (CollectionUtils.isEmpty(caseData.getDigitalCaseFile())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        caseData.getDigitalCaseFile().add(createTribunalCaseFile(documentLink));
        caseData.getDigitalCaseFile().sort(Comparator.comparing(d -> d.getValue().getDateOfCorrespondence()));
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
                TRIBUNAL_CASE_FILE);
        documentTypeItem.getValue().setDateOfCorrespondence(LocalDate.now().toString());
        return documentTypeItem;
    }
}
