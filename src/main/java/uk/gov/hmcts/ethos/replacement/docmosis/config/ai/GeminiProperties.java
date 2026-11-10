package uk.gov.hmcts.ethos.replacement.docmosis.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.gemini")
public class GeminiProperties {

    private boolean enabled;
    private String apiKey;
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private String model = "gemini-3.6-flash";
    private Double temperature = 0.1;
}
