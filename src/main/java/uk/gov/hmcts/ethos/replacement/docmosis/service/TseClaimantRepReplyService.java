package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.WAITING_FOR_THE_TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper.claimantSelectApplicationToType;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantTellSomethingElseHelper.getRespondentEmailAddressList;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItemFromTopLevel;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.createTwoColumnTable;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getClaimantRepSelectedApplicationType;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.TSE_CLAIMANT_REP_REPLY;

@Service
@RequiredArgsConstructor
@Slf4j
public class TseClaimantRepReplyService {
    private final TseService tseService;
    private final DocumentManagementService documentManagementService;
    private final TornadoService tornadoService;
    private final FeatureToggleService featureToggleService;
    private final EmailService emailService;
    private final ClaimantTellSomethingElseService claimantTseService;
    private final UserIdamService userIdamService;

    @Value("${template.tse.claimant-rep.respond.respondent}")
    private String tseClaimantRepResponseTemplateId;
    @Value("${template.tse.claimant-rep.respond.cyRespondent}")
    private String cyTseClaimantRepResponseTemplateId;
    @Value("${template.tse.claimant-rep.respond.claimant-rep.rule-92-no}")
    private String acknowledgementRule92NoEmailTemplateId;
    @Value("${template.tse.claimant-rep.respond.claimant-rep.rule-92-yes}")
    private String acknowledgementRule92YesEmailTemplateId;
    @Value("${template.tse.claimant-rep.reply-to-tribunal.tribunal}")
    private String replyToTribunalEmailToTribunalTemplateId;
    @Value("${template.tse.claimant-rep.reply-to-tribunal.respondent}")
    private String replyToTribunalEmailToClaimantTemplateId;
    @Value("${template.tse.claimant-rep.reply-to-tribunal.claimant-rep.rule-92-yes}")
    private String replyToTribunalAckEmailToLRRule92YesTemplateId;
    @Value("${template.tse.claimant-rep.reply-to-tribunal.claimant-rep.rule-92-no}")
    private String replyToTribunalAckEmailToLRRule92NoTemplateId;

    private static final String GIVE_MISSING_DETAIL = "Use the text box or supporting materials to give details.";

    public boolean isRespondingToTribunal(CaseData caseData) {
        return YES.equals(getClaimantRepSelectedApplicationType(caseData).getRespondentResponseRequired());
    }

    public void initialResReplyToTribunalTableMarkUp(CaseData caseData, String authToken) {
        GenericTseApplicationType application = getClaimantRepSelectedApplicationType(caseData);

        List<String[]> applicationTable = tseService.getApplicationDetailsRows(application, authToken, true);
        List<String[]> responses = tseService.formatApplicationResponses(application, authToken, true);

        caseData.setClaimantRepResponseTable(createTwoColumnTable(new String[]{"Application", ""},
                Stream.of(applicationTable, responses).flatMap(Collection::stream).toList()));
        caseData.setClaimantRepRespondingToTribunal(YES);
    }

    public List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (ObjectUtils.isEmpty(caseData.getClaimantRepResponseText())
                && ObjectUtils.isEmpty(caseData.getClaimantRepRespondingToTribunalText())
                && NO.equals(caseData.getClaimantRepResponseHasSupportingMaterial())) {
            errors.add(GIVE_MISSING_DETAIL);
        }
        return errors;
    }

    /**
     * Creates a pdf copy of the TSE application Response from Respondent and adds it to the case doc collection.
     *
     * @param caseData case data
     * @param userToken autherisation token to use for generating an event document
     * @param caseTypeId the case type id
     */
    public void addTseClaimantRepReplyPdfToDocCollection(CaseData caseData, String userToken, String caseTypeId) {
        try {
            if (isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }

            GenericTseApplicationType applicationType = getClaimantRepSelectedApplicationType(caseData);

            String documentName = "Application %s - %s - Claimant Response.pdf".formatted(
                    applicationType.getNumber(),
                    applicationType.getType());

            UploadedDocumentType uploadedDocumentType = documentManagementService.addDocumentToDocumentField(
                    tornadoService.generateEventDocument(caseData, userToken, caseTypeId, TSE_CLAIMANT_REP_REPLY));
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

    private static String getApplicationDoc(GenericTseApplicationType applicationType) {
        if (RESPONDENT_TITLE.equals(applicationType.getApplicant())) {
            return uk.gov.hmcts.ecm.common.helpers.DocumentHelper.respondentApplicationToDocType(
                    applicationType.getType());
        } else {
            String claimantApplicationType = claimantSelectApplicationToType(applicationType.getType());
            return uk.gov.hmcts.ecm.common.helpers.DocumentHelper.claimantApplicationTypeToDocType(
                    claimantApplicationType);
        }
    }

    /**
     * Reply to a TSE application as a respondent, including updating app status, saving the reply and sending emails.
     *
     * @param userToken authorisation token to get claimant's email address
     * @param caseDetails case details
     * @param caseData case data
     */
    public void claimantReplyToTse(String userToken, CaseDetails caseDetails, CaseData caseData) {
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
     * Change status of application to updated if there was an unanswered request for information from the admin.
     *
     * @param caseData in which the case details are extracted from
     */
    protected void updateApplicationState(CaseData caseData) {
        GenericTseApplicationType selectedApplicationType = getClaimantRepSelectedApplicationType(caseData);
        if (selectedApplicationType.getApplicant().equals(RESPONDENT_TITLE)) {
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
     * Saves the data on the reply page onto the application object.
     *
     * @param caseData contains all the case data
     * @param isRespondingToTribunal determines if responding to the Tribunal's request/order
     */
    protected void saveReplyToApplication(CaseData caseData, boolean isRespondingToTribunal) {
        GenericTseApplicationType genericTseApplicationType = getClaimantRepSelectedApplicationType(caseData);

        if (isEmpty(genericTseApplicationType.getRespondCollection())) {
            genericTseApplicationType.setRespondCollection(new ArrayList<>());
        }
        List<TseRespondTypeItem> respondCollection = genericTseApplicationType.getRespondCollection();

        TseRespondType response = TseRespondType.builder()
                .response(isRespondingToTribunal ? caseData.getClaimantRepRespondingToTribunalText()
                        : caseData.getClaimantRepResponseText())
                .supportingMaterial(caseData.getClaimantRepResSupportingMaterial())
                .hasSupportingMaterial(caseData.getClaimantRepResponseHasSupportingMaterial())
                .from(CLAIMANT_REP_TITLE)
                .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                .copyToOtherParty(caseData.getClaimantRepResponseCopyToOtherParty())
                .copyNoGiveDetails(caseData.getClaimantRepResponseCopyNoGiveDetails())
                .build();

        respondCollection.add(TseRespondTypeItem.builder().id(UUID.randomUUID().toString()).value(response).build());

        if (featureToggleService.isWorkAllocationEnabled()) {
            response.setDateTime(Helper.getCurrentDateTime()); // for Work Allocation DMNs
            response.setApplicationType(genericTseApplicationType.getType()); // for Work Allocation DMNs
        }

        if (isRespondingToTribunal) {
            genericTseApplicationType.setRespondentResponseRequired(NO);
        }

        genericTseApplicationType.setResponsesCount(String.valueOf(respondCollection.size()));
    }

    /**
     * Send emails when LR submits response to Tribunal request/order.
     */
    public void sendRespondingToTribunalEmails(CaseDetails caseDetails, String userToken) {
        sendEmailToTribunal(caseDetails);
        sendEmailToRespondentForRespondingToTrib(caseDetails);
        sendAcknowledgementEmailToLR(caseDetails, userToken, true);
    }

    private void sendEmailToTribunal(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        String email = claimantTseService.getTribunalEmail(caseData);

        if (isNullOrEmpty(email)) {
            return;
        }

        GenericTseApplicationType selectedApplication = getClaimantRepSelectedApplicationType(caseData);
        Map<String, String> personalisation = Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                APPLICATION_TYPE, selectedApplication.getType(),
                LINK_TO_EXUI, emailService.getExuiCaseLink(caseDetails.getCaseId()));
        emailService.sendEmail(replyToTribunalEmailToTribunalTemplateId, email, personalisation);
    }

    private void sendEmailToRespondentForRespondingToTrib(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        if (!YES.equals(caseData.getTseResponseCopyToOtherParty())) {
            return;
        }

        List<String> respondentEmailAddressList = getRespondentEmailAddressList(caseData);
        if (respondentEmailAddressList.isEmpty()) {
            return;
        }

        Map<String, String> personalisation = Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                LINK_TO_CITIZEN_HUB, emailService.getExuiCaseLink(caseDetails.getCaseId()));
        respondentEmailAddressList.forEach(respondentEmail ->
            emailService.sendEmail(replyToTribunalEmailToClaimantTemplateId, respondentEmail, personalisation));
    }

    private void sendAcknowledgementEmailToLR(CaseDetails caseDetails, String userToken,
                                              boolean isRespondingToTribunal) {
        emailService.sendEmail(
                getAckEmailTemplateId(caseDetails, isRespondingToTribunal),
                userIdamService.getUserDetails(userToken).getEmail(),
                TseHelper.getPersonalisationForAcknowledgement(
                        caseDetails, emailService.getExuiCaseLink(caseDetails.getCaseId()), true));
    }

    /**
     * Send emails when LR submits response to application.
     */
    public void sendRespondingToApplicationEmails(CaseDetails caseDetails, String userToken) {
        // should send the email to respondent
        sendEmailToRespondentForRespondingToApp(caseDetails);
        // should send the email to claimant rep
        sendAcknowledgementEmailToLR(caseDetails, userToken, false);
        // send the email to tribunal
        claimantTseService.sendAdminEmail(caseDetails);
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

    private void sendEmailToRespondentForRespondingToApp(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        if (!YES.equals(caseData.getTseResponseCopyToOtherParty())) {
            return;
        }

        List<String> respondentEmailAddressList = getRespondentEmailAddressList(caseData);
        if (respondentEmailAddressList.isEmpty()) {
            return;
        }

        try {
            byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, "",
                    TSE_CLAIMANT_REP_REPLY);

            boolean isWelsh = featureToggleService.isWelshEnabled()
                    && Optional.ofNullable(caseData.getClaimantHearingPreference())
                    .map(preference -> WELSH_LANGUAGE.equals(preference.getContactLanguage()))
                    .orElse(false);

            Map<String, Object> personalisation = TseHelper.getPersonalisationForResponse(caseDetails,
                    bytes, emailService.getExuiCaseLink(caseDetails.getCaseId()), isWelsh);

            String emailTemplate = isWelsh
                    ? cyTseClaimantRepResponseTemplateId
                    : tseClaimantRepResponseTemplateId;

            respondentEmailAddressList.forEach(
                    respondentEmail ->
                            emailService.sendEmail(
                            emailTemplate,
                            respondentEmail,
                            personalisation));
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }

    }

    /**
     * Clears fields that are used when responding to an application.
     *
     * @param caseData contains all the case data
     */
    void resetReplyToApplicationPage(CaseData caseData) {
        caseData.setClaimantRepResponseText(null);
        caseData.setClaimantRepResponseIntro(null);
        caseData.setClaimantRepResponseTable(null);
        caseData.setClaimantRepResponseHasSupportingMaterial(null);
        caseData.setClaimantRepResSupportingMaterial(null);
        caseData.setClaimantRepResponseCopyToOtherParty(null);
        caseData.setClaimantRepResponseCopyNoGiveDetails(null);
        caseData.setClaimantRepRespondSelectApplication(null);
        caseData.setClaimantRepRespondingToTribunal(null);
        caseData.setClaimantRepRespondingToTribunalText(null);
    }
}
