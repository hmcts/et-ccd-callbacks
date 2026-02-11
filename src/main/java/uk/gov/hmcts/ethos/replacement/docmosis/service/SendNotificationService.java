
package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.base.Strings;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SEND_NOTIFICATION_RESPONSE_REQUIRED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EXUI_HEARING_DOCUMENTS_LINK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.HEARING_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.createLinkForUploadedDocument;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isRepresentedClaimantWithMyHmctsCase;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.NOTIFICATION_SUMMARY_PDF;

@Service("sendNotificationService")
@RequiredArgsConstructor
@Slf4j
public class SendNotificationService {
    public static final String EMPLOYER_CONTRACT_CLAIM = "Employer Contract Claim";
    public static final String CASE_MANAGEMENT_ORDERS_REQUESTS = "Case management orders / requests";
    public static final String NOTICE_OF_EMPLOYER_CONTRACT_CLAIM = "Notice of Employer Contract Claim";

    private final HearingSelectionService hearingSelectionService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;
    private final CaseAccessService caseAccessService;
    private final TornadoService tornadoService;
    private final EmailNotificationService emailNotificationService;

    private static final String EMAIL_ADDRESS = "emailAddress";
    @Value("${template.claimantSendNotification}")
    private String claimantSendNotificationTemplateId;
    @Value("${template.pse.claimant-rep.new-notification}")
    private String claimantRepSendNotificationTemplateId;
    @Value("${template.respondentSendNotification}")
    private String respondentSendNotificationTemplateId;
    @Value("${template.bundles.respondentSubmittedNotificationForClaimant}")
    private String bundlesSubmittedNotificationForClaimantTemplateId;
    @Value("${template.bundles.respondentSubmittedNotificationForTribunal}")
    private String bundlesSubmittedNotificationForTribunalTemplateId;

    private static final String BLANK_DOCUMENT_MARKDOWN = "| Document | | \r\n| Description | |";

    private static final String POPULATED_DOCUMENT_MARKDOWN = "| Document |%s|\r\n| Description |%s|";

    private static final String NOTIFICATION_DETAILS = """
            |  | |\r
            | --- | --- |\r
            | Subject | %1$s |\r
            | Notification | %2$s |\r
            | Hearing | %3$s |\r
            | Date Sent | %4$s |\r
            | Sent By | %5$s  |\r
            | Case management order or request | %6$s |\r
            | Response due | %7$s |\r
            | Party or parties to respond | %8$s |\r
            | Additional Information | %9$s |\r
             %10$s\r
            | Case management order made by | %11$s |\r
            | Name | %12$s |\r
            | Sent to | %8$s |\r
            """;

    public void populateHearingSelection(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(hearingSelectionService.getHearingSelectionSortedByDateTime(caseData));
        caseData.setSendNotificationSelectHearing(dynamicFixedListType);
    }

    public void createSendNotification(CaseData caseData) {

        if (caseData.getSendNotificationCollection() == null) {
            caseData.setSendNotificationCollection(new ArrayList<>());
        }
        SendNotificationType sendNotificationType = new SendNotificationType();
        sendNotificationType.setNumber(String.valueOf(getNextNotificationNumber(caseData)));
        sendNotificationType.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        sendNotificationType.setSendNotificationTitle(caseData.getSendNotificationTitle());
        sendNotificationType.setSendNotificationLetter(caseData.getSendNotificationLetter());
        sendNotificationType.setSendNotificationUploadDocument(caseData.getSendNotificationUploadDocument());
        sendNotificationType.setSendNotificationSubject(caseData.getSendNotificationSubject());
        sendNotificationType.setSendNotificationAdditionalInfo(caseData.getSendNotificationAdditionalInfo());
        sendNotificationType.setSendNotificationNotify(caseData.getSendNotificationNotify());
        sendNotificationType.setSendNotificationSelectHearing(caseData.getSendNotificationSelectHearing());
        sendNotificationType.setSendNotificationCaseManagement(caseData.getSendNotificationCaseManagement());
        sendNotificationType.setSendNotificationResponseTribunal(caseData.getSendNotificationResponseTribunal());
        sendNotificationType.setSendNotificationWhoCaseOrder(caseData.getSendNotificationWhoCaseOrder());
        sendNotificationType.setSendNotificationSelectParties(caseData.getSendNotificationSelectParties());
        sendNotificationType.setSendNotificationFullName(caseData.getSendNotificationFullName());
        sendNotificationType.setSendNotificationFullName2(caseData.getSendNotificationFullName2());
        sendNotificationType.setSendNotificationDecision(caseData.getSendNotificationDecision());
        sendNotificationType.setSendNotificationDetails(caseData.getSendNotificationDetails());
        sendNotificationType.setSendNotificationRequestMadeBy(caseData.getSendNotificationRequestMadeBy());
        sendNotificationType.setSendNotificationEccQuestion(caseData.getSendNotificationEccQuestion());
        sendNotificationType.setSendNotificationWhoMadeJudgement(caseData.getSendNotificationWhoMadeJudgement());
        sendNotificationType.setNotificationSentFrom(caseData.getNotificationSentFrom());

        sendNotificationType.setNotificationState(NOT_VIEWED_YET);

        setStatusForCitizenHub(caseData, sendNotificationType);

        sendNotificationType.setSendNotificationSentBy(TRIBUNAL);
        sendNotificationType.setSendNotificationSubjectString(
                String.join(", ", caseData.getSendNotificationSubject())
        );
        sendNotificationType.setSendNotificationResponsesCount("0");
        sendNotificationType.setSendNotificationResponseTribunalTable(
                NO.equals(caseData.getSendNotificationResponseTribunal()) ? NO : YES
        );

        SendNotificationTypeItem sendNotificationTypeItem = new SendNotificationTypeItem();
        sendNotificationTypeItem.setId(UUID.randomUUID().toString());
        sendNotificationTypeItem.setValue(sendNotificationType);
        caseData.getSendNotificationCollection().add(sendNotificationTypeItem);
    }

    private static void setStatusForCitizenHub(CaseData caseData, SendNotificationType sendNotificationType) {
        if (SEND_NOTIFICATION_RESPONSE_REQUIRED.equals(caseData.getSendNotificationResponseTribunal())
                && !RESPONDENT_ONLY.equals(caseData.getSendNotificationSelectParties())) {
            sendNotificationType.setNotificationState(NOT_STARTED_YET);
        } else {
            sendNotificationType.setNotificationState(NOT_VIEWED_YET);
        }
    }

    private static int getNextNotificationNumber(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            return 1;
        }
        return caseData.getSendNotificationCollection().size() + 1;
    }

    public void clearSendNotificationFields(CaseData caseData) {
        caseData.setSendNotificationTitle(null);
        caseData.setSendNotificationLetter(null);
        caseData.setSendNotificationUploadDocument(null);
        caseData.setSendNotificationSubject(null);
        caseData.setSendNotificationAdditionalInfo(null);
        caseData.setSendNotificationNotify(null);
        caseData.setSendNotificationSelectHearing(null);
        caseData.setSendNotificationCaseManagement(null);
        caseData.setSendNotificationResponseTribunal(null);
        caseData.setSendNotificationWhoCaseOrder(null);
        caseData.setSendNotificationSelectParties(null);
        caseData.setSendNotificationFullName(null);
        caseData.setSendNotificationFullName2(null);
        caseData.setSendNotificationDecision(null);
        caseData.setSendNotificationDetails(null);
        caseData.setSendNotificationRequestMadeBy(null);
        caseData.setSendNotificationEccQuestion(null);
        caseData.setSendNotificationWhoCaseOrder(null);
        caseData.setNotificationSentFrom(null);
    }

    /**
     * Gets a list of notifications in the selected format.
     *
     * @param caseData data to get notifications from
     * @param format   lambda contains details for the formatting
     * @return A list of notifications
     */
    public List<DynamicValueType> getSendNotificationSelection(CaseData caseData,
                                                               Function<SendNotificationTypeItem, String> format) {
        List<SendNotificationTypeItem> sendNotificationTypeItemList = caseData.getSendNotificationCollection();
        if (CollectionUtils.isEmpty(sendNotificationTypeItemList)) {
            return new ArrayList<>();
        }
        return sendNotificationTypeItemList.stream()
                .map(sendNotificationType -> DynamicValueType.create(sendNotificationType.getId(),
                        format.apply(sendNotificationType)))
                .toList();
    }

    /**
     * Sends notification emails for the claimant and/or respondent(s) based on the radio list from the
     * sendNotification event.
     *
     * @param caseDetails Details of the case
     */
    public void sendNotifyEmails(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        boolean ecc = featureToggleService.isEccEnabled();

        if (caseData.getSendNotificationSubject().contains(EMPLOYER_CONTRACT_CLAIM)
                && !ecc) {
            log.warn("No emails sent. ECC feature flag is not enabled");
            return;
        }

        // Send notification to the claimant
        String caseId = caseDetails.getCaseId();
        List<CaseUserAssignment> caseUserAssignments = caseAccessService.getCaseUserAssignmentsById(caseId);

        if (caseUserAssignments == null || caseUserAssignments.isEmpty()) {
            log.warn("In SendNotificationService : No case user assignments found for caseId {}",
                    caseDetails.getCaseId());
            return;
        }

        if (!RESPONDENT_ONLY.equals(caseData.getSendNotificationNotify())) {
            // If represented, send notification to claimant representative Only
            Map<String, String> personalisation;
            if (isRepresentedClaimantWithMyHmctsCase(caseDetails.getCaseData())) {
                String linkEnv = emailService.getClaimantRepExuiCaseNotificationsLink(
                        caseDetails.getCaseId());
                personalisation = buildPersonalisation(caseDetails, linkEnv);
                // with shared case there's going to be multiple claimant representatives
                emailNotificationService.getCaseClaimantSolicitorEmails(caseUserAssignments).stream()
                        .filter(email -> email != null && !email.isEmpty())
                        .forEach(email -> emailService.sendEmail(
                                respondentSendNotificationTemplateId,
                                email,
                                personalisation));
            } else {
                // If not represented, send notification to the claimant
                String claimantEmailAddress = caseData.getClaimantType().getClaimantEmailAddress();
                // Send notification to the claimant only if the claimant is a system user
                if (!isClaimantNonSystemUser(caseData) && !isNullOrEmpty(claimantEmailAddress)) {
                    emailService.sendEmail(claimantSendNotificationTemplateId, claimantEmailAddress,
                            buildPersonalisation(caseDetails, emailService.getCitizenCaseLink(caseId)));
                }
            }
        }

        // Send notification to the respondent(s)
        if (!CLAIMANT_ONLY.equals(caseData.getSendNotificationNotify())) {
            Map<String, String> personalisation = buildPersonalisation(caseDetails,
                    emailService.getExuiCaseLink(caseId));
            List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
            sendRespondentEmailHearingOther(caseData, personalisation, respondents, caseUserAssignments);
        }
    }

    public Optional<SendNotificationTypeItem> getSendNotification(CaseData caseData) {
        return caseData.getSendNotificationCollection()
                .stream()
                .filter(s -> s.getId().equals(caseData.getSelectNotificationDropdown().getSelectedCode()))
                .findFirst();
    }

    private String getSendNotificationSingleDocumentMarkdown(DocumentType uploadedDocumentType) {
        String document = BLANK_DOCUMENT_MARKDOWN;
        if (uploadedDocumentType != null) {
            document = String.format(POPULATED_DOCUMENT_MARKDOWN,
                    createLinkForUploadedDocument(uploadedDocumentType.getUploadedDocument()),
                    uploadedDocumentType.getShortDescription());
        }
        return document;
    }

    private String getSendNotificationDocumentsMarkdown(SendNotificationType sendNotification) {
        if (sendNotification.getSendNotificationUploadDocument() == null) {
            return BLANK_DOCUMENT_MARKDOWN;
        }
        List<String> documents = sendNotification.getSendNotificationUploadDocument().stream()
                .map(documentTypeItem ->
                        getSendNotificationSingleDocumentMarkdown(documentTypeItem.getValue()))
                .toList();
        return String.join("\r\n", documents);
    }

    public String getSendNotificationMarkDown(SendNotificationType sendNotification) {

        Optional<DynamicFixedListType> sendNotificationSelectHearing
                = Optional.ofNullable(sendNotification.getSendNotificationSelectHearing());

        return String.format(NOTIFICATION_DETAILS, Strings.nullToEmpty(sendNotification.getSendNotificationTitle()),
                Strings.nullToEmpty(String.join(", ", sendNotification.getSendNotificationSubject())),
                sendNotificationSelectHearing.isPresent() ? sendNotificationSelectHearing.get().getSelectedLabel() : "",
                sendNotification.getDate(),
                TRIBUNAL,
                Strings.nullToEmpty(sendNotification.getSendNotificationCaseManagement()),
                Strings.nullToEmpty(sendNotification.getSendNotificationResponseTribunal()),
                Strings.nullToEmpty(sendNotification.getSendNotificationNotify()),
                Strings.nullToEmpty(sendNotification.getSendNotificationAdditionalInfo()),
                getSendNotificationDocumentsMarkdown(sendNotification),
                Strings.nullToEmpty(sendNotification.getSendNotificationWhoCaseOrder()),
                Strings.nullToEmpty(sendNotification.getSendNotificationFullName()));

    }

    private void sendRespondentEmailHearingOther(CaseData caseData, Map<String, String> emailData,
                                                 List<RespondentSumTypeItem> respondents,
                                                 List<CaseUserAssignment> assignments) {

        Set<String> emails = new HashSet<>();
        respondents.stream()
                .map(respondent ->
                        NotificationHelper.getEmailAddressForRespondent(caseData, respondent.getValue()))
                .filter(email -> email != null && !email.isEmpty())
                .forEach(emails::add);

        emailNotificationService.getRespondentSolicitorEmails(assignments).stream()
                .filter(email -> email != null && !email.isEmpty())
                .forEach(emails::add);
        emails.forEach(email ->
                emailService.sendEmail(respondentSendNotificationTemplateId, email, emailData));
    }

    private Map<String, String> buildPersonalisation(CaseDetails caseDetails, String envUrl) {
        return Map.of(CASE_NUMBER, caseDetails.getCaseData().getEthosCaseReference(), "sendNotificationTitle",
                caseDetails.getCaseData().getSendNotificationTitle(), "environmentUrl", envUrl,
                CASE_ID, caseDetails.getCaseId());
    }

    public void notify(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();
        Map<String, String> emailData = getEmailData(caseData, caseId);
        boolean doesClaimantHaveEmail = isNotEmpty(caseData.getClaimantType())
                                        && isNotEmpty(caseData.getClaimantType().getClaimantEmailAddress());

        if (doesClaimantHaveEmail && !isClaimantNonSystemUser(caseData)) {
            emailService.sendEmail(bundlesSubmittedNotificationForClaimantTemplateId,
                    caseDetails.getCaseData()
                            .getClaimantType().getClaimantEmailAddress(),
                    emailData
            );
        }

        emailData.remove(LINK_TO_CITIZEN_HUB);
        emailData.put(EXUI_HEARING_DOCUMENTS_LINK, emailService.getExuiHearingDocumentsLink(caseId));
        emailService.sendEmail(bundlesSubmittedNotificationForTribunalTemplateId,
                caseDetails.getCaseData().getTribunalCorrespondenceEmail(),
                emailData
        );
    }

    @NotNull
    private Map<String, String> getEmailData(CaseData caseData, String caseId) {
        Map<String, String> emailData = new ConcurrentHashMap<>();
        emailData.put(CLAIMANT, caseData.getClaimant());
        emailData.put(CASE_NUMBER, caseData.getEthosCaseReference());
        emailData.put(RESPONDENT_NAMES, caseData.getRespondent());
        emailData.put(HEARING_DATE, caseData.getTargetHearingDate());
        emailData.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseId));
        return emailData;
    }

    public DocumentInfo createNotificationSummary(CaseData caseData, String userToken, String caseTypeId) {
        try {
            DocumentInfo documentInfo = tornadoService.generateEventDocument(caseData, userToken,
                    caseTypeId, NOTIFICATION_SUMMARY_PDF);
            // Show the custom name of the document in the UI
            documentInfo.setMarkUp(documentInfo.getMarkUp().replace("Document", documentInfo.getDescription()));
            return documentInfo;
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    public void createBfAction(CaseData caseData) {
        boolean subjectContainsEcc = caseData.getSendNotificationSubject() != null
            && caseData.getSendNotificationSubject().contains(EMPLOYER_CONTRACT_CLAIM);
        
        boolean eccQuestionMatches =
                NOTICE_OF_EMPLOYER_CONTRACT_CLAIM.equals(caseData.getSendNotificationEccQuestion());
        
        if (!subjectContainsEcc || !eccQuestionMatches) {
            return;
        }

        BFActionType bfActionType = new BFActionType();
        bfActionType.setLetters(NO);
        bfActionType.setDateEntered(LocalDate.now().toString());
        bfActionType.setCwActions("Other action");
        bfActionType.setAllActions("ECC served");
        bfActionType.setBfDate(LocalDate.now().plusDays(29).toString());
        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setId(UUID.randomUUID().toString());
        bfActionTypeItem.setValue(bfActionType);

        if (CollectionUtils.isEmpty(caseData.getBfActions())) {
            caseData.setBfActions(new ArrayList<>(Collections.singletonList(bfActionTypeItem)));
        } else {
            List<BFActionTypeItem> tmp = caseData.getBfActions();
            tmp.add(bfActionTypeItem);
            caseData.setBfActions(tmp);
        }
    }
}
