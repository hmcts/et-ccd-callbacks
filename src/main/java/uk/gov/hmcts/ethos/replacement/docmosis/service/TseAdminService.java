package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
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
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItem;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.createTwoColumnTable;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getAdminSelectedApplicationType;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.TSE_ADMIN_DECISION_FILE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdminService {
    public static final String NOT_VIEWED_YET = "notViewedYet";

    private final EmailService emailService;
    private final TornadoService tornadoService;
    private final TseService tseService;
    private final DocumentManagementService documentManagementService;
    @Value("${template.tse.admin.record-a-decision.claimant}")
    private String tseAdminRecordClaimantTemplateId;
    @Value("${template.tse.admin.record-a-decision.respondent}")
    private String tseAdminRecordRespondentTemplateId;

    private static final String DECISION_DOC_GEN_ERROR = "Failed to generate decision document for case id: %s";

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
            applicationType.setAdminDecision(new ArrayList<>());
        }

        TseAdminRecordDecisionType decision = TseAdminRecordDecisionType.builder()
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
            TseAdminRecordDecisionTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(decision)
                .build()
        );
    }

    private List<GenericTypeItem<DocumentType>> getResponseRequiredDocYesOrNo(CaseData caseData) {
        if (YES.equals(caseData.getTseAdminIsResponseRequired())) {
            return caseData.getTseAdminResponseRequiredYesDoc();
        }
        return caseData.getTseAdminResponseRequiredNoDoc();
    }

    /**
     * Uses {@link EmailService} to generate an email.
     * @param caseDetails used to get case Id, case ethos reference number and decision pdf file binary URL
     */
    public void sendEmailToClaimant(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
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
                buildPersonalisation(caseDetails.getCaseId(), caseData, emailRecipient.getRecipientName()));
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

        Map<String, String> personalisation = buildPersonalisation(caseDetails.getCaseId(),
                caseData, respondent.getRespondentName());

        emailService.sendEmail(tseAdminRecordRespondentTemplateId, respondentEmail, personalisation);
    }

    private Map<String, String> buildPersonalisation(String caseId, CaseData caseData, String recipientName) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, defaultIfEmpty(caseData.getEthosCaseReference(), ""));
        personalisation.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseId));
        personalisation.put(LINK_TO_EXUI, emailService.getExuiCaseLink(caseId));
        personalisation.put("name", recipientName);
        String decisionDocumentURL = "";
        if (CollectionUtils.isNotEmpty(caseData.getDocumentCollection())) {
            DocumentTypeItem documentTypeItem =
                    caseData.getDocumentCollection().get(caseData.getDocumentCollection().size() - 1);
            if (isNotEmpty(documentTypeItem) && isNotEmpty(documentTypeItem.getValue())
                    && isNotEmpty(documentTypeItem.getValue().getUploadedDocument())) {
                decisionDocumentURL = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
            }
        }
        personalisation.put("linkToDecisionFile", decisionDocumentURL);

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

    /**
     * Creates a pdf copy of the Decision from Tribunal and adds it to the case doc collection.
     *
     * @param caseData details of the case from which required fields are extracted
     * @param userToken autherisation token to use for generating an event document
     * @param caseTypeId case type to use for generating an event document
     */
    public void addTseAdminDecisionPdfToDocCollection(CaseData caseData, String userToken, String caseTypeId) {
        try {
            if (isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }
            if (isNullOrEmpty(caseTypeId)) {
                log.error("Error while creating case document: Case Type ID can not be null or empty");
                throw new DocumentManagementException(
                        "Error while creating case document: Case Type ID can not be null or empty");
            }

            DocumentInfo document = tornadoService.generateEventDocument(caseData, userToken, caseTypeId,
                    TSE_ADMIN_DECISION_FILE_NAME);

            DocumentTypeItem docItem = createDocumentTypeItem(
                    documentManagementService.addDocumentToDocumentField(
                            document),
                    "Referral/Judicial direction",
                    caseData.getResTseSelectApplication()
            );

            caseData.getDocumentCollection().add(docItem);

        } catch (Exception e) {
            throw new DocumentManagementException(
                    String.format(DECISION_DOC_GEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

}
