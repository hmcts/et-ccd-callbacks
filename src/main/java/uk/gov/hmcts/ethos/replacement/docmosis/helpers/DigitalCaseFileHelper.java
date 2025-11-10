package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.BundleDocumentDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EUROPE_LONDON;

@Slf4j
public final class DigitalCaseFileHelper {

    private static final String DONE = "DONE";
    private static final String FAILED = "FAILED";
    public static final String NO_DOCS_FOR_DCF = "There are no documents available to include in the Digital Case "
                                                 + "File.";
    public static final String DCF_CHARACTER_LIMIT_ERROR =
        "The following documents have a name which exceeds the character limit which will cause "
        + "an issue when generating a DCF. Please update the document names and try again.";

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
                createTribunalCaseFile(caseData, bundle.value())));
    }

    private static DigitalCaseFileType createTribunalCaseFile(BaseCaseData caseData,
                                                              BundleDetails bundleDetails) {
        DigitalCaseFileType digitalCaseFile = caseData.getDigitalCaseFile();
        if (isEmpty(digitalCaseFile)) {
            digitalCaseFile = new DigitalCaseFileType();
        }
        switch (bundleDetails.getStitchStatus()) {
            case DONE -> {
                UploadedDocumentType uploadedDocumentType = getUploadedDocumentType(bundleDetails);
                digitalCaseFile.setUploadedDocument(uploadedDocumentType);
                digitalCaseFile.setStatus("DCF Generated: " + LocalDateTime.now(ZoneId.of(EUROPE_LONDON))
                        .format(NEW_DATE_TIME_PATTERN));
                digitalCaseFile.setError(null);
            }
            case FAILED -> {
                digitalCaseFile.setStatus(
                        "DCF Failed to generate: " + LocalDateTime.now(ZoneId.of(EUROPE_LONDON))
                                .format(NEW_DATE_TIME_PATTERN));
                digitalCaseFile.setError(bundleDetails.getStitchingFailureMessage());
            }
            default -> throw new IllegalStateException("Unexpected value: " + bundleDetails.getStitchStatus());
        }

        // Deprecating old field regardless of status
        digitalCaseFile.setDateGenerated(null);

        return digitalCaseFile;
    }

    @NotNull
    private static UploadedDocumentType getUploadedDocumentType(BundleDetails bundleDetails) {
        DocumentLink documentLink = bundleDetails.getStitchedDocument();
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename(documentLink.documentFilename);
        uploadedDocumentType.setDocumentUrl(documentLink.documentUrl);
        uploadedDocumentType.setDocumentBinaryUrl(documentLink.documentBinaryUrl);
        uploadedDocumentType.setCategoryId(DocumentCategory.TRIBUNAL_CASE_FILE.getId());
        return uploadedDocumentType;
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
        return CollectionUtils.isEmpty(doc.getExcludeFromDcf()) || !YES.equals(doc.getExcludeFromDcf().getFirst());
    }

    public static List<BundleDocumentDetails> getDocsForDcf(BaseCaseData caseData) {
        return caseData.getDocumentCollection().stream()
                .map(GenericTypeItem::getValue)
                .filter(doc -> doc.getUploadedDocument() != null && isExcludedFromDcf(doc))
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
        caseData.getDigitalCaseFile().setStatus("DCF Updating: " + LocalDateTime.now(ZoneId.of(EUROPE_LONDON))
                .format(NEW_DATE_TIME_PATTERN));
        caseData.getDigitalCaseFile().setError(null);
    }

    /**
     * Checks the document collection for any issues that would prevent successful DCF generation.
     * @param caseData the case data
     * @return an empty list if there are no issues. Otherwise, a list of error messages which would either be if the
     *      document names exceed the character limit or if there are no documents available to include in the Digital
     *      Case File.
     */
    public static List<String> validateDocumentCollectionForDcf(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())
            || areThereDocumentsAvailableForDcf(caseData.getDocumentCollection())) {
            return List.of(NO_DOCS_FOR_DCF);
        }
        List<String> errors = caseData.getDocumentCollection().stream()
            .map(DocumentTypeItem::getValue)
            .filter(documentType -> documentType.getUploadedDocument() != null
                                    && isExcludedFromDcf(documentType)
                                    && getDocumentName(documentType).length() > 255)
            .map(documentType -> "Document %s - %s".formatted(documentType.getDocNumber(),
                documentType.getUploadedDocument().getDocumentFilename()))
            .toList();

        return isNotEmpty(errors)
            ? Stream.concat(Stream.of(DCF_CHARACTER_LIMIT_ERROR), errors.stream()).toList()
            : errors;
    }

    private static boolean areThereDocumentsAvailableForDcf(List<DocumentTypeItem> documentCollection) {
        return documentCollection.stream()
            .anyMatch(doc -> doc.getValue().getUploadedDocument() != null && !isExcludedFromDcf(doc.getValue()));
    }
}
