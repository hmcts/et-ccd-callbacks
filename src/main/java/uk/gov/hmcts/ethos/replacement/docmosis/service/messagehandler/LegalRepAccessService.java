package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DynamicListHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Service for managing legal representative access to cases.
 * Migrated from et-message-handler.
 */
@Slf4j
@Service
public class LegalRepAccessService {

    private final CcdClient ccdClient;

    @Autowired
    public LegalRepAccessService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    public void updateRepresentativeAccess(String accessToken, String caseTypeId, String jurisdiction,
                                            List<SubmitEvent> submitEventList) throws IOException {
        log.info("Updating representative access for {} cases", submitEventList.size());
        
        for (SubmitEvent submitEvent : submitEventList) {
            String caseId = String.valueOf(submitEvent.getCaseId());
            CaseData caseData = submitEvent.getCaseData();

            if (!hasRepresentedTypeR(caseData)) {
                log.info("No RepresentedTypeR found for case: {}", caseId);
                continue;
            }

            CCDRequest ccdRequest = buildCcdRequest(caseData, accessToken, caseTypeId, jurisdiction);
            ccdClient.startEventForCase(accessToken, caseTypeId, jurisdiction, caseId);
            updateRepresentative(accessToken, caseTypeId, jurisdiction, ccdRequest, caseId);
        }
    }

    private CCDRequest buildCcdRequest(CaseData caseData, String accessToken,
                                        String caseTypeId, String jurisdiction) {
        CaseData updatedCaseData = new CaseData();
        updatedCaseData.setEthosCaseReference(caseData.getEthosCaseReference());

        // Populate RepresentedTypeC from RepresentedTypeR
        List<RepresentedTypeC> representativeClaimantCollection = new ArrayList<>();
        for (RepresentedTypeR repTypeR : caseData.getRepCollection()) {
            RepresentedTypeC repTypeC = new RepresentedTypeC();
            repTypeC.setNameOfRepresentative(repTypeR.getNameOfRepresentative());
            repTypeC.setNameOfOrganisation(repTypeR.getNameOfOrganisation());
            repTypeC.setRepresentativeEmailAddress(repTypeR.getRepresentativeEmailAddress());
            repTypeC.setRepresentativePhoneNumber(repTypeR.getRepresentativePhoneNumber());
            repTypeC.setRepresentativeOccupation(repTypeR.getRepresentativeOccupation());
            repTypeC.setRepresentativePreference(repTypeR.getRepresentativePreference());
            repTypeC.setRepresentativeAddress(repTypeR.getRepresentativeAddress());
            repTypeC.setRepresentativeReference(repTypeR.getRepresentativeReference());
            
            representativeClaimantCollection.add(repTypeC);
        }

        updatedCaseData.setRepresentativeClaimantType(
            DynamicListHelper.createDynamicRespondentName(representativeClaimantCollection));
        updatedCaseData.setRepCollection(caseData.getRepCollection());

        return CCDRequest.builder()
            .caseDetails(uk.gov.hmcts.ecm.common.model.ccd.CaseDetails.builder()
                .caseTypeId(caseTypeId)
                .jurisdiction(jurisdiction)
                .caseData(updatedCaseData)
                .build())
            .build();
    }

    private void updateRepresentative(String accessToken, String caseTypeId, String jurisdiction,
                                       CCDRequest ccdRequest, String caseId) throws IOException {
        try {
            ccdClient.submitEventForCase(accessToken, ccdRequest, caseTypeId, jurisdiction, caseId);
            log.info("Successfully updated representative access for case: {}", caseId);
        } catch (Exception e) {
            log.error("Failed to update representative access for case: {}", caseId, e);
            throw new CaseCreationException("Failed to update representative access for case: " + caseId, e);
        }
    }

    private boolean hasRepresentedTypeR(CaseData caseData) {
        return caseData.getRepCollection() != null && !caseData.getRepCollection().isEmpty();
    }
}
