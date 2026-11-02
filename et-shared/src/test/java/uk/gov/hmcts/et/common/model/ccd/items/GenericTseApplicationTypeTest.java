package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenericTseApplicationTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void readsEnglandWalesCloseApplicationNotes() throws Exception {
        GenericTseApplicationType application =
            objectMapper.readValue("{\"closeApplicationNotes\":\"England and Wales notes\"}",
                GenericTseApplicationType.class);

        assertThat(application.getCloseApplicationNotesValue()).isEqualTo("England and Wales notes");
    }

    @Test
    void readsScotlandCloseApplicationNote() throws Exception {
        GenericTseApplicationType application =
            objectMapper.readValue("{\"closeApplicationNote\":\"Scotland notes\"}",
                GenericTseApplicationType.class);

        assertThat(application.getCloseApplicationNotesValue()).isEqualTo("Scotland notes");
    }

    @Test
    void writesOnlyEnglandWalesCloseApplicationNotes() throws Exception {
        GenericTseApplicationType application = new GenericTseApplicationType();
        application.setCloseApplicationNotes("England and Wales notes");
        var json = objectMapper.readTree(objectMapper.writeValueAsString(application));
        assertThat(json.path("closeApplicationNotes").asText()).isEqualTo("England and Wales notes");
        assertThat(json.has("closeApplicationNote")).isFalse();
    }

    @Test
    void writesOnlyScotlandCloseApplicationNote() throws Exception {
        GenericTseApplicationType application = new GenericTseApplicationType();
        application.setCloseApplicationNote("Scotland notes");
        var json = objectMapper.readTree(objectMapper.writeValueAsString(application));
        assertThat(json.path("closeApplicationNote").asText()).isEqualTo("Scotland notes");
        assertThat(json.has("closeApplicationNotes")).isFalse();
    }
}
