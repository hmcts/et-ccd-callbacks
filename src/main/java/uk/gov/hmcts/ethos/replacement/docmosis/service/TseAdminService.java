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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdminService {

    @Value("${tse.admin.template.id}")
    private String emailTemplateId;

    private final EmailService emailService;
    private final DocumentManagementService documentManagementService;

    private static final String APP_DETAILS = "| | |\r\n"
            + "|--|--|\r\n"
            + "|%s application | %s|\r\n"
            + "|Application date | %s|\r\n"
            + "|%s | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "\r\n";
    private static final String RESPONSE_DETAILS = "|Response %s | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | %s|\r\n"
            + "|Response date | %s|\r\n"
            + "|Details | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "\r\n";

    private static final String ClOSE_APP_DETAILS = "| | |\r\n"
        + "|--|--|\r\n"
        + "|Applicant | %s|\r\n"
        + "|Type of application | %s|\r\n"
        + "|Application date | %s|\r\n"
        + "|What do you want to tell or ask the tribunal? | %s|\r\n"
        + "|Supporting material | %s|\r\n"
        + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
        + "\r\n";

    private static final String CLOSE_APP_RESPONSES_DETAILS = "| | |\r\n"
        + "|--|--|\r\n"
        + "|Response from | %s|\r\n"
        + "|Response date | %s|\r\n"
        + "|What’s your response to the respondent’s application? | %s|\r\n"
        + "|Supporting material | %s|\r\n"
        + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
        + "\r\n";

    private static final String CLOSE_APP_DECISION_DETAILS = "| | |\r\n"
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

    private static final String STRING_BR = "<br>";
    private static final String APPLICATION_QUESTION = "Give details";

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
            GenericTseApplicationType applicationType = applicationTypeItem.getValue();
            String appDetails = initialTseAdminAppDetails(applicationType, authToken);
            String responseDetails = initialTseAdminRespondDetails(applicationType, authToken);
            caseData.setTseAdminTableMarkUp(appDetails + responseDetails);
            // TODO: Add Admin Respond to TseAdminTableMarkUp
        }
    }

    private GenericTseApplicationTypeItem getSelectedApplicationTypeItem(CaseData caseData) {
        String selectedAppId = caseData.getTseAdminSelectApplication().getSelectedCode();
        return caseData.getGenericTseApplicationCollection().stream()
                .filter(genericTseApplicationTypeItem ->
                        genericTseApplicationTypeItem.getValue().getNumber().equals(selectedAppId))
                .findFirst()
                .orElse(null);
    }

    private String initialTseAdminAppDetails(GenericTseApplicationType applicationType, String authToken) {
        return String.format(
                APP_DETAILS,
                applicationType.getApplicant(),
                applicationType.getType(),
                applicationType.getDate(),
                APPLICATION_QUESTION,
                APPLICATION_QUESTION,
                applicationType.getDetails(),
                documentManagementService.displayDocNameTypeSizeLink(applicationType.getDocumentUpload(), authToken)
                documentManagementService.displayDocNameTypeSizeLink(applicationType.getDocumentUpload(), authToken)
        );
    }

    private String initialTseAdminRespondDetails(GenericTseApplicationType applicationType, String authToken) {
        if (applicationType.getRespondentReply() == null) {
            return "";
        }
        IntWrapper respondCount = new IntWrapper(0);
        return applicationType.getRespondentReply().stream()
                .map(respondent -> String.format(
                        RESPONSE_DETAILS,
                        respondCount.incrementAndReturnValue(),
                        respondent.getValue().getFrom(),
                        respondent.getValue().getDate(),
                        respondent.getValue().getResponse(),
                        populateListDocWithInfoAndLink(respondent.getValue().getSupportingMaterial(), authToken)))
                .findFirst()
                .orElse(null);
                .findFirst()
                .orElse(null);
    }

    private String populateListDocWithInfoAndLink(List<DocumentTypeItem> supportingMaterial, String authToken) {
        if (supportingMaterial == null) {
            return "";
        }
        return supportingMaterial.stream()
                .map(documentTypeItem ->
                        documentManagementService.displayDocNameTypeSizeLink(
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
     * @param caseData in which the case details are extracted from
     */
    public void sendRecordADecisionEmails(CaseData caseData) {
        String caseNumber = caseData.getEthosCaseReference();

        Map<String, String> emailsToSend = new HashMap<>();

        // if respondent only or both parties: send Respondents Decision Emails
        if (RESPONDENT_ONLY.equals(caseData.getTseAdminSelectPartyNotify())
                || BOTH.equals(caseData.getTseAdminSelectPartyNotify())) {
            for (RespondentSumTypeItem respondentSumTypeItem: caseData.getRespondentCollection()) {
                if (respondentSumTypeItem.getValue().getRespondentEmail() != null) {
                    emailsToSend.put(respondentSumTypeItem.getValue().getRespondentEmail(),
                        respondentSumTypeItem.getValue().getRespondentName());
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
