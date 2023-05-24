package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DOCUMENT_LINK_MARKDOWN;

/**
 * Helper class for formatting strings into markdown.
 */
public final class MarkdownHelper {
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

    /**
     * Formats data for use in a two column table. Ignores rows if any cell is null.
     * @param rows Rows to format
     * @return formatted string for use in a two column table
     */
    public static String createTwoColumnRows(List<String[]> rows) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String[] columns : rows) {
            if (columns.length < 2 || isEmpty(columns[0]) || isEmpty(columns[1])) {
                continue;
            }

            stringBuilder.append(String.format("|%s|%s|\r\n", columns[0], columns[1]));
        }

        return stringBuilder.toString();
    }

    /**
     * Returns two rows of two columns for a document representing its name and description.
     * @param document Document data
     * @return A list of String arrays representing the two columned rows
     */
    public static List<String[]> addDocumentRow(DocumentType document) {
        UploadedDocumentType uploadedDocument = document.getUploadedDocument();
        String documentLink = String.format(DOCUMENT_LINK_MARKDOWN,
            Helper.extractUUID(uploadedDocument.getDocumentBinaryUrl()), uploadedDocument.getDocumentFilename());
        return addDocumentRow(document, documentLink);
    }

    /**
     * Returns two rows of two columns for a document representing its name and description.
     * @param document Document data
     * @param documentString String to show for the document in the table
     * @return A list of String arrays representing the two columned rows
     */
    public static List<String[]> addDocumentRow(DocumentType document, String documentString) {
        return List.of(
            new String[]{"Document", documentString},
            new String[]{"Description", document.getShortDescription()}
        );
    }
}
