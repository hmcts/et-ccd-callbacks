package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.GeminiProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final String JSON_CODE_BLOCK_PREFIX = "```json";
    private static final String CODE_BLOCK_PREFIX = "```";

    private final GeminiProperties geminiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiClient(GeminiProperties geminiProperties,
                        @Qualifier("restTemplate") RestTemplate restTemplate,
                        ObjectMapper objectMapper) {
        this.geminiProperties = geminiProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Calls Google Gemini Generative Language API with prompt and returns JSON response text.
     *
     * @param prompt User prompt text containing case details and extraction requirements
     * @return Raw JSON text response from Gemini
     */
    public String generateContent(String prompt) {
        return generateContent(prompt, Collections.emptyList());
    }

    /**
     * Calls Google Gemini Generative Language API with text prompt and binary file attachments (multimodal).
     *
     * @param prompt User prompt text containing case details and extraction requirements
     * @param attachments List of binary file attachments (e.g. PDF particulars of claim)
     * @return Raw JSON text response from Gemini
     */
    public String generateContent(String prompt, List<AiDocumentAttachment> attachments) {
        if (StringUtils.isBlank(geminiProperties.getApiKey())) {
            log.warn("Gemini API key is not configured; skipping AI call");
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(API_KEY_HEADER, geminiProperties.getApiKey());

            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(Map.of("text", prompt));

            if (CollectionUtils.isNotEmpty(attachments)) {
                for (AiDocumentAttachment attachment : attachments) {
                    if (attachment != null && attachment.getData() != null && attachment.getData().length > 0) {
                        log.info("Attaching inline {} file ({}) to Gemini multimodal request",
                                attachment.getMimeType(), attachment.getFilename());
                        String base64 = Base64.getEncoder().encodeToString(attachment.getData());
                        parts.add(Map.of("inlineData", Map.of(
                                "mimeType", StringUtils.defaultIfBlank(attachment.getMimeType(), "application/pdf"),
                                "data", base64
                        )));
                    }
                }
            }

            Map<String, Object> content = Map.of("role", "user", "parts", parts);
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", geminiProperties.getTemperature());
            generationConfig.put("responseMimeType", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "contents", Collections.singletonList(content),
                    "generationConfig", generationConfig
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String url = String.format("%s/models/%s:generateContent",
                    geminiProperties.getBaseUrl(), geminiProperties.getModel());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            }

            log.error("Gemini API returned non-success response: status={}", response.getStatusCode());
            return null;
        } catch (Exception ex) {
            log.error("Error communicating with Gemini API: {}", ex.getMessage(), ex);
            return null;
        }
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String rawText = parts.get(0).path("text").asText();
                    return cleanJsonMarkdown(rawText);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to parse Gemini API response payload: {}", ex.getMessage());
        }
        return null;
    }

    private String cleanJsonMarkdown(String rawText) {
        if (rawText == null) {
            return null;
        }
        String trimmed = rawText.trim();
        if (trimmed.startsWith(JSON_CODE_BLOCK_PREFIX)) {
            trimmed = trimmed.substring(JSON_CODE_BLOCK_PREFIX.length());
        } else if (trimmed.startsWith(CODE_BLOCK_PREFIX)) {
            trimmed = trimmed.substring(CODE_BLOCK_PREFIX.length());
        }
        if (trimmed.endsWith(CODE_BLOCK_PREFIX)) {
            trimmed = trimmed.substring(0, trimmed.length() - CODE_BLOCK_PREFIX.length());
        }
        return trimmed.trim();
    }
}
