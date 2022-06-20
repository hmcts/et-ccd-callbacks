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

@Slf4j
public class Et3VettingHelper {

    private Et3VettingHelper() {
        //Access through static methods
    }

    public static String getEt3Dates(CaseData caseData) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("| Dates| |\r\n");
        stringBuilder.append("|--|--|\r\n");
        stringBuilder.append(String.format("|ET1 Served| %s|\r\n", formatDate(caseData.getClaimServedDate())));
        stringBuilder.append(String.format("|ET3 due| %s|\r\n", findEt3Due(caseData.getClaimServedDate())));
        stringBuilder.append("|Extension| None|\r\n");
        stringBuilder.append(String.format("|ET3 received| %s|", findEt3Received(caseData)));

        return stringBuilder.toString();
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
        caseData.setEt3IsThereAnEt3Response(NO);

        if (CollectionUtils.isEmpty(respondentCollection)) {
            log.error("Respondent collection is empty for case ref " + caseData.getEthosCaseReference());
            return NO;
        }

        for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
            RespondentSumType respondent = respondentSumTypeItem.getValue();
            if (!isNullOrEmpty(respondent.getResponseReceived()) && respondent.getResponseReceived().equals(YES)) {
                caseData.setEt3IsThereAnEt3Response(YES);
                return UtilHelper.listingFormatLocalDate(respondent.getResponseReceivedDate());
            }
        }

        return NO;
    }
}