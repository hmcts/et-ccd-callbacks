package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.APPLICATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.HEARING_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatOrdReqDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatRespondDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.getSelectedSendNotificationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class PseRespondToTribunalService {

    @Value("${pse.respondent.acknowledgement.yes.template.id}")
    private String acknowledgeEmailYesTemplateId;
    @Value("${pse.respondent.acknowledgement.no.template.id}")
    private String acknowledgeEmailNoTemplateId;
    @Value("${pse.respondent.notification.claimant.template.id}")
    private String notificationToClaimantTemplateId;
    @Value("${pse.respondent.notification.admin.template.id}")
    private String notificationToAdminTemplateId;

    private final EmailService emailService;
    private final UserService userService;
    private final HearingSelectionService hearingSelectionService;
    private final TribunalOfficesService tribunalOfficesService;

    private static final String GIVE_MISSING_DETAIL =
        "Use the text box or supporting materials to give details.";

    private static final String SUBMITTED_BODY = "### What happens next\r\n\r\n"
        + "%s"
        + "The tribunal will consider all correspondence and let you know what happens next.";

    private static final String RULE92_ANSWERED_YES =
        "You have responded to the tribunal and copied your response to the other party.\r\n\r\n";

    /**
     * Create a list for application dropdown selector.
     * Only populate when
     * - SendNotificationNotify = RESPONDENT_ONLY or BOTH_PARTIES
     * - Respondent has not replied yet
     * @param caseData contains all the case data
     */
    public DynamicFixedListType populateSelectDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getSendNotificationCollection().stream()
            .filter(r -> isNotifyRespondent(r)
                && isNoRespondentReply(r.getValue().getRespondCollection()))
            .map(r ->
                DynamicValueType.create(
                    r.getValue().getNumber(),
                    r.getValue().getNumber() + " " + r.getValue().getSendNotificationTitle()
                )
            )
            .collect(Collectors.toList()));
    }

    private boolean isNotifyRespondent(SendNotificationTypeItem sendNotificationTypeItem) {
        return RESPONDENT_ONLY.equals(sendNotificationTypeItem.getValue().getSendNotificationNotify())
            || BOTH_PARTIES.equals(sendNotificationTypeItem.getValue().getSendNotificationNotify());
    }

    private boolean isNoRespondentReply(List<PseResponseTypeItem> pseResponseTypeItems) {
        return CollectionUtils.isEmpty(pseResponseTypeItems)
            || pseResponseTypeItems.stream().noneMatch(r -> RESPONDENT_TITLE.equals(r.getValue().getFrom()));
    }

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public String initialOrdReqDetailsTableMarkUp(CaseData caseData) {
        SendNotificationType sendNotificationType =
            getSelectedSendNotificationTypeItem(caseData).getValue();
        return formatOrdReqDetails(sendNotificationType)
            + formatRespondDetails(sendNotificationType);
    }

    /**
     * Validate user input.
     * @param caseData contains all the case data
     * @return Error Message List
     */
    public List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isEmpty(caseData.getPseRespondentOrdReqResponseText())
            && (StringUtils.isEmpty(caseData.getPseRespondentOrdReqHasSupportingMaterial())
            || NO.equals(caseData.getPseRespondentOrdReqHasSupportingMaterial()))) {
            errors.add(GIVE_MISSING_DETAIL);
        }
        return errors;
    }

    /**
     * Create a new element in the responses list and assign the PSE data from CaseData to it.
     * @param caseData contains all the case data
     */
    public void addRespondentResponseToJON(CaseData caseData) {
        SendNotificationType sendNotificationType = getSelectedSendNotificationTypeItem(caseData).getValue();
        List<PseResponseTypeItem> responses = sendNotificationType.getRespondCollection();
        if (CollectionUtils.isEmpty(responses)) {
            sendNotificationType.setRespondCollection(new ArrayList<>());
            responses = sendNotificationType.getRespondCollection();
        }

        responses.add(
            PseResponseTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(
                    PseResponseType.builder()
                        .from(RESPONDENT_TITLE)
                        .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                        .response(caseData.getPseRespondentOrdReqResponseText())
                        .hasSupportingMaterial(caseData.getPseRespondentOrdReqHasSupportingMaterial())
                        .supportingMaterial(caseData.getPseRespondentOrdReqUploadDocument())
                        .copyToOtherParty(caseData.getPseRespondentOrdReqCopyToOtherParty())
                        .copyNoGiveDetails(caseData.getPseRespondentOrdReqCopyNoGiveDetails())
                        .build()
                ).build());

        sendNotificationType.setSendNotificationResponsesCount(String.valueOf(responses.size()));
    }

    /**
     * Send Acknowledge Email.
     * @param caseDetails in which the case details are extracted from
     * @param userToken jwt used for authorization
     */
    public void sendAcknowledgeEmail(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        String email = userService.getUserDetails(userToken).getEmail();
        if (YES.equals(caseData.getPseRespondentOrdReqCopyToOtherParty())) {
            emailService.sendEmail(acknowledgeEmailYesTemplateId, email, buildPersonalisationYes(caseDetails));
        } else {
            emailService.sendEmail(acknowledgeEmailNoTemplateId, email, buildPersonalisationNo(caseDetails));
        }
    }

    private Map<String, String> buildPersonalisationYes(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        return Map.of(
            CASE_NUMBER, caseData.getEthosCaseReference(),
                CASE_ID, caseDetails.getCaseId()
        );
    }

    private Map<String, String> buildPersonalisationNo(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        SendNotificationType sendNotificationType = getSelectedSendNotificationTypeItem(caseData).getValue();
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, Helper.getRespondentNames(caseData),
                HEARING_DATE, getHearingDate(caseData, sendNotificationType),
                CASE_ID, caseDetails.getCaseId()
        );
    }

    private String getHearingDate(CaseData caseData, SendNotificationType sendNotificationType) {
        if (sendNotificationType.getSendNotificationSelectHearing() == null) {
            return "";
        }
        DateListedType dateListedType = hearingSelectionService.getSelectedListing(
            caseData, sendNotificationType.getSendNotificationSelectHearing());
        return UtilHelper.formatLocalDateTime(dateListedType.getListedDate());
    }

    /**
     * Generate email notification to claimant when LR responds to order/request.
     * @param caseDetails in which the case details are extracted from
     */
    public void sendClaimantEmail(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        if (YES.equals(caseData.getPseRespondentOrdReqCopyToOtherParty())) {
            emailService.sendEmail(notificationToClaimantTemplateId,
                caseData.getClaimantType().getClaimantEmailAddress(),
                buildPersonalisationNotify(caseDetails));
        }
    }

    private Map<String, String> buildPersonalisationNotify(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, Helper.getRespondentNames(caseData),
                CASE_ID, caseDetails.getCaseId()
        );
    }

    /**
     * Generate email notification to admin when LR responds to order/request.
     * @param caseDetails in which the case details are extracted from
     */
    public void sendTribunalEmail(CaseDetails caseDetails) {
        String managingOffice = caseDetails.getCaseData().getManagingOffice();
        TribunalOffice tribunalOffice = tribunalOfficesService.getTribunalOffice(managingOffice);
        if (tribunalOffice == null) {
            return;
        }

        String adminEmail = tribunalOffice.getOfficeEmail();
        if (isNullOrEmpty(adminEmail)) {
            return;
        }

        emailService.sendEmail(notificationToAdminTemplateId,
            adminEmail,
            buildPersonalisationAdmin(caseDetails));
    }

    private Map<String, String> buildPersonalisationAdmin(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        SendNotificationType sendNotificationType = getSelectedSendNotificationTypeItem(caseData).getValue();
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                APPLICATION, sendNotificationType.getSendNotificationTitle(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, Helper.getRespondentNames(caseData),
                HEARING_DATE, getHearingDate(caseData, sendNotificationType),
                CASE_ID, caseDetails.getCaseId()
        );
    }

    /**
     * Clears fields that are used when responding to a JON, so they can be used in subsequent responses to JONs.
     * @param caseData contains all the case data
     */
    public void clearRespondentResponse(CaseData caseData) {
        caseData.setPseRespondentOrdReqTableMarkUp(null);
        caseData.setPseRespondentOrdReqResponseText(null);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(null);
        caseData.setPseRespondentOrdReqUploadDocument(null);
        caseData.setPseRespondentOrdReqCopyToOtherParty(null);
        caseData.setPseRespondentOrdReqCopyNoGiveDetails(null);
    }

    /**
     * Generate Submitted Body String.
     * @param caseData contains all the case data
     * @return Submitted Body String
     */
    public String getSubmittedBody(CaseData caseData) {
        SendNotificationType sendNotificationType = getSelectedSendNotificationTypeItem(caseData).getValue();
        if (sendNotificationType == null || CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            return SUBMITTED_BODY;
        }

        List<PseResponseTypeItem> respondCollection = sendNotificationType.getRespondCollection();

        PseResponseType response = respondCollection.get(respondCollection.size() - 1).getValue();
        return String.format(SUBMITTED_BODY, YES.equals(response.getCopyToOtherParty()) ? RULE92_ANSWERED_YES : "");
    }

}
