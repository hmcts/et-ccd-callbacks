package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public final class DigitalCaseFileHelper {
    private DigitalCaseFileHelper() {
        // access through static methods
    }

    /**
     * Add the stitched document to the digital case file field.
     * @param caseData data
     */
    public static void addDcfToDocumentCollection(BaseCaseData caseData) {
        Optional<Bundle> stitchedFile = emptyIfNull(caseData.getCaseBundles())
                .stream()
                .filter(bundle -> bundle.value().getStitchedDocument() != null)
                .findFirst();
        if (stitchedFile.isEmpty()) {
            return;
        }

        DocumentLink documentLink = stitchedFile.get().value().getStitchedDocument();
        caseData.setDigitalCaseFile(createTribunalCaseFile(documentLink));
    }

    private static DigitalCaseFileType createTribunalCaseFile(DocumentLink documentLink) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename(documentLink.documentFilename);
        uploadedDocumentType.setDocumentUrl(documentLink.documentUrl);
        uploadedDocumentType.setDocumentBinaryUrl(documentLink.documentBinaryUrl);
        uploadedDocumentType.setCategoryId(DocumentCategory.TRIBUNAL_CASE_FILE.getId());
        DigitalCaseFileType digitalCaseFile = new DigitalCaseFileType();
        digitalCaseFile.setUploadedDocument(uploadedDocumentType);
        digitalCaseFile.setDateGenerated(String.valueOf(LocalDate.now()));

        return digitalCaseFile;
    }

    public static String getDocumentName(DocumentType doc) {
        String docType = isNullOrEmpty(doc.getDocumentType())
                ? ""
                : " - " + doc.getDocumentType();
        String docFileName = isNullOrEmpty(doc.getUploadedDocument().getDocumentFilename())
                ? ""
                : " - " + doc.getUploadedDocument().getDocumentFilename();
        String docDate = isNullOrEmpty(doc.getDateOfCorrespondence())
                ? ""
                : " - " + LocalDate.parse(doc.getDateOfCorrespondence())
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return doc.getDocNumber()  + docType + docFileName + docDate;
    }

    public static boolean isExcludedFromDcf(DocumentType doc) {
        return CollectionUtils.isEmpty(doc.getExcludeFromDcf()) || !YES.equals(doc.getExcludeFromDcf().get(0));
    }
}
