package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TSEAdminEmailRecipientsData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REQUEST;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.displayCopyToOtherPartyYesOrNo;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.formatAdminReply;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.formatLegalRepReplyForReply;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getSelectedApplicationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveImports"})
public class TseAdmReplyService {

    @Value("${tse.admin.reply.notify.claimant.template.id}")
    private String emailToClaimantTemplateId;
    @Value("${tse.admin.reply.notify.respondent.template.id}")
    private String emailToRespondentTemplateId;

    private final EmailService emailService;

    private final DocumentManagementService documentManagementService;

    private static final String APP_DETAILS = "| | |\r\n"
            + "|--|--|\r\n"
            + "|Applicant | %s|\r\n"
            + "|Type of application | %s|\r\n"
            + "|Application date | %s|\r\n"
            + "|Give details | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
            + "\r\n";
    private static final String STRING_BR = "<br>";

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
        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {
            return initialAppDetails(applicationTypeItem.getValue(), authToken)
                    + initialRespondDetails(applicationTypeItem.getValue(), authToken);
        }
        throw new NotFoundException("No selected application type item found.");
    }

    private String initialAppDetails(GenericTseApplicationType applicationType, String authToken) {
        return String.format(
            APP_DETAILS,
            applicationType.getApplicant(),
            applicationType.getType(),
            applicationType.getDate(),
            applicationType.getDetails(),
            documentManagementService.displayDocNameTypeSizeLink(applicationType.getDocumentUpload(), authToken),
            displayCopyToOtherPartyYesOrNo(applicationType.getCopyToOtherPartyYesOrNo())
        );
    }

    private String initialRespondDetails(GenericTseApplicationType application, String authToken) {
        if (CollectionUtils.isEmpty(application.getRespondCollection())) {
            return "";
        }
        IntWrapper respondCount = new IntWrapper(0);
        return application.getRespondCollection().stream()
            .map(replyItem ->
                ADMIN.equals(replyItem.getValue().getFrom())
                ? formatAdminReply(
                    replyItem.getValue(),
                    respondCount.incrementAndReturnValue(),
                    documentManagementService.displayDocNameTypeSizeLink(
                        replyItem.getValue().getAddDocument(), authToken))
                : formatLegalRepReplyForReply(
                    replyItem.getValue(),
                    respondCount.incrementAndReturnValue(),
                    application.getApplicant(),
                    populateListDocWithInfoAndLink(replyItem.getValue().getSupportingMaterial(), authToken)))
            .collect(Collectors.joining(""));
    }

    private String populateListDocWithInfoAndLink(List<DocumentTypeItem> supportingMaterial, String authToken) {
        if (CollectionUtils.isEmpty(supportingMaterial)) {
            return "";
        }
        return supportingMaterial.stream()
            .map(documentTypeItem ->
                documentManagementService.displayDocNameTypeSizeLink(
                    documentTypeItem.getValue().getUploadedDocument(), authToken) + STRING_BR)
            .collect(Collectors.joining());
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
        return caseData.getTseAdmReplyAddDocument() == null
            && (CASE_MANAGEMENT_ORDER.equals(caseData.getTseAdmReplyIsCmoOrRequest())
                || REQUEST.equals(caseData.getTseAdmReplyIsCmoOrRequest()))
            && YES.equals(caseData.getTseAdmReplyIsResponseRequired());
    }

    /**
     * Save Tse Admin Record a Decision data to the application object.
     * @param caseData in which the case details are extracted from
     */
    public void saveTseAdmReplyDataFromCaseData(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {

            GenericTseApplicationType genericTseApplicationType = applicationTypeItem.getValue();
            if (CollectionUtils.isEmpty(genericTseApplicationType.getRespondCollection())) {
                genericTseApplicationType.setRespondCollection(new ArrayList<>());
            }

            genericTseApplicationType.getRespondCollection().add(
                TseRespondTypeItem.builder()
                    .id(UUID.randomUUID().toString())
                    .value(
                        TseRespondType.builder()
                            .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                            .from(ADMIN)
                            .enterResponseTitle(caseData.getTseAdmReplyEnterResponseTitle())
                            .additionalInformation(caseData.getTseAdmReplyAdditionalInformation())
                            .addDocument(caseData.getTseAdmReplyAddDocument())
                            .isCmoOrRequest(caseData.getTseAdmReplyIsCmoOrRequest())
                            .cmoMadeBy(caseData.getTseAdmReplyCmoMadeBy())
                            .requestMadeBy(caseData.getTseAdmReplyRequestMadeBy())
                            .madeByFullName(caseData.getTseAdmReplyEnterFullName())
                            .isResponseRequired(caseData.getTseAdmReplyIsResponseRequired())
                            .selectPartyRespond(caseData.getTseAdmReplySelectPartyRespond())
                            .selectPartyNotify(caseData.getTseAdmReplySelectPartyNotify())
                            .build()
                    ).build());
        }
    }

    /**
     * Uses {@link EmailService} to generate an email.
     * @param caseId used in email link to case
     * @param caseData in which the case details are extracted from
     */
    public void sendAdmReplyEmails(String caseId, CaseData caseData) {
        String caseNumber = caseData.getEthosCaseReference();

        List<TSEAdminEmailRecipientsData> emailsToSend = new ArrayList<>();
        collectRespondents(caseData, emailsToSend);
        collectClaimants(caseData, emailsToSend);

        for (final TSEAdminEmailRecipientsData emailRecipient : emailsToSend) {
            emailService.sendEmail(
                emailRecipient.getRecipientTemplate(),
                emailRecipient.getRecipientEmail(),
                buildPersonalisation(caseNumber, caseId, emailRecipient.getCustomisedText()));
        }
    }

    private void collectRespondents(CaseData caseData, List<TSEAdminEmailRecipientsData> emailsToSend) {
        // if respondent only or both parties: send Respondents Reply Emails
        if (RESPONDENT_ONLY.equals(caseData.getTseAdmReplySelectPartyNotify())
            || BOTH_PARTIES.equals(caseData.getTseAdmReplySelectPartyNotify())) {
            TSEAdminEmailRecipientsData respondentDetails;
            for (RespondentSumTypeItem respondentSumTypeItem: caseData.getRespondentCollection()) {
                if (respondentSumTypeItem.getValue().getRespondentEmail() != null) {
                    respondentDetails =
                        new TSEAdminEmailRecipientsData(
                            emailToRespondentTemplateId,
                            respondentSumTypeItem.getValue().getRespondentEmail());

                    if (YES.equals(caseData.getTseAdmReplyIsResponseRequired())
                        && (BOTH_PARTIES.equals(caseData.getTseAdmReplySelectPartyRespond())
                        || RESPONDENT_TITLE.equals(caseData.getTseAdmReplySelectPartyRespond()))) {
                        respondentDetails.setCustomisedText(RESPONSE_REQUIRED);
                    } else {
                        respondentDetails.setCustomisedText(RESPONSE_NOT_REQUIRED);
                    }

                    emailsToSend.add(respondentDetails);
                }
            }
        }
    }

    private void collectClaimants(CaseData caseData, List<TSEAdminEmailRecipientsData> emailsToSend) {
        // if claimant only or both parties: send Claimant Reply Email
        if (CLAIMANT_ONLY.equals(caseData.getTseAdmReplySelectPartyNotify())
            || BOTH_PARTIES.equals(caseData.getTseAdmReplySelectPartyNotify())) {
            String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();

            if (claimantEmail != null) {
                TSEAdminEmailRecipientsData claimantDetails =
                    new TSEAdminEmailRecipientsData(emailToClaimantTemplateId, claimantEmail);

                if (YES.equals(caseData.getTseAdmReplyIsResponseRequired())
                    && (BOTH_PARTIES.equals(caseData.getTseAdmReplySelectPartyRespond())
                    || CLAIMANT_TITLE.equals(caseData.getTseAdmReplySelectPartyRespond()))) {
                    claimantDetails.setCustomisedText(RESPONSE_REQUIRED);
                } else {
                    claimantDetails.setCustomisedText(RESPONSE_NOT_REQUIRED);
                }

                emailsToSend.add(claimantDetails);
            }
        }
    }

    private Map<String, String> buildPersonalisation(String caseNumber, String caseId, String customText) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseNumber);
        personalisation.put("caseId", caseId);
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
        caseData.setTseAdmReplyEnterFullName(null);
        caseData.setTseAdmReplyIsResponseRequired(null);
        caseData.setTseAdmReplySelectPartyRespond(null);
        caseData.setTseAdmReplySelectPartyNotify(null);
    }

}
