package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EmailClient;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;
import uk.gov.service.notify.NotificationClientException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.getLast;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@Service
public class EmailService {

    private final transient EmailClient emailClient;

    @Autowired
    public EmailService(EmailClient emailClient) {
        this.emailClient = emailClient;
    }

    public void sendReferralEmailJudgeHasSentDirections(CaseData caseData) {
        Map<String, String> personalisation = buildPersonalisation(caseData);
        personalisation.put("body", "A judge has sent directions on this employment tribunal case.");

        sendEmail(this.emailClient.referralTemplateId, personalisation);
    }

    public void sendReferralEmailYouHaveReceivedNewMessage(CaseData caseData) {
        Map<String, String> personalisation = buildPersonalisation(caseData);
        personalisation.put("body", "You have a new message about this employment tribunal case.");

        sendEmail(this.emailClient.referralTemplateId, personalisation);
    }

    private Map<String, String> buildPersonalisation(CaseData caseData) {
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put("emailAddress", getEmailAddressFromLastReferral(caseData));
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("emailFlag", getEmailFlag(caseData));
        personalisation.put("claimant", caseData.getClaimant());
        personalisation.put("respondents",
            caseData.getRespondentCollection().stream().map(o -> o.getValue().getRespondentName())
                .collect(Collectors.joining(", ")));
        personalisation.put("date", getHearingDate(caseData));
        return personalisation;
    }

    private String getEmailAddressFromLastReferral(CaseData caseData) {
        ReferralTypeItem lastReferral = getLast(caseData.getReferralCollection());
        // Add validation if/when needed
        return lastReferral.getValue().getReferentEmail();
    }

    private String getEmailFlag(CaseData caseData) {
        ReferralTypeItem lastReferral = getLast(caseData.getReferralCollection());
        return YES.equals(lastReferral.getValue().getIsUrgent()) ? "URGENT" : "";
    }

    private String getHearingDate(CaseData caseData) {
        var thing = HearingsHelper.getEarliestFutureHearingDate(caseData.getHearingCollection());
        if (thing != null) {
            try {
                Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(thing);
                return new SimpleDateFormat("dd MMM yyyy").format(hearingStartDate);
            } catch (ParseException e) {
                log.info("Failed to parse hearing date when creating new referral");
            }
        }
        return "Not set";
    }

    private void sendEmail(String templateId, Map<String, String> personalisation) {

        var referenceId = UUID.randomUUID().toString();

        try {
            emailClient.sendEmail(
                templateId,
                personalisation.get("emailAddress"),
                personalisation,
                referenceId
            );
            log.info("Sending email success. Reference ID: {}", referenceId);

        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason:", referenceId, e);
        }
    }
}