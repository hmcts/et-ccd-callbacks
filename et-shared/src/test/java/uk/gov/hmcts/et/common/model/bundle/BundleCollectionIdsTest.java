package uk.gov.hmcts.et.common.model.bundle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class BundleCollectionIdsTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    void shouldPreserveCollectionIdsWhenProjectingCaseData() throws Exception {
        JsonNode original = mapper.readTree("""
            {
              "caseBundles": [{
                "id": "bundle-collection-id",
                "value": {
                  "id": "em-bundle-id",
                  "documents": [{"id": "document-id", "value": {}}],
                  "folders": [{
                    "id": "folder-id",
                    "value": {
                      "documents": [{"id": "folder-document-id", "value": {}}],
                      "folders": [{
                        "id": "subfolder-id",
                        "value": {
                          "documents": [{"id": "subfolder-document-id", "value": {}}]
                        }
                      }]
                    }
                  }]
                }
              }]
            }
            """);

        JsonNode projected = mapper.valueToTree(mapper.treeToValue(original, CaseData.class));
        JsonNode projectedAgain = mapper.valueToTree(mapper.treeToValue(projected, CaseData.class));

        for (String path : new String[]{
            "/caseBundles/0/id",
            "/caseBundles/0/value/id",
            "/caseBundles/0/value/documents/0/id",
            "/caseBundles/0/value/folders/0/id",
            "/caseBundles/0/value/folders/0/value/documents/0/id",
            "/caseBundles/0/value/folders/0/value/folders/0/id",
            "/caseBundles/0/value/folders/0/value/folders/0/value/documents/0/id"
        }) {
            assertThat(projected.at(path)).as(path).isEqualTo(original.at(path));
            assertThat(projectedAgain.at(path)).as(path).isEqualTo(original.at(path));
        }
    }

    @Test
    void shouldAllowNewBundleItemsWithoutCollectionIds() throws Exception {
        for (Class<?> type : new Class<?>[]{Bundle.class, BundleDocument.class,
            BundleFolder.class, BundleSubfolder.class}) {
            Object item = mapper.readValue("{\"value\": {}}", type);

            JsonNode json = mapper.valueToTree(item);

            assertThat(json.has("id")).as(type.getSimpleName()).isFalse();
            assertThat(json.has("value")).as(type.getSimpleName()).isTrue();
        }
    }
}
