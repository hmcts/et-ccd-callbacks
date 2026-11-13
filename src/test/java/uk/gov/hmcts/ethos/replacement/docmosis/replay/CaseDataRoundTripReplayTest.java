package uk.gov.hmcts.ethos.replacement.docmosis.replay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import uk.gov.hmcts.ccd.sdk.config.CcdCaseDataMapperConfiguration;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.JacksonConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Replays exported case data through ET's Jackson mappers so two builds (e.g. master and a framework
 * upgrade) can be compared on real data. Disabled unless ET_ROUNDTRIP_IN is set.
 *
 * <p>Each input file is one case: {@code {"reference":..., "case_type_id":..., "data":{...}}}. For each,
 * the data is read into the case type's model and written back with the primary (callback) mapper and
 * with the SDK's decentralised persistence mapper. Output goes to ET_ROUNDTRIP_OUT as sorted-key JSON,
 * one file per case and mapper, so the two builds' output directories can be compared with
 * {@code diff -r}. Read failures are written as {@code .error} files rather than failing the run.
 */
@EnabledIfEnvironmentVariable(named = "ET_ROUNDTRIP_IN", matches = ".+")
class CaseDataRoundTripReplayTest {

    @Test
    void replayExportedCases() throws IOException {
        Path in = Path.of(System.getenv("ET_ROUNDTRIP_IN"));
        Path out = Path.of(System.getenv().getOrDefault("ET_ROUNDTRIP_OUT", "build/roundtrip"));
        Files.createDirectories(out);

        ObjectMapper primary = new JacksonConfiguration().objectMapper();
        ObjectMapper persistence = new CcdCaseDataMapperConfiguration().ccdCaseDataObjectMapper(primary);
        ObjectMapper canonical = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        List<Path> files;
        try (Stream<Path> listing = Files.list(in)) {
            files = listing.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        }
        assertThat(files).as("no *.json case exports in %s", in).isNotEmpty();

        for (Path file : files) {
            JsonNode row = canonical.readTree(file.toFile());
            String reference = row.path("reference").asText(file.getFileName().toString());
            Class<?> model = modelFor(row.path("case_type_id").asText());
            JsonNode data = row.path("data");
            write(out, reference + ".primary", canonical, primary, data, model);
            write(out, reference + ".persistence", canonical, persistence, data, model);
        }
    }

    private static void write(Path out, String name, ObjectMapper canonical, ObjectMapper mapper,
                              JsonNode data, Class<?> model) throws IOException {
        try {
            Object value = mapper.treeToValue(data, model);
            JsonNode written = mapper.valueToTree(value);
            Files.writeString(out.resolve(name + ".json"), canonical.writeValueAsString(sorted(canonical, written)));
        } catch (IOException | IllegalArgumentException e) {
            Files.writeString(out.resolve(name + ".error"), e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // ORDER_MAP_ENTRIES_BY_KEYS sorts maps, not ObjectNode fields; re-read as a Map for stable key order.
    private static Object sorted(ObjectMapper canonical, JsonNode node) {
        return node instanceof ObjectNode ? canonical.convertValue(node, Object.class) : node;
    }

    private static Class<?> modelFor(String caseTypeId) {
        if (caseTypeId.endsWith("_Multiple")) {
            return MultipleData.class;
        }
        if (caseTypeId.endsWith("_Listings")) {
            return ListingData.class;
        }
        if ("ET_Admin".equals(caseTypeId)) {
            return AdminData.class;
        }
        return CaseData.class;
    }
}
