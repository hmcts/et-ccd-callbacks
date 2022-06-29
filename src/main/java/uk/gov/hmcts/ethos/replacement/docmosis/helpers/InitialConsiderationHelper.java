package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getHearingDuration;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

public class InitialConsiderationHelper {

    static final String RESPONDENT_NAME =
            "| Respondent name given | |\r\n"
                    + "|-------------|:------------|\r\n"
                    + "|In Et1 by claimant | %s|\r\n"
                    + "|In Et3 by Respondent | %s|";

    static final String HEARING_DETAILS =
            "|Hearing Details | |\r\n"
                    + "|-------------|:------------|\r\n"
                    + "|Date | %s|\r\n"
                    + "|Type | %s|\r\n"
                    + "|Duration | %s|";


    private InitialConsiderationHelper() {
    }


    /**
     * Creates the respondent detail section for Initial Consideration.
     * Only shows details from the first record
     * @param respondentCollection collection of respondents
     * @return table with respondent details
     */
    public static String getRespondentName(List<RespondentSumTypeItem> respondentCollection) {
        return respondentCollection.stream().map(
                        respondent -> String.format(
                                RESPONDENT_NAME, nullCheck(respondent.getValue().getRespondentName()),
                                nullCheck(respondent.getValue().getResponseRespondentName()))).findFirst()
                .orElse(String.format(RESPONDENT_NAME, "", ""));
    }

    /**
     * Creates hearing detail section for Initial Consideration.
     * Display details of the hearing with the earliest hearing date from the collection of hearings
     * @param hearingCollection the collection of hearings
     * @return return table with details of hearing
     */
    public static String getHearingDetails(List<HearingTypeItem> hearingCollection) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        return hearingCollection.stream()
                .map(HearingTypeItem::getValue)
                .filter(hearing -> hearing.getHearingDateCollection() != null && !hearing.getHearingDateCollection().isEmpty()).min(Comparator.comparing(
                        (HearingType hearing) ->
                                getEarliestHearingDate(hearing.getHearingDateCollection()).orElse(LocalDate.now().plusYears(100))))
                .map(hearing -> String.format(HEARING_DETAILS,
                        getEarliestHearingDate(hearing.getHearingDateCollection()).map(formatter::format).orElse(""),
                        hearing.getHearingType(),
                        getHearingDuration(hearing)))
                .orElse(String.format(HEARING_DETAILS, "", "", ""));
    }

    public static Optional<LocalDate> getEarliestHearingDate(List<DateListedTypeItem> hearingDates) {
        return hearingDates.stream()
                .filter(hearingDate -> hearingDate.getValue() != null && hearingDate.getValue().getListedDate() != null && !hearingDate.getValue().getListedDate().isEmpty())
                .map(hearingDateItem -> LocalDateTime.parse(hearingDateItem.getValue().getListedDate()).toLocalDate()).min(Comparator.naturalOrder());
    }


    /**
     * Creates the jurisdiction section for Initial Consideration.
     * @param jurisdictionCodes the list of jurisdiction codes assigned to the case
     * @return jurisdiction code section
     */
    public static String generateJurisdictionCodesHtml(List<JurCodesTypeItem> jurisdictionCodes) {
        StringBuilder sb = new StringBuilder()
                .append("<h2>Jurisdiction Codes</h2>")
                .append("<a target=\"_blank\" href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">")
                .append("View all jurisdiction codes and descriptors (opens in new tab)</a><br><br>");


        for (JurCodesTypeItem codeItem : jurisdictionCodes) {
            String codeName = codeItem.getValue().getJuridictionCodesList();
            sb.append("<strong>")
                    .append(codeName)
                    .append("</strong>")
                    .append(" - ")
                    .append(JurisdictionCode.valueOf(codeName).getDescription())
                    .append("<br><br>");
        }

        return sb.append("<hr>").toString();
    }
}
