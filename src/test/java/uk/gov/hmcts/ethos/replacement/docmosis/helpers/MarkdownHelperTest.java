package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarkdownHelperTest {
    @Test
    void createTwoColumnTable_hidesEmptyData() {
        String[] headers = {"Header 1", "Header 2"};
        List<String[]> rows = List.of(new String[]{"Key", "Value"}, new String[]{"Key", null});
        String actual = MarkdownHelper.createTwoColumnTable(headers, rows);

        assertThat(actual).isEqualTo("|Header 1|Header 2|\r\n|--|--|\r\n|Key|Value|\r\n");
    }

    @Test
    void createTwoColumnTable_throwsWithMalformedHeader() {
        String[] headers = {"Header 1"};
        List<String[]> rows = List.of(new String[]{"Key", "Value"}, new String[]{"Key"});

        assertThrows(IllegalArgumentException.class, () -> MarkdownHelper.createTwoColumnTable(headers, rows));
    }
}
