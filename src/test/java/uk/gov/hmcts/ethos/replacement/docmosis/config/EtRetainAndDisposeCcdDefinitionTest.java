package uk.gov.hmcts.ethos.replacement.docmosis.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EtRetainAndDisposeCcdDefinitionTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path JURISDICTIONS = Path.of("ccd-definitions", "jurisdictions");
    private static final Map<String, String> CASE_TYPES = Map.of(
        "england-wales", "ET_EnglandWales",
        "scotland", "ET_Scotland"
    );

    @Test
    void restrictsTheDeletingTransitionAndItsPermissionsToTheSystemUser() throws IOException {
        for (Map.Entry<String, String> jurisdiction : CASE_TYPES.entrySet()) {
            Path json = JURISDICTIONS.resolve(jurisdiction.getKey()).resolve("json");

            List<JsonNode> deletingEvents = definitionsMatching(
                json.resolve("CaseEvent"),
                "PostConditionState",
                "Deleting"
            );
            assertThat(deletingEvents).hasSize(1);

            JsonNode event = deletingEvents.get(0);
            assertThat(event.path("CaseTypeID").asText()).isEqualTo(jurisdiction.getValue());
            assertThat(event.path("ID").asText()).isEqualTo("MarkForDisposal");
            assertThat(event.path("PreConditionState(s)").asText().split(";"))
                .containsExactlyInAnyOrder("AWAITING_SUBMISSION_TO_HMCTS", "Delete");
            assertThat(event.path("TTLIncrement").asText()).isEqualTo("0");

            assertThat(definitionsContaining(json.resolve("CaseEvent"), "TTLIncrement"))
                .singleElement()
                .satisfies(ttlEvent -> assertThat(ttlEvent.path("ID").asText()).isEqualTo("MarkForDisposal"));

            assertThat(definitionsMatching(json.resolve("State"), "ID", "Deleting"))
                .singleElement()
                .satisfies(state -> assertThat(state.path("CaseTypeID").asText())
                    .isEqualTo(jurisdiction.getValue()));

            assertThat(definitionsMatching(
                json.resolve("AuthorisationCaseState"),
                "CaseStateID",
                "Deleting"
            )).singleElement().satisfies(authorisation -> {
                assertThat(authorisation.path("CaseTypeID").asText()).isEqualTo(jurisdiction.getValue());
                assertThat(authorisation.path("UserRole").asText()).isEqualTo("caseworker-employment-api");
                assertThat(authorisation.path("CRUD").asText()).isEqualTo("R");
            });

            assertThat(definitionsMatching(
                json.resolve("AuthorisationCaseEvent"),
                "CaseEventID",
                "MarkForDisposal"
            )).singleElement().satisfies(authorisation -> {
                assertThat(authorisation.path("CaseTypeId").asText()).isEqualTo(jurisdiction.getValue());
                assertThat(authorisation.path("UserRole").asText()).isEqualTo("caseworker-employment-api");
                assertThat(authorisation.path("CRUD").asText()).isEqualTo("CRUD");
            });
        }
    }

    private List<JsonNode> definitionsMatching(Path directory, String field, String value) throws IOException {
        return definitions(directory).stream()
            .filter(definition -> value.equals(definition.path(field).asText()))
            .toList();
    }

    private List<JsonNode> definitionsContaining(Path directory, String field) throws IOException {
        return definitions(directory).stream()
            .filter(definition -> definition.has(field))
            .toList();
    }

    private List<JsonNode> definitions(Path directory) throws IOException {
        List<JsonNode> matches = new ArrayList<>();

        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                for (JsonNode definition : OBJECT_MAPPER.readTree(file.toFile())) {
                    matches.add(definition);
                }
            }
        }

        return matches;
    }
}
