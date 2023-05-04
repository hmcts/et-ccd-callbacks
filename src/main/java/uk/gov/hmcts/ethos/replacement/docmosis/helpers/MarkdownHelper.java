package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

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
        if (header.length < 2) {
            throw new IllegalArgumentException("header array should contain two items");
        }

        StringBuilder stringBuilder = new StringBuilder(String.format("|%s|%s|\r\n|--|--|\r\n", header[0], header[1]));

        for (String[] columns : rows) {
            if (columns.length < 2 || isEmpty(columns[0]) || isEmpty(columns[1])) {
                continue;
            }

            stringBuilder.append(String.format("|%s|%s|\r\n", columns[0], columns[1]));
        }

        return stringBuilder.toString();
    }
}
