package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse.SuperUser;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NotificationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_FAILED_TO_SEND_EMAIL_ORGANISATION_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.NEW_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.NEW_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.OLD_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.OLD_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.WARNING_CLAIMANT_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_ADDITION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_CLAIMANT_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_TRIBUNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_TRIBUNAL_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_CASE_DETAILS_TO_RESOLVE_ORGANISATION_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_PARTY_NAME_TO_NOTIFY_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_REPRESENTATIVE_TO_RESOLVE_ORGANISATION_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_REP_EMAIL_NOTIFY_NEW_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_MISSING_RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_ORGANISATION_SUPER_USER_EMAIL_NOT_FOUND_WITH_PARAMETERS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_RESPONDENT_NAME_MISSING_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_TRIBUNAL_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantRepresentedByMyHmctsOrganisation;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.addCommonEmailValues;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildNoCPersonalisation;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPersonalisationWithPartyName;

/**
 * Service to support the notification of change journey with email notifications.
 */
@RequiredArgsConstructor
@Service("nocNotificationService")
@Slf4j
public class NocNotificationService {
    private final EmailService emailService;
    private final NocRespondentHelper nocRespondentHelper;
    private final EmailNotificationService emailNotificationService;
    private final CaseAccessService caseAccessService;
    private final OrganisationService organisationService;

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

            if (NOC_TYPE_ADDITION.equals(nocType)) {
                // sending notification e-mail to respondent of new representation
                notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
                // sending email to the new legal representative
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
        try {
            Map<String, String> personalisation = buildNoCPersonalisation(caseDetails,
                    respondent.getValue().getRespondentName());
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
        String organisationSuperUserEmail =
                resolveRespondentRepresentativeOrganisationSuperuserEmail(caseDetails, representative, nocType);
        if (StringUtils.isBlank(organisationSuperUserEmail)) {
            return;
        }
        Map<String, String> personalisation;
        String templateId;
        if (NOC_TYPE_REMOVAL.equals(nocType)) {
            personalisation = addCommonEmailValues(caseDetails.getCaseData());
            templateId = previousRespondentSolicitorTemplateId;
        } else {
            String citUrl = emailService.getCitizenCaseLink(caseDetails.getCaseId());
            personalisation = buildPersonalisationWithPartyName(caseDetails, partyName, citUrl);
            templateId = newRespondentSolicitorTemplateId;
        }
        try {
            emailService.sendEmail(
                    templateId,
                    organisationSuperUserEmail,
                    personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }

    /**
     * Resolves the superuser email address of the respondent representative's organisation
     * for notification purposes.
     *
     * <p>This method performs a series of validation checks before attempting to retrieve
     * the organisation's superuser email:</p>
     * <ul>
     *     <li>Verifies that the case details are valid for notification,</li>
     *     <li>Verifies that the representative is eligible for organisation notification,</li>
     *     <li>Retrieves the organisation details using the representative's organisation ID,</li>
     *     <li>Validates that the organisation response allows resolution of the superuser email.</li>
     * </ul>
     *
     * <p>If any validation step fails, a warning is logged and an empty string is returned.
     * This method does not throw an exception for validation failures.</p>
     *
     * <p><strong>Assumption:</strong> The provided parameters are expected to be non-null.
     * This method relies on downstream validation utilities for eligibility checks.</p>
     *
     * @param caseDetails  the case details used to determine notification eligibility
     * @param representative the respondent representative whose organisation superuser email
     *                       is to be resolved
     * @param nocType the notice of change (NoC) type used for validation and logging context
     * @return the organisation superuser email address if all validation checks pass;
     *         otherwise, an empty string
     */
    public String resolveRespondentRepresentativeOrganisationSuperuserEmail(CaseDetails caseDetails,
                                                                            RepresentedTypeRItem representative,
                                                                            String nocType) {
        if (!NotificationUtils.isCaseValidForNotification(caseDetails)) {
            String caseId = ObjectUtils.isEmpty(caseDetails) ? StringUtils.EMPTY : caseDetails.getCaseId();
            log.warn(WARNING_INVALID_CASE_DETAILS_TO_RESOLVE_ORGANISATION_EMAIL, caseId, nocType);
            return StringUtils.EMPTY;
        }
        if (!NotificationUtils.canNotifyRespondentRepresentativeOrganisation(representative)) {
            log.warn(WARNING_INVALID_REPRESENTATIVE_TO_RESOLVE_ORGANISATION_EMAIL, caseDetails.getCaseId(), nocType);
            return StringUtils.EMPTY;
        }

        String organisationId = representative.getValue().getRespondentOrganisation().getOrganisationID();
        SuperUser superUser = organisationService.findSuperUserByOrganisationId(organisationId);
        if (superUser == null) {
            final String orgType = NOC_TYPE_REMOVAL.equals(nocType) ? OLD_LOWERCASE : NEW_LOWERCASE;
            log.warn(WARNING_ORGANISATION_SUPER_USER_EMAIL_NOT_FOUND_WITH_PARAMETERS, orgType, caseDetails.getCaseId());
            return StringUtils.EMPTY;
        }
        return superUser.getEmail();
    }

    /**
     * Attempts to retrieve the superuser email address for the claimant representative's organisation.
     *
     * <p>The method first resolves the organisation ID from the provided representative.
     * If the organisation ID is blank, or if the organisation response does not contain
     * a resolvable superuser email, an empty string is returned.</p>
     *
     * @param representative the claimant representative details used to resolve the organisation
     * @return the organisation superuser email if available; otherwise an empty string
     */
    public String findClaimantRepOrgSuperUserEmail(RepresentedTypeC representative) {
        String organisationId = NotificationUtils.findClaimantRepresentativeOrganisationId(representative);
        if (StringUtils.isBlank(organisationId)) {
            return StringUtils.EMPTY;
        }
        SuperUser superUser  = organisationService.findSuperUserByOrganisationId(organisationId);
        if (ObjectUtils.isEmpty(superUser)) {
            return StringUtils.EMPTY;
        }
        return superUser.getEmail();
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

    private void sendEmailToOldOrgAdmin(String orgId, CaseData caseDataPrevious) {

        SuperUser superUser = organisationService.findSuperUserByOrganisationId(orgId);
        if (ObjectUtils.isEmpty(superUser)) {
            log.warn("{} organisation {} is missing org admin email", OLD_CAPITALISED, orgId);
            return;
        }
        Map<String, String> personalisation = addCommonEmailValues(caseDataPrevious);
        try {
            emailService.sendEmail(
                    previousRespondentSolicitorTemplateId,
                    superUser.getEmail(),
                    personalisation);
        } catch (Exception e) {
            LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_ORGANISATION_ADMIN, e);
        }
    }

    private void sendEmailToNewOrgAdmin(String orgId, CaseDetails caseDetailsNew, String partyName) {
        SuperUser superUser = organisationService.findSuperUserByOrganisationId(orgId);
        if (ObjectUtils.isEmpty(superUser)) {
            log.warn("{} org {} is missing org admin email", NEW_CAPITALISED, orgId);
            return;
        }
        String citUrl = emailService.getCitizenCaseLink(caseDetailsNew.getCaseId());
        Map<String, String> personalisation = buildPersonalisationWithPartyName(caseDetailsNew, partyName, citUrl);
        emailService.sendEmail(newRespondentSolicitorTemplateId, superUser.getEmail(), personalisation);
    }

    public void sendNotificationOfChangeEmails(CaseDetails caseDetailsPrevious,
                                               CaseDetails caseDetailsNew,
                                               ChangeOrganisationRequest changeRequest,
                                               boolean sendToOtherParty) {
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
                handleClaimantNocEmails(caseDetailsNew, partyName, sendToOtherParty);
            }
        } else {
            // send respondent noc change email
            handleRespondentNocEmails(caseDetailsPrevious, caseDetailsNew, changeRequest, sendToOtherParty);
            partyName = NocNotificationHelper.getRespondentNameForNewSolicitor(changeRequest, caseDataNew);
        }

        // send organisation noc change email
        handleOrganisationNocEmails(caseDataPrevious, caseDetailsNew, changeRequest, partyName, newRepEmailAddress);

        // send tribunal noc change email
        notifyTribunalOfRespondentRepresentativeUpdate(caseDetailsPrevious, NOC_TYPE_REMOVAL);
    }

    private void handleClaimantNocEmails(CaseDetails caseDetailsNew, String partyName, boolean sendToOtherParty) {
        CaseData caseDataNew = caseDetailsNew.getCaseData();

        List<CaseUserAssignment> caseUserAssignments =
                caseAccessService.getCaseUserAssignmentsById(caseDetailsNew.getCaseId());

        if (caseUserAssignments == null || caseUserAssignments.isEmpty()) {
            log.warn("In NocNotificationService : No case user assignments found for caseId {}",
                    caseDetailsNew.getCaseId());
            return;
        }

        // send respondents or respondent solicitors the claimant noc change email
        if (sendToOtherParty) {
            emailNotificationService.getRespondentsAndRepsEmailAddresses(caseDataNew, caseUserAssignments)
                    .forEach((email, respondentId) -> {
                        String caseLink = StringUtils.isNotBlank(respondentId)
                                ? emailService.getSyrCaseLink(caseDetailsNew.getCaseId(), respondentId)
                                : emailService.getExuiCaseLink(caseDetailsNew.getCaseId());
                        emailService.sendEmail(claimantTemplateId, email,
                                buildPersonalisationWithPartyName(caseDetailsNew, partyName, caseLink));
                    });
        }
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
                                           ChangeOrganisationRequest changeRequest,
                                           boolean sendToOtherParty) {
        CaseData caseDataNew = caseDetailsNew.getCaseData();
        CaseData caseDataPrevious = caseDetailsPrevious.getCaseData();
        String partyName = NocNotificationHelper.getRespondentNameForNewSolicitor(changeRequest, caseDataNew);
        // send claimant or claimant solicitor noc change email
        if ((!isClaimantNonSystemUser(caseDataPrevious)
                || isClaimantRepresentedByMyHmctsOrganisation(caseDataPrevious) && sendToOtherParty)) {
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
            LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT, e);
        }
    }
}