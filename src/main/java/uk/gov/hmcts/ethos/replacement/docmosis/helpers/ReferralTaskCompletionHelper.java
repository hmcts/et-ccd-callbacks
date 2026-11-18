package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses Service Controlled Task Completion (SCTC) to decide whether the Work Allocation task ExUI is about to complete
 * relates to the referral being closed. ExUI sends the task in a base64 encoded JSON "Client-Context" header and
 * a modified header returned on the callback response changes whether ExUI completes the task.
 * Review Referral tasks are titled "Review Referral #[number] - [subject]" (optionally prefixed "EJ - " or "LO - "),
 * see et-wa-task-configuration.
 */
@Slf4j
public final class ReferralTaskCompletionHelper {
    public static final String CLIENT_CONTEXT = "Client-Context";
    private static final String CLIENT_CONTEXT_FIELD = "client_context";
    private static final String USER_TASK = "user_task";
    private static final String TASK_DATA = "task_data";
    private static final String COMPLETE_TASK = "complete_task";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> REVIEW_REFERRAL_TASK_TYPES = Set.of(
        "ReviewReferralAdmin",
        "ReviewReferralJudiciary",
        "ReviewReferralLegalOps",
        "ReviewReferralResponseAdmin",
        "ReviewReferralResponseJudiciary",
        "ReviewReferralResponseLegalOps"
    );
    private static final Pattern REFERRAL_NUMBER_IN_TITLE = Pattern.compile("Review Referral (?:Response )?#(\\d+)");

    private ReferralTaskCompletionHelper() {
        // Access through static methods
    }

    /**
     * Tells ExUI to complete a Review Referral task only when it belongs to the referral being closed.
     * Called from the mid event callback once the referral has been selected. The about to submit callback cannot
     * be used because CCD does not forward the Client-Context header to decentralised case types on submit.
     * complete_task is set both ways so that going back and changing the selected referral is handled.
     * @param clientContext Client-Context header from the request
     * @param selectedReferralNumber number of the referral being closed
     * @return updated Client-Context header value, or null if the header should be left unchanged
     */
    public static String updateReferralTaskCompletion(String clientContext, String selectedReferralNumber) {
        if (StringUtils.isBlank(selectedReferralNumber)) {
            return null;
        }
        Optional<ObjectNode> context = decode(clientContext);
        Optional<ObjectNode> userTask = context.flatMap(ReferralTaskCompletionHelper::getUserTask);
        if (userTask.isEmpty()) {
            return null;
        }

        String taskType = getTaskDataField(userTask.get(), "type");
        if (!REVIEW_REFERRAL_TASK_TYPES.contains(taskType)) {
            return null;
        }

        String taskReferralNumber = getReferralNumber(userTask.get());
        if (taskReferralNumber == null) {
            return null;
        }

        boolean completeTask = taskReferralNumber.equals(selectedReferralNumber.trim());
        log.info("Task {} is for referral {}, closing referral {}, complete task: {}",
            getTaskDataField(userTask.get(), "id"), taskReferralNumber, selectedReferralNumber, completeTask);
        userTask.get().put(COMPLETE_TASK, completeTask);
        return encode(context.get());
    }

    private static String getReferralNumber(ObjectNode userTask) {
        for (String field : new String[]{"task_title", "name"}) {
            String title = getTaskDataField(userTask, field);
            if (title != null) {
                Matcher matcher = REFERRAL_NUMBER_IN_TITLE.matcher(title);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    private static Optional<ObjectNode> decode(String clientContext) {
        if (StringUtils.isBlank(clientContext)) {
            return Optional.empty();
        }
        try {
            String json = new String(Base64.getDecoder().decode(clientContext.trim()), StandardCharsets.UTF_8);
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return node instanceof ObjectNode objectNode ? Optional.of(objectNode) : Optional.empty();
        } catch (IllegalArgumentException | JsonProcessingException e) {
            log.warn("Unable to decode {} header", CLIENT_CONTEXT, e);
            return Optional.empty();
        }
    }

    private static String encode(ObjectNode clientContext) {
        return Base64.getEncoder().encodeToString(clientContext.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the user_task object, supporting both the documented wrapped form ({"client_context": {...}})
     * and the unwrapped form.
     */
    private static Optional<ObjectNode> getUserTask(ObjectNode clientContext) {
        JsonNode context = clientContext.has(CLIENT_CONTEXT_FIELD) ? clientContext.get(CLIENT_CONTEXT_FIELD)
            : clientContext;
        JsonNode userTask = context.get(USER_TASK);
        return userTask instanceof ObjectNode objectNode ? Optional.of(objectNode) : Optional.empty();
    }

    private static String getTaskDataField(ObjectNode userTask, String fieldName) {
        JsonNode taskData = userTask.get(TASK_DATA);
        if (taskData == null || !taskData.hasNonNull(fieldName)) {
            return null;
        }
        return taskData.get(fieldName).asText();
    }
}
