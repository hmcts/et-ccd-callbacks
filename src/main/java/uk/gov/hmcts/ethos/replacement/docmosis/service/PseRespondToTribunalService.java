package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.APPLICATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EXUI_CASE_DETAILS_LINK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.HEARING_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.canPartyViewNotification;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatOrdReqDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatResponseDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.formatTribunalResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.getSelectedClaimantNotification;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.getSelectedRespondentNotification;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.getNearestHearingToReferral;

@Slf4j
@Service
@RequiredArgsConstructor
public class PseRespondToTribunalService {
    public static final String INVALID_PARTY_SELECTION = "Invalid party selection";
    private final EmailService emailService;
    private final UserIdamService userIdamService;
    private final HearingSelectionService hearingSelectionService;
    private final TribunalOfficesService tribunalOfficesService;
    private final FeatureToggleService featureToggleService;

    @Value("${template.pse.myhmcts.rule-92-yes}")
    private String acknowledgeEmailYesTemplateId;
    @Value("${template.pse.myhmcts.rule-92-no}")
    private String acknowledgeEmailNoTemplateId;
    @Value("${template.pse.claimant}")
    private String notificationToClaimantTemplateId;
    @Value("${template.pse.cyClaimant}")
    private String cyNotificationToClaimantTemplateId;
    @Value("${template.pse.admin}")
    private String notificationToAdminTemplateId;
    @Value("${template.pse.claimant-rep.acknowledgement-of-response}")
    private String claimantRepResponseConfirmationTemplateId;
    @Value("${template.pse.respondent-rep.response-received}")
    private String respondentRepResponseConfirmationTemplateId;

    public static final String GIVE_MISSING_DETAIL =
        "Use the text box or supporting materials to give details.";

    public static final String SUBMITTED_BODY = """
        ### What happens next\r
        \r
        %sThe tribunal will consider all correspondence and let you know what happens next.""";

    private static final String RULE92_ANSWERED_YES =
        "You have responded to the tribunal and copied your response to the other party.\r\n\r\n";

    /**
     * Create a list for application dropdown selector.
     * Only populate when
     * - SendNotificationNotify = RESPONDENT_ONLY or BOTH_PARTIES
     * - Respondent has not replied yet
     * @param caseData contains all the case data
     */
    public DynamicFixedListType populateSelectDropdown(CaseData caseData, String party) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            return null;
        }
        IntWrapper count = new IntWrapper(0);
        return DynamicFixedListType.from(caseData.getSendNotificationCollection().stream()
            .filter(r -> canPartyViewNotification(r, party)
                         && isNoReply(r.getValue().getRespondCollection(), party))
            .map(r ->
                DynamicValueType.create(
                    r.getValue().getNumber(),
                    count.incrementAndReturnValue() + " - " + r.getValue().getSendNotificationTitle()
                )
            )
            .toList());
    }

    private boolean isNoReply(List<PseResponseTypeItem> pseResponseTypeItems, String party) {
        if (CLAIMANT_TITLE.equals(party)) {
            return CollectionUtils.isEmpty(pseResponseTypeItems)
                || pseResponseTypeItems.stream().noneMatch(r -> CLAIMANT_TITLE.equals(r.getValue().getFrom()));
        } else if (RESPONDENT_TITLE.equals(party)) {
            return CollectionUtils.isEmpty(pseResponseTypeItems)
                || pseResponseTypeItems.stream().noneMatch(r -> RESPONDENT_TITLE.equals(r.getValue().getFrom()));
        } else {
            throw new IllegalArgumentException(INVALID_PARTY_SELECTION);
        }
    }

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public String initialOrdReqDetailsTableMarkUp(CaseData caseData, String party) {
        SendNotificationType sendNotificationType = switch (party) {
            case CLAIMANT_TITLE -> getSelectedClaimantNotification(caseData).getValue();
            case RESPONDENT_TITLE -> getSelectedRespondentNotification(caseData).getValue();
            default -> throw new IllegalArgumentException(INVALID_PARTY_SELECTION);
        };

        return formatOrdReqDetails(sendNotificationType)
               + formatResponseDetails(sendNotificationType, party)
               + formatTribunalResponse(sendNotificationType, party);
    }

    /**
     * Validate user input.
     * @param caseData contains all the case data
     * @return Error Message List
     */
    public List<String> validateRespondentInput(CaseData caseData) {
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
    public void  addRespondentResponseToJON(CaseData caseData, String userToken) {
        SendNotificationType sendNotificationType = getSelectedRespondentNotification(caseData).getValue();
        List<PseResponseTypeItem> responses = sendNotificationType.getRespondCollection();
        if (CollectionUtils.isEmpty(responses)) {
            sendNotificationType.setRespondCollection(new ArrayList<>());
            responses = sendNotificationType.getRespondCollection();
        }

        PseResponseType response = PseResponseType.builder()
                .from(RESPONDENT_TITLE)
                .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                .response(caseData.getPseRespondentOrdReqResponseText())
                .hasSupportingMaterial(caseData.getPseRespondentOrdReqHasSupportingMaterial())
                .supportingMaterial(caseData.getPseRespondentOrdReqUploadDocument())
                .copyToOtherParty(caseData.getPseRespondentOrdReqCopyToOtherParty())
                .copyNoGiveDetails(caseData.getPseRespondentOrdReqCopyNoGiveDetails())
                .build();

        if (featureToggleService.isMultiplesEnabled()) {
            response.setAuthor(userIdamService.getUserDetails(userToken).getName());
        }

        responses.add(
            PseResponseTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(response).build());

        sendNotificationType.setSendNotificationResponsesCount(String.valueOf(responses.size()));
    }

    /**
     * Send Acknowledge Email.
     * @param caseDetails in which the case details are extracted from
     * @param userToken jwt used for authorization
     */
    public void sendAcknowledgeEmail(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        String email = userIdamService.getUserDetails(userToken).getEmail();
        if (YES.equals(caseData.getPseRespondentOrdReqCopyToOtherParty())) {
            emailService.sendEmail(acknowledgeEmailYesTemplateId, email,
                    buildPersonalisationYes(caseDetails));
        } else {
            emailService.sendEmail(acknowledgeEmailNoTemplateId, email,
                    buildResponsePersonalisationNo(caseDetails, RESPONDENT_TITLE));
        }
    }

    private Map<String, String> buildPersonalisationYes(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                LINK_TO_EXUI,  emailService.getExuiCaseLink(caseDetails.getCaseId())
        );
    }

    private Map<String, String> buildResponsePersonalisationNo(CaseDetails caseDetails, String party) {
        CaseData caseData = caseDetails.getCaseData();
        SendNotificationType sendNotificationType = switch (party) {
            case CLAIMANT_TITLE -> getSelectedClaimantNotification(caseData).getValue();
            case RESPONDENT_TITLE -> getSelectedRespondentNotification(caseData).getValue();
            default -> throw new IllegalArgumentException(INVALID_PARTY_SELECTION);
        };
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, getRespondentNames(caseData),
                HEARING_DATE, getHearingDate(caseData, sendNotificationType),
                LINK_TO_EXUI, emailService.getExuiCaseLink(caseDetails.getCaseId())
        );
    }

    private String getHearingDate(CaseData caseData, SendNotificationType sendNotificationType) {
        if (sendNotificationType.getSendNotificationSelectHearing() == null) {
            return "";
        }
        DateListedType dateListedType = hearingSelectionService.getSelectedListingWithList(
            caseData, sendNotificationType.getSendNotificationSelectHearing());
        return UtilHelper.formatLocalDateTime(dateListedType.getListedDate());
    }

    /**
     * Generate email notification to claimant when LR responds to order/request.
     * @param caseDetails in which the case details are extracted from
     */
    public void sendClaimantEmail(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        boolean isWelsh = isWelsh(caseData);
        String emailTemplate = isWelsh
                ? cyNotificationToClaimantTemplateId
                : notificationToClaimantTemplateId;
        if (YES.equals(caseData.getPseRespondentOrdReqCopyToOtherParty()) && !isClaimantNonSystemUser(caseData)) {
            emailService.sendEmail(emailTemplate,
                caseData.getClaimantType().getClaimantEmailAddress(),
                buildPersonalisationNotify(caseDetails, isWelsh));
        }
    }

    private Map<String, String> buildPersonalisationNotify(CaseDetails caseDetails, boolean isWelsh) {
        CaseData caseData = caseDetails.getCaseData();
        String linkToCitizenHub = isWelsh
                ? emailService.getCitizenCaseLink(caseDetails.getCaseId()) + WELSH_LANGUAGE_PARAM
                : emailService.getCitizenCaseLink(caseDetails.getCaseId());
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, getRespondentNames(caseData),
                LINK_TO_CITIZEN_HUB, linkToCitizenHub
        );
    }

    public boolean isWelsh(CaseData caseData) {
        return featureToggleService.isWelshEnabled() && WELSH_LANGUAGE.equals(
                caseData.getClaimantHearingPreference().getContactLanguage());
    }

    /**
     * Generate email notification to admin when LR responds to order/request.
     * @param caseDetails in which the case details are extracted from
     */
    public void sendTribunalEmail(CaseDetails caseDetails, String party) {
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
            buildPersonalisationAdmin(caseDetails, party));
    }

    private Map<String, String> buildPersonalisationAdmin(CaseDetails caseDetails, String party) {
        CaseData caseData = caseDetails.getCaseData();
        SendNotificationType sendNotificationType = switch (party) {
            case CLAIMANT_TITLE -> getSelectedClaimantNotification(caseData).getValue();
            case RESPONDENT_TITLE -> getSelectedRespondentNotification(caseData).getValue();
            default -> throw new IllegalArgumentException(INVALID_PARTY_SELECTION);
        };
        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                APPLICATION, sendNotificationType.getSendNotificationTitle(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, getRespondentNames(caseData),
                HEARING_DATE, getHearingDate(caseData, sendNotificationType),
                LINK_TO_EXUI, emailService.getExuiCaseLink(caseDetails.getCaseId())
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
    public String getRespondentSubmittedBody(CaseData caseData) {
        SendNotificationType sendNotificationType = getSelectedRespondentNotification(caseData).getValue();
        if (sendNotificationType == null || CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            return SUBMITTED_BODY;
        }

        List<PseResponseTypeItem> respondCollection = sendNotificationType.getRespondCollection();

        PseResponseType response = respondCollection.get(respondCollection.size() - 1).getValue();
        return String.format(SUBMITTED_BODY, YES.equals(response.getCopyToOtherParty()) ? RULE92_ANSWERED_YES : "");
    }

    public void sendEmailsForClaimantResponse(CaseDetails caseDetails, String userToken) {
        sendClaimantResponseConfirmationEmail(caseDetails, userToken);
        sendTribunalEmail(caseDetails, CLAIMANT_TITLE);

        if (YES.equals(caseDetails.getCaseData().getClaimantNotificationCopyToOtherParty())) {
            sendRespondentResponseConfirmationEmail(caseDetails);
        }
    }

    private void sendRespondentResponseConfirmationEmail(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        if (CollectionUtils.isEmpty(caseData.getRepCollection())) {
            return;
        }
        Set<RepresentedTypeRItem> respondentReps = caseData.getRepCollection().stream()
                .filter(r -> YES.equals(r.getValue().getMyHmctsYesNo())
                             && !ObjectUtils.isEmpty(r.getValue().getRespondentOrganisation())
                && !isNullOrEmpty(r.getValue().getRepresentativeEmailAddress()))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(respondentReps)) {
            return;
        }

        respondentReps.forEach(
                respondentRep ->
                        emailService.sendEmail(respondentRepResponseConfirmationTemplateId,
                                respondentRep.getValue().getRepresentativeEmailAddress(),
                                buildPersonalisationRespondentRep(caseDetails)));

    }

    private Map<String, ?> buildPersonalisationRespondentRep(CaseDetails caseDetails) {
        return Map.of(
                CLAIMANT, caseDetails.getCaseData().getClaimant(),
                RESPONDENTS, getRespondentNames(caseDetails.getCaseData()),
                RESPONDENT_NAMES, getRespondentNames(caseDetails.getCaseData()),
                CASE_NUMBER, caseDetails.getCaseData().getEthosCaseReference(),
                HEARING_DATE, getNearestHearingToReferral(caseDetails.getCaseData(), "Not set"),
                LINK_TO_EXUI, emailService.getExuiCaseLink(caseDetails.getCaseId()),
                EXUI_CASE_DETAILS_LINK, emailService.getExuiCaseLink(caseDetails.getCaseId())
        );
    }

    private void sendClaimantResponseConfirmationEmail(CaseDetails caseDetails, String userToken) {
        String email = userIdamService.getUserDetails(userToken).getEmail();
        if (YES.equals(caseDetails.getCaseData().getClaimantNotificationCopyToOtherParty())) {
            emailService.sendEmail(acknowledgeEmailYesTemplateId, email, buildPersonalisationYes(caseDetails));
        } else {
            emailService.sendEmail(acknowledgeEmailNoTemplateId, email,
                    buildResponsePersonalisationNo(caseDetails, CLAIMANT_TITLE));
        }
    }

    public void saveClaimantResponse(CaseData caseData) {
        SendNotificationType sendNotificationType = getSelectedClaimantNotification(caseData).getValue();
        if (CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            sendNotificationType.setRespondCollection(new ArrayList<>());
        }
        List<PseResponseTypeItem> responses = sendNotificationType.getRespondCollection();
        PseResponseType response = PseResponseType.builder()
                .from("Claimant Representative")
                .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                .response(caseData.getClaimantNotificationResponseText())
                .hasSupportingMaterial(caseData.getClaimantNotificationSupportingMaterial())
                .supportingMaterial(caseData.getClaimantNotificationDocuments())
                .copyToOtherParty(caseData.getClaimantNotificationCopyToOtherParty())
                .copyNoGiveDetails(caseData.getClaimantNotificationsCopyNoDetails())
                .build();
        responses.add(PseResponseTypeItem.builder()
                .value(response)
                .id(UUID.randomUUID().toString())
                .build());
        sendNotificationType.setSendNotificationResponsesCount(String.valueOf(responses.size()));

    }

    public List<String> validateClaimantInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isEmpty(caseData.getClaimantNotificationResponseText())
            && (StringUtils.isEmpty(caseData.getClaimantNotificationSupportingMaterial())
                || NO.equals(caseData.getClaimantNotificationSupportingMaterial()))) {
            errors.add(GIVE_MISSING_DETAIL);
        }
        return errors;
    }

    public void clearClaimantNotificationDetails(CaseData caseData) {
        caseData.setClaimantNotificationResponseText(null);
        caseData.setClaimantNotificationSupportingMaterial(null);
        caseData.setClaimantNotificationDocuments(null);
        caseData.setClaimantNotificationsCopyNoDetails(null);
    }

}
