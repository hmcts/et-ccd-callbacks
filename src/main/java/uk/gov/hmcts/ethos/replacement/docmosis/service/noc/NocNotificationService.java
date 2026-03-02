package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NotificationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_FAILED_TO_SEND_EMAIL_ORGANISATION_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.WARNING_CLAIMANT_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_ADDITION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_CLAIMANT_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_TRIBUNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_REMOVAL_OF_REPRESENTATIVE_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_CLAIMANT_NOT_NOTIFIED_OF_REMOVAL_OF_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_TRIBUNAL_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CLAIMANT_EMAIL_CLAIMANT_NOT_NOTIFIED_FOR_REMOVAL_OF_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_PARTY_NAME_TO_NOTIFY_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_REPRESENTATIVE_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_REP_EMAIL_NOTIFY_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_MISSING_RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_RESPONDENT_NAME_MISSING_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_TRIBUNAL_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantRepresentedByMyHmctsOrganisation;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildNoCPersonalisation;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPersonalisationWithPartyName;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation;

/**
 * Service to support the notification of change journey with email notifications.
 */
@RequiredArgsConstructor
@Service("nocNotificationService")
@Slf4j
public class NocNotificationService {
    private final EmailService emailService;
    private final NocRespondentHelper nocRespondentHelper;
    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final EmailNotificationService emailNotificationService;
    private final CaseAccessService caseAccessService;

    @Value("${template.nocNotification.respondent}")
    private String respondentTemplateId;
    @Value("${template.nocNotification.claimant}")
    private String claimantTemplateId;
    @Value("${template.nocNotification.claimantRepAssigned}")
    private String claimantRepAssignedTemplateId;
    @Value("${template.nocNotification.respondent-solicitor.previous}")
    private String previousRespondentSolicitorTemplateId;
    @Value("${template.nocNotification.respondent-solicitor.new}")
    private String newRespondentSolicitorTemplateId;
    @Value("${template.nocNotification.tribunal}")
    private String tribunalTemplateId;
    @Value("${template.nocNotification.org-admin-not-representing}")
    private String nocOrgAdminNotRepresentingTemplateId;
    @Value("${template.nocNotification.noc-legal-rep-no-longer-assigned}")
    private String nocLegalRepNoLongerAssignedTemplateId;
    @Value("${template.nocNotification.noc-citizen-no-longer-represented}")
    private String nocCitizenNoLongerRepresentedTemplateId;
    @Value("${template.nocNotification.noc-other-party-not-represented}")
    private String nocOtherPartyNotRepresentedTemplateId;

    private static final String LEGAL_REP_NAME = "legalRepName";
    private static final String LEGAL_REP_ORG = "legalRepOrg";
    private static final String LINK_TO_CIT_UI = "linkToCitUI";
    private static final String PARTY_NAME = "party_name";

    public void sendRespondentRepresentationUpdateNotifications(CaseDetails caseDetails,
                                                                List<RepresentedTypeRItem> representatives,
                                                                String nocType) {
        if (CollectionUtils.isEmpty(representatives) || StringUtils.isBlank(nocType)) {
            return;
        }
        for (RepresentedTypeRItem representative : representatives) {
            RespondentSumTypeItem respondent = RespondentRepresentativeUtils.findRespondentByRepresentative(
                    caseDetails.getCaseData(), representative);
            if (!RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }
            assert respondent != null;
            // sending respondent representative removal notification email to claimant
            notifyClaimantOfRespondentRepresentativeUpdate(caseDetails, respondent.getValue().getRespondentName());

            // sending notification email to organisation admin of the representative
            notifyOrganisationOfRespondentRepresentativeUpdate(caseDetails, representative,
                    respondent.getValue().getRespondentName(), nocType);

            // sending notification e-mail to tribunal
            notifyTribunalOfRespondentRepresentativeUpdate(caseDetails, nocType);

            // sending notification e-mail to respondent
            notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);

            // sending email to the new legal representative
            if (NOC_TYPE_ADDITION.equals(nocType)) {
                notifyRepresentativeOfNewAssignment(caseDetails, respondent.getValue().getRespondentName(),
                        representative);
            }
        }
    }

    /**
     * Sends a notification to a respondent when their legal representative
     * has been updated on a case.
     *
     * <p>The notification is sent only when the case is valid for notifications,
     * the respondent is valid, and a respondent email address is available.</p>
     *
     * <p>No notification will be sent if required case or respondent information
     * is missing, or if the respondent does not have a notification email
     * address configured.</p>
     *
     * <p>This method performs defensive validation and fails safely by logging
     * warnings rather than throwing exceptions.</p>
     *
     * @param caseDetails the case details containing case reference and party
     *                    information
     * @param respondent the respondent whose representative has been updated
     */
    public void notifyRespondentOfRepresentativeUpdate(CaseDetails caseDetails,
                                                       RespondentSumTypeItem respondent) {
        // sending notification e-mail to respondent
        if (!NotificationUtils.isCaseValidForNotification(caseDetails)) {
            String caseId = ObjectUtils.isEmpty(caseDetails) || StringUtils.isBlank(caseDetails.getCaseId())
                    ? StringUtils.EMPTY : caseDetails.getCaseId();
            log.warn(WARNING_INVALID_CASE_DETAILS, caseId);
            return;
        }
        if (!RespondentUtils.isValidRespondent(respondent)) {
            log.warn(WARNING_INVALID_RESPONDENT, caseDetails.getCaseId());
            return;
        }
        if (StringUtils.isBlank(respondent.getValue().getRespondentEmail())) {
            log.warn(WARNING_MISSING_RESPONDENT_EMAIL_ADDRESS, caseDetails.getCaseId());
            return;
        }
        Map<String, String> personalisation = buildNoCPersonalisation(caseDetails,
                respondent.getValue().getRespondentName());
        try {
            emailService.sendEmail(respondentTemplateId, respondent.getValue().getRespondentEmail(),
                    personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT, caseDetails.getCaseId(), e.getMessage());
        }
    }

    /**
     * Sends a notification to the claimant when a respondent’s representative
     * has been updated on a case.
     *
     * <p>The notification is sent only when the case contains sufficient data
     * for notifications, a respondent name is provided, and a claimant
     * notification email address can be resolved.</p>
     *
     * <p>No notification will be sent if the claim was created by a caseworker
     * (system user), if required case or respondent information is missing,
     * or if the claimant email address cannot be determined.</p>
     *
     * <p>This method performs defensive checks and fails safely by logging
     * warnings rather than throwing exceptions.</p>
     *
     * @param caseDetails the case details containing claimant, respondent,
     *                    and case reference information
     * @param respondentName the name of the respondent whose representative
     *                       has been updated
     */
    public void notifyClaimantOfRespondentRepresentativeUpdate(CaseDetails caseDetails, String respondentName) {
        if (!NotificationUtils.isCaseValidForNotification(caseDetails)) {
            String caseId = ObjectUtils.isNotEmpty(caseDetails) && StringUtils.isNotBlank(caseDetails.getCaseId())
                    ? caseDetails.getCaseId() : StringUtils.EMPTY;
            log.warn(WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESPONDENT_REP_UPDATE, caseId);
            return;
        }
        if (StringUtils.isBlank(respondentName)) {
            log.warn(WARNING_RESPONDENT_NAME_MISSING_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE, caseDetails.getCaseId());
            return;
        }
        // should not send email to claimant if claim created by caseworker
        if (isClaimantNonSystemUser(caseDetails.getCaseData())) {
            return;
        }
        String claimantEmail = ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetails);
        if (isNullOrEmpty(claimantEmail)) {
            log.warn(WARNING_CLAIMANT_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE, caseDetails.getCaseId());
            return;
        }
        String citUILink = ObjectUtils.isEmpty(caseDetails.getCaseData().getRepresentativeClaimantType())
                ? emailService.getExuiCaseLink(caseDetails.getCaseId())
                : emailService.getCitizenCaseLink(caseDetails.getCaseId());
        var personalisation = buildPersonalisationWithPartyName(caseDetails, respondentName, citUILink);
        try {
            emailService.sendEmail(claimantTemplateId, claimantEmail, personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT, caseDetails.getCaseId(), e.getMessage());
        }
    }

    /**
     * Sends a notification to a respondent organisation when a respondent’s
     * representative has been added or removed via a Notice of Change (NoC).
     *
     * <p>The notification is sent to the organisation’s superuser email address,
     * provided that:</p>
     * <ul>
     *   <li>The case contains sufficient data for notifications</li>
     *   <li>The representative is associated with a valid organisation</li>
     *   <li>The organisation lookup is successful and a superuser email is available</li>
     * </ul>
     *
     * <p>The notification template and personalisation differ depending on
     * whether the NoC represents a removal (previous representative) or an
     * addition (new representative).</p>
     *
     * <p>This method performs defensive validation and fails safely by logging
     * warnings rather than throwing exceptions.</p>
     *
     * @param caseDetails the case details containing party and reference information
     * @param representative the respondent representative associated with the organisation
     * @param partyName the name of the relevant party used for notification personalisation
     * @param nocType the Notice of Change (NoC) type indicating whether the
     *                representative is being added or removed
     */
    public void notifyOrganisationOfRespondentRepresentativeUpdate(CaseDetails caseDetails,
                                                                   RepresentedTypeRItem representative,
                                                                   String partyName,
                                                                   String nocType) {
        if (!NotificationUtils.isCaseValidForNotification(caseDetails)) {
            String caseId = ObjectUtils.isEmpty(caseDetails) ? StringUtils.EMPTY : caseDetails.getCaseId();
            log.warn(WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE, caseId, nocType);
            return;
        }
        if (!NotificationUtils.canNotifyRepresentativeOrganisation(representative)) {
            log.warn(WARNING_INVALID_REPRESENTATIVE_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE,
                    caseDetails.getCaseId(), nocType);
            return;
        }
        String organisationId = representative.getValue().getRespondentOrganisation().getOrganisationID();
        ResponseEntity<RetrieveOrgByIdResponse> organisationResponse = getOrganisationById(organisationId);
        if (!NotificationUtils.canNotifyOrganisationForRepresentativeUpdate(caseDetails.getCaseId(), organisationId,
                nocType, organisationResponse)) {
            return;
        }
        Map<String, String> personalisation;
        String templateId;
        if (NOC_TYPE_REMOVAL.equals(nocType)) {
            personalisation = buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
            templateId = previousRespondentSolicitorTemplateId;
        } else {
            String citUrl = emailService.getCitizenCaseLink(caseDetails.getCaseId());
            personalisation = buildPersonalisationWithPartyName(caseDetails, partyName, citUrl);
            templateId = newRespondentSolicitorTemplateId;
        }
        try {
            assert organisationResponse.getBody() != null;
            emailService.sendEmail(
                    templateId,
                    organisationResponse.getBody().getSuperUser().getEmail(),
                    personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }

    /**
     * Sends a notification to the tribunal when a respondent’s representative
     * has been added or removed via a Notice of Change (NoC).
     *
     * <p>The notification is sent only if the case contains sufficient
     * information for notifications and a tribunal correspondence email
     * address is available.</p>
     *
     * <p>If the required case details or tribunal email address are missing,
     * no notification is sent and a warning is logged.</p>
     *
     * <p>This method performs defensive validation and fails safely by logging
     * warnings rather than throwing exceptions.</p>
     *
     * @param caseDetails the case details containing reference and tribunal
     *                    correspondence information
     * @param notificationType the type of notification (e.g. addition or removal)
     *                         used for logging and contextual purposes
     */
    public void notifyTribunalOfRespondentRepresentativeUpdate(CaseDetails caseDetails, String notificationType) {
        if (!NotificationUtils.isCaseValidForNotification(caseDetails)) {
            String caseId = ObjectUtils.isEmpty(caseDetails) || StringUtils.isBlank(caseDetails.getCaseId())
                    ? StringUtils.EMPTY : caseDetails.getCaseId();
            log.warn(WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_TRIBUNAL_FOR_RESPONDENT_REP_UPDATE, caseId,
                    notificationType);
            return;
        }
        if (StringUtils.isBlank(caseDetails.getCaseData().getTribunalCorrespondenceEmail())) {
            log.warn(WARNING_TRIBUNAL_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE, caseDetails.getCaseId(),
                    notificationType);
            return;
        }
        Map<String, String> personalisation = NocNotificationHelper.buildTribunalPersonalisation(
                caseDetails.getCaseData());
        try {
            emailService.sendEmail(tribunalTemplateId, caseDetails.getCaseData().getTribunalCorrespondenceEmail(),
                    personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_TRIBUNAL, caseDetails.getCaseId(), e.getMessage());
        }
    }

    /**
     * Sends a notification to a representative informing them that they have been
     * newly assigned to a party on a case.
     *
     * <p>The notification is sent only if:</p>
     * <ul>
     *     <li>The case contains sufficient information for notifications</li>
     *     <li>A valid party name is provided for personalisation</li>
     *     <li>The representative has a valid email address</li>
     * </ul>
     *
     * <p>If any required information is missing, no notification is sent and
     * a warning is logged. This method performs defensive validation and fails
     * safely without throwing exceptions.</p>
     *
     * @param caseDetails the case details containing reference and party information
     * @param partyName the name of the party to which the representative has been assigned,
     *                  used for notification personalisation
     * @param representative the representative who has been newly assigned and
     *                       will receive the notification
     */
    public void notifyRepresentativeOfNewAssignment(CaseDetails caseDetails,
                                                    String partyName,
                                                    RepresentedTypeRItem representative) {
        if (!NotificationUtils.isCaseValidForNotification(caseDetails)) {
            String caseId = ObjectUtils.isEmpty(caseDetails) ? StringUtils.EMPTY : caseDetails.getCaseId();
            log.warn(WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_NEW_REPRESENTATIVE, caseId);
            return;
        }
        if (StringUtils.isBlank(partyName)) {
            log.warn(WARNING_INVALID_PARTY_NAME_TO_NOTIFY_NEW_REPRESENTATIVE, caseDetails.getCaseId());
            return;
        }
        if (Objects.isNull(representative)
                || ObjectUtils.isEmpty(representative.getValue())
                || StringUtils.isBlank(representative.getValue().getRepresentativeEmailAddress())) {
            log.warn(WARNING_INVALID_REP_EMAIL_NOTIFY_NEW_REPRESENTATIVE, caseDetails.getCaseId());
            return;
        }
        Map<String, String> personalisation = buildPersonalisationWithPartyName(caseDetails, partyName,
                emailService.getExuiCaseLink(caseDetails.getCaseId()));
        try {
            emailService.sendEmail(newRespondentSolicitorTemplateId,
                    representative.getValue().getRepresentativeEmailAddress(), personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_NEW_REPRESENTATIVE, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }

    /**
     * Notifies the claimant by email that their legal representative has been removed from the case.
     *
     * <p>This method performs validation and notification in the following steps:
     * <ul>
     *     <li>Validates that the {@code caseDetails} object is suitable for notification
     *     using {@code NotificationUtils.isCaseValidForNotification}. If invalid, a warning
     *     is logged and processing stops.</li>
     *     <li>Attempts to retrieve the claimant's email address from the case data.
     *     If no valid email address is found, a warning is logged and no notification is sent.</li>
     *     <li>Builds the email personalisation map, including claimant details and a link
     *     to the Citizen Hub for the relevant case.</li>
     *     <li>Sends the notification email using the configured claimant template.</li>
     * </ul>
     *
     * <p>If any exception occurs while retrieving the claimant email address or sending
     * the email, the error is logged and the method exits without throwing the exception
     * further, ensuring the calling flow is not interrupted.
     *
     * @param caseDetails the {@link CaseDetails} containing the case and claimant information
     *                    required to send the notification
     */
    public void notifyClaimantOfRepresentationRemoval(CaseDetails caseDetails) {
        if (!NotificationUtils.isCaseValidForNotification(caseDetails)) {
            String caseId = ObjectUtils.isEmpty(caseDetails) ? StringUtils.EMPTY : caseDetails.getCaseId();
            log.warn(WARNING_INVALID_CASE_DETAILS_CLAIMANT_NOT_NOTIFIED_OF_REMOVAL_OF_REPRESENTATIVE, caseId);
            return;
        }
        String claimantEmailAddress;
        try {
            claimantEmailAddress = ClaimantUtils.getClaimantEmailAddress(caseDetails.getCaseData());
        } catch (NotFoundException e) {
            log.warn(WARNING_INVALID_CLAIMANT_EMAIL_CLAIMANT_NOT_NOTIFIED_FOR_REMOVAL_OF_REPRESENTATIVE,
                    caseDetails.getCaseId(), e.getMessage());
            return;
        }
        String claimant = StringUtils.isBlank(ClaimantUtils.getClaimant(caseDetails.getCaseData()))
                ? StringUtils.EMPTY : ClaimantUtils.getClaimant(caseDetails.getCaseData());
        claimantEmailAddress = StringUtils.isBlank(claimantEmailAddress) ? StringUtils.EMPTY : claimantEmailAddress;
        Map<String, String> personalisation = buildNoCPersonalisation(caseDetails, claimant);
        personalisation.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseDetails.getCaseId()));
        try {
            emailService.sendEmail(claimantTemplateId, claimantEmailAddress, personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_REMOVAL_OF_REPRESENTATIVE_CLAIMANT, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }

    public RetrieveOrgByIdResponse getOrganisationResponse(String orgId, boolean isNewOrg) {
        ResponseEntity<RetrieveOrgByIdResponse> getOrgResponse = getOrganisationById(orgId);
        HttpStatusCode statusCode = getOrgResponse.getStatusCode();
        if (!HttpStatus.OK.equals(statusCode)) {
            String orgType = isNewOrg ? "new" : "old";
            log.error("Cannot retrieve {} org by id {} [{}] {}", orgType, orgId, statusCode, getOrgResponse.getBody());
            return null;
        }
        RetrieveOrgByIdResponse resBody = getOrgResponse.getBody();
        if (resBody == null) {
            return null;
        }
        if (resBody.getSuperUser() == null || isNullOrEmpty(resBody.getSuperUser().getEmail())) {
            String orgType = isNewOrg ? "New" : "Previous";
            log.warn("{} Org {} is missing org admin email", orgType, orgId);
            return null;
        }
        return resBody;
    }

    private void sendEmailToOldOrgAdmin(String orgId, CaseData caseDataPrevious) {

        RetrieveOrgByIdResponse resBody = getOrganisationResponse(orgId, false);
        if (ObjectUtils.isEmpty(resBody)) {
            return;
        }
        Map<String, String> personalisation = buildPreviousRespondentSolicitorPersonalisation(caseDataPrevious);
        try {
            emailService.sendEmail(
                    previousRespondentSolicitorTemplateId,
                    resBody.getSuperUser().getEmail(),
                    personalisation);
        } catch (Exception e) {
            LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_ORGANISATION_ADMIN, orgId, e);
        }
    }

    private void sendEmailToNewOrgAdmin(String orgId, CaseDetails caseDetailsNew, String partyName) {
        RetrieveOrgByIdResponse resBody = getOrganisationResponse(orgId, true);
        if (ObjectUtils.isEmpty(resBody)) {
            return;
        }
        String citUrl = emailService.getCitizenCaseLink(caseDetailsNew.getCaseId());
        Map<String, String> personalisation = buildPersonalisationWithPartyName(caseDetailsNew, partyName, citUrl);
        emailService.sendEmail(newRespondentSolicitorTemplateId, resBody.getSuperUser().getEmail(), personalisation);
    }

    public void sendNotificationOfChangeEmails(CaseDetails caseDetailsPrevious,
                             CaseDetails caseDetailsNew,
                             ChangeOrganisationRequest changeRequest) {
        DynamicFixedListType caseRoleId = changeRequest.getCaseRoleId();
        CaseData caseDataNew = caseDetailsNew.getCaseData();
        CaseData caseDataPrevious = caseDetailsPrevious.getCaseData();
        String partyName;
        String newRepEmailAddress = null;

        if (caseRoleId.getValue().getCode().equals(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())) {
            // send claimant noc change email
            partyName = caseDataPrevious.getClaimant();
            if (caseDataNew.getRepresentativeClaimantType() != null) {
                newRepEmailAddress = caseDataNew.getRepresentativeClaimantType().getRepresentativeEmailAddress();
                handleClaimantNocEmails(caseDetailsNew, partyName);
            }
        } else {
            // send respondent noc change email
            handleRespondentNocEmails(caseDetailsPrevious, caseDetailsNew, changeRequest);
            partyName = NocNotificationHelper.getRespondentNameForNewSolicitor(changeRequest, caseDataNew);
        }

        // send organisation noc change email
        handleOrganisationNocEmails(caseDataPrevious, caseDetailsNew, changeRequest, partyName, newRepEmailAddress);

        // send tribunal noc change email
        notifyTribunalOfRespondentRepresentativeUpdate(caseDetailsPrevious, NOC_TYPE_REMOVAL);
    }

    private void handleClaimantNocEmails(CaseDetails caseDetailsNew, String partyName) {
        CaseData caseDataNew = caseDetailsNew.getCaseData();

        List<CaseUserAssignment> caseUserAssignments =
                caseAccessService.getCaseUserAssignmentsById(caseDetailsNew.getCaseId());

        if (caseUserAssignments == null || caseUserAssignments.isEmpty()) {
            log.warn("In NocNotificationService : No case user assignments found for caseId {}",
                    caseDetailsNew.getCaseId());
            return;
        }

        // send respondents or respondent solicitors the claimant noc change email
        emailNotificationService.getRespondentsAndRepsEmailAddresses(caseDataNew, caseUserAssignments)
                .forEach((email, respondentId) -> {
                    String caseLink = StringUtils.isNotBlank(respondentId)
                            ? emailService.getSyrCaseLink(caseDetailsNew.getCaseId(), respondentId)
                            : emailService.getExuiCaseLink(caseDetailsNew.getCaseId());
                    emailService.sendEmail(claimantTemplateId, email,
                            buildPersonalisationWithPartyName(caseDetailsNew, partyName, caseLink));
                });

        // send claimant noc change email
        String claimantEmail = ClaimantUtils.getClaimantEmailAddress(caseDataNew);
        if (isNullOrEmpty(claimantEmail)) {
            log.warn("missing claimantEmail");
            return;
        }

        Map<String, String> personalisation = buildNoCPersonalisation(caseDetailsNew, partyName);
        personalisation.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseDetailsNew.getCaseId()));
        emailService.sendEmail(claimantRepAssignedTemplateId, claimantEmail, personalisation);
    }

    private void handleRespondentNocEmails(CaseDetails caseDetailsPrevious,
                                          CaseDetails caseDetailsNew,
                                          ChangeOrganisationRequest changeRequest) {
        CaseData caseDataNew = caseDetailsNew.getCaseData();
        CaseData caseDataPrevious = caseDetailsPrevious.getCaseData();
        String partyName = NocNotificationHelper.getRespondentNameForNewSolicitor(changeRequest, caseDataNew);
        // send claimant or claimant solicitor noc change email
        if (!isClaimantNonSystemUser(caseDataPrevious)
                || isClaimantRepresentedByMyHmctsOrganisation(caseDataPrevious)) {
            sendClaimantEmail(caseDetailsPrevious, caseDetailsNew, partyName);
        }

        // send respondent noc change email
        RespondentSumType respondent =
                NocNotificationHelper.getRespondent(changeRequest, caseDataPrevious, nocRespondentHelper);
        String respondentEmail = respondent == null ? null : respondent.getRespondentEmail();

        if (isNullOrEmpty(respondentEmail)) {
            log.warn("Missing respondentEmail");
            return;
        }

        Map<String, String> personalisation = buildNoCPersonalisation(caseDetailsPrevious,
                respondent.getRespondentName());
        emailService.sendEmail(respondentTemplateId, respondentEmail, personalisation);
    }

    private void handleOrganisationNocEmails(CaseData caseDataPrevious,
                                            CaseDetails caseDetailsNew,
                                            ChangeOrganisationRequest changeRequest,
                                            String partyName,
                                            String newRepEmailAddress) {

        if (changeRequest.getOrganisationToRemove() != null) {
            String previousOrgId = changeRequest.getOrganisationToRemove().getOrganisationID();
            sendEmailToOldOrgAdmin(previousOrgId, caseDataPrevious);
        }

        if (changeRequest.getOrganisationToAdd() != null) {
            String newOrgId = changeRequest.getOrganisationToAdd().getOrganisationID();
            sendEmailToNewOrgAdmin(newOrgId, caseDetailsNew, partyName);
        }

        // send email to the new legal representative
        if (StringUtils.isNotBlank(newRepEmailAddress)) {
            Map<String, String> personalisation = buildPersonalisationWithPartyName(caseDetailsNew, partyName,
                    emailService.getExuiCaseLink(caseDetailsNew.getCaseId()));
            emailService.sendEmail(newRespondentSolicitorTemplateId, newRepEmailAddress, personalisation);
        }
    }

    public void sendClaimantEmail(CaseDetails caseDetailsPrevious, CaseDetails caseDetailsNew, String partyName) {
        if (ObjectUtils.isEmpty(caseDetailsPrevious) || ObjectUtils.isEmpty(caseDetailsNew)) {
            log.error(EXCEPTION_CASE_DETAILS_NOT_FOUND);
            return;
        }
        RepresentedTypeC claimantRep = caseDetailsPrevious.getCaseData().getRepresentativeClaimantType();
        String email = ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetailsPrevious);
        if (isNullOrEmpty(email)) {
            log.warn(WARNING_CLAIMANT_EMAIL_NOT_FOUND, caseDetailsPrevious.getCaseId());
            return;
        }
        String citUILink = claimantRep != null
                ? emailService.getExuiCaseLink(caseDetailsNew.getCaseId())
                : emailService.getCitizenCaseLink(caseDetailsNew.getCaseId());
        var personalisation = buildPersonalisationWithPartyName(caseDetailsPrevious, partyName, citUILink);
        try {
            emailService.sendEmail(claimantTemplateId, email, personalisation);
        } catch (Exception e) {
            LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT, email, e);
        }
    }

    private ResponseEntity<RetrieveOrgByIdResponse> getOrganisationById(String orgId) {
        return organisationClient.getOrganisationById(
                adminUserService.getAdminUserToken(),
                authTokenGenerator.generate(),
                orgId);
    }

    public void sendEmailToOrgAdmin(CaseDetails caseDetails, RepresentedTypeC repCopy) {
        String organisationEmail = getOrganisationEmailWithID(repCopy);
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, repCopy.getNameOfRepresentative());
        try {
            emailService.sendEmail(
                nocOrgAdminNotRepresentingTemplateId,
                organisationEmail,
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    private String getOrganisationEmailWithID(RepresentedTypeC repCopy) {
        if (repCopy.getMyHmctsOrganisation() == null) {
            return null;
        }
        String organisationId = repCopy.getMyHmctsOrganisation().getOrganisationID();
        ResponseEntity<RetrieveOrgByIdResponse> organisationResponse = getOrganisationById(organisationId);
        if (organisationResponse.getBody() == null) {
            return null;
        }
        return organisationResponse.getBody().getSuperUser().getEmail();
    }

    public void sendEmailToRemovedLegalRep(CaseDetails caseDetails, RepresentedTypeC repCopy) {
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        try {
            emailService.sendEmail(
                nocLegalRepNoLongerAssignedTemplateId,
                repCopy.getRepresentativeEmailAddress(),
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    public void sendEmailToUnrepresentedParty(CaseDetails caseDetails, RepresentedTypeC repCopy) {
        String claimantEmailAddress;
        try {
            claimantEmailAddress = ClaimantUtils.getClaimantEmailAddress(caseDetails.getCaseData());
        } catch (NotFoundException e) {
            log.warn(WARNING_INVALID_CLAIMANT_EMAIL_CLAIMANT_NOT_NOTIFIED_FOR_REMOVAL_OF_REPRESENTATIVE,
                caseDetails.getCaseId(), e.getMessage());
            return;
        }

        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_ORG, repCopy.getNameOfOrganisation());
        personalisation.put(LINK_TO_CIT_UI, emailService.getCitizenCaseLink(caseDetails.getCaseId()));

        try {
            emailService.sendEmail(
                nocCitizenNoLongerRepresentedTemplateId,
                claimantEmailAddress,
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    public void sendEmailToOtherParty(CaseDetails caseDetails) {
        List<CaseUserAssignment> caseUserAssignments =
            caseAccessService.getCaseUserAssignmentsById(caseDetails.getCaseId());
        emailNotificationService
            .getRespondentsAndRepsEmailAddresses(caseDetails.getCaseData(), caseUserAssignments)
            .forEach((email, respondentId) -> sendRespondentEmail(caseDetails, email, respondentId));
    }

    private void sendRespondentEmail(CaseDetails caseDetails, String email, String respondentId) {
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(PARTY_NAME, CLAIMANT_TITLE);
        String caseLink = StringUtils.isNotBlank(respondentId)
            ? emailService.getSyrCaseLink(caseDetails.getCaseId(), respondentId)
            : emailService.getExuiCaseLink(caseDetails.getCaseId());
        personalisation.put(LINK_TO_CIT_UI, caseLink);
        emailService.sendEmail(
            nocOtherPartyNotRepresentedTemplateId,
            email,
            personalisation);
    }
}