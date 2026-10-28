package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralTaskCompletionHelper.updateReferralTaskCompletion;

class ReferralTaskCompletionHelperTest {

    private static String clientContext(String type, String title, boolean completeTask) {
        String json = """
            {"client_context":{"user_task":{"complete_task":%s,"task_data":{
            "id":"task-id","type":"%s","task_title":"%s","name":"Review Referral"}}}}
            """.formatted(completeTask, type, title);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String clientContext(String type, String title) {
        return clientContext(type, title, true);
    }

    private static JsonNode userTask(String header) throws IOException {
        return new ObjectMapper().readTree(Base64.getDecoder().decode(header)).at("/client_context/user_task");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Review Referral #32 - Subject",
        "EJ - Review Referral #32 - Subject",
        "LO - Review Referral Response #32 - Subject"
    })
    void preventsCompletionWhenTaskIsForAnotherReferral(String title) throws Exception {
        String updated = updateReferralTaskCompletion(clientContext("ReviewReferralJudiciary", title), "5");

        assertThat(updated).isNotNull();
        assertThat(userTask(updated).get("complete_task").asBoolean()).isFalse();
    }

    @Test
    void completesTaskWhenTaskIsForSelectedReferral() throws Exception {
        String updated = updateReferralTaskCompletion(
            clientContext("ReviewReferralAdmin", "Review Referral #32 - Subject"), "32");

        assertThat(userTask(updated).get("complete_task").asBoolean()).isTrue();
    }

    @Test
    void resetsCompletionWhenSelectionChangedToTaskReferral() throws Exception {
        // User selected another referral, went back and selected the task's referral
        String updated = updateReferralTaskCompletion(
            clientContext("ReviewReferralAdmin", "Review Referral #32 - Subject", false), "32");

        assertThat(userTask(updated).get("complete_task").asBoolean()).isTrue();
    }

    @Test
    void keepsTaskDataWhenUpdating() throws Exception {
        String updated = updateReferralTaskCompletion(
            clientContext("ReviewReferralAdmin", "Review Referral #32 - Subject"), "5");

        assertThat(userTask(updated).at("/task_data/id").asText()).isEqualTo("task-id");
    }

    @Test
    void leavesHeaderUnchangedWhenTaskIsNotReviewReferral() {
        assertThat(updateReferralTaskCompletion(
            clientContext("ReviewET1Form", "Review Referral #32 - Subject"), "5")).isNull();
    }

    @Test
    void leavesHeaderUnchangedWhenTitleHasNoReferralNumber() {
        assertThat(updateReferralTaskCompletion(clientContext("ReviewReferralAdmin", "Review Referral"), "5"))
            .isNull();
    }

    @Test
    void leavesHeaderUnchangedWhenNoReferralSelected() {
        assertThat(updateReferralTaskCompletion(
            clientContext("ReviewReferralAdmin", "Review Referral #32 - Subject"), null)).isNull();
    }

    @Test
    void leavesHeaderUnchangedWhenHeaderMissingOrInvalid() {
        assertThat(updateReferralTaskCompletion(null, "5")).isNull();
        assertThat(updateReferralTaskCompletion("not base64!", "5")).isNull();
        String noUserTask = Base64.getEncoder().encodeToString("{\"client_context\":{}}".getBytes());
        assertThat(updateReferralTaskCompletion(noUserTask, "5")).isNull();
    }
}
