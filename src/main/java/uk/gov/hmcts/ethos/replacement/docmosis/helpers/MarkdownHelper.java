package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DOCUMENT_LINK_MARKDOWN;

/**
 * Helper class for formatting strings into markdown.
 */
public final class MarkdownHelper {
    public static final String[] MD_TABLE_EMPTY_LINE = {"", ""};

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
        if (header.length != 2) {
            throw new IllegalArgumentException("header array should contain only two items");
        }

        return String.format("|%s|%s|\r\n|--|--|\r\n%s", header[0], header[1], createTwoColumnRows(rows));
    }

    // Formats data for use in a two column table. Ignores rows if any cell is null.
    private static String createTwoColumnRows(List<String[]> rows) {
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
        UploadedDocumentType uploadedDocument = document.getUploadedDocument();
        if (uploadedDocument == null) {
            List<String[]> ignoredLine = new ArrayList<>();
            ignoredLine.add(new String[]{"Document", null});
            return ignoredLine;
        }

        String documentLink = String.format(DOCUMENT_LINK_MARKDOWN,
            Helper.extractLink(uploadedDocument.getDocumentBinaryUrl()), uploadedDocument.getDocumentFilename());
        return addRowsForDocument(document, documentLink);
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
}
