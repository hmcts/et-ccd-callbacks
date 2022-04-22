package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class ResourceLoader {
    private static final JsonMapper jsonMapper = JsonMapperFactory.create();

    private ResourceLoader() {
    }

    public static UploadResponse successfulDocumentManagementUploadResponse() throws URISyntaxException, IOException {
        String response = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(ResourceLoader.class.getClassLoader()
                .getResource("response.success.json")).toURI())));
        return jsonMapper.fromJson(response, UploadResponse.class);
    }

    public static UploadResponse unsuccessfulDocumentManagementUploadResponse() throws URISyntaxException, IOException {
        String response = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(ResourceLoader.class.getClassLoader()
                .getResource("response.failure.json")).toURI())));
        return jsonMapper.fromJson(response, UploadResponse.class);
    }

    public static uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse successfulDocStoreUpload() throws URISyntaxException, IOException {
        String response = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(ResourceLoader.class.getClassLoader()
                .getResource("responseDocStore.success.json")).toURI())));
        return jsonMapper.fromJson(response, uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse.class);
    }

    public static ListingDetails generateListingDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(ListingDetails.class.getClassLoader()
                .getResource(jsonFileName)).toURI())));
        return jsonMapper.fromJson(json, ListingDetails.class);
    }

    public static List<SubmitEvent> generateSubmitEventList(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(
                new TypeReference<List<SubmitEvent>>(){}.getClass().getClassLoader().getResource(jsonFileName)).toURI())
        ));
        return jsonMapper.fromJson(json, new TypeReference<>() {});
    }
}
