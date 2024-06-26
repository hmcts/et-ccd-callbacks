package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.SneakyThrows;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public final class ResourceLoader {
    private static final JsonMapper JSON_MAPPER = JsonMapperFactory.create();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private ResourceLoader() {
    }

    public static UploadResponse successfulDocumentManagementUploadResponse()
            throws URISyntaxException, IOException {
        String response = getResource("response.success.json");
        return JSON_MAPPER.fromJson(response, UploadResponse.class);
    }

    public static UploadResponse unsuccessfulDocumentManagementUploadResponse()
            throws URISyntaxException, IOException {
        String response = getResource("response.failure.json");
        return JSON_MAPPER.fromJson(response, UploadResponse.class);
    }

    public static uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse successfulDocStoreUpload()
            throws URISyntaxException, IOException {
        String response = getResource("responseDocStore.success.json");
        return JSON_MAPPER.fromJson(response, uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse.class);
    }

    public static ListingDetails generateListingDetails(String jsonFileName) throws URISyntaxException, IOException {
        String json = getResource(jsonFileName);
        return JSON_MAPPER.fromJson(json, ListingDetails.class);
    }

    public static BundleCreateResponse stitchBundleRequest() throws URISyntaxException, IOException {
        return JSON_MAPPER.fromJson(getResource("stitchBundleRequest.json"), BundleCreateResponse.class);
    }

    public static List<SubmitEvent> generateSubmitEventList(String jsonFileName)
            throws URISyntaxException, IOException {

        String json = getResource(jsonFileName);
        return JSON_MAPPER.fromJson(json, new TypeReference<>() {});
    }

    public static String getResource(String resourceName) throws URISyntaxException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = Objects.requireNonNull(classLoader.getResource(resourceName));
        Path path = Paths.get(resource.toURI());
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }

    @SneakyThrows
    public static <T> T fromString(String jsonFileName, Class<T> clazz) {
        String json = resourceAsString(jsonFileName);
        return OBJECT_MAPPER.readValue(json, clazz);
    }

    public static String toJson(Object input) {
        try {
            return OBJECT_MAPPER.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    String.format("Failed to serialize '%s' to JSON", input.getClass().getSimpleName()), e
            );
        }
    }

    private static String resourceAsString(final String resourcePath) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = ResourceUtils.getFile(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
