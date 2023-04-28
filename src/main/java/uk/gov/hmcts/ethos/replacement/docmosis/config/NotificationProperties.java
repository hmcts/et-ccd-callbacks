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
    @Value("${pse.respondent.acknowledgement.yes.template.id}")
    private String acknowledgeEmailYesTemplateId;
    @Value("${pse.respondent.acknowledgement.no.template.id}")
    private String acknowledgeEmailNoTemplateId;
    @Value("${pse.respondent.notification.claimant.template.id}")
    private String notificationToClaimantTemplateId;
    @Value("${pse.respondent.notification.admin.template.id}")
    private String notificationToAdminTemplateId;
    @Value("${tse.admin.record-a-decision.notify.claimant.template.id}")
    private String tseAdminRecordClaimantTemplateId;
    @Value("${tse.admin.record-a-decision.notify.respondent.template.id}")
    private String tseAdminRecordRespondentTemplateId;
    @Value("${tse.admin.reply.notify.claimant.template.id}")
    private String tseAdminReplyClaimantTemplateId;
    @Value("${tse.admin.reply.notify.respondent.template.id}")
    private String tseAdminReplyRespondentTemplateId;
    @Value("${sendNotification.template.id}")
    private String responseTemplateId;
    @Value("${respondNotification.noResponseTemplate.id}")
    private String noResponseTemplateId;
    @Value("${tse.respondent.application.acknowledgement.template.id}")
    private String tseRespondentAcknowledgeTemplateId;
    @Value("${tse.respondent.application.acknowledgement.type.c.template.id}")
    private String tseRespondentAcknowledgeTypeCTemplateId;
    @Value("${tse.respondent.application.notify.claimant.template.id}")
    private String tseRespondentToClaimantTemplateId;
    @Value("${tse.respondent.application.tribunal.template.id}")
    private String tseNewApplicationAdminTemplateId;
    @Value("${tse.respondent.respond.notify.claimant.template.id}")
    private String tseRespondentResponseTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92no.template.id}")
    private String acknowledgementRule92NoEmailTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92yes.template.id}")
    private String acknowledgementRule92YesEmailTemplateId;
    @Value("${sendNotification.template.id}")
    private String sendNotificationTemplateId;
}
