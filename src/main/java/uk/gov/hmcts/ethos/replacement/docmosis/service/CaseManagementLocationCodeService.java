package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.Objects;

/**
 * This provides services to add caseManagementLocationCode to caseData.
 */
@Service
public class CaseManagementLocationCodeService {

    private final TribunalOfficesService tribunalOfficesService;

    /**
     * Standard constructor.
     * @param tribunalOfficesService used for location code lookups
     */
    public CaseManagementLocationCodeService(TribunalOfficesService tribunalOfficesService) {
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
}