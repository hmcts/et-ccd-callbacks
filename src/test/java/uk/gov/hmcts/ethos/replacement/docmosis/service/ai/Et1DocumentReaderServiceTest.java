package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.AiGatewayProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;

@ExtendWith(MockitoExtension.class)
class Et1DocumentReaderServiceTest {

    private static final String AUTH_TOKEN = "Bearer test-token";

    @Mock
    private DocumentManagementService documentManagementService;

    private AiGatewayProperties aiGatewayProperties;
    private Et1DocumentReaderService documentReaderService;

    @BeforeEach
    void setUp() {
        aiGatewayProperties = new AiGatewayProperties();
        documentReaderService = new Et1DocumentReaderService(documentManagementService, aiGatewayProperties);
    }

    @Test
    void downloadDocumentAttachment_respectsConfiguredMaxAttachmentBytes() {
        aiGatewayProperties.setMaxAttachmentBytes(5);

        UploadedDocumentType doc = uploadedDoc("http://dm/1/binary", "et1.pdf");
        when(documentManagementService.downloadFile(AUTH_TOKEN, "http://dm/1/binary"))
                .thenReturn(uploadedDocContent("et1.pdf", 6));

        assertThat(documentReaderService.downloadDocumentAttachment(AUTH_TOKEN, doc)).isNull();

        aiGatewayProperties.setMaxAttachmentBytes(6);
        AiDocumentAttachment attachment = documentReaderService.downloadDocumentAttachment(AUTH_TOKEN, doc);
        assertThat(attachment).isNotNull();
        assertThat(attachment.getData()).hasSize(6);
    }

    @Test
    void downloadDocumentAttachment_extractsDocumentUuidAndResolvesMimeTypes() {
        UploadedDocumentType xlsxDoc = uploadedDoc(
                "http://dm-store/documents/11111111-2222-3333-4444-555555555555/binary", "schedule.xlsx");
        when(documentManagementService.downloadFile(AUTH_TOKEN, xlsxDoc.getDocumentBinaryUrl()))
                .thenReturn(UploadedDocument.builder()
                        .content(new ByteArrayResource(new byte[4]))
                        .name("schedule.xlsx")
                        .contentType("application/octet-stream")
                        .build());

        AiDocumentAttachment attachment = documentReaderService.downloadDocumentAttachment(AUTH_TOKEN, xlsxDoc);

        assertThat(attachment).isNotNull();
        assertThat(attachment.getDocumentId()).isEqualTo("11111111-2222-3333-4444-555555555555");
        assertThat(attachment.getMimeType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    void downloadDocumentAttachment_handlesMissingTokenEmptyContentAndErrors() {
        assertThat(documentReaderService.downloadDocumentAttachment(null, uploadedDoc("http://dm/1", "a.pdf")))
                .isNull();
        assertThat(documentReaderService.downloadDocumentAttachment(AUTH_TOKEN, null)).isNull();

        UploadedDocumentType doc = uploadedDoc("http://dm/documents/abc-123/binary", "a.pdf");
        when(documentManagementService.downloadFile(AUTH_TOKEN, doc.getDocumentBinaryUrl())).thenReturn(null);
        assertThat(documentReaderService.downloadDocumentAttachment(AUTH_TOKEN, doc)).isNull();

        when(documentManagementService.downloadFile(AUTH_TOKEN, doc.getDocumentBinaryUrl()))
                .thenThrow(new RuntimeException("DM Store down"));
        assertThat(documentReaderService.downloadDocumentAttachment(AUTH_TOKEN, doc)).isNull();
    }

    @Test
    void findClaimDocumentAttachments_handlesNullCaseDeduplicatesAndFiltersEt1Vetting() {
        assertThat(documentReaderService.findClaimDocumentAttachments(null, AUTH_TOKEN)).isEmpty();

        when(documentManagementService.downloadFile(AUTH_TOKEN, "http://dm/documents/u1/binary"))
                .thenReturn(uploadedDocContent("ACAS_cert.pdf", 4));

        CaseData caseData = new CaseData();
        caseData.setDocumentCollection(List.of(
                docItem("1", "Other", uploadedDoc("http://dm/documents/u1/binary", "ACAS_cert.pdf")),
                docItem("2", "Other", uploadedDoc("http://dm/documents/u1/binary", "ACAS_cert.pdf")),
                docItem("3", "ET1 Vetting", uploadedDoc("http://dm/documents/u2/binary", "ET1 Vetting.pdf")),
                docItem("4", "Other", uploadedDoc("http://dm/documents/u3/binary", "unrelated.pdf"))
        ));

        List<AiDocumentAttachment> attachments = documentReaderService.findClaimDocumentAttachments(
                caseData, AUTH_TOKEN);

        assertThat(attachments).hasSize(1);
        assertThat(attachments.getFirst().getDocumentId()).isEqualTo("u1");
    }

    @Test
    void findClaimDocumentAttachments_respectsConfiguredTotalBytesAndMaxCount() {
        aiGatewayProperties.setMaxAttachmentBytes(10);
        aiGatewayProperties.setMaxTotalAttachmentBytes(12);
        aiGatewayProperties.setMaxAttachmentCount(2);

        when(documentManagementService.downloadFile(AUTH_TOKEN, "http://dm/1/binary"))
                .thenReturn(uploadedDocContent("ET1_1.pdf", 8));
        when(documentManagementService.downloadFile(AUTH_TOKEN, "http://dm/2/binary"))
                .thenReturn(uploadedDocContent("ET1_2.pdf", 6));
        when(documentManagementService.downloadFile(AUTH_TOKEN, "http://dm/3/binary"))
                .thenReturn(uploadedDocContent("ET1_3.pdf", 4));

        CaseData caseData = new CaseData();
        caseData.setDocumentCollection(List.of(
                docItem("1", ET1, uploadedDoc("http://dm/1/binary", "ET1_1.pdf")),
                docItem("2", ET1, uploadedDoc("http://dm/2/binary", "ET1_2.pdf")),
                docItem("3", ET1, uploadedDoc("http://dm/3/binary", "ET1_3.pdf"))
        ));

        List<AiDocumentAttachment> attachments = documentReaderService.findClaimDocumentAttachments(
                caseData, AUTH_TOKEN);

        assertThat(attachments)
                .extracting(AiDocumentAttachment::getFilename)
                .containsExactly("ET1_1.pdf", "ET1_3.pdf");
    }

    private UploadedDocument uploadedDocContent(String filename, int size) {
        return UploadedDocument.builder()
                .content(new ByteArrayResource(new byte[size]))
                .name(filename)
                .contentType("application/pdf")
                .build();
    }

    private UploadedDocumentType uploadedDoc(String binaryUrl, String filename) {
        UploadedDocumentType doc = new UploadedDocumentType();
        doc.setDocumentBinaryUrl(binaryUrl);
        doc.setDocumentFilename(filename);
        return doc;
    }

    private DocumentTypeItem docItem(String id, String type, UploadedDocumentType uploadedDoc) {
        DocumentType docType = new DocumentType();
        docType.setTypeOfDocument(type);
        docType.setUploadedDocument(uploadedDoc);

        DocumentTypeItem item = new DocumentTypeItem();
        item.setId(id);
        item.setValue(docType);
        return item;
    }
}
