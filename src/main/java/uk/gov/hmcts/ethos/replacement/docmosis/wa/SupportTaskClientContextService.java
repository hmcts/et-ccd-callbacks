package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTaskClientContextService {
    private static final String CLIENT_CONTEXT = "client_context";
    private static final String USER_TASK = "user_task";
    private static final String TASK_DATA = "task_data";
    private static final String TASK_TYPE = "type";
    private static final String COMPLETE_TASK = "complete_task";

    private final ObjectMapper objectMapper;

    public String updateTaskCompletion(String encodedClientContext, Set<String> taskTypesToComplete) {
        if (isBlank(encodedClientContext) || taskTypesToComplete == null) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(Base64.getDecoder().decode(encodedClientContext));
            JsonNode userTask = root.path(CLIENT_CONTEXT).path(USER_TASK);
            if (!(userTask instanceof ObjectNode userTaskObject)) {
                return null;
            }

            String taskType = userTask.path(TASK_DATA).path(TASK_TYPE).textValue();
            boolean completeTask = taskType != null && taskTypesToComplete.contains(taskType);
            userTaskObject.put(COMPLETE_TASK, completeTask);
            return Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(root).getBytes(UTF_8));
        } catch (IllegalArgumentException | IOException e) {
            log.warn("Unable to update task completion in client-context", e);
            return null;
        }
    }
}
