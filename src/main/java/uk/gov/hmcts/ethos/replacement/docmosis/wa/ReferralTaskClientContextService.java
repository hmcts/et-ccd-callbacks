package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Stops ExUI completing a Work Allocation task that belongs to a different referral.
 *
 * <p>The task completion DMN can only match on the CCD event id, so for a referral event it offers
 * ExUI the review tasks of every open referral on the case. ExUI then completes the one the user
 * launched the event from, or the one assigned to them, whichever referral it was raised for.</p>
 *
 * <p>ExUI sends that task in the Client-Context header and completes it after the event only if
 * complete_task is still true in the header it gets back. This service sets the flag according to
 * whether the task was raised for the referral selected in the event.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralTaskClientContextService {

    public static final String CLIENT_CONTEXT_HEADER = "Client-Context";

    private static final String CLIENT_CONTEXT = "client_context";
    private static final String USER_TASK = "user_task";
    private static final String TASK_DATA = "task_data";
    private static final String TASK_ID = "id";
    private static final String TASK_TYPE = "type";
    private static final String TASK_TITLE = "task_title";
    private static final String ADDITIONAL_PROPERTIES = "additional_properties";
    private static final String REFERRAL_NUMBER = "referralNumber";
    private static final String COMPLETE_TASK = "complete_task";

    /**
     * The configuration DMN titles these tasks "Review Referral #32 - Subject" (with EJ - / LO -
     * prefixes and a Response variant), so the number can be read from the title when the task
     * carries no referralNumber additional property.
     */
    private static final Pattern TITLE_REFERRAL_NUMBER = Pattern.compile("#(\\d+)");

    private static final Set<String> REFERRAL_TASK_TYPES = Set.of(
        "ReviewReferralAdmin",
        "ReviewReferralJudiciary",
        "ReviewReferralLegalOps",
        "ReviewReferralResponseAdmin",
        "ReviewReferralResponseJudiciary",
        "ReviewReferralResponseLegalOps"
    );

    private final ObjectMapper objectMapper;

    /**
     * Returns the callback response with the Client-Context header updated for the referral
     * selected in the case data. The response is returned unchanged when the request carries no
     * task, or the task cannot be tied to a referral.
     */
    public ResponseEntity<CCDCallbackResponse> applyTaskCompletion(ResponseEntity<CCDCallbackResponse> response,
                                                                    CaseData caseData) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletAttributes)) {
            return response;
        }

        String updatedClientContext = updateTaskCompletion(
            servletAttributes.getRequest().getHeader(CLIENT_CONTEXT_HEADER), selectedReferralNumber(caseData));
        if (updatedClientContext == null) {
            return response;
        }

        // Callbacks invoked in-process have their ResponseEntity unwrapped, so the header is also
        // set on the enclosing HTTP response.
        if (servletAttributes.getResponse() != null) {
            servletAttributes.getResponse().setHeader(CLIENT_CONTEXT_HEADER, updatedClientContext);
        }
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .header(CLIENT_CONTEXT_HEADER, updatedClientContext)
            .body(response.getBody());
    }

    /**
     * Sets complete_task on the task in the client context to whether it was raised for the given
     * referral.
     *
     * @return the re-encoded client context, or null when it should be left as it was sent
     */
    public String updateTaskCompletion(String encodedClientContext, String referralNumber) {
        if (isBlank(encodedClientContext) || isBlank(referralNumber)) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(Base64.getDecoder().decode(encodedClientContext));
            JsonNode userTask = root.path(CLIENT_CONTEXT).path(USER_TASK);
            JsonNode taskData = userTask.path(TASK_DATA);
            if (!(userTask instanceof ObjectNode userTaskObject)
                || !REFERRAL_TASK_TYPES.contains(taskData.path(TASK_TYPE).asText())) {
                return null;
            }

            String taskReferralNumber = referralNumberOf(taskData);
            if (taskReferralNumber == null) {
                log.warn("Referral task {} carries no referral number, leaving task completion unchanged",
                    taskData.path(TASK_ID).asText());
                return null;
            }

            boolean completeTask = referralNumber.equals(taskReferralNumber);
            log.info("Referral task {} is for referral {}, referral {} selected: complete_task={}",
                taskData.path(TASK_ID).asText(), taskReferralNumber, referralNumber, completeTask);
            userTaskObject.put(COMPLETE_TASK, completeTask);

            // ExUI decodes the header as Latin-1, so anything outside ASCII has to be escaped.
            String json = objectMapper.writer().with(JsonWriteFeature.ESCAPE_NON_ASCII).writeValueAsString(root);
            return Base64.getEncoder().encodeToString(json.getBytes(UTF_8));
        } catch (IllegalArgumentException | IOException e) {
            log.warn("Unable to update task completion in client context", e);
            return null;
        }
    }

    private static String selectedReferralNumber(CaseData caseData) {
        try {
            return ReferralHelper.getSelectedReferral(caseData).getReferralNumber();
        } catch (RuntimeException e) {
            // No usable selection yet - the event's own validation reports that to the user.
            return null;
        }
    }

    private static String referralNumberOf(JsonNode taskData) {
        String fromProperty = taskData.path(ADDITIONAL_PROPERTIES).path(REFERRAL_NUMBER).asText();
        if (!isBlank(fromProperty)) {
            return fromProperty;
        }
        Matcher matcher = TITLE_REFERRAL_NUMBER.matcher(taskData.path(TASK_TITLE).asText());
        return matcher.find() ? matcher.group(1) : null;
    }
}
