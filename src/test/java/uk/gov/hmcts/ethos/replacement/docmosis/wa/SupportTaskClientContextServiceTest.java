package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class SupportTaskClientContextServiceTest {
    private static final String ADMIN_TASK = "ReviewSupportRequestAdmin";
    private static final String ARRANGE_TASK = "ArrangeSupport";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private SupportTaskClientContextService service;

    @BeforeEach
    void setUp() {
        service = new SupportTaskClientContextService(objectMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ReviewSupportRequestAdmin",
        "ReviewSupportRequestLegalOfficer",
        "ReviewSupportRequestJudge"
    })
    void completesInvokingReviewTaskWhenItsCategoryNoLongerHasRequestedFlags(String taskType) throws Exception {
        String updatedContext = service.updateTaskCompletion(
                encode(clientContext(taskType)), Set.of(taskType));

        JsonNode result = decode(updatedContext);
        assertThat(result.at("/client_context/user_task/complete_task").booleanValue()).isTrue();
        assertThat(result.at("/client_context/user_task/task_data/id").textValue()).isEqualTo("task-id");
        assertThat(result.at("/client_context/user_language/language").textValue()).isEqualTo("en");
    }

    @Test
    void keepsInvokingReviewTaskOpenWhileItsCategoryHasRequestedFlags() throws Exception {
        String updatedContext = service.updateTaskCompletion(encode(clientContext(ADMIN_TASK)), Set.of());

        assertThat(decode(updatedContext).at("/client_context/user_task/complete_task").booleanValue()).isFalse();
    }

    @Test
    void doesNotCompleteArrangeTaskWhenReviewCategoryNeedsCompletion() throws Exception {
        String updatedContext = service.updateTaskCompletion(
                encode(clientContext(ARRANGE_TASK)), Set.of(ADMIN_TASK));

        assertThat(decode(updatedContext).at("/client_context/user_task/complete_task").booleanValue()).isFalse();
    }

    @Test
    void returnsNullWhenClientContextIsAbsentOrInvalid() {
        assertThat(service.updateTaskCompletion(null, Set.of(ADMIN_TASK))).isNull();
        assertThat(service.updateTaskCompletion("not-base64", Set.of(ADMIN_TASK))).isNull();
        assertThat(service.updateTaskCompletion(encode("{}"), Set.of(ADMIN_TASK))).isNull();
    }

    private String clientContext(String taskType) {
        return """
                {
                  "client_context": {
                    "user_task": {
                      "task_data": {
                        "id": "task-id",
                        "type": "%s"
                      },
                      "complete_task": false
                    },
                    "user_language": {
                      "language": "en"
                    }
                  }
                }
                """.formatted(taskType);
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(UTF_8));
    }

    private JsonNode decode(String value) throws IOException {
        return objectMapper.readTree(Base64.getDecoder().decode(value));
    }
}
