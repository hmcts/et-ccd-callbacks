package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateReferralHelper {

    private static final String GUIDANCE_DOC_LINK = "<hr>To help you complete this form, open the "
        + "<a href=\"url\">referral guidance documents</a>";
    private static final String HEARING_DETAILS = "<hr><h3>Hearing details %s</h3>"
        + "<pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Hearing &#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 %s</pre>";

    /**
     * Formats the hearing details into HTML for ExUI to display. It's expected that there are at least one hearing
     * already created before this event is started. Hearing details should contain the hearing date, hearing
     * type and the track type for each hearing.
     */
    public void populateHearingDetails(CaseData caseData) {
        if (caseData.getHearingCollection() != null) {
            String trackType = caseData.getTrackType();
            StringBuilder hearingDetails = new StringBuilder();
            hearingDetails.append(GUIDANCE_DOC_LINK);
            IntWrapper count = new IntWrapper(0);
            boolean singleHearing = caseData.getHearingCollection().size() == 1;
            for (HearingTypeItem hearing : caseData.getHearingCollection()) {
                hearingDetails.append(hearing.getValue().getHearingDateCollection().stream()
                    .map(h -> String.format(
                        HEARING_DETAILS, singleHearing ? "" : count.incrementAndReturnValue(),
                        UtilHelper.formatLocalDateTime(h.getValue().getListedDate()),
                        hearing.getValue().getHearingType(), trackType))
                    .collect(Collectors.joining()));
            }
            hearingDetails.append("<hr>");

            caseData.setReferralHearingDetails(hearingDetails.toString());
        }
    }
}
