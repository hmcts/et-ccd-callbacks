package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DOCUMENT_LINK_MARKDOWN;

/**
 * Helper class for formatting strings into markdown.
 */
public final class MarkdownHelper {

    private static final String DETAILS_SUMMARY = """
      <details class="govuk-details"> <summary class="govuk-details__summary">
      <span class="govuk-details__summary-text">%s</span></summary>
      <div class="govuk-details__text">
      
      %s
      
      </div> </details>
      
        """;
    private static final String FOUR_COLUMN_HEADER = "|%s|%s|%s|%s|\r\n|--|--|--|--|\r\n%s";
    private static final String FOUR_COLUMN_ROW = "|%s|%s|%s|%s|\r\n";
    private static final String DOCUMENT_LINK = "<a href=\"%s\" target=\"_blank\">%s</a>";
    private static final int TWO = 2;
    private static final int FOUR = 4;

    private MarkdownHelper() {
        // Access through static methods
    }

    /**
     * Formats data into a two column table in markdown. Hides rows if any item is null.
     * @param header The header items for the table
     * @param rows Rows of the table
     * @return formatted string representing data in a two column Markdown table
     */
    public static String createTwoColumnTable(String[] header, List<String[]> rows) {
        if (header.length != TWO) {
            throw new IllegalArgumentException("header array should contain only two items");
        }

        return String.format("|%s|%s|\r\n|--|--|\r\n%s", header[0], header[1], createTwoColumnRows(rows));
    }

    /**
     * Formats data into a two column table in markdown. Hides rows if any item is null.
     * @param header The header items for the table
     * @param rows Rows of the table
     * @return formatted string representing data in a two column Markdown table
     */
    public static String createTwoColumnTable(String[] header, Stream<List<String[]>> rows) {
        List<String[]> listRows = rows.flatMap(List::stream).toList();
        return createTwoColumnTable(header, listRows);
    }

    // Formats data for use in a two column table. Ignores rows if any cell is null.
    public static String createTwoColumnRows(List<String[]> rows) {
        return rows.stream()
            .filter(columns -> columns[1] != null)
            .map(columns -> String.format("|%s|%s|\r\n", columns[0], columns[1]))
            .collect(Collectors.joining());
    }

    /**
     * Returns two rows of two columns for a document representing its name and description.
     * @param document Document data
     * @return A list of String arrays representing the two columned rows
     */
    public static List<String[]> addDocumentRow(DocumentType document) {
        return addDocumentRow(document, "Document");
    }

    /**
     * Returns two rows of two columns for a document representing its name and description.
     * @param document Document data
     * @return A list of String arrays representing the two columned rows
     */
    public static List<String[]> addDocumentRow(DocumentType document, String title) {
        UploadedDocumentType uploadedDocument = document.getUploadedDocument();
        if (uploadedDocument == null) {
            List<String[]> ignoredLine = new ArrayList<>();
            ignoredLine.add(new String[]{title, null});
            return ignoredLine;
        }

        String documentLink = String.format(DOCUMENT_LINK_MARKDOWN,
                Helper.extractLink(uploadedDocument.getDocumentBinaryUrl()), uploadedDocument.getDocumentFilename());
        return addRowsForDocument(document, documentLink, title);
    }

    public static List<String[]> addGenericTypeDocumentRows(List<GenericTypeItem<DocumentType>> documents,
                                                            String title) {
        if (documents == null) {
            return List.of();
        }

        return documents.stream()
            .filter(Objects::nonNull)
            .flatMap(o -> addDocumentRow(o.getValue(), title).stream())
            .toList();
    }

    public static List<String[]> addDocumentTypeRows(List<DocumentTypeItem> documents, String title) {
        if (documents == null) {
            return List.of();
        }

        return documents.stream()
                .filter(Objects::nonNull)
                .flatMap(o -> addDocumentRow(o.getValue(), title).stream())
                .toList();
    }

    /**
     * Returns two rows of two columns for a document representing its name and description.
     * @param document Document data
     * @param documentString String to show for the document in the table
     * @return A list of String arrays representing the two columned rows
     */
    public static List<String[]> addRowsForDocument(DocumentType document, String documentString) {
        return List.of(
                new String[]{"Document", documentString},
                new String[]{"Description", document.getShortDescription()}
        );
    }

    /**
     * Returns two rows of two columns for a document representing its name and description.
     * @param document Document data
     * @param documentString String to show for the document in the table
     * @return A list of String arrays representing the two columned rows
     */
    public static List<String[]> addRowsForDocument(DocumentType document, String documentString, String title) {
        return List.of(
                new String[]{title, documentString},
                new String[]{"Description", document.getShortDescription()}
        );
    }

    public static List<String[]> asRow(String item1, String item2) {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(new String[] {item1, item2});
        return rows;
    }

    public static String detailsWrapper(String heading, String body) {
        return DETAILS_SUMMARY.formatted(heading, body);
    }

    public static String getDocumentLink(DocumentTypeItem documentTypeItem) {
        String docFileName = "";
        if (documentExists(documentTypeItem)) {
            docFileName = documentTypeItem.getValue().getUploadedDocument().getDocumentFilename();
            return String.format(DOCUMENT_LINK, createDocLinkBinary(documentTypeItem),
                    docFileName);
        } else {
            return docFileName;
        }
    }

    private static boolean documentExists(DocumentTypeItem documentTypeItem) {
        return documentTypeItem != null && documentTypeItem.getValue() != null
               && documentTypeItem.getValue().getUploadedDocument() != null
               && !isNullOrEmpty(documentTypeItem.getValue()
                .getUploadedDocument().getDocumentBinaryUrl());
    }

    private static String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        if (!isNullOrEmpty(documentBinaryUrl) && documentBinaryUrl.contains("/documents/")) {
            return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
        } else {
            return "";
        }
    }

    public static String createFourColumnTable(String[] header, List<String[]> rows) {
        if (header.length != FOUR) {
            throw new IllegalArgumentException("header array should contain only four items");
        }

        return String.format(FOUR_COLUMN_HEADER, header[0], header[1], header[2], header[3],
                createFourColumnRows(rows));
    }

    private static String createFourColumnRows(List<String[]> rows) {
        return rows.stream()
                .filter(columns -> columns[1] != null)
                .map(columns -> String.format(FOUR_COLUMN_ROW, columns[0], columns[1], columns[2], columns[3]))
                .collect(Collectors.joining());
    }
}
