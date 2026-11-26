package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.AiGatewayProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiGatewayClient {

    private static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
    private static final String JSON_CODE_BLOCK_PREFIX = "```json";
    private static final String CODE_BLOCK_PREFIX = "```";
    private static final long INITIAL_BACKOFF_MS = 1_000L;

    private final AiGatewayProperties properties;
    private final AiAttachmentExtractor attachmentExtractor;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TokenCredential tokenCredential;

    @Autowired
    public AiGatewayClient(AiGatewayProperties properties,
                           AiAttachmentExtractor attachmentExtractor,
                           ObjectMapper objectMapper) {
        this(properties, attachmentExtractor, buildRestTemplate(properties), objectMapper,
                new DefaultAzureCredentialBuilder().build());
    }

    AiGatewayClient(AiGatewayProperties properties,
                    RestTemplate restTemplate,
                    ObjectMapper objectMapper,
                    TokenCredential tokenCredential) {
        this(properties, new AiAttachmentExtractor(properties), restTemplate, objectMapper, tokenCredential);
    }

    AiGatewayClient(AiGatewayProperties properties,
                    AiAttachmentExtractor attachmentExtractor,
                    RestTemplate restTemplate,
                    ObjectMapper objectMapper,
                    TokenCredential tokenCredential) {
        this.properties = properties;
        this.attachmentExtractor = attachmentExtractor;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenCredential = tokenCredential;
    }

    /**
     * Calls the HMCTS AI Gateway Chat Completions endpoint with a static system instruction,
     * dynamic case prompt, and converted/extracted document attachments.
     *
     * @param systemInstruction Static system rules and JSON schema (prefix-cached by Azure OpenAI)
     * @param prompt Dynamic user prompt text containing claim facts
     * @param attachments List of binary file attachments (ET1, Particulars, ACAS certificates)
     * @return Raw JSON text response from the model, or null if unavailable/failed
     */
    public String generateContent(String systemInstruction,
                                  String prompt,
                                  List<AiDocumentAttachment> attachments) {
        if (StringUtils.isBlank(properties.getSubscriptionKey())
                || StringUtils.isBlank(properties.getGatewayClientId())) {
            log.warn("AI Gateway subscription key or client ID is not configured; skipping AI call");
            return null;
        }

        try {
            String bearerToken = fetchAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bearerToken);
            headers.set(SUBSCRIPTION_KEY_HEADER, properties.getSubscriptionKey());

            Map<String, Object> requestBody = buildRequestBody(systemInstruction, prompt, attachments);
            log.info("Calling AI Gateway model={} with {} attachment(s)",
                    properties.getModel(), attachments == null ? 0 : attachments.size());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String url = buildChatCompletionsUrl();

            return executeWithRetry(url, entity);
        } catch (Exception ex) {
            log.error("Error communicating with HMCTS AI Gateway: {}", ex.getMessage(), ex);
            return null;
        }
    }

    private String fetchAccessToken() {
        String scope = "api://" + properties.getGatewayClientId().trim() + "/.default";
        TokenRequestContext context = new TokenRequestContext().addScopes(scope);
        AccessToken token = tokenCredential.getTokenSync(context);
        if (token == null || StringUtils.isBlank(token.getToken())) {
            throw new IllegalStateException("Acquired empty Entra ID access token for AI Gateway");
        }
        return token.getToken();
    }

    private String buildChatCompletionsUrl() {
        String baseUrl = StringUtils.removeEnd(StringUtils.trimToEmpty(properties.getBaseUrl()), "/");
        return String.format("%s/ai/platform/v1/chat/completions?api-version=%s",
                baseUrl, properties.getApiVersion());
    }

    private Map<String, Object> buildRequestBody(String systemInstruction,
                                                 String prompt,
                                                 List<AiDocumentAttachment> attachments) {
        List<Map<String, Object>> messages = new ArrayList<>();
        if (StringUtils.isNotBlank(systemInstruction)) {
            messages.add(Map.of("role", "system", "content", systemInstruction));
        }
        messages.add(Map.of("role", "user", "content", attachmentExtractor.buildUserParts(prompt, attachments)));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", properties.getModel());
        if (properties.getTemperature() != null) {
            requestBody.put("temperature", properties.getTemperature());
        }
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("messages", messages);
        return requestBody;
    }

    private String executeWithRetry(String url, HttpEntity<Map<String, Object>> entity) {
        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return extractTextFromResponse(response.getBody());
                }
                log.error("AI Gateway returned non-success response: status={}", response.getStatusCode());
                return null;
            } catch (HttpClientErrorException.TooManyRequests | HttpServerErrorException ex) {
                if (attempt == maxAttempts) {
                    log.error("AI Gateway request failed after {} attempts: {}", attempt, ex.getMessage());
                    return null;
                }
                log.warn("AI Gateway transient error (attempt {}/{}): {}", attempt, maxAttempts, ex.getMessage());
                sleepBackoff(attempt);
            }
        }
        return null;
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(INITIAL_BACKOFF_MS * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode contentNode = choices.get(0).path("message").path("content");
                if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                    return stripMarkdownCodeFences(contentNode.asText());
                }
            }
            log.warn("AI Gateway response did not contain expected choices[0].message.content structure");
            return null;
        } catch (Exception ex) {
            log.error("Failed to parse AI Gateway response JSON: {}", ex.getMessage(), ex);
            return null;
        }
    }

    private String stripMarkdownCodeFences(String rawText) {
        if (StringUtils.isBlank(rawText)) {
            return rawText;
        }
        String trimmed = rawText.trim();
        if (trimmed.startsWith(JSON_CODE_BLOCK_PREFIX)) {
            trimmed = trimmed.substring(JSON_CODE_BLOCK_PREFIX.length()).trim();
        } else if (trimmed.startsWith(CODE_BLOCK_PREFIX)) {
            trimmed = trimmed.substring(CODE_BLOCK_PREFIX.length()).trim();
        }
        if (trimmed.endsWith(CODE_BLOCK_PREFIX)) {
            trimmed = trimmed.substring(0, trimmed.length() - CODE_BLOCK_PREFIX.length()).trim();
        }
        return trimmed;
    }

    private static RestTemplate buildRestTemplate(AiGatewayProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }
}
