package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.WAITING_FOR_THE_TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItemFromTopLevel;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.createTwoColumnTable;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getRespondentSelectedApplicationType;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.TSE_REPLY;

@Service
@RequiredArgsConstructor
@Slf4j
public class TseRespondentReplyService {
    private final TornadoService tornadoService;
    private final EmailService emailService;
    private final UserService userService;
    private final RespondentTellSomethingElseService respondentTseService;
    private final TseService tseService;
    private final DocumentManagementService documentManagementService;

    @Value("${template.tse.respondent.respond.claimant}")
    private String tseRespondentResponseTemplateId;
    @Value("${template.tse.respondent.respond.respondent.rule-92-no}")
    private String acknowledgementRule92NoEmailTemplateId;
    @Value("${template.tse.respondent.respond.respondent.rule-92-yes}")
    private String acknowledgementRule92YesEmailTemplateId;
    @Value("${template.tse.respondent.reply-to-tribunal.tribunal}")
    private String replyToTribunalEmailToTribunalTemplateId;
    @Value("${template.tse.respondent.reply-to-tribunal.claimant}")
    private String replyToTribunalEmailToClaimantTemplateId;
    @Value("${template.tse.respondent.reply-to-tribunal.respondent.rule-92-yes}")
    private String replyToTribunalAckEmailToLRRule92YesTemplateId;
    @Value("${template.tse.respondent.reply-to-tribunal.respondent.rule-92-no}")
    private String replyToTribunalAckEmailToLRRule92NoTemplateId;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";
    private static final String GIVE_MISSING_DETAIL = "Use the text box or supporting materials to give details.";

    /**
     * Reply to a TSE application as a respondent, including updating app status, saving the reply and sending emails.
     *
     * @param userToken autherisation token to get claimant's email address
     * @param caseDetails case details
     * @param caseData case data
     */
    public void respondentReplyToTse(String userToken, CaseDetails caseDetails, CaseData caseData) {
        updateApplicationState(caseData);

        boolean isRespondingToTribunal = isRespondingToTribunal(caseData);
        saveReplyToApplication(caseData, isRespondingToTribunal);

        if (isRespondingToTribunal) {
            sendRespondingToTribunalEmails(caseDetails, userToken);
        } else {
            sendRespondingToApplicationEmails(caseDetails, userToken);
        }

        resetReplyToApplicationPage(caseData);
    }

    /**
     * Creates a pdf copy of the TSE application Response from Respondent and adds it to the case doc collection.
     *
     * @param caseData case data
     * @param userToken autherisation token to use for generating an event document
     * @param caseTypeId the case type id
     */
    public void addTseRespondentReplyPdfToDocCollection(CaseData caseData, String userToken, String caseTypeId) {
        try {
            if (isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }

            GenericTseApplicationType applicationType = getRespondentSelectedApplicationType(caseData);

            String documentName = "Application %s - %s - Respondent Response.pdf".formatted(
                    applicationType.getNumber(),
                    applicationType.getType());

            UploadedDocumentType uploadedDocumentType = documentManagementService.addDocumentToDocumentField(
                    tornadoService.generateEventDocument(caseData, userToken, caseTypeId, TSE_REPLY));
            uploadedDocumentType.setDocumentFilename(documentName);

            String applicationDoc = getApplicationDoc(applicationType);
            String topLevel = uk.gov.hmcts.ecm.common.helpers.DocumentHelper.getTopLevelDocument(applicationDoc);

            DocumentTypeItem docItem = createDocumentTypeItemFromTopLevel(uploadedDocumentType, topLevel,
                    applicationDoc, applicationType.getType());

            caseData.getDocumentCollection().add(docItem);

        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    /**
     * Change status of application to updated if there was an unanswered request for information from the admin.
     *
     * @param caseData in which the case details are extracted from
     */
    void updateApplicationState(CaseData caseData) {
        GenericTseApplicationType selectedApplicationType = getRespondentSelectedApplicationType(caseData);
        if (selectedApplicationType.getApplicant().equals(CLAIMANT_TITLE)) {
            if (isRespondingToTribunal(caseData)) {
                selectedApplicationType.setApplicationState(WAITING_FOR_THE_TRIBUNAL);
            } else {
                selectedApplicationType.setApplicationState(UPDATED);
            }
        } else if (isRespondingToTribunal(caseData)) {
            selectedApplicationType.setApplicationState(UPDATED);
        }
    }

    /**
     * Check if the Tribunal has requested for a response from Respondent.
     *
     * @param caseData contains all the case data
     * @return a boolean value of whether the Respondent is responding to a Tribunal order/request
     */
    public boolean isRespondingToTribunal(CaseData caseData) {
        return YES.equals(getRespondentSelectedApplicationType(caseData).getRespondentResponseRequired());
    }

    /**
     * Saves the data on the reply page onto the application object.
     *
     * @param caseData contains all the case data
     * @param isRespondingToTribunal determines if responding to the Tribunal's request/order
     */
    void saveReplyToApplication(CaseData caseData, boolean isRespondingToTribunal) {
        GenericTseApplicationType genericTseApplicationType = getRespondentSelectedApplicationType(caseData);

        if (CollectionUtils.isEmpty(genericTseApplicationType.getRespondCollection())) {
            genericTseApplicationType.setRespondCollection(new ArrayList<>());
        }
        List<TseRespondTypeItem> respondCollection = genericTseApplicationType.getRespondCollection();

        respondCollection.add(TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseRespondType.builder()
                    .response(caseData.getTseResponseText())
                    .supportingMaterial(caseData.getTseResponseSupportingMaterial())
                    .hasSupportingMaterial(caseData.getTseResponseHasSupportingMaterial())
                    .from(RESPONDENT_TITLE)
                    .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                    .copyToOtherParty(caseData.getTseResponseCopyToOtherParty())
                    .copyNoGiveDetails(caseData.getTseResponseCopyNoGiveDetails())
                    .build()
            ).build());

        if (isRespondingToTribunal) {
            genericTseApplicationType.setRespondentResponseRequired(NO);
        }

        genericTseApplicationType.setResponsesCount(String.valueOf(respondCollection.size()));
    }

    /**
     * Clears fields that are used when responding to an application.
     *
     * @param caseData contains all the case data
     */
    void resetReplyToApplicationPage(CaseData caseData) {
        caseData.setTseResponseText(null);
        caseData.setTseResponseIntro(null);
        caseData.setTseResponseTable(null);
        caseData.setTseResponseHasSupportingMaterial(null);
        caseData.setTseResponseSupportingMaterial(null);
        caseData.setTseResponseCopyToOtherParty(null);
        caseData.setTseResponseCopyNoGiveDetails(null);
        caseData.setTseRespondSelectApplication(null);
        caseData.setTseRespondingToTribunal(null);
        caseData.setTseRespondingToTribunalText(null);
    }

    /**
     * Initial Application and Respond details table for when respondent responds to Tribunal's request/order.
     * @param caseData contains all the case data
     * @param authToken the caller's bearer token used to verify the caller
     */
    public void initialResReplyToTribunalTableMarkUp(CaseData caseData, String authToken) {
        GenericTseApplicationType application = getRespondentSelectedApplicationType(caseData);

        List<String[]> applicationTable = tseService.getApplicationDetailsRows(application, authToken, true);
        List<String[]> responses = tseService.formatApplicationResponses(application, authToken, true);

        caseData.setTseResponseTable(createTwoColumnTable(new String[]{"Application", ""},
            Stream.of(applicationTable, responses).flatMap(Collection::stream).toList()));
        caseData.setTseRespondingToTribunal(YES);
    }

    /**
     * Returns error if LR selects No to supporting materials question and does not enter response details.
     * @param caseData contains all the case data
     * @return Error Message List
     */
    public List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (ObjectUtils.isEmpty(caseData.getTseResponseText())
                && ObjectUtils.isEmpty(caseData.getTseRespondingToTribunalText())
                && NO.equals(caseData.getTseResponseHasSupportingMaterial())) {
            errors.add(GIVE_MISSING_DETAIL);
        }
        return errors;
    }

    /**
     * Send emails when LR submits response to application.
     */
    public void sendRespondingToApplicationEmails(CaseDetails caseDetails, String userToken) {
        sendEmailToClaimantForRespondingToApp(caseDetails);
        sendAcknowledgementEmailToLR(caseDetails, userToken, false);
        respondentTseService.sendAdminEmail(caseDetails);
    }

    private void sendEmailToClaimantForRespondingToApp(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        if (!YES.equals(caseData.getTseResponseCopyToOtherParty())) {
            return;
        }

        try {
            byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, "",
                    "TSE Reply.pdf");
            String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
            Map<String, Object> personalisation = TseHelper.getPersonalisationForResponse(caseDetails,
                    bytes, emailService.getCitizenCaseLink(caseDetails.getCaseId()));
            emailService.sendEmail(tseRespondentResponseTemplateId,
                    claimantEmail, personalisation);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }

    }

    private void sendAcknowledgementEmailToLR(CaseDetails caseDetails, String userToken,
                                              boolean isRespondingToTribunal) {
        emailService.sendEmail(
            getAckEmailTemplateId(caseDetails, isRespondingToTribunal),
            userService.getUserDetails(userToken).getEmail(),
            TseHelper.getPersonalisationForAcknowledgement(
                caseDetails, emailService.getExuiCaseLink(caseDetails.getCaseId())));
    }

    private String getAckEmailTemplateId(CaseDetails caseDetails, boolean isRespondingToTribunal) {
        boolean copyToOtherParty = YES.equals(caseDetails.getCaseData().getTseResponseCopyToOtherParty());

        if (isRespondingToTribunal) {
            return copyToOtherParty
                    ? replyToTribunalAckEmailToLRRule92YesTemplateId
                    : replyToTribunalAckEmailToLRRule92NoTemplateId;
        }

        return copyToOtherParty
                ? acknowledgementRule92YesEmailTemplateId
                : acknowledgementRule92NoEmailTemplateId;
    }

    /**
     * Send emails when LR submits response to Tribunal request/order.
     */
    public void sendRespondingToTribunalEmails(CaseDetails caseDetails, String userToken) {
        sendEmailToTribunal(caseDetails);
        sendEmailToClaimantForRespondingToTrib(caseDetails);
        sendAcknowledgementEmailToLR(caseDetails, userToken, true);
    }

    private void sendEmailToTribunal(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        String email = respondentTseService.getTribunalEmail(caseData);

        if (isNullOrEmpty(email)) {
            return;
        }

        GenericTseApplicationType selectedApplication = getRespondentSelectedApplicationType(caseData);
        Map<String, String> personalisation = Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                APPLICATION_TYPE, selectedApplication.getType(),
                LINK_TO_EXUI, emailService.getExuiCaseLink(caseDetails.getCaseId()));
        emailService.sendEmail(replyToTribunalEmailToTribunalTemplateId, email, personalisation);
    }

    private void sendEmailToClaimantForRespondingToTrib(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        if (!YES.equals(caseData.getTseResponseCopyToOtherParty())) {
            return;
        }

        String claimantEmail = getClaimantEmailAddress(caseData);

        if (isNullOrEmpty(claimantEmail)) {
            return;
        }

        Map<String, String> personalisation = Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseDetails.getCaseId()));
        emailService.sendEmail(replyToTribunalEmailToClaimantTemplateId, claimantEmail, personalisation);
    }

    private static String getClaimantEmailAddress(CaseData caseData) {
        return caseData.getClaimantType().getClaimantEmailAddress();
    }

    private static String getApplicationDoc(GenericTseApplicationType applicationType) {
        if (CLAIMANT_TITLE.equals(applicationType.getApplicant())) {
            return uk.gov.hmcts.ecm.common.helpers.DocumentHelper.claimantApplicationTypeToDocType(
                    getClaimantApplicationType(applicationType));
        } else {
            return uk.gov.hmcts.ecm.common.helpers.DocumentHelper.respondentApplicationToDocType(
                    applicationType.getType());
        }
    }

    private static String getClaimantApplicationType(GenericTseApplicationType applicationType) {
        return ClaimantTse.APP_TYPE_MAP.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(applicationType.getType()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");

    }

}
