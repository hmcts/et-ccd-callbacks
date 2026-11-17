package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TornadoDocumentFilterTest {

    @Test
    void testFilterJsonEscapesBackslashCharacter() throws URISyntaxException, IOException {
        String json = ResourceLoader.getResource("caseDataWithBackslash.json");
        String result = TornadoDocumentFilter.filterJson(json);

        assertThat(result,
                is("{\"respondentCollection\":[{\"value\":{\"respondent_name\":\"Respondent\\\\WithBlackslash\"}}]}"));
    }

    @Test
    void newlineInsideAnAddressBecomesAJsonLineBreak() {
        String json = "{\"Court_addressLine3\":\"Glasgow Tribunals Centre\n3 Atlantic Quay, 20 York Street\",\n"
                + "\"Court_town\":\"Glasgow\"}";

        String result = TornadoDocumentFilter.filterJson(json);

        assertThat(result, is("{\"Court_addressLine3\":\"Glasgow Tribunals Centre\\n3 Atlantic Quay, "
                + "20 York Street\",\n\"Court_town\":\"Glasgow\"}"));
    }
}
