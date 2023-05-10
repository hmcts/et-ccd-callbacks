package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEAdminEmailRecipientsData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getSelectedApplicationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdminService {
    private final EmailService emailService;
    private final TseService tseService;
    private final NotificationProperties notificationProperties;
    @Value("${tse.admin.record-a-decision.notify.claimant.template.id}")
    private String tseAdminRecordClaimantTemplateId;
    @Value("${tse.admin.record-a-decision.notify.respondent.template.id}")
    private String tseAdminRecordRespondentTemplateId;

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public void initialTseAdminTableMarkUp(CaseData caseData, String authToken) {
        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem == null) {
            return;
        }

        caseData.setTseAdminTableMarkUp(String.format("%s\r\n%s",
                tseService.formatApplicationDetails(applicationTypeItem.getValue(), authToken, false),
                tseService.formatApplicationResponses(applicationTypeItem.getValue(), authToken)
        ));
    }

    /**
     * Save Tse Admin Record a Decision data to the application object.
     * @param caseData in which the case details are extracted from
     */
    public void saveTseAdminDataFromCaseData(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {

            GenericTseApplicationType genericTseApplicationType = applicationTypeItem.getValue();
            if (CollectionUtils.isEmpty(genericTseApplicationType.getAdminDecision())) {
                genericTseApplicationType.setAdminDecision(new ArrayList<>());
            }

            genericTseApplicationType.getAdminDecision().add(
                TseAdminRecordDecisionTypeItem.builder()
                    .id(UUID.randomUUID().toString())
                    .value(
                        TseAdminRecordDecisionType.builder()
                            .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                            .enterNotificationTitle(caseData.getTseAdminEnterNotificationTitle())
                            .decision(caseData.getTseAdminDecision())
                            .decisionDetails(caseData.getTseAdminDecisionDetails())
                            .typeOfDecision(caseData.getTseAdminTypeOfDecision())
                            .isResponseRequired(caseData.getTseAdminIsResponseRequired())
                            .selectPartyRespond(caseData.getTseAdminSelectPartyRespond())
                            .additionalInformation(caseData.getTseAdminAdditionalInformation())
                            .responseRequiredDoc(getResponseRequiredDocYesOrNo(caseData))
                            .decisionMadeBy(caseData.getTseAdminDecisionMadeBy())
                            .decisionMadeByFullName(caseData.getTseAdminDecisionMadeByFullName())
                            .selectPartyNotify(caseData.getTseAdminSelectPartyNotify())
                            .build()
                    ).build());
        }
    }

    private List<GenericTypeItem<DocumentType>> getResponseRequiredDocYesOrNo(CaseData caseData) {
        if (YES.equals(caseData.getTseAdminIsResponseRequired())) {
            return caseData.getTseAdminResponseRequiredYesDoc();
        }
        return caseData.getTseAdminResponseRequiredNoDoc();
    }

    /**
     * Uses {@link EmailService} to generate an email.
     * @param caseId used in email link to case
     * @param caseData in which the case details are extracted from
     */
    public void sendRecordADecisionEmails(String caseId, CaseData caseData) {
        String caseNumber = caseData.getEthosCaseReference();

        List<TSEAdminEmailRecipientsData> emailsToSend = new ArrayList<>();

        // if respondent only or both parties: send Respondents Decision Emails
        if (RESPONDENT_ONLY.equals(caseData.getTseAdminSelectPartyNotify())
                || BOTH_PARTIES.equals(caseData.getTseAdminSelectPartyNotify())) {
            TSEAdminEmailRecipientsData respondentDetails;
            for (RespondentSumTypeItem respondentSumTypeItem: caseData.getRespondentCollection()) {
                if (respondentSumTypeItem.getValue().getRespondentEmail() != null) {
                    respondentDetails =
                        new TSEAdminEmailRecipientsData(
                            tseAdminRecordRespondentTemplateId,
                            respondentSumTypeItem.getValue().getRespondentEmail());
                    respondentDetails.setRecipientName(respondentSumTypeItem.getValue().getRespondentName());

                    emailsToSend.add(respondentDetails);
                }
            }
        }

        // if claimant only or both parties: send Claimant Decision Email
        if (CLAIMANT_ONLY.equals(caseData.getTseAdminSelectPartyNotify())
                || BOTH_PARTIES.equals(caseData.getTseAdminSelectPartyNotify())) {
            String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
            String claimantName = caseData.getClaimantIndType().claimantFullNames();

            if (claimantEmail != null) {
                TSEAdminEmailRecipientsData claimantDetails =
                    new TSEAdminEmailRecipientsData(tseAdminRecordClaimantTemplateId,
                            claimantEmail);
                claimantDetails.setRecipientName(claimantName);

                emailsToSend.add(claimantDetails);
            }
        }

        for (final TSEAdminEmailRecipientsData emailRecipient : emailsToSend) {
            emailService.sendEmail(
                emailRecipient.getRecipientTemplate(),
                emailRecipient.getRecipientEmail(),
                buildPersonalisation(caseNumber, caseId, emailRecipient.getRecipientName()));
        }
    }

    private Map<String, String> buildPersonalisation(String caseNumber, String caseId, String recipientName) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseNumber);
        personalisation.put(LINK_TO_CITIZEN_HUB, notificationProperties.getCitizenLinkWithCaseId(caseId));
        personalisation.put(LINK_TO_EXUI, notificationProperties.getExuiLinkWithCaseId(caseId));
        personalisation.put("name", recipientName);
        return personalisation;
    }

    /**
     * Clear Tse Admin Record a Decision Interface data from caseData.
     * @param caseData in which the case details are extracted from
     */
    public void clearTseAdminDataFromCaseData(CaseData caseData) {
        caseData.setTseAdminSelectApplication(null);
        caseData.setTseAdminTableMarkUp(null);
        caseData.setTseAdminEnterNotificationTitle(null);
        caseData.setTseAdminDecision(null);
        caseData.setTseAdminDecisionDetails(null);
        caseData.setTseAdminTypeOfDecision(null);
        caseData.setTseAdminIsResponseRequired(null);
        caseData.setTseAdminSelectPartyRespond(null);
        caseData.setTseAdminAdditionalInformation(null);
        caseData.setTseAdminResponseRequiredYesDoc(null);
        caseData.setTseAdminResponseRequiredNoDoc(null);
        caseData.setTseAdminDecisionMadeBy(null);
        caseData.setTseAdminDecisionMadeByFullName(null);
        caseData.setTseAdminSelectPartyNotify(null);
    }

}
