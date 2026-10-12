package uk.gov.hmcts.ethos.replacement.docmosis.service.applications.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.applications.TseHelper.getAdminSelectedApplicationType;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdmCloseService {

    private final TseService tseService;

    public String generateCloseApplicationDetailsMarkdown(CaseData caseData, String authToken) {
        if (getAdminSelectedApplicationType(caseData) == null) {
            return null;
        }
        return tseService.formatViewApplication(caseData, authToken, false);
    }

    /**
     * About to Submit Close Application.
     * @param caseData in which the case details are extracted from
     * @param caseTypeId the case type id
     */
    public void aboutToSubmitCloseApplication(CaseData caseData, String caseTypeId) {
        GenericTseApplicationType applicationType = getAdminSelectedApplicationType(caseData);
        if (applicationType != null) {
            setCloseApplicationNotes(applicationType, caseData.getTseAdminCloseApplicationText(), caseTypeId);
            applicationType.setStatus(CLOSED_STATE);
            caseData.setTseAdminCloseApplicationTable(null);
            caseData.setTseAdminCloseApplicationText(null);
            caseData.setTseAdminSelectApplication(null);
        }
    }

    private void setCloseApplicationNotes(GenericTseApplicationType applicationType, String notes,
                                          String caseTypeId) {
        if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            applicationType.setCloseApplicationNote(notes);
            applicationType.setCloseApplicationNotes(null);
        } else {
            applicationType.setCloseApplicationNotes(notes);
            applicationType.setCloseApplicationNote(null);
        }
    }

}
