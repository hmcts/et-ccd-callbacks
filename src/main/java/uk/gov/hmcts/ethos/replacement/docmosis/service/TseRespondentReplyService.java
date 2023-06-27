package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getRespondentSelectedApplicationType;

@Service
@RequiredArgsConstructor
@Slf4j
public class TseRespondentReplyService {
    private final TornadoService tornadoService;
    private final EmailService emailService;
    private final UserService userService;
    private final NotificationProperties notificationProperties;
    private final RespondentTellSomethingElseService respondentTellSomethingElseService;
    private final TseService tseService;

    @Value("${tse.respondent.respond.notify.claimant.template.id}")
    private String tseRespondentResponseTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92no.template.id}")
    private String acknowledgementRule92NoEmailTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92yes.template.id}")
    private String acknowledgementRule92YesEmailTemplateId;

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
        saveReplyToApplication(caseData, isRespondingToTribunal(caseData));

        respondentTellSomethingElseService.sendAdminEmail(caseDetails);
        sendAcknowledgementAndClaimantEmail(caseDetails, userToken);

        resetReplyToApplicationPage(caseData);
    }

    /**
     * Change status of application to updated if there was an unanswered request for information from the admin.
     *
     * @param caseData in which the case details are extracted from
     */
    void updateApplicationState(CaseData caseData) {
        if (isRespondingToTribunal(caseData)) {
            getRespondentSelectedApplicationType(caseData).setApplicationState(UPDATED);
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

    void sendAcknowledgementAndClaimantEmail(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        if (YES.equals(caseData.getTseResponseCopyToOtherParty())) {
            try {
                byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, "", "TSE Reply.pdf");
                String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
                Map<String, Object> personalisation = TseHelper.getPersonalisationForResponse(caseDetails,
                    bytes, notificationProperties.getCitizenUrl());
                emailService.sendEmail(tseRespondentResponseTemplateId,
                    claimantEmail, personalisation);
            } catch (Exception e) {
                throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
            }
        }

        String legalRepEmail = userService.getUserDetails(userToken).getEmail();
        emailService.sendEmail(
            YES.equals(caseData.getTseResponseCopyToOtherParty())
                ? acknowledgementRule92YesEmailTemplateId
                : acknowledgementRule92NoEmailTemplateId,
            legalRepEmail,
            TseHelper.getPersonalisationForAcknowledgement(caseDetails, notificationProperties.getExuiUrl()));
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

        String applicationTable = tseService.formatApplicationDetails(application, authToken, true);
        String responses = tseService.formatApplicationResponses(application, authToken, true);

        caseData.setTseResponseTable(applicationTable + "\r\n" + responses);
        caseData.setTseRespondingToTribunal(YES);
    }

    /**
     * Returns error if LR selects No to supporting materials question and does not enter response details.
     * @param caseData contains all the case data
     * @return Error Message List
     */
    public List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isEmpty(caseData.getTseResponseText())
                && StringUtils.isEmpty(caseData.getTseRespondingToTribunalText())
                && NO.equals(caseData.getTseResponseHasSupportingMaterial())) {
            errors.add(GIVE_MISSING_DETAIL);
        }
        return errors;
    }
}
