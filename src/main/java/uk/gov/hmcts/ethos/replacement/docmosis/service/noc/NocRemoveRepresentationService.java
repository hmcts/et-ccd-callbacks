package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.noc.NocRespondentMapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.MISSING_REP_CLAIMANT_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.MISSING_REP_TYPE_R_ITEM;
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

    /**
     * Revoke claimant legal rep and send email notifications to related parties.
     * @param caseDetails the case details of the case to revoke claimant legal rep
     * @param userToken the user token of the requester
     */
    public void revokeClaimantLegalRep(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();

        // get existing rep and organisation details for sending emails
        RepresentedTypeC existingClaimantRep = caseData.getRepresentativeClaimantType();
        if (existingClaimantRep == null) {
            throw new IllegalStateException(MISSING_REP_CLAIMANT_TYPE + caseDetails.getCaseId());
        }
        final String orgName = existingClaimantRep.getNameOfOrganisation();
        final String orgEmailAddress = nocNotificationService.findClaimantRepOrgSuperUserEmail(existingClaimantRep);
        final String repName = existingClaimantRep.getNameOfRepresentative();
        final String repEmailAddress = existingClaimantRep.getRepresentativeEmailAddress();
        final String partyName = caseData.getClaimant();

        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseData);

        // send email to organisation admin
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetails, orgEmailAddress, repName);
        // send email to removed legal rep
        nocRemoveRepresentationEmailService.sendEmailToRemovedLegalRep(caseDetails, repEmailAddress);
        // send email to unrepresented party, i.e. claimant
        nocRemoveRepresentationEmailService.sendEmailToUnrepresentedClaimant(caseDetails, orgName);
        // send email to other party, i.e. respondents
        nocRemoveRepresentationEmailService.sendEmailToOtherPartyRespondent(caseDetails, List.of(), partyName);
    }

    /**
     * About to start event to check if more than 1 representative from the organisation.
     * @param caseDetails the case details of the case to revoke respondent legal rep
     * @param userToken the user token of the requester
     * @return return Yes if more than 1 representative from the organisation, else return No
     */
    public String hasMultipleRepresentativesForOrg(CaseDetails caseDetails, String userToken) {
        // get list of RepresentedTypeRItem that represented by this legal rep
        List<RepresentedTypeRItem> currentRepList =
            nocRespondentRepresentativeService.findRepresentativesByToken(userToken, caseDetails);
        if (CollectionUtils.isEmpty(currentRepList)) {
            return NO;
        }

        // get the organisation id for this legal rep
        String orgId = NocRespondentMapper.getFirstRepOrganisationId(currentRepList);
        if (isNullOrEmpty(orgId)) {
            return NO;
        }

        // get all legal reps who are under the same organisation
        List<RepresentedTypeRItem> orgRepList =
            RespondentRepresentativeUtils.findRepresentativesByOrganisationId(caseDetails.getCaseData(), orgId);

        // compare and see if other legal reps involved in this case
        return orgRepList.size() > currentRepList.size()
            ? YES
            : NO;
    }

    /**
     * Revoke respondent legal rep and send email notifications to related parties.
     * @param caseDetails the case details of the case to revoke respondent legal rep
     * @param userToken the user token of the requester
     */
    public void revokeRespondentLegalRep(CaseDetails caseDetails, String userToken) {
        // get a list of RepresentedTypeRItem to be revoked
        List<RepresentedTypeRItem> repListToRevoke = getRespondentRepListToRevoke(caseDetails, userToken);
        if (CollectionUtils.isEmpty(repListToRevoke)) {
            throw new IllegalStateException(MISSING_REP_TYPE_R_ITEM + caseDetails.getCaseId());
        }

        // get existing rep and organisation details for sending emails
        final String orgName = NocRespondentMapper.getOrganisationName(repListToRevoke);
        final String orgEmailAddress =
            nocNotificationService.resolveRespondentRepresentativeOrganisationSuperuserEmail(
                caseDetails,
                repListToRevoke.getFirst(),
                NOC_TYPE_REMOVAL
            );
        final String repName = NocRespondentMapper.getRepresentativeNames(repListToRevoke);
        final List<String> repEmailAddress = NocRespondentMapper.getRepresentativeEmails(repListToRevoke);
        final String partyName = NocRespondentMapper.getRespondentPartyNames(repListToRevoke);
        final List<String> respondentIdRevoke = NocRespondentMapper.getRespondentIds(repListToRevoke);

        // revoke respondent legal rep
        nocRespondentRepresentativeService.revokeAndRemoveRespondentRepresentatives(
            caseDetails,
            repListToRevoke
        );

        // send email to organisation admin
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetails, orgEmailAddress, repName);
        // send email to removed legal rep
        nocRemoveRepresentationEmailService.sendEmailToListOfRemovedLegalRep(caseDetails, repEmailAddress);
        // send email to unrepresented party, i.e. this respondent
        nocRemoveRepresentationEmailService.sendEmailToUnrepresentedRespondent(caseDetails, repListToRevoke, orgName);
        // send email to claimant
        nocRemoveRepresentationEmailService.sendEmailToOtherPartyClaimant(caseDetails, partyName);
        // send email to other respondent
        nocRemoveRepresentationEmailService.sendEmailToOtherPartyRespondent(caseDetails, respondentIdRevoke, partyName);
    }

    private List<RepresentedTypeRItem> getRespondentRepListToRevoke(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();

        List<RepresentedTypeRItem> currentRepList =
            nocRespondentRepresentativeService.findRepresentativesByToken(userToken, caseDetails);
        if (CollectionUtils.isEmpty(currentRepList)) {
            throw new IllegalStateException(MISSING_REP_TYPE_R_ITEM + caseDetails.getCaseId());
        }

        if (YES.equals(caseData.getNocRemoveRepIsMoreThanOneFlag())
            && REMOVE_ORGANISATION.equals(caseData.getNocRemoveRepOption())) {
            String orgId = NocRespondentMapper.getFirstRepOrganisationId(currentRepList);
            return RespondentRepresentativeUtils.findRepresentativesByOrganisationId(caseData, orgId);
        }

        return currentRepList;
    }
}
