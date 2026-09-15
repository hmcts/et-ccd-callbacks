package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_OLD_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveRepresentationService {

    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;
    private final NocRemoveRepresentationEmailService nocRemoveRepresentationEmailService;
    private final AdminUserService adminUserService;

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
        CaseDetails caseDetailsBeforeRepUpdate = CaseDataUtils.cloneCaseDetails(caseDetails);
        if (caseDetailsBeforeRepUpdate == null) {
            throw new NotFoundException(EXCEPTION_OLD_CASE_DETAILS_NOT_FOUND);
        }
        CaseData caseData = caseDetails.getCaseData();
        // get existing rep and organisation details for sending emails
        RepresentedTypeC existingClaimantRep = caseData.getRepresentativeClaimantType();
        if (existingClaimantRep == null) {
            throw new IllegalStateException(String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId()));
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
                EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT);
        // send email to removed legal rep if his/her email exists
        nocRemoveRepresentationEmailService.sendEmailToRemovedLegalRep(caseDetailsBeforeRepUpdate, repEmailAddress);
        // send email to unrepresented party, i.e. claimant if his/her email exists
        nocRemoveRepresentationEmailService.sendEmailToUnrepresentedClaimant(caseDetailsBeforeRepUpdate, orgName);
        // send email to other party, i.e. respondents if any exists
        nocRemoveRepresentationEmailService.sendEmailToOtherRespondents(caseDetailsBeforeRepUpdate,
                List.of(), partyName);
    }
}
