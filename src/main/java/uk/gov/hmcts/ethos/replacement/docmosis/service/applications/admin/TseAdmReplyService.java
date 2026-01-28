package uk.gov.hmcts.ethos.replacement.docmosis.service.applications.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.applications.TseAdmReplyHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.applications.TseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEAdminEmailRecipientsData;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEITHER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TSE_ADMIN_CORRESPONDENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.setDocumentNumbers;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isRepresentedClaimantWithMyHmctsCase;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.isPartyToNotifyMismatch;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.applications.TseHelper.getAdminSelectedApplicationType;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.TSE_ADMIN_REPLY;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdmReplyService {
    private final DocumentManagementService documentManagementService;
    private final EmailService emailService;
    private final TornadoService tornadoService;
    private final TseService tseService;
    private final FeatureToggleService featureToggleService;
    private final CaseAccessService caseAccessService;
    private final EmailNotificationService emailNotificationService;

    @Value("${template.tse.admin.reply.claimant}")
    private String tseAdminReplyClaimantTemplateId;
    @Value("${template.tse.admin.reply.respondent}")
    private String tseAdminReplyRespondentTemplateId;

    private static final String RESPONSE_REQUIRED =
        "The tribunal requires some information from you about an application.";
    private static final String RESPONSE_NOT_REQUIRED =
        "You have a new message from HMCTS about a claim made to an employment tribunal.";
    private static final String ERROR_MSG_ADD_DOC_MISSING = "Select or fill the required Add document field";
    private static final String ERROR_MSG_PARTY_TO_NOTIFY_MUST_INCLUDE_SELECTED =
        "Select the party or parties to notify must include the party or parties who must respond";

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
        if (isPartyToNotifyMismatch(
            caseData.getTseAdmReplyRequestSelectPartyRespond(),
            caseData.getTseAdmReplySelectPartyNotify()
        )) {
            errors.add(ERROR_MSG_PARTY_TO_NOTIFY_MUST_INCLUDE_SELECTED);
        }
        if (isPartyToNotifyMismatch(
            caseData.getTseAdmReplyCmoSelectPartyRespond(),
            caseData.getTseAdmReplySelectPartyNotify()
        )) {
            errors.add(ERROR_MSG_PARTY_TO_NOTIFY_MUST_INCLUDE_SELECTED);
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
        if (isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationType applicationType = getAdminSelectedApplicationType(caseData);
        if (applicationType == null) {
            return;
        }

        // Update ApplicationState for Claimant Citizen UI
        if (isResponseRequired(caseData, CLAIMANT_TITLE)) {
            applicationType.setApplicationState(NOT_STARTED_YET);
        } else if (isResponseRequired(caseData, RESPONDENT_TITLE)) {
            applicationType.setApplicationState(UPDATED);
        }

        // Update ApplicationState for Respondent Citizen UI
        if (isResponseRequired(caseData, RESPONDENT_TITLE)) {
            TseHelper.setRespondentApplicationState(applicationType, NOT_STARTED_YET);
        } else if (isResponseRequired(caseData, CLAIMANT_TITLE)) {
            TseHelper.setRespondentApplicationState(applicationType, UPDATED);
        }
    }

    /**
     * Save Tse Admin Record a Decision data to the application object.
     * @param caseData in which the case details are extracted from
     */
    public void saveTseAdmReplyDataFromCaseData(CaseData caseData) {
        if (isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationType applicationType = getAdminSelectedApplicationType(caseData);
        if (applicationType == null) {
            return;
        }

        if (isEmpty(applicationType.getRespondCollection())) {
            applicationType.setRespondCollection(new ArrayList<>());
        }

        String tseAdmReplyRequestSelectPartyRespond = caseData.getTseAdmReplyRequestSelectPartyRespond();
        String tseAdmReplyCmoSelectPartyRespond = caseData.getTseAdmReplyCmoSelectPartyRespond();

        TseRespondType response = TseRespondType.builder()
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
                .build();

        applicationType.getRespondCollection().add(TseRespondTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(response)
                .build());

        if (featureToggleService.isWorkAllocationEnabled()) {
            response.setDateTime(Helper.getCurrentDateTime()); // for Work Allocation DMNs
            response.setApplicationType(applicationType.getType()); // for Work Allocation DMNs
        }

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
    public Map<String, Object> sendNotifyEmailsToClaimant(String caseId, CaseData caseData, String userToken) {
        Map<String, Object> personalisationHashMap = new ConcurrentHashMap<>();
        if (RESPONDENT_ONLY.equals(caseData.getTseAdmReplySelectPartyNotify())) {
            return personalisationHashMap;
        }

        String caseNumber = caseData.getEthosCaseReference();
        List<TSEAdminEmailRecipientsData> emailsToSend = new ArrayList<>();
        collectClaimants(caseData, emailsToSend);
        byte[] uploadedDocContent =  getUploadedDocumentContent(caseData, userToken);
        for (final TSEAdminEmailRecipientsData emailRecipient : emailsToSend) {
            personalisationHashMap = buildPersonalisation(caseNumber, caseId,
                    emailRecipient.getCustomisedText(), uploadedDocContent);
            if (isRepresentedClaimantWithMyHmctsCase(caseData)) {
                personalisationHashMap.put(LINK_TO_CITIZEN_HUB, emailService.getExuiCaseLink(caseId));
            }
            emailService.sendEmail(emailRecipient.getRecipientTemplate(), emailRecipient.getRecipientEmail(),
                    personalisationHashMap);
        }

        return personalisationHashMap;
    }

    private byte[] getUploadedDocumentContent(CaseData caseData, String userToken) {
        DocumentTypeItem docItem = caseData.getDocumentCollection().getLast();
        byte[] uploadedDocContent = null;
        try {
            UploadedDocument uploadedDoc = documentManagementService.downloadFile(userToken,
                    docItem.getValue().getUploadedDocument().getDocumentBinaryUrl());
            if (uploadedDoc != null) {
                uploadedDocContent = uploadedDoc.getContent().getInputStream().readAllBytes();
            }
        } catch (IOException ioException) {
            log.error("Source: TseAdmReplyService, method - getUploadedDocumentContent. "
                    + "Error downloading uploaded document binary content: " + ioException.getMessage());
        }

        return uploadedDocContent;
    }

    /**
     * Send notify emails to Respondents (or LR if they are assigned).
     */
    public Map<String, Object> sendNotifyEmailsToRespondents(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        if (CLAIMANT_ONLY.equals(caseData.getTseAdmReplySelectPartyNotify())) {
            return Collections.emptyMap();
        }

        String customisedText = isResponseRequired(caseData, RESPONDENT_TITLE)
                ? RESPONSE_REQUIRED : RESPONSE_NOT_REQUIRED;

        Map<String, Object> personalisationHashMap;
        personalisationHashMap = buildPersonalisation(caseData.getEthosCaseReference(),
                caseDetails.getCaseId(), customisedText, getUploadedDocumentContent(caseData, userToken));

        String caseId = caseDetails.getCaseId();
        List<CaseUserAssignment> caseUserAssignments = caseAccessService.getCaseUserAssignmentsById(caseId);
        emailNotificationService.getRespondentsAndRepsEmailAddresses(caseData, caseUserAssignments)
                .forEach((emailAddress, respondentId) ->
                        sendRespondentEmail(personalisationHashMap, caseId, emailAddress, respondentId));

        return personalisationHashMap;
    }

    private void sendRespondentEmail(Map<String, Object> emailData, String caseId,
                                     String emailAddress, String respondentId) {
        if (StringUtils.isNotBlank(respondentId)) {
            emailData.put(LINK_TO_EXUI, emailService.getSyrCaseLink(caseId, respondentId));
        }
        emailService.sendEmail(tseAdminReplyRespondentTemplateId, emailAddress, emailData);
    }

    private void collectClaimants(CaseData caseData, List<TSEAdminEmailRecipientsData> emailsToSend) {
        // if claimant only or both parties: send Claimant Reply Email
        if (CLAIMANT_ONLY.equals(caseData.getTseAdmReplySelectPartyNotify())
            || BOTH_PARTIES.equalsIgnoreCase(caseData.getTseAdmReplySelectPartyNotify())) {
            String claimantEmail = Optional.ofNullable(caseData.getClaimantType().getClaimantEmailAddress())
                    .orElseGet(() -> Optional.ofNullable(caseData.getRepresentativeClaimantType())
                            .map(RepresentedTypeC::getRepresentativeEmailAddress)
                            .orElse(null));

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
            && (BOTH_PARTIES.equalsIgnoreCase(caseData.getTseAdmReplyCmoSelectPartyRespond())
            || party.equals(caseData.getTseAdmReplyCmoSelectPartyRespond()));
    }

    private static boolean isRequestAndResponseRequiredFromParty(CaseData caseData, String party) {
        return YES.equals(caseData.getTseAdmReplyRequestIsResponseRequired())
            && (BOTH_PARTIES.equalsIgnoreCase(caseData.getTseAdmReplyRequestSelectPartyRespond())
            || party.equals(caseData.getTseAdmReplyRequestSelectPartyRespond()));
    }

    private Map<String, Object> buildPersonalisation(String caseNumber, String caseId, String customText,
                                                     byte[] adminReplyPdfDocContent) {
        Map<String, Object> personalisation = new ConcurrentHashMap<>();
        try {
            personalisation.put(CASE_NUMBER, caseNumber);
            personalisation.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseId));
            personalisation.put(LINK_TO_EXUI, emailService.getExuiCaseLink(caseId));
            personalisation.put("customisedText", customText);
            personalisation.put("linkToUploadedPdfDocumentBinary", prepareUpload(adminReplyPdfDocContent));
        } catch (NotificationClientException notificationClientEx) {
            log.error("Attaching pdf content to email failed: " + notificationClientEx.getMessage());
        }
        return personalisation;
    }

    /**
     * Creates a PDF copy of the TSE application Response from Admin/Caseworker and
     * adds it to the case doc collection.
     *
     * @param caseDetails details of the case from which required fields are extracted
     * @param userToken autherisation token to use for generating an event document
     */
    public void addTseAdmReplyPdfToDocCollection(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        if (isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        DocumentTypeItem docItem = TseAdmReplyHelper.getDocumentTypeItem(documentManagementService, tornadoService,
                caseDetails, userToken, TSE_ADMIN_REPLY, TSE_ADMIN_CORRESPONDENCE);
        caseData.getDocumentCollection().add(docItem);
        setDocumentNumbers(caseData);
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
