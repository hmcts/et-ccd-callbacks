package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService.getOrganisationAddress;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocClaimantRepresentativeService {
    public static final String NOC_REQUEST = "nocRequest";

    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;
    private final NocCcdService nocCcdService;

    /**
     * Update claimant representation based on NoC request.
     * @param caseDetails containing case data with change organisation request field
     * @return updated case data
     * @throws IOException if CCD operation fails
     */
    public CaseData updateClaimantRepresentation(CaseDetails caseDetails, String userToken) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();
        updateClaimantRepMap(caseData, caseId);
        prepopulateOrgAddress(caseData, userToken);

        return caseData;
    }

    private void updateClaimantRepMap(CaseData caseData, String caseId) throws IOException {
        RepresentedTypeC claimantRep = createRepresentedTypeC(caseId, caseData.getChangeOrganisationRequestField());
        caseData.setClaimantRepresentedQuestion(YES);
        caseData.setRepresentativeClaimantType(claimantRep);
    }

    private RepresentedTypeC createRepresentedTypeC(String caseId, ChangeOrganisationRequest change)
            throws IOException {
        String accessToken = adminUserService.getAdminUserToken();

        Optional<AuditEvent> auditEvent =
                nocCcdService.getLatestAuditEventByName(accessToken, caseId, NOC_REQUEST);
        Optional<UserDetails> userDetailsOptional = auditEvent
                .map(event -> adminUserService.getUserDetails(event.getUserId()));

        UserDetails userDetails = userDetailsOptional.orElseThrow();

        RepresentedTypeC claimantRep = new RepresentedTypeC();
        claimantRep.setNameOfRepresentative(userDetails.getFirstName() + " " + userDetails.getLastName());
        claimantRep.setRepresentativeEmailAddress(userDetails.getEmail());
        claimantRep.setMyHmctsOrganisation(change.getOrganisationToAdd());

        return claimantRep;
    }

    private void prepopulateOrgAddress(CaseData caseData, String userToken) {
        List<OrganisationsResponse> organisationList = organisationClient.getOrganisations(
                userToken, authTokenGenerator.generate());

        RepresentedTypeC claimantRep = caseData.getRepresentativeClaimantType();
        if (claimantRep != null && claimantRep.getMyHmctsOrganisation() != null) {
            String orgId = claimantRep.getMyHmctsOrganisation().getOrganisationID();
            Optional<OrganisationsResponse> organisationResponse = organisationList.stream()
                    .filter(org -> org.getOrganisationIdentifier().equals(orgId))
                    .findFirst();

            if (organisationResponse.isPresent()) {
                OrganisationsResponse organisation = organisationResponse.get();
                claimantRep.setRepresentativeAddress(getOrganisationAddress(organisation));
                caseData.setRepresentativeClaimantType(claimantRep);
            }
        }
    }
}
