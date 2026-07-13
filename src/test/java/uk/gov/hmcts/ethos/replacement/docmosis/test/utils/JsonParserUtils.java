package uk.gov.hmcts.ethos.replacement.docmosis.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public final class JsonParserUtils {

    private JsonParserUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static CaseDetails generateCaseDetails(String jsonFileName) throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

}
