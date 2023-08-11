package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEAdminEmailRecipientsData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEITHER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getAdminSelectedApplicationType;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdmReplyService {
    private final EmailService emailService;

    private final TseService tseService;

    private final NotificationProperties notificationProperties;
    @Value("${tse.admin.reply.notify.claimant.template.id}")
    private String tseAdminReplyClaimantTemplateId;
    @Value("${tse.admin.reply.notify.respondent.template.id}")
    private String tseAdminReplyRespondentTemplateId;

    private static final String RESPONSE_REQUIRED =
        "The tribunal requires some information from you about an application.";
    private static final String RESPONSE_NOT_REQUIRED =
        "You have a new message from HMCTS about a claim made to an employment tribunal.";
    private static final String ERROR_MSG_ADD_DOC_MISSING = "Select or fill the required Add document field";

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     * @param authToken the caller's bearer token used to verify the caller
     */
    public String initialTseAdmReplyTableMarkUp(CaseData caseData, String authToken) {
        if (getAdminSelectedApplicationType(caseData) != null) {
            return tseService.formatViewApplication(caseData, authToken, false);
        }
        throw new NotFoundException("No selected application type item found.");
    }

    /**
     * Validate user input.
     * @param caseData in which the case details are extracted from
     * @return Error message list
     */
    public List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (addDocumentMissing(caseData)) {
            errors.add(ERROR_MSG_ADD_DOC_MISSING);
        }
        return errors;
    }

    private boolean addDocumentMissing(CaseData caseData) {
        List<GenericTypeItem<DocumentType>> documents = caseData.getTseAdmReplyAddDocument();
        if (NEITHER.equals(caseData.getTseAdmReplyIsCmoOrRequest()) || isNotEmpty(documents)) {
            return false;
        }

        return YES.equals(caseData.getTseAdmReplyCmoIsResponseRequired())
                || YES.equals(caseData.getTseAdmReplyRequestIsResponseRequired());
    }

    /**
     * Update state of application based on admin reply.
     * @param caseData in which the case details are extracted from
     */
    public void updateApplicationState(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationType applicationType = getAdminSelectedApplicationType(caseData);
        if (applicationType == null) {
            return;
        }

        if (isResponseRequired(caseData, CLAIMANT_TITLE)) {
            applicationType.setApplicationState(NOT_STARTED_YET);
        } else if (isResponseRequired(caseData, RESPONDENT_TITLE)) {
            applicationType.setApplicationState(UPDATED);
        }
    }

    /**
     * Save Tse Admin Record a Decision data to the application object.
     * @param caseData in which the case details are extracted from
     */
    public void saveTseAdmReplyDataFromCaseData(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationType applicationType = getAdminSelectedApplicationType(caseData);
        if (applicationType == null) {
            return;
        }

        if (CollectionUtils.isEmpty(applicationType.getRespondCollection())) {
            applicationType.setRespondCollection(new ArrayList<>());
        }

        String tseAdmReplyRequestSelectPartyRespond = caseData.getTseAdmReplyRequestSelectPartyRespond();
        String tseAdmReplyCmoSelectPartyRespond = caseData.getTseAdmReplyCmoSelectPartyRespond();

        applicationType.getRespondCollection().add(
            TseRespondTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(TseRespondType.builder()
                    .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                    .from(ADMIN)
                    .enterResponseTitle(caseData.getTseAdmReplyEnterResponseTitle())
                    .additionalInformation(caseData.getTseAdmReplyAdditionalInformation())
                    .addDocument(caseData.getTseAdmReplyAddDocument())
                    .isCmoOrRequest(caseData.getTseAdmReplyIsCmoOrRequest())
                    .cmoMadeBy(caseData.getTseAdmReplyCmoMadeBy())
                    .requestMadeBy(caseData.getTseAdmReplyRequestMadeBy())
                    .madeByFullName(defaultIfEmpty(caseData.getTseAdmReplyCmoEnterFullName(),
                        caseData.getTseAdmReplyRequestEnterFullName()))
                    .isResponseRequired(defaultIfEmpty(caseData.getTseAdmReplyCmoIsResponseRequired(),
                        caseData.getTseAdmReplyRequestIsResponseRequired()))
                    .selectPartyRespond(defaultIfEmpty(tseAdmReplyCmoSelectPartyRespond,
                        tseAdmReplyRequestSelectPartyRespond))
                    .selectPartyNotify(caseData.getTseAdmReplySelectPartyNotify())
                    .build()
            ).build());

        applicationType.setResponsesCount(String.valueOf(applicationType.getRespondCollection().size()));

        if (tseAdmReplyRequestSelectPartyRespond != null || tseAdmReplyCmoSelectPartyRespond != null) {
            switch (defaultIfEmpty(tseAdmReplyRequestSelectPartyRespond, tseAdmReplyCmoSelectPartyRespond)) {
                case RESPONDENT_TITLE -> applicationType.setRespondentResponseRequired(YES);
                case CLAIMANT_TITLE -> applicationType.setClaimantResponseRequired(YES);
                case BOTH_PARTIES -> {
                    applicationType.setRespondentResponseRequired(YES);
                    applicationType.setClaimantResponseRequired(YES);
                }
                default ->
                    throw new IllegalStateException("Illegal SelectPartyRespond values: "
                        + tseAdmReplyRequestSelectPartyRespond + " " + tseAdmReplyCmoSelectPartyRespond);
            }
        }
    }

    /**
     * Uses {@link EmailService} to generate an email.
     * @param caseId used in email link to case
     * @param caseData in which the case details are extracted from
     */
    public void sendNotifyEmailsToClaimant(String caseId, CaseData caseData) {
        String caseNumber = caseData.getEthosCaseReference();

        List<TSEAdminEmailRecipientsData> emailsToSend = new ArrayList<>();
        collectClaimants(caseData, emailsToSend);

        for (final TSEAdminEmailRecipientsData emailRecipient : emailsToSend) {
            emailService.sendEmail(
                emailRecipient.getRecipientTemplate(),
                emailRecipient.getRecipientEmail(),
                buildPersonalisation(caseNumber, caseId, emailRecipient.getCustomisedText()));
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

        String customisedText = isResponseRequired(caseData, RESPONDENT_TITLE)
                ? RESPONSE_REQUIRED : RESPONSE_NOT_REQUIRED;

        Map<String, String> personalisation = buildPersonalisation(caseData.getEthosCaseReference(),
                caseDetails.getCaseId(), customisedText);

        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        respondents.forEach(obj -> sendRespondentEmail(caseData, personalisation, obj.getValue()));
    }

    private void sendRespondentEmail(CaseData caseData, Map<String, String> emailData, RespondentSumType respondent) {
        String respondentEmail = NotificationHelper.getEmailAddressForRespondent(caseData, respondent);
        if (isNullOrEmpty(respondentEmail)) {
            return;
        }
        emailService.sendEmail(tseAdminReplyRespondentTemplateId, respondentEmail, emailData);
    }

    private void collectClaimants(CaseData caseData, List<TSEAdminEmailRecipientsData> emailsToSend) {
        // if claimant only or both parties: send Claimant Reply Email
        if (CLAIMANT_ONLY.equals(caseData.getTseAdmReplySelectPartyNotify())
            || BOTH_PARTIES.equals(caseData.getTseAdmReplySelectPartyNotify())) {
            String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();

            if (claimantEmail != null) {
                TSEAdminEmailRecipientsData claimantDetails =
                    new TSEAdminEmailRecipientsData(
                            tseAdminReplyClaimantTemplateId, claimantEmail);

                if (isResponseRequired(caseData, CLAIMANT_TITLE)) {
                    claimantDetails.setCustomisedText(RESPONSE_REQUIRED);
                } else {
                    claimantDetails.setCustomisedText(RESPONSE_NOT_REQUIRED);
                }

                emailsToSend.add(claimantDetails);
            }
        }
    }

    private boolean isResponseRequired(CaseData caseData, String party) {
        return CASE_MANAGEMENT_ORDER.equals(caseData.getTseAdmReplyIsCmoOrRequest())
            ? isCmoAndResponseRequiredFromParty(caseData, party)
            : isRequestAndResponseRequiredFromParty(caseData, party);
    }

    private static boolean isCmoAndResponseRequiredFromParty(CaseData caseData, String party) {
        return YES.equals(caseData.getTseAdmReplyCmoIsResponseRequired())
            && (BOTH_PARTIES.equals(caseData.getTseAdmReplyCmoSelectPartyRespond())
            || party.equals(caseData.getTseAdmReplyCmoSelectPartyRespond()));
    }

    private static boolean isRequestAndResponseRequiredFromParty(CaseData caseData, String party) {
        return YES.equals(caseData.getTseAdmReplyRequestIsResponseRequired())
            && (BOTH_PARTIES.equals(caseData.getTseAdmReplyRequestSelectPartyRespond())
            || party.equals(caseData.getTseAdmReplyRequestSelectPartyRespond()));
    }

    private Map<String, String> buildPersonalisation(String caseNumber, String caseId, String customText) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseNumber);
        personalisation.put(LINK_TO_CITIZEN_HUB, notificationProperties.getCitizenLinkWithCaseId(caseId));
        personalisation.put(LINK_TO_EXUI, notificationProperties.getExuiLinkWithCaseId(caseId));
        personalisation.put("customisedText", customText);
        return personalisation;
    }

    /**
     * Clear Tse Admin Record a Decision Interface data from caseData.
     * @param caseData in which the case details are extracted from
     */
    public void clearTseAdmReplyDataFromCaseData(CaseData caseData) {
        caseData.setTseAdminSelectApplication(null);
        caseData.setTseAdmReplyTableMarkUp(null);
        caseData.setTseAdmReplyEnterResponseTitle(null);
        caseData.setTseAdmReplyAdditionalInformation(null);
        caseData.setTseAdmReplyAddDocument(null);
        caseData.setTseAdmReplyIsCmoOrRequest(null);
        caseData.setTseAdmReplyCmoMadeBy(null);
        caseData.setTseAdmReplyRequestMadeBy(null);
        caseData.setTseAdmReplyCmoEnterFullName(null);
        caseData.setTseAdmReplyCmoIsResponseRequired(null);
        caseData.setTseAdmReplyRequestEnterFullName(null);
        caseData.setTseAdmReplyRequestIsResponseRequired(null);
        caseData.setTseAdmReplyCmoSelectPartyRespond(null);
        caseData.setTseAdmReplyRequestSelectPartyRespond(null);
        caseData.setTseAdmReplySelectPartyNotify(null);
    }
}
