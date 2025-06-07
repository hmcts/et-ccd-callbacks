package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantRepRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.UUID;



@Service
@RequiredArgsConstructor
@Slf4j
public class NocClaimantRepresentativeService {
    private static final String YES = "Yes";
    private static final String ORGANISATION_NOT_FOUND = "Organisation not found: %s";

    private final CaseConverter caseConverter;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;

    /**
     * Update claimant representation based on NoC request.
     * @param caseDetails containing case data with change organisation request field
     * @return updated case data
     * @throws IOException if CCD operation fails
     */
    public CaseData updateClaimantRepresentation(CaseDetails caseDetails) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> repCollection = updateClaimantRepMap(caseData, caseDetails.getCaseId());
        caseDataAsMap.putAll(repCollection);
        return caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    private Map<String, Object> updateClaimantRepMap(CaseData caseData, String caseId) throws IOException {
        ChangeOrganisationRequest change = validateChangeRequest(caseData);
        String organisationId = change.getOrganisationToAdd();
        
        OrganisationsResponse organisation = findOrganisation(organisationId);
        RepresentedTypeR claimantRep = createRepresentedTypeR(organisation);
        
        List<RepresentedTypeRItem> repCollection = updateRepCollection(caseData, claimantRep);
        return Map.of(ClaimantRepRole.CASE_FIELD.toString(), repCollection);
    }

    private ChangeOrganisationRequest validateChangeRequest(CaseData caseData) {
        ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();
        if (Objects.isNull(change) || Objects.isNull(change.getCaseRoleId()) || Objects.isNull(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }
        return change;
    }

    private OrganisationsResponse findOrganisation(String organisationId) throws IOException {
        String serviceToken = authTokenGenerator.generate();
        ResponseEntity<OrganisationsResponse> response = organisationClient.retrieveOrganisationDetailsByUserId(
            "Bearer " + authTokenGenerator.generate(), serviceToken, organisationId);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new CcdInputOutputException(
                String.format(ORGANISATION_NOT_FOUND, organisationId), null);
        }
    }

    private RepresentedTypeR createRepresentedTypeR(OrganisationsResponse organisation) {
        RepresentedTypeR claimantRep = new RepresentedTypeR();
        updateRepDetails(organisation, claimantRep);
        claimantRep.setMyHmctsYesNo(YES);
        claimantRep.setNonMyHmctsOrganisationId(generateOrganisationId());
        return claimantRep;
    }

    private void updateRepDetails(OrganisationsResponse orgRes, RepresentedTypeR repDetails) {
        if (orgRes != null && orgRes.getName() != null) {
            repDetails.setNameOfOrganisation(orgRes.getName());
        }
        if (orgRes != null && !CollectionUtils.isEmpty(orgRes.getContactInformation())) {
            Address repAddress = Objects.requireNonNullElse(repDetails.getRepresentativeAddress(), new Address());
            OrganisationAddress orgAddress = orgRes.getContactInformation().get(0);

            repAddress.setAddressLine1(orgAddress.getAddressLine1());
            repAddress.setAddressLine2(orgAddress.getAddressLine2());
            repAddress.setAddressLine3(orgAddress.getAddressLine3());
            repAddress.setPostTown(orgAddress.getTownCity());
            repAddress.setCounty(orgAddress.getCounty());
            repAddress.setCountry(orgAddress.getCountry());
            repAddress.setPostCode(orgAddress.getPostCode());
            repDetails.setRepresentativeAddress(repAddress);
        }
    }

    private List<RepresentedTypeRItem> updateRepCollection(CaseData caseData, RepresentedTypeR claimantRep) {
        List<RepresentedTypeRItem> repCollection = Optional.ofNullable(caseData.getRepCollection())
            .orElse(new ArrayList<>());
        RepresentedTypeRItem repItem = new RepresentedTypeRItem();
        repItem.setValue(claimantRep);
        repCollection.add(repItem);
        return repCollection;
    }

    private String generateOrganisationId() {
        return UUID.randomUUID().toString();
    }
}
