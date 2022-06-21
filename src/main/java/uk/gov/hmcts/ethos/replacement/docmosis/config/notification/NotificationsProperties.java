package uk.gov.hmcts.ethos.replacement.docmosis.config.notification;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@Data
public class NotificationsProperties {

    @Value("${notifications.govNotifyApiKey}")
    @NotEmpty
    private String govNotifyApiKey;

    @Value("${notifications.emailTemplateId}")
    @NotEmpty
    private String sampleEmailTemplateId;
}
