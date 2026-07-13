package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

@Slf4j
public final class NotificationUtils {

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
    public static boolean canNotifyRespondentRepresentativeOrganisation(RepresentedTypeRItem representative) {
        return ObjectUtils.isNotEmpty(representative)
                && StringUtils.isNotBlank(representative.getId())
                && ObjectUtils.isNotEmpty(representative.getValue())
                && ObjectUtils.isNotEmpty(representative.getValue().getRespondentOrganisation())
                && StringUtils.isNotBlank(representative.getValue().getRespondentOrganisation().getOrganisationID());
    }

    /**
     * Attempts to retrieve the organisation ID associated with the given claimant representative.
     *
     * <p>If the representative, their organisation details, or the organisation ID
     * is null, empty, or blank, this method returns an empty string.</p>
     *
     * @param claimantRepresentative the claimant representative whose organisation ID is to be retrieved
     * @return the organisation ID if available; otherwise an empty string
     */
    public static String findClaimantRepresentativeOrganisationId(RepresentedTypeC claimantRepresentative) {
        return ObjectUtils.isEmpty(claimantRepresentative)
                || ObjectUtils.isEmpty(claimantRepresentative.getMyHmctsOrganisation())
                || StringUtils.isBlank(claimantRepresentative.getMyHmctsOrganisation().getOrganisationID())
                ? StringUtils.EMPTY : claimantRepresentative.getMyHmctsOrganisation().getOrganisationID();
    }

}
