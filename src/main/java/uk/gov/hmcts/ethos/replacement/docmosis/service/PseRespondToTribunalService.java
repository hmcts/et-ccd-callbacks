package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PseRespondToTribunalService {

    private static final String TODO_ISSUE_REQUEST_ORDER = "[ToDo: Dependency on RET-2949]";
    private static final String TODO_RESPONSE = "[ToDo: Dependency on RET-2928]";

    private static final String APP_DETAILS = "|Hearing, case management order or request | |\r\n"
        + "|--|--|\r\n"
        + "|Notification | %s|\r\n"
        + "|Hearing | %s|\r\n"
        + "|Date sent | %s|\r\n"
        + "|Sent by | %s|\r\n"
        + "|Case management order or request? | %s|\r\n"
        + "|Response due | %s|\r\n"
        + "|Party or parties to respond | %s|\r\n"
        + "|Additional information | %s|\r\n"
        + "|Description | %s|\r\n"
        + "|Document | %s|\r\n"
        + "|Case management order made by | %s|\r\n"
        + "|Name | %s|\r\n"
        + "|Sent to | %s|\r\n"
        + "\r\n"
        + "\r\n";

    private static final String RESPONSE_DETAILS = "|Response %s | |\r\n"
        + "|--|--|\r\n"
        + "|Response from | %s|\r\n"
        + "|Response date | %s|\r\n"
        + "|What's your response to the tribunal? | %s|\r\n"
        + "|Supporting material | %s|\r\n"
        + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
        + "\r\n";
    private static final String GIVE_MISSING_DETAIL =
        "Use the text box or supporting materials to give details.";
    private static final String NO = "No";

    /**
     * Initial Application and Respond details table.
     * @param caseData contains all the case data
     */
    public String initialOrdReqDetailsTableMarkUp(CaseData caseData) {
        return initialOrdReqDetails() + responses();

        /*GenericTseApplicationTypeItem applicationTypeItem = getSelectedApplicationTypeItem(caseData);
        if (applicationTypeItem != null) {
            return initialAppDetails() + responses();
        }
        return null;*/
    }

    public List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.isEmpty(caseData.getPseRespondentOrdReqResponseText())
            && (StringUtils.isEmpty(caseData.getPseRespondentOrdReqHasSupportingMaterial())
            || NO.equals(caseData.getPseRespondentOrdReqHasSupportingMaterial()))) {
            errors.add(GIVE_MISSING_DETAIL);
        }
        return errors;
    }

    private String initialOrdReqDetails() {
        return String.format(
            APP_DETAILS,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER,
            TODO_ISSUE_REQUEST_ORDER
        );
    }

    private String responses() {
        return String.format(
            RESPONSE_DETAILS,
            TODO_RESPONSE,
            TODO_RESPONSE,
            TODO_RESPONSE,
            TODO_RESPONSE,
            TODO_RESPONSE,
            TODO_RESPONSE
        );
    }
}
