package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.BundleDocumentDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper.DCF_CHARACTER_LIMIT_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper.NO_DOCS_FOR_DCF;

class DigitalCaseFileHelperTest {

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("123456/2021")
                .build();
        Bundle bundle = Bundle.builder()
                .value(createBundleDetails())
                .build();
        caseData.setCaseBundles(List.of(bundle));
    }

    @Test
    void addDcfToDocumentCollection() {
        DigitalCaseFileHelper.addDcfToDocumentCollection(caseData);
        assertNotNull(caseData.getDigitalCaseFile());
    }

    @Test
    void addDcfToDocumentCollectionNoBundle() {
        caseData.setCaseBundles(null);
        DigitalCaseFileHelper.addDcfToDocumentCollection(caseData);
        assertNull(caseData.getDigitalCaseFile());
    }

    @ParameterizedTest
    @MethodSource("getDocumentTypes")
    void shouldGetDocumentName(DocumentType documentType, String docType, String docFileName, String docDate) {
        assertEquals(DigitalCaseFileHelper.getDocumentName(documentType),
                documentType.getDocNumber() + docType + docFileName + docDate);
    }

    private static Stream<Arguments> getDocumentTypes() {
        return Stream.of(
                Arguments.of(documentTypes().get(0),
                        "", "", ""),
                Arguments.of(documentTypes().get(1),
                        "", "", ""),
                Arguments.of(documentTypes().get(2),
                        " - pdf", " - test", " - 01-05-2011")
        );
    }

    @ParameterizedTest
    @MethodSource("getDocumentTypesForExcluded")
    void isIncludedInDCF(DocumentType documentType, boolean excluded) {
        assertEquals(DigitalCaseFileHelper.isIncludedInDcf(documentType), excluded);
    }

    private static Stream<Arguments> getDocumentTypesForExcluded() {
        return Stream.of(
                Arguments.of(documentTypes().get(0), true),
                Arguments.of(documentTypes().get(1), true),
                Arguments.of(documentTypes().get(2), true),
                Arguments.of(documentTypes().get(3), false),
                Arguments.of(documentTypes().get(4), true)
        );
    }

    private static List<DocumentType> documentTypes() {
        return List.of(DocumentType.builder()
                        .uploadedDocument(UploadedDocumentType.builder().build())
                        .build(),
                DocumentType.builder()
                        .documentType("")
                        .dateOfCorrespondence("")
                        .uploadedDocument(UploadedDocumentType.builder().documentFilename("").build())
                        .build(),
                DocumentType.builder().documentType("pdf")
                        .docNumber("1")
                        .dateOfCorrespondence("2011-05-01")
                        .uploadedDocument(UploadedDocumentType.builder()
                                .documentFilename("test")
                                .build()).build(),
                DocumentType.builder()
                        .excludeFromDcf(List.of("Yes"))
                        .uploadedDocument(UploadedDocumentType.builder().build())
                        .build(),
                DocumentType.builder()
                        .excludeFromDcf(List.of("No"))
                        .uploadedDocument(UploadedDocumentType.builder().build())
                        .build()
        );
    }

    private BundleDetails createBundleDetails() {
        DocumentLink documentLink = DocumentLink.builder()
                .documentFilename("test.pdf")
                .documentUrl("http://test.com")
                .documentBinaryUrl("http://test.com/binary")
                .build();
        return BundleDetails.builder()
                .id(UUID.randomUUID().toString())
                .stitchedDocument(documentLink)
                .stitchStatus("DONE")
                .build();
    }

    @Test
    void dcf_failedToGenerate() {
        BundleDetails bundleDetails = BundleDetails.builder()
                .stitchStatus("FAILED")
                .stitchingFailureMessage("Failed to generate")
                .build();
        caseData.setCaseBundles(List.of(Bundle.builder().value(bundleDetails).build()));
        assertDoesNotThrow(() -> DigitalCaseFileHelper.addDcfToDocumentCollection(caseData));
        assertEquals("DCF Failed to generate: "
                     + LocalDateTime.now(ZoneId.of("Europe/London")).format(NEW_DATE_TIME_PATTERN),
                caseData.getDigitalCaseFile().getStatus());
        assertEquals("Failed to generate", caseData.getDigitalCaseFile().getError());
    }

    @Test
    void setDcfUpdatingStatus() {
        DigitalCaseFileHelper.setUpdatingStatus(caseData);
        assertEquals("DCF Updating: " + LocalDateTime.now(ZoneId.of("Europe/London")).format(NEW_DATE_TIME_PATTERN),
                caseData.getDigitalCaseFile().getStatus());
    }

    @Test
    void validateDocumentCollectionForDcf_noDocuments_returnsError() {
        List<String> errors = DigitalCaseFileHelper.validateDocumentCollectionForDcf(caseData);
        assertEquals(List.of(NO_DOCS_FOR_DCF), errors);
    }

    @Test
    void validateDocumentCollectionForDcf_allDocumentsExcluded_returnsError() {
        // Build a single excluded document (excludeFromDcf = Yes)
        UploadedDocumentType uploaded = UploadedDocumentType.builder()
                .documentFilename("a.pdf")
                .documentUrl("http://dm/doc/1")
                .documentBinaryUrl("http://dm/doc/1/binary")
                .build();
        DocumentType excludedDoc = DocumentType.builder()
                .docNumber("1")
                .uploadedDocument(uploaded)
                .excludeFromDcf(List.of("Yes"))
                .build();
        DocumentTypeItem item = DocumentTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(excludedDoc)
                .build();
        caseData.setDocumentCollection(new ArrayList<>(List.of(item)));

        List<String> errors = DigitalCaseFileHelper.validateDocumentCollectionForDcf(caseData);
        assertEquals(List.of(NO_DOCS_FOR_DCF), errors);
    }

    @Test
    void getDocsForDcf_filtersExcludedAndNullUploads_andMapsFields() {

        // Included document (no exclude flag)
        UploadedDocumentType incUpload = UploadedDocumentType.builder()
                .documentFilename("inc.pdf")
                .documentUrl("http://dm/doc/inc")
                .documentBinaryUrl("http://dm/doc/inc/binary")
                .build();
        DocumentType included = DocumentType.builder()
                .docNumber("1")
                .documentType("TYPE")
                .dateOfCorrespondence("2025-01-01")
                .uploadedDocument(incUpload)
                .build();
        DocumentTypeItem includedItem = DocumentTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(included)
            .build();

        // Excluded document (should be filtered out)
        UploadedDocumentType excUpload = UploadedDocumentType.builder()
                .documentFilename("exc.pdf")
                .documentUrl("http://dm/doc/exc")
                .documentBinaryUrl("http://dm/doc/exc/binary")
                .build();
        DocumentType excluded = DocumentType.builder()
                .docNumber("2")
                .uploadedDocument(excUpload)
                .excludeFromDcf(List.of("Yes"))
                .build();
        DocumentTypeItem excludedItem = DocumentTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(excluded)
            .build();

        // Null upload (should be filtered out)
        DocumentType nullUpload = DocumentType.builder()
                .docNumber("3")
                .build();
        DocumentTypeItem nullUploadItem = DocumentTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(nullUpload)
            .build();

        caseData.setDocumentCollection(List.of(includedItem, excludedItem, nullUploadItem));

        List<BundleDocumentDetails> docs = DigitalCaseFileHelper.getDocsForDcf(caseData);
        assertEquals(1, docs.size());
    }

    @Test
    void validateDocumentCollectionForDcf_filenameExceeds255_addsHeaderAndEntry() {
        String longName = "a".repeat(260) + ".pdf";
        UploadedDocumentType upload = UploadedDocumentType.builder()
                .documentFilename(longName)
                .documentUrl("http://dm/doc/long")
                .documentBinaryUrl("http://dm/doc/long/binary")
                .build();
        DocumentType doc = DocumentType.builder()
                .docNumber("1")
                .documentType("TYPE")
                .dateOfCorrespondence("2025-01-01")
                .uploadedDocument(upload)
                .build();
        DocumentTypeItem item = DocumentTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(doc)
            .build();
        caseData.setDocumentCollection(List.of(item));

        List<String> errors = DigitalCaseFileHelper.validateDocumentCollectionForDcf(caseData);
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals(DCF_CHARACTER_LIMIT_ERROR, errors.getFirst());
        assertEquals("Document 1 - " + longName, errors.get(1));
    }
}
