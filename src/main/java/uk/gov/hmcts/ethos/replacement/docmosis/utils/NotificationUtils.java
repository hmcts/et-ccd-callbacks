package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_ORGANISATION_RESPONSE_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_PARAMETERS_TO_NOTIFY_ORGANISATION_FOR_REP_UPDATE;

@Slf4j
public final class NotificationUtils {

    private static final String OLD_LOWERCASE = "old";
    private static final String NEW_LOWERCASE = "new";

    private NotificationUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Indicates whether the given case contains sufficient information
     * to allow notification messages to be sent.
     *
     * <p>A case is considered valid for notification when it includes the
     * minimum required identifiers and parties needed to construct and
     * deliver a notification.</p>
     *
     * <p>This method performs a defensive validation only and does not
     * log, throw exceptions, or modify the provided case details.</p>
     *
     * @param caseDetails the case details to evaluate
     * @return {@code true} if the case is non-null and contains all required
     *         data for sending notifications; {@code false} otherwise
     */
    public static boolean isCaseValidForNotification(CaseDetails caseDetails) {
        return ObjectUtils.isNotEmpty(caseDetails)
                && StringUtils.isNotBlank(caseDetails.getCaseId())
                && ObjectUtils.isNotEmpty(caseDetails.getCaseData())
                && StringUtils.isNotBlank(caseDetails.getCaseData().getEthosCaseReference())
                && StringUtils.isNotBlank(caseDetails.getCaseData().getClaimant())
                && CollectionUtils.isNotEmpty(caseDetails.getCaseData().getRespondentCollection());
    }

    /**
     * Determines whether a notification can be sent to a representative’s organisation.
     *
     * <p>A representative is considered eligible for organisation-level notification
     * when the representative exists, has an identifier, and is associated with a
     * respondent organisation that has a valid organisation ID.</p>
     *
     * <p>This method performs a defensive, read-only check and does not log,
     * throw exceptions, or modify the supplied representative.</p>
     *
     * @param representative the representative to evaluate for organisation
     *                       notification eligibility
     * @return {@code true} if sufficient representative and organisation
     *         information is present to allow a notification to be sent;
     *         {@code false} otherwise
     */
    public static boolean canNotifyRepresentativeOrganisation(RepresentedTypeRItem representative) {
        return ObjectUtils.isNotEmpty(representative)
                && StringUtils.isNotBlank(representative.getId())
                && ObjectUtils.isNotEmpty(representative.getValue())
                && ObjectUtils.isNotEmpty(representative.getValue().getRespondentOrganisation())
                && StringUtils.isNotBlank(representative.getValue().getRespondentOrganisation().getOrganisationID());
    }

    /**
     * Determines whether an organisation can be notified about a representative
     * update for a given case.
     *
     * <p>The organisation is eligible for notification only if all required
     * identifiers are provided and the organisation lookup response contains
     * a successful result with a resolvable superuser email address.</p>
     *
     * <p>The notification context (old or new organisation) is derived from
     * the supplied Notice of Change (NoC) type.</p>
     *
     * <p>This method performs defensive validation and fails safely by logging
     * warnings rather than throwing exceptions.</p>
     *
     * @param caseId the unique identifier of the case
     * @param orgId the identifier of the organisation to be notified
     * @param nocType the Notice of Change (NoC) type indicating whether the
     *                organisation represents the old or new party
     * @param orgResponse the response returned from the organisation lookup service
     * @return {@code true} if the organisation can be notified for the
     *         representative update; {@code false} otherwise
     */
    public static boolean canNotifyOrganisationForRepresentativeUpdate(String caseId,
                                                                       String orgId,
                                                                       String nocType,
                                                                       ResponseEntity<RetrieveOrgByIdResponse>
                                                                               orgResponse) {
        if (StringUtils.isBlank(caseId) || StringUtils.isBlank(orgId) || StringUtils.isBlank(nocType)) {
            String tmpCaseId = StringUtils.isBlank(caseId) ? StringUtils.EMPTY : caseId;
            log.warn(WARNING_INVALID_PARAMETERS_TO_NOTIFY_ORGANISATION_FOR_REP_UPDATE, tmpCaseId);
            return false;
        }
        final String orgType = NOC_TYPE_REMOVAL.equals(nocType) ? OLD_LOWERCASE : NEW_LOWERCASE;
        if (ObjectUtils.isEmpty(orgResponse)
                || !orgResponse.getStatusCode().is2xxSuccessful()
                || ObjectUtils.isEmpty(orgResponse.getBody())
                || ObjectUtils.isEmpty(orgResponse.getBody().getSuperUser())
                || StringUtils.isBlank(orgResponse.getBody().getSuperUser().getEmail())) {
            RetrieveOrgByIdResponse body = ObjectUtils.isEmpty(orgResponse) ? null : orgResponse.getBody();
            HttpStatusCode statusCode = ObjectUtils.isEmpty(orgResponse) ? null : orgResponse.getStatusCode();
            log.warn(WARNING_INVALID_ORGANISATION_RESPONSE_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE, orgType, orgId,
                    statusCode, body, caseId);
            return false;
        }
        return true;
    }
}
