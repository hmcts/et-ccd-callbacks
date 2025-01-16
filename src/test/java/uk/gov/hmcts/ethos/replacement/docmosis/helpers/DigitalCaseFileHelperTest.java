package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_TIME_PATTERN;

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
    void isExcludedFromDCF(DocumentType documentType, boolean excluded) {
        assertEquals(DigitalCaseFileHelper.isExcludedFromDcf(documentType), excluded);
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
    void uploadOrRemoveDcf_InvalidValue() {
        caseData.setUploadOrRemoveDcf("Invalid value");
        assertDoesNotThrow(() -> DigitalCaseFileHelper.uploadOrRemoveDcf(caseData));
        assertNull(caseData.getUploadOrRemoveDcf());
    }

    @Test
    void dcf_failedToGenerate() {
        BundleDetails bundleDetails = BundleDetails.builder()
                .stitchStatus("FAILED")
                .stitchingFailureMessage("Failed to generate")
                .build();
        caseData.setCaseBundles(List.of(Bundle.builder().value(bundleDetails).build()));
        assertDoesNotThrow(() -> DigitalCaseFileHelper.addDcfToDocumentCollection(caseData));
        assertEquals("DCF Failed to generate: " + LocalDateTime.now().format(NEW_DATE_TIME_PATTERN),
                caseData.getDigitalCaseFile().getStatus());
        assertEquals("Failed to generate", caseData.getDigitalCaseFile().getError());
    }

    @Test
    void setDcfUpdatingStatus() {
        DigitalCaseFileHelper.setUpdatingStatus(caseData);
        assertEquals("DCF Updating: " + LocalDateTime.now().format(NEW_DATE_TIME_PATTERN),
                caseData.getDigitalCaseFile().getStatus());
    }
}