package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ReferralTaskClientContextServiceTest {

    private static final String SELECTED_REFERRAL = "5";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReferralTaskClientContextService service = new ReferralTaskClientContextService(objectMapper);

    @ParameterizedTest
    @CsvSource({
        "ReviewReferralAdmin, Review Referral #5 - ET1, true",
        "ReviewReferralAdmin, Review Referral #32 - ET1, false",
        "ReviewReferralJudiciary, EJ - Review Referral #5 - ET3/ECC, true",
        "ReviewReferralLegalOps, LO - Review Referral #55 - ET1, false",
        "ReviewReferralResponseAdmin, Review Referral Response #5 - ET1, true",
        "ReviewReferralResponseJudiciary, EJ - Review Referral Response #6 - ET1, false"
    })
    void setsCompleteTaskFromTheReferralNumberInTheTaskTitle(String type, String title, boolean expected)
        throws IOException {
        String updated = service.updateTaskCompletion(
            clientContext(taskData(type, title, null), !expected), SELECTED_REFERRAL);

        assertThat(completeTask(updated)).isEqualTo(expected);
    }

    @Test
    void prefersTheReferralNumberAdditionalPropertyOverTheTitle() throws IOException {
        String updated = service.updateTaskCompletion(
            clientContext(taskData("ReviewReferralAdmin", "Review Referral #32 - ET1", SELECTED_REFERRAL), false),
            SELECTED_REFERRAL);

        assertThat(completeTask(updated)).isTrue();
    }

    @Test
    void keepsTheRestOfTheClientContext() throws IOException {
        String updated = service.updateTaskCompletion(
            clientContext(taskData("ReviewReferralAdmin", "Review Referral #32 - Reconsidération", null), true),
            SELECTED_REFERRAL);

        // ExUI reads the header with atob, so it has to stay within ASCII.
        String json = new String(Base64.getDecoder().decode(updated), UTF_8);
        assertThat(json).isASCII();
        JsonNode clientContext = objectMapper.readTree(json).path("client_context");
        assertThat(clientContext.path("user_language").path("language").asText()).isEqualTo("en");
        assertThat(clientContext.path("user_task").path("task_data").path("task_title").asText())
            .isEqualTo("Review Referral #32 - Reconsidération");
    }

    @Test
    void leavesATaskThatIsNotAReferralTaskUnchanged() {
        assertThat(service.updateTaskCompletion(
            clientContext(taskData("Et1Vetting", "Et1 Vetting #32", null), true), SELECTED_REFERRAL)).isNull();
    }

    @Test
    void leavesAReferralTaskWithNoReferralNumberUnchanged() {
        assertThat(service.updateTaskCompletion(
            clientContext(taskData("ReviewReferralAdmin", "Review Referral", null), true), SELECTED_REFERRAL))
            .isNull();
    }

    @Test
    void leavesAClientContextWithNoTaskUnchanged() {
        String noTask = encode("{\"client_context\":{\"user_language\":{\"language\":\"en\"}}}");

        assertThat(service.updateTaskCompletion(noTask, SELECTED_REFERRAL)).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not base64 !", "bm90IGpzb24="})
    void leavesAMissingOrUnreadableClientContextUnchanged(String encodedClientContext) {
        assertThat(service.updateTaskCompletion(encodedClientContext, SELECTED_REFERRAL)).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void leavesTheClientContextUnchangedWhenNoReferralIsSelected(String referralNumber) {
        assertThat(service.updateTaskCompletion(
            clientContext(taskData("ReviewReferralAdmin", "Review Referral #5 - ET1", null), true), referralNumber))
            .isNull();
    }

    private boolean completeTask(String encodedClientContext) throws IOException {
        return objectMapper.readTree(Base64.getDecoder().decode(encodedClientContext))
            .path("client_context").path("user_task").path("complete_task").asBoolean();
    }

    private static String taskData(String type, String title, String referralNumber) {
        String additionalProperties = referralNumber == null ? ""
            : ",\"additional_properties\":{\"referralNumber\":\"" + referralNumber + "\"}";
        return "{\"id\":\"task-id\",\"type\":\"" + type + "\",\"task_title\":\"" + title + "\""
            + additionalProperties + "}";
    }

    private static String clientContext(String taskData, boolean completeTask) {
        return encode("{\"client_context\":{\"user_task\":{\"task_data\":" + taskData
            + ",\"complete_task\":" + completeTask + "},\"user_language\":{\"language\":\"en\"}}}");
    }

    private static String encode(String json) {
        return Base64.getEncoder().encodeToString(json.getBytes(UTF_8));
    }
}
