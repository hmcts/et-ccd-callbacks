package uk.gov.hmcts.ethos.replacement.docmosis.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdminService {
    private final EmailService emailService;

    @Value("${tse.admin.template.id}")
    private String emailTemplateId;

    private static final String BOTH = "Both parties";
    private static final String CLAIMANT_ONLY = "Claimant only";
    private static final String RESPONDENT_ONLY = "Respondent only";

    /**
     * Uses {@link EmailService} to generate an email.
     * @param caseData in which the case details are extracted from
     */
    public void sendRecordADecisionEmails(CaseData caseData) {
        String caseNumber = caseData.getEthosCaseReference();

        Map<String, String> emailsToSend = new HashMap<>();

        // if respondent only or both parties: send Respondents Decision Emails
        if (RESPONDENT_ONLY.equals(caseData.getTseAdminSelectPartyNotify()) || BOTH.equals(caseData.getTseAdminSelectPartyNotify())) {
            for (RespondentSumTypeItem respondentSumTypeItem: caseData.getRespondentCollection()) {
                if (respondentSumTypeItem.getValue().getRespondentEmail() != null) {
                    emailsToSend.put(respondentSumTypeItem.getValue().getRespondentEmail(),
                        respondentSumTypeItem.getValue().getRespondentName());
                }
            }
        }

        // if claimant only or both parties: send Claimant Decision Email
        if (CLAIMANT_ONLY.equals(caseData.getTseAdminSelectPartyNotify()) || BOTH.equals(caseData.getTseAdminSelectPartyNotify())) {
            String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
            String claimantName = caseData.getClaimantIndType().getClaimantFirstNames()
                    + " " + caseData.getClaimantIndType().getClaimantLastName();

            if (claimantEmail != null) {
                emailsToSend.put(claimantEmail, claimantName);
            }
        }

        for (Map.Entry<String, String> emailRecipient : emailsToSend.entrySet()) {
            emailService.sendEmail(
                emailTemplateId,
                emailRecipient.getKey(),
                buildPersonalisation(caseNumber, emailRecipient.getValue()));
        }
    }

    private Map<String, String> buildPersonalisation(String caseNumber, String name) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseNumber);
        personalisation.put("name", name);
        return personalisation;
    }
}
