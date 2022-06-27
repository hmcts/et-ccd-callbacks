package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getHearingDuration;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Slf4j
public class InitialConsiderationHelper {

    static final String RESPONDENT_NAME =
            "| Respondent name given | |\r\n"
                    + "|-------------|-------------|\r\n"
                    + "|In Et1 by claimant | %s|\r\n"
                    + "|In Et3 by Respondent | %s|";

    static final String HEARING_DETAILS =
            "| Hearing Details | |\r\n"
                    + "|-------------|-------------|\r\n"
                    + "|Date | %s|\r\n"
                    + "|Type | %s|\r\n"
                    + "|Duration | %s|";


    private InitialConsiderationHelper() {
    }


    public static String getRespondentName(CaseData caseData) {
        return caseData.getRespondentCollection().stream().map(
                        respondent -> String.format(
                                RESPONDENT_NAME, nullCheck(respondent.getValue().getRespondentName()),
                                nullCheck(respondent.getValue().getResponseRespondentName()))).findFirst()
                .orElse(String.format(RESPONDENT_NAME, "", ""));
    }

    public static String getHearingDetails(CaseData caseData) {
       return caseData.getHearingCollection().stream()
               .map(HearingTypeItem::getValue)
               .filter(hearing -> hearing.getHearingDateCollection() != null && !hearing.getHearingDateCollection().isEmpty()).min(Comparator.comparing(
                       (HearingType hearing) ->
                               getEarliestHearingDate(hearing.getHearingDateCollection()).orElse(LocalDate.now().plusYears(100))))
                .map(hearing -> String.format(HEARING_DETAILS,
                        hearing.getHearingType(),
                        getHearingDuration(hearing),
                        getEarliestHearingDate(hearing.getHearingDateCollection()).map(hearingDate -> hearingDate.toString()).orElse("")))

                .orElse(String.format(HEARING_DETAILS, "", "", ""));
    }

    public static Optional<LocalDate> getEarliestHearingDate(List<DateListedTypeItem> hearingDates) {
        return hearingDates.stream()
                .filter(hearingDate -> hearingDate.getValue().getListedDate() != null && !hearingDate.getValue().getListedDate().isEmpty())
                .map(hearingDateItem -> LocalDate.parse(hearingDateItem.getValue().getListedDate())).min(Comparator.naturalOrder());
    }
}
