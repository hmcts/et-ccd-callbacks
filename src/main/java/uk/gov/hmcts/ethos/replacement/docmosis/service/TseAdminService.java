package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecision;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEAdminEmailRecipientsData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.createTwoColumnTable;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getAdminSelectedApplicationType;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdminService {
    public static final String NOT_VIEWED_YET = "notViewedYet";

    private final EmailService emailService;
    private final TseService tseService;
    @Value("${template.tse.admin.record-a-decision.claimant}")
    private String tseAdminRecordClaimantTemplateId;
    @Value("${template.tse.admin.record-a-decision.respondent}")
    private String tseAdminRecordRespondentTemplateId;

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public void initialTseAdminTableMarkUp(CaseData caseData, String authToken) {
        GenericTseApplicationType applicationType = getAdminSelectedApplicationType(caseData);
        if (applicationType == null) {
            return;
        }
        List<String[]> applicationTable = tseService.getApplicationDetailsRows(applicationType, authToken, true);
        List<String[]> applicationResponses = tseService.formatApplicationResponses(applicationType, authToken, false);
        caseData.setTseAdminTableMarkUp(createTwoColumnTable(new String[]{"Application", ""},
            Stream.of(applicationTable, applicationResponses).flatMap(Collection::stream).toList()));
    }

    /**
     * Save Tse Admin Record a Decision data to the application object.
     * This includes the new application state.
     * @param caseData in which the case details are extracted from
     */
    public void saveTseAdminDataFromCaseData(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationType applicationType = getAdminSelectedApplicationType(caseData);
        if (applicationType == null) {
            return;
        }

        applicationType.setApplicationState(NOT_VIEWED_YET);

        if (CollectionUtils.isEmpty(applicationType.getAdminDecision())) {
            applicationType.setAdminDecision(new ListTypeItem<>());
        }

        TseAdminRecordDecision decision = TseAdminRecordDecision.builder()
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
            .build();

        applicationType.getAdminDecision().add(
            TypeItem.<TseAdminRecordDecision>builder()
                .id(UUID.randomUUID().toString())
                .value(decision)
                .build()
        );
    }

    private ListTypeItem<DocumentType> getResponseRequiredDocYesOrNo(CaseData caseData) {
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
    public void sendEmailToClaimant(String caseId, CaseData caseData) {
        String caseNumber = caseData.getEthosCaseReference();

        List<TSEAdminEmailRecipientsData> emailsToSend = new ArrayList<>();

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

    /**
     * Send notify emails to Respondents (or LR if they are assigned).
     */
    public void sendNotifyEmailsToRespondents(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        if (CLAIMANT_ONLY.equals(caseData.getTseAdmReplySelectPartyNotify())) {
            return;
        }

        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        respondents.forEach(obj -> sendRespondentEmail(caseDetails, obj.getValue()));
    }

    private void sendRespondentEmail(CaseDetails caseDetails, RespondentSumType respondent) {
        CaseData caseData = caseDetails.getCaseData();

        String respondentEmail = NotificationHelper.getEmailAddressForRespondent(caseData, respondent);
        if (isNullOrEmpty(respondentEmail)) {
            return;
        }

        Map<String, String> personalisation = buildPersonalisation(caseData.getEthosCaseReference(),
                caseDetails.getCaseId(), respondent.getRespondentName());

        emailService.sendEmail(tseAdminRecordRespondentTemplateId, respondentEmail, personalisation);
    }

    private Map<String, String> buildPersonalisation(String caseNumber, String caseId, String recipientName) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseNumber);
        personalisation.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseId));
        personalisation.put(LINK_TO_EXUI, emailService.getExuiCaseLink(caseId));
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
