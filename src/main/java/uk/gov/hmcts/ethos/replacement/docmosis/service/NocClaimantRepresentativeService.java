package uk.gov.hmcts.ethos.replacement.docmosis.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService.getOrganisationAddress;


@Service
@RequiredArgsConstructor
@Slf4j
public class NocClaimantRepresentativeService {
    private final UserIdamService userIdamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;

    /**
     * Update claimant representation based on NoC request.
     * @param caseDetails containing case data with change organisation request field
     * @return updated case data
     * @throws IOException if CCD operation fails
     */
    public CaseData updateClaimantRepresentation(CaseDetails caseDetails, String userToken) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        updateClaimantRepMap(caseData, userToken);
        return caseData;
    }

    private void updateClaimantRepMap(CaseData caseData, String userToken) throws IOException {
        RepresentedTypeC claimantRep = createRepresentedTypeC(userToken);
        caseData.setClaimantRepresentedQuestion(YES);
        caseData.setRepresentativeClaimantType(claimantRep);
    }

    private RepresentedTypeC createRepresentedTypeC(String userToken) throws IOException {
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        RepresentedTypeC claimantRep = new RepresentedTypeC();
        claimantRep.setNameOfRepresentative(userDetails.getFirstName() + " " + userDetails.getLastName());
        claimantRep.setRepresentativeEmailAddress(userDetails.getEmail());
        OrganisationsResponse organisationDetails = getOrganisationDetailsFromUserId(userDetails.getUid());
        claimantRep.setMyHmctsOrganisation(Organisation.builder()
                .organisationID(organisationDetails.getOrganisationIdentifier())
                .organisationName(organisationDetails.getName())
                .build());
        claimantRep.setNameOfOrganisation(organisationDetails.getName());
        claimantRep.setRepresentativeAddress(getOrganisationAddress(organisationDetails));

        return claimantRep;
    }

    public OrganisationsResponse getOrganisationDetailsFromUserId(String userId) {
        try {
            String userToken = adminUserService.getAdminUserToken();
            ResponseEntity<OrganisationsResponse> response =
                    organisationClient.retrieveOrganisationDetailsByUserId(userToken,
                            authTokenGenerator.generate(),
                            userId);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to retrieve organisation details", e);
        }
        return null;
    }

    private List<RepresentedTypeRItem> updateRepCollection(CaseData caseData, RepresentedTypeR claimantRep) {
        List<RepresentedTypeRItem> repCollection = Optional.ofNullable(caseData.getRepCollection())
                .orElse(new ArrayList<>());
        RepresentedTypeRItem repItem = new RepresentedTypeRItem();
        repItem.setValue(claimantRep);
        repCollection.add(repItem);
        return repCollection;
    }
}
