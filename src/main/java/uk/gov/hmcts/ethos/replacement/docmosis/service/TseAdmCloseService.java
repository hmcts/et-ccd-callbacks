package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DATE_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.NAME_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.TABLE_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getSelectedApplicationTypeItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseAdmCloseService {

    private final TseService tseService;

    private static final String CLOSE_APP_DECISION_DETAILS = "|Decision | |\r\n"
        + TABLE_STRING
        + "|Notification | %s|\r\n"
        + "|Decision | %s|\r\n"
        + "%s" // Decision details
        + DATE_MARKUP
        + "|Sent by | %s|\r\n"
        + "|Type of decision | %s|\r\n"
        + "%s%s"
        + "|Decision made by | %s|\r\n"
        + NAME_MARKUP
        + "|Sent to | %s|\r\n"
        + "\r\n";

    public String generateCloseApplicationDetailsMarkdown(CaseData caseData, String authToken) {
        if (getSelectedApplicationTypeItem(caseData) == null) {
            return null;
        }
        return tseService.formatViewApplication(caseData, authToken);
    }

    /**
     * About to Submit Close Application.
     * @param caseData in which the case details are extracted from
     */
    public void aboutToSubmitCloseApplication(CaseData caseData) {
        GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {
            applicationTypeItem.getValue().setCloseApplicationNotes(caseData.getTseAdminCloseApplicationText());
            applicationTypeItem.getValue().setStatus(CLOSED_STATE);
            caseData.setTseAdminCloseApplicationTable(null);
            caseData.setTseAdminCloseApplicationText(null);
            caseData.setTseAdminSelectApplication(null);
        }
    }

}
