package uk.gov.hmcts.ethos.replacement.docmosis.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.gateway")
public class AiGatewayProperties {

    private boolean enabled;
    private String baseUrl;
    private String apiVersion = "2024-10-21";
    private String model = "gpt-6.2-sol";
    private String gatewayClientId;
    private String subscriptionKey;
    private Double temperature;
    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 90_000;
    private int maxRetries = 2;
    private int maxAttachmentBytes = 15 * 1024 * 1024;
    private long maxTotalAttachmentBytes = 25L * 1024 * 1024;
    private int maxAttachmentCount = 10;
    private int maxExtractedCharsPerDocument = 60_000;
}
