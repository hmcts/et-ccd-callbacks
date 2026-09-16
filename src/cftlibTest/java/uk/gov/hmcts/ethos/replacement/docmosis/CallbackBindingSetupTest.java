package uk.gov.hmcts.ethos.replacement.docmosis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.rse.ccd.lib.Database;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CallbackBindingSetupTest extends CftlibTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final Path DEFINITION_SNAPSHOTS_ROOT =
        Path.of("build", "cftlib", "definition-snapshots");
    private static final Path CONTROLLERS_SOURCE_ROOT = Path.of(
        "src", "main", "java", "uk", "gov", "hmcts", "ethos", "replacement", "docmosis", "controllers"
    );
    // Admin-only case type; callbacks live under controllers/admin/prehearingdeposit, outside this scan root.
    private static final String PRE_HEARING_DEPOSIT = "Pre_Hearing_Deposit";
    private static final String DEFAULT_CALLBACK_BASE_URL = "http://localhost:8081";
    private static final List<String> CALLBACK_EVENT_FIELDS = List.of(
        "callback_url_about_to_start_event",
        "callback_url_about_to_submit_event",
        "callback_url_submitted_event"
    );
    private static final Pattern CLASS_REQUEST_MAPPING_PATTERN = Pattern.compile("@RequestMapping\\(\"([^\"]+)\"\\)");
    private static final Pattern MAPPING_ANNOTATION_PATTERN = Pattern.compile(
        "@(?:Request|Get|Post|Put|Patch|Delete)Mapping\\s*\\(([^)]*)\\)",
        Pattern.DOTALL
    );
    private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("\"([^\"]+)\"");

    @Test
    void callbackBindingsFromCcdDefinitionsResolveAgainstEtControllers()
        throws IOException, SQLException {
        Set<String> expectedCaseTypeIds = loadCaseTypeIds();
        clearDefinitionSnapshots();
        cftlib().dumpDefinitionSnapshots();
        Map<String, JsonNode> definitions = loadCaseTypeDefinitions();

        assertThat(definitions.keySet())
            .as("CFT Lib must dump one JSON snapshot for every imported CCD case type")
            .containsExactlyInAnyOrderElementsOf(expectedCaseTypeIds);

        definitions.remove(PRE_HEARING_DEPOSIT);
        Set<String> controllerCallbackPaths = discoverControllerCallbackPaths();
        Set<String> definitionCallbackPaths = extractDefinitionCallbackPaths(definitions);

        assertThat(definitions).isNotEmpty();
        assertThat(controllerCallbackPaths).isNotEmpty();
        assertThat(definitionCallbackPaths).isNotEmpty();

        List<String> unresolvedDefinitionPaths = definitionCallbackPaths.stream()
            .filter(definitionPath -> !controllerCallbackPaths.contains(definitionPath))
            .sorted()
            .toList();

        assertThat(unresolvedDefinitionPaths)
            .as("CCD callback URLs must map to a controller endpoint exposed by et-cos")
            .isEmpty();
    }

    private static Map<String, JsonNode> loadCaseTypeDefinitions() throws IOException {
        Map<String, JsonNode> definitions = new LinkedHashMap<>();

        try (Stream<Path> paths = Files.walk(DEFINITION_SNAPSHOTS_ROOT)) {
            for (Path path : paths
                .filter(Files::isRegularFile)
                .filter(snapshot -> snapshot.getFileName().toString().endsWith(".json"))
                .sorted(Comparator.naturalOrder())
                .toList()) {
                String caseTypeId = path.getFileName().toString().replaceFirst("\\.json$", "");
                JsonNode caseTypeDefinition = OBJECT_MAPPER.readTree(path.toFile());
                definitions.put(caseTypeId, caseTypeDefinition);
            }
        }

        return definitions;
    }

    private Set<String> loadCaseTypeIds() throws SQLException {
        Set<String> caseTypeIds = new LinkedHashSet<>();
        try (var connection = cftlib().getConnection(Database.Definitionstore);
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select distinct reference from case_type order by reference")) {
            while (resultSet.next()) {
                caseTypeIds.add(resultSet.getString("reference"));
            }
        }
        return caseTypeIds;
    }

    private static void clearDefinitionSnapshots() throws IOException {
        Files.createDirectories(DEFINITION_SNAPSHOTS_ROOT);
        try (Stream<Path> paths = Files.list(DEFINITION_SNAPSHOTS_ROOT)) {
            for (Path path : paths
                .filter(Files::isRegularFile)
                .filter(snapshot -> snapshot.getFileName().toString().endsWith(".json"))
                .toList()) {
                Files.delete(path);
            }
        }
    }

    private static Set<String> extractDefinitionCallbackPaths(Map<String, JsonNode> definitions) {
        String callbackBaseUrl = resolveCallbackBaseUrl();
        Set<String> callbackPaths = new LinkedHashSet<>();
        for (JsonNode definition : definitions.values()) {
            JsonNode events = definition.path("events");
            if (!events.isArray()) {
                continue;
            }
            for (JsonNode event : events) {
                for (String callbackField : CALLBACK_EVENT_FIELDS) {
                    JsonNode callbackUrlNode = event.path(callbackField);
                    if (callbackUrlNode.isTextual()) {
                        String callbackUrl = callbackUrlNode.asText();
                        if (callbackUrl.startsWith(callbackBaseUrl)) {
                            callbackPaths.add(normalisePath(URI.create(callbackUrl).getPath()));
                        }
                    }
                }
            }
        }
        return callbackPaths;
    }

    private static String resolveCallbackBaseUrl() {
        String callbackBaseUrl = System.getenv("ET_COS_URL");
        if (callbackBaseUrl == null || callbackBaseUrl.isBlank()) {
            return DEFAULT_CALLBACK_BASE_URL;
        }
        return callbackBaseUrl.replaceAll("/+$", "");
    }

    private static Set<String> discoverControllerCallbackPaths() throws IOException {
        Set<String> callbackPaths = new LinkedHashSet<>();
        if (!Files.isDirectory(CONTROLLERS_SOURCE_ROOT)) {
            return callbackPaths;
        }

        try (Stream<Path> sourceFiles = Files.walk(CONTROLLERS_SOURCE_ROOT)) {
            for (Path sourceFile : sourceFiles
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".java"))
                .sorted(Comparator.naturalOrder())
                .toList()) {
                callbackPaths.addAll(extractControllerPaths(Files.readString(sourceFile)));
            }
        }
        return callbackPaths;
    }

    private static Set<String> extractControllerPaths(String source) {
        Set<String> callbackPaths = new LinkedHashSet<>();
        String classBasePath = extractClassBasePath(source);
        Set<String> mappingPaths = extractMappingPaths(source);

        for (String path : mappingPaths) {
            callbackPaths.add(path);
            if (!classBasePath.isEmpty()
                && !path.equals(classBasePath)
                && !path.startsWith(classBasePath + "/")) {
                callbackPaths.add(normalisePath(classBasePath + path));
            }
        }
        return callbackPaths;
    }

    private static String extractClassBasePath(String source) {
        int classDeclarationIndex = source.indexOf("public class ");
        if (classDeclarationIndex < 0) {
            return "";
        }
        Matcher matcher = CLASS_REQUEST_MAPPING_PATTERN.matcher(source.substring(0, classDeclarationIndex));
        String classBasePath = "";
        while (matcher.find()) {
            classBasePath = matcher.group(1);
        }
        return classBasePath.isEmpty() ? "" : normalisePath(classBasePath);
    }

    private static Set<String> extractMappingPaths(String source) {
        Set<String> paths = new LinkedHashSet<>();
        Matcher annotationMatcher = MAPPING_ANNOTATION_PATTERN.matcher(source);
        while (annotationMatcher.find()) {
            String annotationArguments = annotationMatcher.group(1);
            Matcher quotedStringMatcher = QUOTED_STRING_PATTERN.matcher(annotationArguments);
            while (quotedStringMatcher.find()) {
                String possiblePath = quotedStringMatcher.group(1).trim();
                if (possiblePath.startsWith("/")) {
                    paths.add(normalisePath(possiblePath));
                } else if (possiblePath.contains("/")) {
                    paths.add(normalisePath("/" + possiblePath));
                }
            }
        }
        return paths;
    }

    private static String normalisePath(String rawPath) {
        String path = rawPath == null ? "" : rawPath.trim();
        path = path.replaceAll("/+", "/");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
