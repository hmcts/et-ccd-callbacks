package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.AiGatewayProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAttachmentExtractor {

    private static final int MAX_PDF_IMAGE_PAGES = 8;
    private static final float PDF_IMAGE_DPI = 100f;
    private static final int MAX_TOTAL_IMAGE_BYTES = 1_500_000;
    private static final Set<String> DIRECT_IMAGE_MIME_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif"
    );
    private static final Set<String> EXCEL_OPENXML_EXT = Set.of(".xlsx", ".xltx");
    private static final Set<String> PPT_OPENXML_EXT = Set.of(".pptx", ".potx", ".ppsx");
    private static final Set<String> LEGACY_OFFICE_EXT = Set.of(
            ".doc", ".dot", ".xls", ".xlt", ".xla", ".ppt", ".pot", ".pps", ".ppa"
    );
    private static final Pattern SHARED_STRING_PATTERN = Pattern.compile("<si>(.*?)</si>", Pattern.DOTALL);
    private static final Pattern XML_TEXT_PATTERN = Pattern.compile("<t[^>]*>(.*?)</t>", Pattern.DOTALL);
    private static final Pattern CELL_PATTERN = Pattern.compile("<c\\b([^>]*)>(.*?)</c>", Pattern.DOTALL);
    private static final Pattern CELL_VALUE_PATTERN = Pattern.compile("<v>(.*?)</v>", Pattern.DOTALL);

    private final AiGatewayProperties properties;

    /**
     * Converts the user prompt and claim attachments into Chat Completions content parts
     * (extracted text for PDFs/Office/RTF/CSV/TXT and base64 images for PNG/JPEG/BMP/TIFF/PDF pages).
     *
     * @param prompt Dynamic case prompt text
     * @param attachments Downloaded binary attachments
     * @return Ordered list of Chat Completions user content parts
     */
    public List<Map<String, Object>> buildUserParts(String prompt, List<AiDocumentAttachment> attachments) {
        List<Map<String, Object>> userParts = new ArrayList<>();
        userParts.add(Map.of("type", "text", "text", prompt));
        appendAttachmentParts(userParts, attachments);
        return userParts;
    }

    private void appendAttachmentParts(List<Map<String, Object>> userParts,
                                       List<AiDocumentAttachment> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }
        for (AiDocumentAttachment attachment : attachments) {
            if (attachment == null || attachment.getData() == null || attachment.getData().length == 0) {
                continue;
            }
            String mimeType = StringUtils.defaultIfBlank(attachment.getMimeType(), "application/pdf")
                    .toLowerCase(Locale.ROOT);
            String filename = StringUtils.defaultIfBlank(attachment.getFilename(), "attachment");
            String docId = StringUtils.defaultIfBlank(attachment.getDocumentId(), "unknown");
            String ext = extractExtension(filename);

            if (DIRECT_IMAGE_MIME_TYPES.contains(mimeType)) {
                addImagePart(userParts, mimeType, attachment.getData());
            } else if (isConvertibleImage(mimeType, ext)) {
                convertImageToPngPart(userParts, docId, attachment.getData());
            } else if ("application/pdf".equals(mimeType) || ".pdf".equals(ext)) {
                appendPdfParts(userParts, docId, filename, attachment.getData());
            } else if (".docx".equals(ext)) {
                appendDocxTextPart(userParts, docId, filename, attachment.getData());
            } else if (EXCEL_OPENXML_EXT.contains(ext)) {
                appendExcelOpenXmlTextPart(userParts, docId, filename, attachment.getData());
            } else if (PPT_OPENXML_EXT.contains(ext)) {
                appendPowerPointOpenXmlTextPart(userParts, docId, filename, attachment.getData());
            } else if (".rtf".equals(ext) || mimeType.contains("rtf")) {
                appendRtfTextPart(userParts, docId, filename, attachment.getData());
            } else if (LEGACY_OFFICE_EXT.contains(ext)) {
                appendLegacyOfficeTextPart(userParts, docId, filename, attachment.getData());
            } else if (mimeType.startsWith("text/")) {
                String text = new String(attachment.getData(), StandardCharsets.UTF_8);
                appendLabeledTextPart(userParts, filename, text);
            } else {
                log.info("Skipping unsupported binary attachment {} ({}) for Chat Completions", docId, mimeType);
            }
        }
    }

    private void appendPdfParts(List<Map<String, Object>> userParts, String docId, String filename, byte[] data) {
        try (PDDocument document = Loader.loadPDF(data)) {
            StringBuilder combinedText = new StringBuilder();
            String acroFormText = extractAcroFormFields(document);
            if (StringUtils.isNotBlank(acroFormText)) {
                combinedText.append("[Form Fields]\n").append(acroFormText).append('\n');
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);
            if (StringUtils.isNotBlank(extractedText)) {
                combinedText.append(extractedText);
            }
            if (!combinedText.isEmpty()) {
                appendLabeledTextPart(userParts, filename, combinedText.toString());
            }

            // Render PDF pages as compact JPEGs (within total image budget) so checkboxes, tables,
            // and visual form layout are preserved without exceeding APIM's request body buffer.
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = Math.min(document.getNumberOfPages(), MAX_PDF_IMAGE_PAGES);
            long totalImageBytes = currentImageBytes(userParts);
            for (int idx = 0; idx < pageCount; idx++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(idx, PDF_IMAGE_DPI);
                ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
                ImageIO.write(pageImage, "jpg", jpegOut);
                byte[] jpegBytes = jpegOut.toByteArray();
                if (totalImageBytes + jpegBytes.length > MAX_TOTAL_IMAGE_BYTES) {
                    log.info("Stopping PDF page rendering for {} at page {}/{} to stay within {} byte image budget",
                            docId, idx, pageCount, MAX_TOTAL_IMAGE_BYTES);
                    break;
                }
                addImagePart(userParts, "image/jpeg", jpegBytes);
                totalImageBytes += jpegBytes.length;
            }
        } catch (IOException ex) {
            log.warn("Failed to extract PDF content from {}: {}", docId, ex.getMessage());
        }
    }

    private String extractAcroFormFields(PDDocument document) {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        if (acroForm == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (PDField field : acroForm.getFieldTree()) {
            String value = field.getValueAsString();
            if (StringUtils.isNotBlank(value) && !"Off".equalsIgnoreCase(value.trim())) {
                sb.append(field.getFullyQualifiedName()).append(": ").append(value.trim()).append('\n');
            }
        }
        return sb.toString();
    }

    private void appendDocxTextPart(List<Map<String, Object>> userParts, String docId, String filename, byte[] data) {
        Map<String, String> entries = readZipXmlEntries(data, docId, "word/document.xml");
        String xml = entries.get("word/document.xml");
        if (StringUtils.isNotBlank(xml)) {
            appendLabeledTextPart(userParts, filename, stripXmlTags(xml, "<w:p[^>]*>"));
        }
    }

    private void appendExcelOpenXmlTextPart(List<Map<String, Object>> userParts,
                                            String docId,
                                            String filename,
                                            byte[] data) {
        Map<String, String> entries = readZipXmlEntries(data, docId, "xl/sharedStrings.xml", "xl/worksheets/sheet");
        if (entries.isEmpty()) {
            return;
        }
        List<String> sharedStrings = parseSharedStrings(entries.get("xl/sharedStrings.xml"));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (!entry.getKey().startsWith("xl/worksheets/sheet")) {
                continue;
            }
            sb.append('[').append(entry.getKey()).append("]\n");
            extractWorksheetCells(entry.getValue(), sharedStrings, sb);
        }
        appendLabeledTextPart(userParts, filename, sb.toString());
    }

    private List<String> parseSharedStrings(String sharedStringsXml) {
        List<String> strings = new ArrayList<>();
        if (StringUtils.isBlank(sharedStringsXml)) {
            return strings;
        }
        Matcher siMatcher = SHARED_STRING_PATTERN.matcher(sharedStringsXml);
        while (siMatcher.find()) {
            StringBuilder itemText = new StringBuilder();
            Matcher textMatcher = XML_TEXT_PATTERN.matcher(siMatcher.group(1));
            while (textMatcher.find()) {
                itemText.append(textMatcher.group(1));
            }
            strings.add(itemText.toString());
        }
        return strings;
    }

    private void extractWorksheetCells(String sheetXml, List<String> sharedStrings, StringBuilder sb) {
        Matcher cellMatcher = CELL_PATTERN.matcher(sheetXml);
        while (cellMatcher.find()) {
            String attrs = cellMatcher.group(1);
            String cellBody = cellMatcher.group(2);
            Matcher valMatcher = CELL_VALUE_PATTERN.matcher(cellBody);
            String value = null;
            if (valMatcher.find()) {
                String rawVal = valMatcher.group(1);
                if (attrs.contains("t=\"s\"") && StringUtils.isNumeric(rawVal)) {
                    int idx = Integer.parseInt(rawVal);
                    value = idx >= 0 && idx < sharedStrings.size() ? sharedStrings.get(idx) : rawVal;
                } else {
                    value = rawVal;
                }
            } else {
                Matcher inlineMatcher = XML_TEXT_PATTERN.matcher(cellBody);
                if (inlineMatcher.find()) {
                    value = inlineMatcher.group(1);
                }
            }
            if (StringUtils.isNotBlank(value)) {
                sb.append(value.trim()).append('\n');
            }
        }
    }

    private void appendPowerPointOpenXmlTextPart(List<Map<String, Object>> userParts,
                                                 String docId,
                                                 String filename,
                                                 byte[] data) {
        Map<String, String> entries = readZipXmlEntries(
                data, docId, "ppt/slides/slide", "ppt/notesSlides/notesSlide");
        if (entries.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            String text = stripXmlTags(entry.getValue(), "<a:p[^>]*>");
            if (StringUtils.isNotBlank(text)) {
                sb.append('[').append(entry.getKey()).append("]\n").append(text).append('\n');
            }
        }
        appendLabeledTextPart(userParts, filename, sb.toString());
    }

    private Map<String, String> readZipXmlEntries(byte[] data, String docId, String... prefixes) {
        Map<String, String> entries = new TreeMap<>();
        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                if (!entry.isDirectory() && name.endsWith(".xml") && matchesAnyPrefix(name, prefixes)) {
                    entries.put(name, new String(zipIn.readAllBytes(), StandardCharsets.UTF_8));
                }
                entry = zipIn.getNextEntry();
            }
        } catch (IOException ex) {
            log.warn("Failed to extract OpenXML content from {}: {}", docId, ex.getMessage());
        }
        return entries;
    }

    private boolean matchesAnyPrefix(String name, String... prefixes) {
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String stripXmlTags(String xml, String paragraphTagRegex) {
        return xml.replaceAll(paragraphTagRegex, "\n")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("[ \\t]+", " ")
                .trim();
    }

    private void appendRtfTextPart(List<Map<String, Object>> userParts, String docId, String filename, byte[] data) {
        try {
            RTFEditorKit rtfKit = new RTFEditorKit();
            Document doc = rtfKit.createDefaultDocument();
            rtfKit.read(new ByteArrayInputStream(data), doc, 0);
            appendLabeledTextPart(userParts, filename, doc.getText(0, doc.getLength()));
        } catch (IOException | BadLocationException ex) {
            log.warn("Failed to extract RTF text from {}: {}", docId, ex.getMessage());
        }
    }

    private void appendLegacyOfficeTextPart(List<Map<String, Object>> userParts,
                                            String docId,
                                            String filename,
                                            byte[] data) {
        String header = new String(data, 0, Math.min(data.length, 8), StandardCharsets.ISO_8859_1);
        if (header.startsWith("{\\rtf")) {
            appendRtfTextPart(userParts, docId, filename, data);
            return;
        }
        if (header.startsWith("PK")) {
            String ext = extractExtension(filename);
            if (ext.startsWith(".xl")) {
                appendExcelOpenXmlTextPart(userParts, docId, filename, data);
            } else if (ext.startsWith(".pp")) {
                appendPowerPointOpenXmlTextPart(userParts, docId, filename, data);
            } else {
                appendDocxTextPart(userParts, docId, filename, data);
            }
            return;
        }
        appendLabeledTextPart(userParts, filename, extractPrintableRuns(data));
    }

    private String extractPrintableRuns(byte[] data) {
        StringBuilder sb = new StringBuilder();
        StringBuilder current = new StringBuilder();
        // Scan UTF-16LE and single-byte ASCII runs common in OLE2 .doc/.xls/.ppt streams.
        int idx = 0;
        while (idx < data.length) {
            int low = data[idx] & 0xFF;
            int high = idx + 1 < data.length ? (data[idx + 1] & 0xFF) : -1;
            if (high == 0 && isPrintableChar(low)) {
                current.append((char) low);
                idx += 2;
                continue;
            }
            if (isPrintableChar(low)) {
                current.append((char) low);
            } else {
                flushRunIfMeaningful(current, sb);
            }
            idx++;
        }
        flushRunIfMeaningful(current, sb);
        return sb.toString();
    }

    private boolean isPrintableChar(int codePoint) {
        return (codePoint >= 32 && codePoint <= 126) || codePoint == '\n' || codePoint == '\r' || codePoint == '\t';
    }

    private void flushRunIfMeaningful(StringBuilder current, StringBuilder out) {
        if (current.length() >= 4) {
            out.append(current).append('\n');
        }
        current.setLength(0);
    }

    private String extractExtension(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        int dotIdx = lower.lastIndexOf('.');
        return dotIdx >= 0 ? lower.substring(dotIdx) : "";
    }

    private void convertImageToPngPart(List<Map<String, Object>> userParts, String docId, byte[] data) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if (image == null) {
                log.warn("Could not decode image {} for PNG conversion", docId);
                return;
            }
            ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
            ImageIO.write(image, "png", pngOut);
            addImagePart(userParts, "image/png", pngOut.toByteArray());
        } catch (IOException ex) {
            log.warn("Failed to convert image {} to PNG: {}", docId, ex.getMessage());
        }
    }

    private boolean isConvertibleImage(String mimeType, String ext) {
        return mimeType.contains("bmp") || mimeType.contains("tiff")
                || ".bmp".equals(ext) || ".tif".equals(ext) || ".tiff".equals(ext);
    }

    private long currentImageBytes(List<Map<String, Object>> userParts) {
        long total = 0;
        for (Map<String, Object> part : userParts) {
            if ("image_url".equals(part.get("type"))) {
                Object imageUrlObj = part.get("image_url");
                if (imageUrlObj instanceof Map<?, ?> imageUrlMap) {
                    Object url = imageUrlMap.get("url");
                    if (url instanceof String urlStr) {
                        total += (urlStr.length() * 3L) / 4L;
                    }
                }
            }
        }
        return total;
    }

    private void addImagePart(List<Map<String, Object>> userParts, String mimeType, byte[] data) {
        String base64 = Base64.getEncoder().encodeToString(data);
        userParts.add(Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:" + mimeType + ";base64," + base64)
        ));
    }

    private void appendLabeledTextPart(List<Map<String, Object>> userParts, String filename, String rawText) {
        if (StringUtils.isBlank(rawText)) {
            return;
        }
        int maxChars = Math.max(1_000, properties.getMaxExtractedCharsPerDocument());
        String bounded = rawText.length() > maxChars
                ? rawText.substring(0, maxChars) + "\n[Truncated]"
                : rawText;
        userParts.add(Map.of(
                "type", "text",
                "text", "--- Document: " + filename + " ---\n" + bounded
        ));
    }
}
