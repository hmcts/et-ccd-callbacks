package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateReferralHelper {

    private static final String HEARING_DETAILS = "<hr>To help you complete this form, open the "
        + "<a href=\"url\">referral guidance documents</a>"
        + "<hr><h3>Hearing details</h3>"
        + "<pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Hearing &#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 %s</pre><hr>";

    /**
     * Formats the hearing details into HTML for ExUI to display. It's expected that there are at least one hearing
     * already created before this event is started. Hearing details should contain the first event of the case
     * and the first date of the event .
     */
    public void populateHearingDetails(CaseData caseData) {
        if (caseData.getHearingCollection() != null) {
            HearingType hearing = caseData.getHearingCollection().get(0).getValue();
            String hearingDate = UtilHelper.formatLocalDateTime(hearing.getHearingDateCollection().get(0)
                .getValue().getListedDate());
            caseData.setReferralHearingDetails(
                String.format(HEARING_DETAILS, hearingDate, hearing.getHearingType(), caseData.getTrackType()));
        }
    }
}
