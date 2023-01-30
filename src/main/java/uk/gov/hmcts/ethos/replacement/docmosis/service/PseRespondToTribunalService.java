package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    /**
     * Create a new element in the responses list and assign the PSE data from CaseData to it.
     * @param caseData contains all the case data
     */
    public void addRespondentResponseToJON(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getPseOrdReqResponses())) {
            caseData.setPseOrdReqResponses(new ArrayList<>());
        }

        caseData.getPseOrdReqResponses().add(
            PseResponseItem.builder()
                .id(UUID.randomUUID().toString())
                .value(
                    PseResponseType.builder()
                        .from("Respondent")
                        .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                        .response(caseData.getPseRespondentOrdReqResponseText())
                        .hasSupportingMaterial(caseData.getPseRespondentOrdReqHasSupportingMaterial())
                        .supportingMaterial(caseData.getPseRespondentOrdReqUploadDocument())
                        .copyToOtherParty(caseData.getPseRespondentOrdReqCopyToOtherParty())
                        .copyNoGiveDetails(caseData.getPseRespondentOrdReqCopyNoGiveDetails())
                        .build()
                ).build());
    }

    /**
     * Clears fields that are used when responding to a JON, so they can be used in subsequent responses to JONs.
     * @param caseData contains all the case data
     */
    public void clearRespondentResponse(CaseData caseData) {
        caseData.setPseRespondentSelectOrderOrRequest(null);
        caseData.setPseRespondentOrdReqTableMarkUp(null);
        caseData.setPseRespondentOrdReqResponseText(null);
        caseData.setPseRespondentOrdReqHasSupportingMaterial(null);
        caseData.setPseRespondentOrdReqUploadDocument(null);
        caseData.setPseRespondentOrdReqCopyToOtherParty(null);
        caseData.setPseRespondentOrdReqCopyNoGiveDetails(null);
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
