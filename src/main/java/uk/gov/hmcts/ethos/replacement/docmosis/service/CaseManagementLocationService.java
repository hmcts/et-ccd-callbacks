package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLocation;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.CourtLocations;

import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * This provides services to add caseManagementLocationCode to caseData.
 */
@Service
@Slf4j
public class CaseManagementLocationService {

    private final TribunalOfficesService tribunalOfficesService;

    /**
     * Standard constructor.
     * @param tribunalOfficesService used for location code lookups
     */
    public CaseManagementLocationService(TribunalOfficesService tribunalOfficesService) {
        this.tribunalOfficesService = tribunalOfficesService;
    }

    /**
     * Sets caseManagementLocationCode in case data based on the
     * tribunal office retrieved from managingOffice.
     */
    public void setCaseManagementLocationCode(CaseData caseData) {
        TribunalOffice tribunalOffice;
        try {
            tribunalOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        } catch (IllegalArgumentException i) {
            caseData.setCaseManagementLocationCode("");
            return;
        }
        String caseManagementLocationCode = tribunalOfficesService.getEpimmsIdLocationCode(tribunalOffice);
        caseData.setCaseManagementLocationCode(Objects.toString(caseManagementLocationCode, ""));
    }

    public void setCaseManagementLocation(BaseCaseData caseData) {
        String managingOfficeName = caseData.getManagingOffice();
        if (isNullOrEmpty(managingOfficeName)) {
            log.debug("leave `caseManagementLocation` blank since it may be the multiCourts case.");
            return;
        }

        CourtLocations tribunalLocations = tribunalOfficesService.getTribunalLocations(managingOfficeName);
        if (tribunalLocations.getEpimmsId().isBlank()) {
            log.debug("leave `caseManagementLocation` blank since Managing office is un-assigned.");
            return;
        }
        caseData.setCaseManagementLocation(CaseLocation.builder()
                .baseLocation(tribunalLocations.getEpimmsId())
                .region(tribunalLocations.getRegionId())
                .build());
    }
}