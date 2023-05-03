package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Holds gov-notify api key and templateId details.
 */

@Configuration
@Data
public class NotificationProperties {
    @Value("${url.exui.case-details}")
    private String exuiUrl;
    @Value("${url.citizen.case-details}")
    private String citizenUrl;
    @Value("${sendNotification.template.id}")
    private String sendNotificationTemplateId;

    public String getCitizenLinkWithCaseId(String caseId) {
        return citizenUrl + caseId;
    }

    public String getExuiLinkWithCaseId(String caseId) {
        return exuiUrl + caseId;
    }
}
