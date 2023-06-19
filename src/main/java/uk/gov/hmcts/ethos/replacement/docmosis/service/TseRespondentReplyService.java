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
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REQUEST;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getRespondentSelectedApplicationTypeItem;

@Service
@RequiredArgsConstructor
@Slf4j
public class TseRespondentReplyService {
    private final TornadoService tornadoService;
    private final EmailService emailService;
    private final UserService userService;
    private final NotificationProperties notificationProperties;
    @Value("${tse.respondent.respond.notify.claimant.template.id}")
    private String tseRespondentResponseTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92no.template.id}")
    private String acknowledgementRule92NoEmailTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92yes.template.id}")
    private String acknowledgementRule92YesEmailTemplateId;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    /**
     * Change status of application to updated if there was an unanswered request for information from the admin.
     *
     * @param caseData in which the case details are extracted from
     */
    public void updateApplicationStatus(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationTypeItem applicationTypeItem = getRespondentSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem == null) {
            return;
        }

        List<TseRespondTypeItem> respondCollection = applicationTypeItem.getValue().getRespondCollection();
        if (CollectionUtils.isEmpty(respondCollection)) {
            return;
        }

        if (hasDueRequestForInfo(respondCollection)) {
            applicationTypeItem.getValue().setApplicationState(UPDATED);
        }
    }

    private static boolean hasDueRequestForInfo(List<TseRespondTypeItem> respondCollection) {
        boolean hasDueRequestForInfo = false;
        for (TseRespondTypeItem tseRespondTypeItem : respondCollection) {
            TseRespondType tseRespondType = tseRespondTypeItem.getValue();
            if (tseRespondType.getFrom().equals(RESPONDENT_TITLE)) {
                hasDueRequestForInfo = false;
            }

            if (isResponseRequestForInfoFromRespondent(tseRespondType)) {
                hasDueRequestForInfo = true;
            }
        }
        return hasDueRequestForInfo;
    }

    private static boolean isResponseRequestForInfoFromRespondent(TseRespondType tseRespondType) {
        return tseRespondType.getFrom().equals(ADMIN)
            && tseRespondType.getIsCmoOrRequest().equals(REQUEST)
            && tseRespondType.getIsResponseRequired().equals(YES)
            && (tseRespondType.getSelectPartyRespond().equals(RESPONDENT_TITLE)
            || tseRespondType.getSelectPartyRespond().equals(BOTH_PARTIES));
    }

    /**
     * Saves the data on the reply page onto the application object.
     *
     * @param caseData contains all the case data
     */
    public void saveReplyToApplication(CaseData caseData) {

        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications)) {
            return;
        }

        GenericTseApplicationType genericTseApplicationType = TseHelper.getSelectedApplication(caseData);

        if (CollectionUtils.isEmpty(genericTseApplicationType.getRespondCollection())) {
            genericTseApplicationType.setRespondCollection(new ArrayList<>());
        }

        genericTseApplicationType.getRespondCollection().add(TseRespondTypeItem.builder()
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

        genericTseApplicationType.setResponsesCount(
                String.valueOf(genericTseApplicationType.getRespondCollection().size())
        );
    }

    public void sendAcknowledgementAndClaimantEmail(CaseDetails caseDetails, String userToken) {
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
    public void resetReplyToApplicationPage(CaseData caseData) {
        caseData.setTseResponseText(null);
        caseData.setTseResponseIntro(null);
        caseData.setTseResponseTable(null);
        caseData.setTseResponseHasSupportingMaterial(null);
        caseData.setTseResponseSupportingMaterial(null);
        caseData.setTseResponseCopyToOtherParty(null);
        caseData.setTseResponseCopyNoGiveDetails(null);
        caseData.setTseRespondSelectApplication(null);
    }
}
