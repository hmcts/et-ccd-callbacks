package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.time.LocalDate;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * ET3 vetting helper provides methods to assist with the ET3 vetting pages
 * this includes formatting markdown and querying the state of the ET3 response
 */
@Slf4j
public class Et3VettingHelper {

    static final String ET3_TABLE_DATA =
        "| Dates| |\r\n"
        + "|--|--|\r\n"
        + "|ET1 served| %s|\r\n"
        + "|ET3 due| %s|\r\n"
        + "|Extension| None|\r\n"
        + "|ET3 received| %s|";
    private Et3VettingHelper() {
        //Access through static methods
    }

    /**
     * Formats the case data into the table that is rendered on the "Is there an ET3 Response?" page
     * @param caseData The case data containing the ET3 response
     * @return A string containing markdown for a table, will change content depending on if/when the ET3 response has been submitted
     */
    public static String getEt3DatesInMarkdown(CaseData caseData) {
        return String.format(
                ET3_TABLE_DATA,
                formatDate(caseData.getClaimServedDate()),
                findEt3Due(caseData.getClaimServedDate()),
                findEt3Received(caseData)
        );
    }

    /**
     * Check if an ET3 response has been submitted on a given case data
     * @param caseData The case data to check
     * @return True if ET3 submitted, False if not submitted or if the respondent collection is empty
     */
    public static boolean isThereAnEt3Response(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        if (CollectionUtils.isEmpty(respondentCollection)) {
            log.error("Respondent collection is empty for case ref " + caseData.getEthosCaseReference());
            return false;
        }

        for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
            RespondentSumType respondent = respondentSumTypeItem.getValue();
            if (!isNullOrEmpty(respondent.getResponseReceived()) && respondent.getResponseReceived().equals(YES)) {
                return true;
            }
        }

        return false;
    }

    private static String findEt3Due(String et3DueDate) {
        return isNullOrEmpty(et3DueDate)
                ? "Cannot find ET3 Due Date"
                : UtilHelper.formatCurrentDatePlusDays(LocalDate.parse(et3DueDate), 29);
    }

    private static String formatDate(String date) {
        return isNullOrEmpty(date)
                ? "Cannot find ET1 Served Date"
                : UtilHelper.listingFormatLocalDate(date);
    }

    private static String findEt3Received(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        if (CollectionUtils.isEmpty(respondentCollection)) {
            log.error("Respondent collection is empty for case ref " + caseData.getEthosCaseReference());
            return NO;
        }

        for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
            RespondentSumType respondent = respondentSumTypeItem.getValue();
            if (!isNullOrEmpty(respondent.getResponseReceived()) && respondent.getResponseReceived().equals(YES)) {
                return UtilHelper.listingFormatLocalDate(respondent.getResponseReceivedDate());
            }
        }

        return NO;
    }
}