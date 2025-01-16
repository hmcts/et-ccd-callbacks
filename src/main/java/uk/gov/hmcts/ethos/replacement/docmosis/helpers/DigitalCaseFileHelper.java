package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.BundleDocumentDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
public final class DigitalCaseFileHelper {

    private static final String DONE = "DONE";
    private static final String FAILED = "FAILED";
    private static final String UPLOAD = "Upload";
    private static final String REMOVE = "Remove";

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
                .filter(bundle -> List.of(DONE, FAILED).contains(bundle.value().getStitchStatus()))
                .findFirst();
        stitchedFile.ifPresent(bundle -> caseData.setDigitalCaseFile(
                createTribunalCaseFile(caseData.getDigitalCaseFile(), bundle.value())));
    }

    private static DigitalCaseFileType createTribunalCaseFile(DigitalCaseFileType digitalCaseFile,
                                                              BundleDetails bundleDetails) {
        if (isEmpty(digitalCaseFile)) {
            digitalCaseFile = new DigitalCaseFileType();
        }
        switch (bundleDetails.getStitchStatus()) {
            case DONE -> {
                DocumentLink documentLink = bundleDetails.getStitchedDocument();
                UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
                uploadedDocumentType.setDocumentFilename(documentLink.documentFilename);
                uploadedDocumentType.setDocumentUrl(documentLink.documentUrl);
                uploadedDocumentType.setDocumentBinaryUrl(documentLink.documentBinaryUrl);
                uploadedDocumentType.setCategoryId(DocumentCategory.TRIBUNAL_CASE_FILE.getId());
                digitalCaseFile.setUploadedDocument(uploadedDocumentType);
                digitalCaseFile.setStatus("DCF Updated: " + LocalDateTime.now().format(NEW_DATE_TIME_PATTERN));
                digitalCaseFile.setError(null);
            }
            case FAILED -> {
                digitalCaseFile.setStatus(
                        "DCF Failed to generate: " + LocalDateTime.now().format(NEW_DATE_TIME_PATTERN));
                digitalCaseFile.setError(bundleDetails.getStitchingFailureMessage());
            }
            default -> throw new IllegalStateException("Unexpected value: " + bundleDetails.getStitchStatus());
        }

        // Deprecating old field regardless of status
        digitalCaseFile.setDateGenerated(null);

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

    public static List<BundleDocumentDetails> getDocsForDcf(BaseCaseData caseData) {
        return caseData.getDocumentCollection().stream()
                .map(GenericTypeItem::getValue)
                .filter(doc -> doc.getUploadedDocument() != null && DigitalCaseFileHelper.isExcludedFromDcf(doc))
                .map(doc -> BundleDocumentDetails.builder()
                        .name(DigitalCaseFileHelper.getDocumentName(doc))
                        .sourceDocument(DocumentLink.builder()
                                .documentUrl(doc.getUploadedDocument().getDocumentUrl())
                                .documentBinaryUrl(doc.getUploadedDocument().getDocumentBinaryUrl())
                                .documentFilename(doc.getUploadedDocument().getDocumentFilename())
                                .build())
                        .build())
                .toList();
    }

    public static void setUpdatingStatus(CaseData caseData) {
        if (isEmpty(caseData.getDigitalCaseFile())) {
            caseData.setDigitalCaseFile(new DigitalCaseFileType());
        }
        caseData.getDigitalCaseFile().setStatus("DCF Updating: " + LocalDateTime.now().format(NEW_DATE_TIME_PATTERN));
    }

    public static void uploadOrRemoveDcf(CaseData caseData) {
        switch (caseData.getUploadOrRemoveDcf()) {
            case UPLOAD -> {
                DigitalCaseFileType digitalCaseFile = caseData.getDigitalCaseFile();
                if (isNotEmpty(digitalCaseFile)) {
                    digitalCaseFile.setStatus("DCF Uploaded: " + LocalDateTime.now().format(NEW_DATE_TIME_PATTERN));
                    digitalCaseFile.setError(null);

                    // Deprecating old field
                    digitalCaseFile.setDateGenerated(null);
                }
            }
            case REMOVE -> caseData.setDigitalCaseFile(null);
            default -> log.error("Invalid uploadOrRemoveDcf value: {}", caseData.getUploadOrRemoveDcf());
        }
        caseData.setUploadOrRemoveDcf(null);
    }
}
