package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.CourtLocations;

import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.ENGLANDWALES_OFFICES;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.SCOTLAND_OFFICES;

@RequiredArgsConstructor
@Service
@Slf4j
public class TribunalOfficesService {
    private final TribunalOfficesConfiguration config;
    private final PostcodeToOfficeService postcodeToOfficeService;
    public static final String UNASSIGNED_OFFICE = "Unassigned";

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
        courtLocations.setEpimmsId("");
        courtLocations.setRegion("");
        courtLocations.setRegionId("");
        return courtLocations;
    }

    /**
     * Retrieves the case management location code ePIMMS id for a tribunal office.
     * @param tribunalOffice managingOffice the case has been assigned
     * @return Epimms location id for the managing office
     */
    public String getEpimmsIdLocationCode(TribunalOffice tribunalOffice) {
        String epimmsId = config.getCourtLocations().get(tribunalOffice).getEpimmsId();
        return isNullOrEmpty(epimmsId) ? "" : epimmsId;
    }

    public void addManagingOffice(CaseData caseData, String caseTypeId) {
        String managingOffice = UNASSIGNED_OFFICE;
        if (claimantWorkPostcodeExists(caseData)) {
            managingOffice = getManagingOffice(
                    caseData.getClaimantWorkAddress().getClaimantWorkAddress().getPostCode(),
                    caseTypeId);
        } else if (CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
            managingOffice = caseData.getRespondentCollection().stream()
                    .map(respondent -> respondent.getValue().getRespondentAddress())
                    .filter(respondentAddress -> respondentAddress != null
                                                 && StringUtils.isNotBlank(respondentAddress.getPostCode()))
                    .findFirst()
                    .map(respondentAddress -> getManagingOffice(respondentAddress.getPostCode(), caseTypeId))
                    .orElse(UNASSIGNED_OFFICE);
        }

        caseData.setManagingOffice(managingOffice);
    }

    private String getManagingOffice(String postCode, String caseTypeId) {
        try {
            Optional<TribunalOffice> office = postcodeToOfficeService.getTribunalOfficeFromPostcode(postCode);
            return retrieveManagingOfficeAccordingToCaseTypeId(caseTypeId, office);
        } catch (InvalidPostcodeException e) {
            log.error("Invalid postcode: {}", postCode);
            return UNASSIGNED_OFFICE;
        }
    }

    private String retrieveManagingOfficeAccordingToCaseTypeId(String caseTypeId, Optional<TribunalOffice> office) {
        if (office.isEmpty()) {
            return UNASSIGNED_OFFICE;
        }
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId) && SCOTLAND_OFFICES.contains(office.get())
            || SCOTLAND_CASE_TYPE_ID.equals(caseTypeId) && ENGLANDWALES_OFFICES.contains(office.get())) {
            return UNASSIGNED_OFFICE;
        } else {
            return reassignAnyScottishOfficeToGlasgow(office.get());
        }
    }

    private String reassignAnyScottishOfficeToGlasgow(TribunalOffice tribunalOffice) {
        return tribunalOffice.equals(TribunalOffice.GLASGOW) ? TribunalOffice.GLASGOW.getOfficeName() :
                tribunalOffice.getOfficeName();
    }

    private static boolean claimantWorkPostcodeExists(CaseData caseData) {
        return caseData.getClaimantWorkAddress() != null
               && caseData.getClaimantWorkAddress().getClaimantWorkAddress() != null
               && StringUtils.isNotBlank(caseData.getClaimantWorkAddress().getClaimantWorkAddress().getPostCode());
    }
}

