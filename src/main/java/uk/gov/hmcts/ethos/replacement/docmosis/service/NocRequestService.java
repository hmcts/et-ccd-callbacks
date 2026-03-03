package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocCcdService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRequestService {

    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;

    public void revokeClaimantLegalRep(CaseDetails caseDetails, String userToken) {
        // create a copy of existing claimant legal rep details
        RepresentedTypeC repCopy = getRepTrueCopy(caseDetails);

        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseDetails.getCaseData());

        // send email to organisation admin
        nocNotificationService.sendClaimantNocRequestEmailToOrgAdmin(caseDetails, repCopy);
        // send email to removed legal rep
        nocNotificationService.sendClaimantNocRequestEmailToRemovedLegalRep(caseDetails, repCopy);
        // send email to unrepresented party, i.e. claimant
        nocNotificationService.sendClaimantNocRequestEmailToUnrepresentedParty(caseDetails, repCopy);
        // send email to other party, i.e. respondents
        nocNotificationService.sendClaimantNocRequestEmailToOtherParty(caseDetails);
    }

    private static RepresentedTypeC getRepTrueCopy(CaseDetails caseDetails) {
        RepresentedTypeC existingClaimantRep = caseDetails.getCaseData().getRepresentativeClaimantType();
        return RepresentedTypeC.builder()
            .representativeId(existingClaimantRep.getRepresentativeId())
            .nameOfRepresentative(existingClaimantRep.getNameOfRepresentative())
            .nameOfOrganisation(existingClaimantRep.getNameOfOrganisation())
            .representativeEmailAddress(existingClaimantRep.getRepresentativeEmailAddress())
            .organisationId(existingClaimantRep.getOrganisationId())
            .myHmctsOrganisation(existingClaimantRep.getMyHmctsOrganisation() == null
                ? null
                : Organisation.builder()
                    .organisationID(existingClaimantRep.getMyHmctsOrganisation().getOrganisationID())
                    .organisationName(existingClaimantRep.getMyHmctsOrganisation().getOrganisationName())
                    .build())
            .build();
    }
}
