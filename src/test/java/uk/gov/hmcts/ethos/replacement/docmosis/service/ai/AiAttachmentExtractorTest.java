package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.AiGatewayProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class AiAttachmentExtractorTest {

    private AiGatewayProperties properties;
    private AiAttachmentExtractor extractor;

    @BeforeEach
    void setUp() {
        properties = new AiGatewayProperties();
        properties.setMaxExtractedCharsPerDocument(1_000);
        extractor = new AiAttachmentExtractor(properties);
    }

    @Test
    void buildUserParts_handlesNullEmptyAndUnsupportedAttachments() {
        List<AiDocumentAttachment> attachments = Arrays.asList(
                null,
                AiDocumentAttachment.builder().filename("empty.txt").mimeType("text/plain").data(new byte[0]).build(),
                AiDocumentAttachment.builder().filename("blob.bin").mimeType("application/octet-stream")
                        .data(new byte[]{1, 2, 3}).build()
        );

        List<Map<String, Object>> parts = extractor.buildUserParts("Prompt", attachments);

        assertThat(parts).hasSize(1);
        assertThat(parts.getFirst().get("text")).isEqualTo("Prompt");
    }

    @Test
    void buildUserParts_truncatesLongTextAndHandlesCsv() {
        String longText = "A".repeat(1_500);
        List<AiDocumentAttachment> attachments = List.of(
                AiDocumentAttachment.builder()
                        .documentId("doc-1")
                        .filename("notes.txt")
                        .mimeType("text/plain")
                        .data(longText.getBytes(StandardCharsets.UTF_8))
                        .build(),
                AiDocumentAttachment.builder()
                        .documentId("doc-2")
                        .filename("pay.csv")
                        .mimeType("text/csv")
                        .data("col1,col2\n1,2".getBytes(StandardCharsets.UTF_8))
                        .build()
        );

        List<Map<String, Object>> parts = extractor.buildUserParts("Prompt", attachments);

        assertThat(parts).hasSize(3);
        assertThat((String) parts.get(1).get("text")).endsWith("\n[Truncated]");
        assertThat((String) parts.get(2).get("text")).contains("col1,col2");
    }

    @Test
    void buildUserParts_rendersScannedPdfAndHandlesCorruptInputsGracefully() throws IOException {
        byte[] blankPdf;
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(out);
            blankPdf = out.toByteArray();
        }

        List<AiDocumentAttachment> attachments = List.of(
                AiDocumentAttachment.builder().documentId("pdf-1").filename("scan.pdf")
                        .mimeType("application/pdf").data(blankPdf).build(),
                AiDocumentAttachment.builder().documentId("bad-pdf").filename("bad.pdf")
                        .mimeType("application/pdf").data("not-a-pdf".getBytes(StandardCharsets.UTF_8)).build(),
                AiDocumentAttachment.builder().documentId("bad-bmp").filename("bad.bmp")
                        .mimeType("image/bmp").data("not-an-image".getBytes(StandardCharsets.UTF_8)).build(),
                AiDocumentAttachment.builder().documentId("bad-zip").filename("bad.docx")
                        .mimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        .data("not-a-zip".getBytes(StandardCharsets.UTF_8)).build()
        );

        List<Map<String, Object>> parts = extractor.buildUserParts("Prompt", attachments);

        assertThat(parts).hasSize(2);
        assertThat(parts.get(1).get("type")).isEqualTo("image_url");
    }

    @Test
    void buildUserParts_handlesMisnamedLegacyOfficeRtfAndZip() throws IOException {
        byte[] zipXlsBytes;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            zipOut.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            zipOut.write("<worksheet><sheetData><row><c><v>42</v></c></row></sheetData></worksheet>"
                    .getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();
            zipOut.finish();
            zipXlsBytes = out.toByteArray();
        }

        List<AiDocumentAttachment> attachments = List.of(
                AiDocumentAttachment.builder().documentId("doc-rtf").filename("claim.doc")
                        .mimeType("application/msword")
                        .data("{\\rtf1\\ansi Misnamed RTF}".getBytes(StandardCharsets.UTF_8)).build(),
                AiDocumentAttachment.builder().documentId("xls-zip").filename("sheet.xls")
                        .mimeType("application/vnd.ms-excel").data(zipXlsBytes).build()
        );

        List<Map<String, Object>> parts = extractor.buildUserParts("Prompt", attachments);

        assertThat(parts).hasSize(3);
        assertThat((String) parts.get(1).get("text")).contains("Misnamed RTF");
        assertThat((String) parts.get(2).get("text")).contains("42");
    }

    @Test
    void buildUserParts_extractsAcroFormFieldsAndAsciiLegacyRuns() throws IOException {
        byte[] acroPdf;
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            PDAcroForm acroForm = new PDAcroForm(document);
            PDResources resources = new PDResources();
            resources.put(org.apache.pdfbox.cos.COSName.getPDFName("Helv"),
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA));
            acroForm.setDefaultResources(resources);
            acroForm.setDefaultAppearance("/Helv 12 Tf 0 g");
            PDTextField field = new PDTextField(acroForm);
            field.setPartialName("claimantName");
            field.setValue("Alice Smith");
            acroForm.getFields().add(field);
            document.getDocumentCatalog().setAcroForm(acroForm);
            document.save(out);
            acroPdf = out.toByteArray();
        }

        byte[] asciiDoc = {0, 1, 'C', 'l', 'a', 'i', 'm', ' ', 't', 'e', 'x', 't', 0, 2};

        List<Map<String, Object>> parts = extractor.buildUserParts("Prompt", List.of(
                AiDocumentAttachment.builder().documentId("acro-1").filename("form.pdf")
                        .mimeType("application/pdf").data(acroPdf).build(),
                AiDocumentAttachment.builder().documentId("doc-ascii").filename("old.doc")
                        .mimeType("application/msword").data(asciiDoc).build()
        ));

        assertThat((String) parts.get(1).get("text"))
                .contains("[Form Fields]")
                .contains("claimantName: Alice Smith");
        assertThat((String) parts.get(3).get("text")).contains("Claim text");
    }
}
