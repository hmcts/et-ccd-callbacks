package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.noc.NocRespondentMapper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EMAIL_TYPE_TO_ORG_ADMIN_REMOVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.REMOVE_ORGANISATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveRepresentationService {

    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final NocRemoveRepresentationEmailService nocRemoveRepresentationEmailService;
    private final AdminUserService adminUserService;
    private final UserIdamService userIdamService;
    private final OrganisationService organisationService;

    private static final String CLASS_NAME = NocRemoveRepresentationService.class.getSimpleName();

    /**
     * Revokes the claimant's legal representative from the case and sends notification emails to all relevant parties.
     * This method performs the following actions:
     * - Retrieves the current claimant representative and organisation details.
     * - Revokes the claimant's legal representation in CCD.
     * - Marks the claimant as unrepresented in the case data.
     * - Sends notification emails to the organisation admin, removed legal representative, claimant, and all other
     *   respondents.
     *
     * @param caseDetails The case details containing the case data and ID.
     * @throws IllegalStateException if the claimant representative is missing in the case data.
     */
    public void revokeClaimantLegalRep(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        // get existing rep and organisation details for sending emails
        RepresentedTypeC existingClaimantRep = caseData.getRepresentativeClaimantType();
        if (existingClaimantRep == null) {
            throw new IllegalStateException(String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId()));
        }
        CaseDetails caseDetailsBeforeRepUpdate = CaseDataUtils.cloneCaseDetails(caseDetails);
        if (caseDetailsBeforeRepUpdate == null) {
            return;
        }
        // revoke claimant legal rep
        final String adminUserToken = adminUserService.getAdminUserToken();
        nocCcdService.revokeClaimantRepresentation(adminUserToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseData);

        final String orgName = existingClaimantRep.getNameOfOrganisation();
        final String orgEmailAddress = nocNotificationService.findClaimantRepOrgSuperUserEmail(existingClaimantRep);
        final String repName = existingClaimantRep.getNameOfRepresentative();
        final String repEmailAddress = existingClaimantRep.getRepresentativeEmailAddress();
        final String partyName = caseDetailsBeforeRepUpdate.getCaseData().getClaimant();
        // send email to organisation admin if his/her email exists
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetailsBeforeRepUpdate, orgEmailAddress, repName,
                EMAIL_TYPE_TO_ORG_ADMIN_REMOVED);
        // send email to removed legal rep if his/her email exists
        nocRemoveRepresentationEmailService.sendEmailToRemovedLegalRep(caseDetailsBeforeRepUpdate, repEmailAddress);
        // send email to unrepresented party, i.e. claimant if his/her email exists
        nocRemoveRepresentationEmailService.sendEmailToUnrepresentedClaimant(caseDetailsBeforeRepUpdate, orgName);
        // send email to other party, i.e. respondents if any exists
        nocRemoveRepresentationEmailService.sendEmailToOtherRespondents(caseDetailsBeforeRepUpdate,
                List.of(), partyName);
    }

    /**
     * Revokes the respondent's legal representative(s) from the case and sends notification emails to all relevant
     * parties.
     * This method performs the following actions:
     * - Identifies the respondent representatives to be revoked based on the user token and case data.
     * - Revokes and removes the respondent representatives from the case.
     * - Sends notification emails to the organisation admin, removed legal representatives, unrepresented respondents,
     *   claimant, and other respondents.
     *
     * @param caseDetails The case details containing the case data and ID.
     * @param userToken The user token of the requester performing the revocation.
     * @throws IllegalStateException if no respondent representatives are found to revoke.
     */
    public void revokeRespondentLegalRep(CaseDetails caseDetails, String userToken) {
        final String methodName = "revokeRespondentLegalRep";
        if (ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isBlank(caseDetails.getCaseId())
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRespondentCollection())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())) {
            String caseId = caseDetails != null && caseDetails.getCaseId() != null ? caseDetails.getCaseId()
                    : StringUtils.EMPTY;
            String exceptionMessage = String.format(EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE, caseId);
            throw new GenericRuntimeException(
                    new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                            caseId, CLASS_NAME, methodName));
        }
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        if (ObjectUtils.isEmpty(userDetails)) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId());
            throw new GenericRuntimeException(
                    new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                            caseDetails.getCaseId(), CLASS_NAME, methodName));
        }
        List<RepresentedTypeRItem> repListToRevoke = getRespondentRepListToRevoke(caseDetails, userToken);
        if (CollectionUtils.isEmpty(repListToRevoke)) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId());
            throw new GenericRuntimeException(
                    new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                            caseDetails.getCaseId(), CLASS_NAME, methodName));
        }
        OrganisationsResponse organisation = organisationService.findOrganisationByIdamUserId(userDetails.getUid());
        String orgName = StringUtils.EMPTY;
        if (ObjectUtils.isNotEmpty(organisation)) {
            orgName = organisation.getName();
        }
        CaseDetails caseDetailsBeforeRepUpdate = CaseDataUtils.cloneCaseDetails(caseDetails);
        // revoke respondent legal rep
        nocRespondentRepresentativeService.revokeAndRemoveRespondentRepresentatives(
                caseDetails,
                repListToRevoke
        );
        // get existing rep and organisation details for sending emails
        final String orgEmailAddress =
            nocNotificationService.resolveRespondentRepresentativeOrganisationSuperuserEmail(
                caseDetails,
                repListToRevoke.getFirst(),
                NOC_TYPE_REMOVAL
            );
        final String repName = userDetails.getName();
        final List<String> removedRepsEmailAddresses = NocRespondentMapper.getRepresentativeEmails(repListToRevoke);
        // send email to organisation admin when organisation is revoked
        if (REMOVE_ORGANISATION.equals(caseDetails.getCaseData().getNocRemoveRepOption())) {
            nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetailsBeforeRepUpdate, orgEmailAddress,
                    repName, EMAIL_TYPE_TO_ORG_ADMIN_REMOVED);
        } else if (ObjectUtils.isNotEmpty(organisation) && !OrganisationUtils.hasRemainingRespondentReps(
                caseDetails.getCaseData(), organisation.getOrganisationIdentifier())) {
            nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetailsBeforeRepUpdate, orgEmailAddress,
                    repName, EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT);
        }
        // send email to removed legal rep
        nocRemoveRepresentationEmailService.sendEmailToListOfRemovedLegalRep(caseDetailsBeforeRepUpdate,
                removedRepsEmailAddresses);
        // send email to unrepresented party, i.e. this respondent
        List<RespondentSumTypeItem> repsRevokedRespondents = RespondentRepresentativeUtils
                .findRespondentsByRepresentatives(caseDetailsBeforeRepUpdate.getCaseData(), repListToRevoke);
        nocRemoveRepresentationEmailService.sendRepresentationRemovedEmailToRespondents(caseDetailsBeforeRepUpdate,
                repsRevokedRespondents, orgName);
        // send email to claimant
        final String partyName = NocRespondentMapper.getRespondentPartyNames(repListToRevoke);
        nocRemoveRepresentationEmailService.sendEmailToOtherPartyClaimant(caseDetailsBeforeRepUpdate, partyName);
        // send email to other respondent
        nocRemoveRepresentationEmailService.sendEmailToOtherRespondents(caseDetailsBeforeRepUpdate,
                repsRevokedRespondents, partyName);

    }

    private List<RepresentedTypeRItem> getRespondentRepListToRevoke(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        List<RepresentedTypeRItem> currentRepList =
            nocRespondentRepresentativeService.findRepresentativesByToken(userToken, caseDetails);
        if (CollectionUtils.isEmpty(currentRepList)) {
            // When not found currentRepList is an empty arraylist.
            return currentRepList;
        }
        if (REMOVE_ORGANISATION.equals(caseData.getNocRemoveRepOption())) {
            String orgId = NocRespondentMapper.getFirstRepOrganisationId(currentRepList);
            return RespondentRepresentativeUtils.findRepresentativesByOrganisationId(caseData, orgId);
        }
        return currentRepList;
    }
}
