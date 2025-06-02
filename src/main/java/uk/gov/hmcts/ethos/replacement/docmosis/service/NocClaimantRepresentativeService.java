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
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantRepRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.io.IOException;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocClaimantRepresentativeService {
    private final CaseConverter caseConverter;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;

    /**
     * Update claimant representation based on NoC request
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
        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        String serviceToken = authTokenGenerator.generate();
        List<OrganisationsResponse> organisationList = organisationClient.getOrganisations("Bearer " + authTokenGenerator.generate(), serviceToken);
        Optional<OrganisationsResponse> orgRes = organisationList.stream()
            .filter(org -> org.getOrganisationIdentifier() != null && org.getOrganisationIdentifier().equals(change.getOrganisationToAdd()))
            .findFirst();

        if (orgRes.isEmpty()) {
            throw new CcdInputOutputException("Organisation not found: " + change.getOrganisationToAdd(), null);
        }

        RepresentedTypeR claimantRep = new RepresentedTypeR();
        updateRepDetails(orgRes.get(), claimantRep);
        claimantRep.setMyHmctsYesNo("Yes");
        claimantRep.setNonMyHmctsOrganisationId(UUID.randomUUID().toString());

        List<RepresentedTypeRItem> repCollection = defaultIfNull(caseData.getRepCollection(), new ArrayList<>());
        RepresentedTypeRItem repItem = new RepresentedTypeRItem();
        repItem.setValue(claimantRep);
        repCollection.add(repItem);

        return Map.of(ClaimantRepRole.CASE_FIELD.toString(), repCollection);
    }

    private void updateRepDetails(OrganisationsResponse orgRes, RepresentedTypeR repDetails) {
        repDetails.setNameOfOrganisation(orgRes.getName());

        if (!isEmpty(orgRes.getContactInformation())) {
            OrganisationAddress orgAddress = orgRes.getContactInformation().get(0);
            Address repAddress = new Address();
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
}
