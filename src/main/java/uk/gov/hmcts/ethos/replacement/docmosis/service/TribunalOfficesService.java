package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.CourtLocations;

@Service
@Slf4j
public class TribunalOfficesService {
    private final TribunalOfficesConfiguration config;
    public static final String UNASSIGNED_OFFICE = "Unassigned";

    public TribunalOfficesService(TribunalOfficesConfiguration config) {
        this.config = config;
    }

    public TribunalOffice getTribunalOffice(String officeName) {
        return TribunalOffice.valueOfOfficeName(officeName);
    }

    public ContactDetails getTribunalContactDetails(String officeName) {
        if (officeName == null || UNASSIGNED_OFFICE.equals(officeName)) {
            return createUnassignedContactDetails();
        }
        var tribunalName = getTribunalOffice(officeName);
        return config.getContactDetails().get(tribunalName);
    }

    private ContactDetails createUnassignedContactDetails() {
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setManagingOffice(UNASSIGNED_OFFICE);
        contactDetails.setAddress1("");
        contactDetails.setAddress2("");
        contactDetails.setAddress3("");
        contactDetails.setDx("");
        contactDetails.setFax("");
        contactDetails.setEmail("");
        contactDetails.setPostcode("");
        contactDetails.setTown("");
        contactDetails.setTelephone("");
        return contactDetails;
    }

    public CourtLocations getTribunalLocations(String officeName) {
        if (officeName == null || UNASSIGNED_OFFICE.equals(officeName)) {
            return createUnassignedTribunalLocations();
        }
        var tribunalName = getTribunalOffice(officeName);
        return config.getCourtLocations().get(tribunalName);
    }

    private CourtLocations createUnassignedTribunalLocations() {
        CourtLocations courtLocations = new CourtLocations();
        courtLocations.setName(UNASSIGNED_OFFICE);
        courtLocations.setEpimmsId("");
        courtLocations.setRegion("");
        courtLocations.setRegionId("");
        return courtLocations;
    }
}

