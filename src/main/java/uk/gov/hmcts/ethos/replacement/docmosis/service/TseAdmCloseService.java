package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getAdminSelectedApplicationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdmCloseService {

    private final TseService tseService;

    public String generateCloseApplicationDetailsMarkdown(CaseData caseData, String authToken) {
        if (getAdminSelectedApplicationTypeItem(caseData) == null) {
            return null;
        }
        return tseService.formatViewApplication(caseData, authToken);
    }

    /**
     * About to Submit Close Application.
     * @param caseData in which the case details are extracted from
     */
    public void aboutToSubmitCloseApplication(CaseData caseData) {
        GenericTseApplicationTypeItem applicationTypeItem = getAdminSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {
            applicationTypeItem.getValue().setCloseApplicationNotes(caseData.getTseAdminCloseApplicationText());
            applicationTypeItem.getValue().setStatus(CLOSED_STATE);
            caseData.setTseAdminCloseApplicationTable(null);
            caseData.setTseAdminCloseApplicationText(null);
            caseData.setTseAdminSelectApplication(null);
        }
    }

}
