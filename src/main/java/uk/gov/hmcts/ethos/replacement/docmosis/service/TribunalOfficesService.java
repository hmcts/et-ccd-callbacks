package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
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

    public void setCaseManagementLocationCode(CaseData caseData) {
        String managingOffice = caseData.getManagingOffice();
        if (Strings.isNullOrEmpty(managingOffice) || UNASSIGNED_OFFICE.equals(managingOffice)) {
            log.info("CaseManagementLocationCode set to blank as managing office isNullorEmpty");
            caseData.setCaseManagementLocationCode("");
        } else {
            // do we need a null check here
            TribunalOffice tribunalOffice = getTribunalOffice(managingOffice);
            CourtLocations courtLocation = config.getCourtLocations().get(tribunalOffice);
            caseData.setCaseManagementLocationCode(courtLocation.getEpimmsId());
            log.info("The epimms id has been set to " + courtLocation.getEpimmsId());
            log.info("The court location " + courtLocation.getName() + " region " + courtLocation.getRegion());
            log.info("The epimms id has been set to " + courtLocation.getEpimmsId());
        }
    }

    public String tribunalOfficeToEpimmsId(TribunalOffice tribunalOffice) {
        return config.getCourtLocations().get(tribunalOffice).getEpimmsId();
    }
}


