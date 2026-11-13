package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlagDetailTypeTest {
    private static final String COMPLETE_FLAG_JSON = """
            {
              "name": "Other support",
              "name_cy": "Cymorth arall",
              "subTypeValue": "Forms",
              "subTypeValue_cy": "Ffurflenni",
              "subTypeKey": "forms",
              "otherDescription": "Help reading forms",
              "otherDescription_cy": "Help i ddarllen ffurflenni",
              "flagComment": "Support requested",
              "flagComment_cy": "Cais am gymorth",
              "flagUpdateComment": "Support approved",
              "flagUpdateComment_cy": "Cymorth wedi ei gymeradwyo",
              "dateTimeModified": "2026-10-08T11:30:00.123Z",
              "dateTimeCreated": "2026-10-07T10:00:00.000Z",
              "path": [
                {"id": "party", "value": "Party"},
                {"id": "adjustment", "value": "Reasonable adjustment"}
              ],
              "hearingRelevant": "Yes",
              "flagCode": "OT0001",
              "status": "Active",
              "requestReason": "No",
              "availableExternally": "Yes"
            }
            """;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesEveryFieldUsingCcdPropertyNames() throws JsonProcessingException {
        JsonNode actual = mapper.readTree(mapper.writeValueAsString(completeFlag()));

        assertEquals(mapper.readTree(COMPLETE_FLAG_JSON), actual);
    }

    @Test
    void deserializesEveryFieldIncludingWelshUpdateComment() throws JsonProcessingException {
        FlagDetailType actual = mapper.readValue(COMPLETE_FLAG_JSON, FlagDetailType.class);

        assertEquals(completeFlag(), actual);
        assertEquals("Cymorth wedi ei gymeradwyo", actual.getFlagUpdateComment_cy());
        assertEquals("party", actual.getPath().getFirst().getId());
        assertEquals("Reasonable adjustment", actual.getPath().get(1).getValue());
    }

    @Test
    void acceptsLegacyFlagsWithMissingOptionalAndUnknownFields() throws JsonProcessingException {
        FlagDetailType actual = mapper.readValue("""
                {"name": "Intermediary", "flagCode": "RA0038", "status": "Requested", "futureField": "value"}
                """, FlagDetailType.class);

        assertEquals(FlagDetailType.builder().name("Intermediary").flagCode("RA0038")
                .status("Requested").build(), actual);
        assertNull(actual.getFlagUpdateComment_cy());
        assertNull(actual.getPath());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-10-08T11:30:00", "2026-10-08T11:30:00.123Z", "2026-10-08T12:30:00+01:00"})
    void preservesDateStringsWithoutReformatting(String dateTime) throws JsonProcessingException {
        FlagDetailType original = FlagDetailType.builder()
                .dateTimeCreated(dateTime).dateTimeModified(dateTime).build();

        FlagDetailType actual = mapper.readValue(mapper.writeValueAsString(original), FlagDetailType.class);

        assertEquals(dateTime, actual.getDateTimeCreated());
        assertEquals(dateTime, actual.getDateTimeModified());
    }

    private static FlagDetailType completeFlag() {
        return FlagDetailType.builder()
                .name("Other support").name_cy("Cymorth arall")
                .subTypeValue("Forms").subTypeValue_cy("Ffurflenni").subTypeKey("forms")
                .otherDescription("Help reading forms").otherDescription_cy("Help i ddarllen ffurflenni")
                .flagComment("Support requested").flagComment_cy("Cais am gymorth")
                .flagUpdateComment("Support approved").flagUpdateComment_cy("Cymorth wedi ei gymeradwyo")
                .dateTimeModified("2026-10-08T11:30:00.123Z").dateTimeCreated("2026-10-07T10:00:00.000Z")
                .path(ListTypeItem.from(GenericTypeItem.from("party", "Party"),
                        GenericTypeItem.from("adjustment", "Reasonable adjustment")))
                .hearingRelevant("Yes").flagCode("OT0001").status("Active")
                .requestReason("No").availableExternally("Yes").build();
    }
}
