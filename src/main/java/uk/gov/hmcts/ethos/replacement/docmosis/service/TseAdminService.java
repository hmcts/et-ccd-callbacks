package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEAdminEmailRecipientsData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.formatAdminReply;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.formatRespondentReplyForDecision;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getSelectedApplicationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"squid:S1192", "PMD.AvoidInstantiatingObjectsInLoops"})
public class TseAdminService {

    @Value("${tse.admin.record-a-decision.claimant.template.id}")
    private String emailToClaimantTemplateId;
    @Value("${tse.admin.record-a-decision.respondent.template.id}")
    private String emailToRespondentTemplateId;

    private final EmailService emailService;
    private final DocumentManagementService documentManagementService;

    private static final String RESPONSE_APP_DETAILS = "| | |\r\n"
            + "|--|--|\r\n"
            + "|%s application | %s|\r\n"
            + "|Application date | %s|\r\n"
            + "|%s | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "\r\n";

    private static final String CLOSE_APP_DETAILS = "| | |\r\n"
        + "|--|--|\r\n"
        + "|Applicant | %s|\r\n"
        + "|Type of application | %s|\r\n"
        + "|Application date | %s|\r\n"
        + "|What do you want to tell or ask the tribunal? | %s|\r\n"
        + "|Supporting material | %s|\r\n"
        + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
        + "\r\n";

    private static final String CLOSE_APP_DECISION_DETAILS = "|Decision | |\r\n"
        + "|--|--|\r\n"
        + "|Notification | %s|\r\n"
        + "|Decision | %s|\r\n"
        + "|Date | %s|\r\n"
        + "|Sent by | %s|\r\n"
        + "|Type of decision | %s|\r\n"
        + "|Additional information | %s|\r\n"
        + "|Description | %s|\r\n"
        + "|Document | %s|\r\n"
        + "|Decision made by | %s|\r\n"
        + "|Name | %s|\r\n"
        + "|Sent to | %s|\r\n"
        + "\r\n";

    private static final String BOTH = "Both parties";
    private static final String CLAIMANT_ONLY = "Claimant only";
    private static final String RESPONDENT_ONLY = "Respondent only";
    private static final String STRING_BR = "<br>";
    private static final String APPLICATION_QUESTION = "Give details";

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public void initialTseAdminTableMarkUp(CaseData caseData, String authToken) {
        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {
            caseData.setTseAdminTableMarkUp(initialTseAdminAppDetails(applicationTypeItem.getValue(), authToken)
                + initialTseAdminRespondDetails(applicationTypeItem.getValue(), authToken));
        }
    }

    private String initialTseAdminAppDetails(GenericTseApplicationType applicationType, String authToken) {
        return String.format(
            RESPONSE_APP_DETAILS,
            applicationType.getApplicant(),
            applicationType.getType(),
            applicationType.getDate(),
            APPLICATION_QUESTION,
            applicationType.getDetails(),
            documentManagementService.displayDocNameTypeSizeLink(applicationType.getDocumentUpload(), authToken)
        );
    }

    private String initialTseAdminRespondDetails(GenericTseApplicationType applicationType, String authToken) {
        if (applicationType.getRespondCollection() == null) {
            return "";
        }
        IntWrapper respondCount = new IntWrapper(0);
        return applicationType.getRespondCollection().stream()
            .map(replyItem ->
                "Admin".equals(replyItem.getValue().getFrom())
                    ? formatAdminReply(
                        replyItem.getValue(),
                        respondCount.incrementAndReturnValue(),
                        documentManagementService.displayDocNameTypeSizeLink(
                            replyItem.getValue().getAddDocument(), authToken))
                    : formatRespondentReplyForDecision(
                        replyItem.getValue(),
                        respondCount.incrementAndReturnValue(),
                        populateListDocWithInfoAndLink(replyItem.getValue().getSupportingMaterial(), authToken)))
            .collect(Collectors.joining(""));
    }

    private String populateListDocWithInfoAndLink(List<DocumentTypeItem> supportingMaterial, String authToken) {
        if (supportingMaterial == null) {
            return "";
        }
        return supportingMaterial.stream()
            .map(documentTypeItem ->
                documentManagementService.displayDocNameTypeSizeLink(
                    documentTypeItem.getValue().getUploadedDocument(), authToken) + STRING_BR)
            .collect(Collectors.joining());
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

    private UploadedDocumentType getResponseRequiredDocYesOrNo(CaseData caseData) {
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
                || BOTH.equals(caseData.getTseAdminSelectPartyNotify())) {
            TSEAdminEmailRecipientsData respondentDetails;
            for (RespondentSumTypeItem respondentSumTypeItem: caseData.getRespondentCollection()) {
                if (respondentSumTypeItem.getValue().getRespondentEmail() != null) {
                    respondentDetails =
                        new TSEAdminEmailRecipientsData(
                            emailToRespondentTemplateId,
                            respondentSumTypeItem.getValue().getRespondentEmail());
                    respondentDetails.setRecipientName(respondentSumTypeItem.getValue().getRespondentName());

                    emailsToSend.add(respondentDetails);
                }
            }
        }

        // if claimant only or both parties: send Claimant Decision Email
        if (CLAIMANT_ONLY.equals(caseData.getTseAdminSelectPartyNotify())
                || BOTH.equals(caseData.getTseAdminSelectPartyNotify())) {
            String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
            String claimantName = caseData.getClaimantIndType().getClaimantFirstNames()
                + " " + caseData.getClaimantIndType().getClaimantLastName();

            if (claimantEmail != null) {
                TSEAdminEmailRecipientsData claimantDetails =
                    new TSEAdminEmailRecipientsData(emailToClaimantTemplateId, claimantEmail);
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
        personalisation.put("caseNumber", caseNumber);
        personalisation.put("caseId", caseId);
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

    // TODO -
    //  display decision's document link (currently not sure if it is a list or just a single decision to display)
    //  display responses from admin and/or claimant/respondent
    public String generateCloseApplicationDetailsMarkdown(CaseData caseData, String authToken) {
        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        String decisionsMarkdown = "";
        if (applicationTypeItem.getValue().getAdminDecision() != null) {
            // Multiple decisions can be made for the same application but we are only showing the last one for now
            Optional<String> decisionsMarkdownResult = applicationTypeItem.getValue().getAdminDecision()
                .stream()
                .reduce((first, second) -> second)
                .map(d -> String.format(CLOSE_APP_DECISION_DETAILS,
                    Optional.ofNullable(d.getValue().getEnterNotificationTitle()).orElse(""),
                    d.getValue().getDecision(),
                    d.getValue().getDate(),
                    "Tribunal",
                    d.getValue().getTypeOfDecision(),
                    Optional.ofNullable(d.getValue().getAdditionalInformation()).orElse(""),
                    Optional.ofNullable(d.getValue().getDecisionDetails()).orElse(""),
                    getDecisionDocumentLink(d.getValue(), authToken),
                    d.getValue().getDecisionMadeBy(),
                    d.getValue().getDecisionMadeByFullName(),
                    d.getValue().getSelectPartyNotify()));

            if (decisionsMarkdownResult.isPresent()) {
                decisionsMarkdown = decisionsMarkdownResult.get();
            }
        }

        return String.format(
            CLOSE_APP_DETAILS,
            applicationTypeItem.getValue().getApplicant(),
            applicationTypeItem.getValue().getType(),
            applicationTypeItem.getValue().getDate(),
            applicationTypeItem.getValue().getDetails(),
            getApplicationDocumentLink(applicationTypeItem, authToken),
            applicationTypeItem.getValue().getCopyToOtherPartyYesOrNo()
        )
            + initialTseAdminRespondDetails(applicationTypeItem.getValue(), authToken)
            + decisionsMarkdown;

    }

    private String getDecisionDocumentLink(TseAdminRecordDecisionType decisionType, String authToken) {
        if (decisionType.getResponseRequiredDoc() == null) {
            return "";
        }

        return documentManagementService
            .displayDocNameTypeSizeLink(decisionType.getResponseRequiredDoc(), authToken);
    }

    private String getApplicationDocumentLink(GenericTseApplicationTypeItem applicationTypeItem, String authToken) {
        if (applicationTypeItem.getValue().getDocumentUpload() == null) {
            return "";
        }

        return documentManagementService
            .displayDocNameTypeSizeLink(applicationTypeItem.getValue().getDocumentUpload(), authToken);
    }

}
