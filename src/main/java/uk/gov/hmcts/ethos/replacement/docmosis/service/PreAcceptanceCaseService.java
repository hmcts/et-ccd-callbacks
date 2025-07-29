package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@Service("preAcceptanceCaseService")
public class PreAcceptanceCaseService {
    private static final String ACCEPTED_DATE_SHOULD_NOT_BE_EARLIER_THAN_THE_CASE_RECEIVED_DATE =
            "Accepted date should not be earlier than the case received date";
    private static final String REJECTED_DATE_SHOULD_NOT_BE_EARLIER_THAN_THE_CASE_RECEIVED_DATE =
            "Rejected date should not be earlier than the case received date";

    public List<String> validateAcceptanceDate(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (caseData == null) {
            errors.add("Case data is missing");
            return errors;
        }

        CasePreAcceptType preAcceptCase = caseData.getPreAcceptCase();
        if (preAcceptCase == null) {
            errors.add("Pre-acceptance case data is missing");
            return errors;
        }

        LocalDate receiptDate = getLocalDate(caseData.getReceiptDate());
        if (receiptDate == null) {
            errors.add("Receipt date is missing or invalid");
            return errors;
        }

        if (isNullOrEmpty(preAcceptCase.getCaseAccepted())) {
            errors.add("Case acceptance status is missing");
            return errors;
        }

        if (YES.equals(preAcceptCase.getCaseAccepted())) {
            LocalDate dateAccepted = getLocalDate(preAcceptCase.getDateAccepted());
            if (dateAccepted == null) {
                errors.add("Accepted date is missing or invalid");
                return errors;
            }
            if (dateAccepted.isBefore(receiptDate)) {
                errors.add(ACCEPTED_DATE_SHOULD_NOT_BE_EARLIER_THAN_THE_CASE_RECEIVED_DATE);
            }
        } else if (NO.equals(preAcceptCase.getCaseAccepted())) {
            LocalDate dateRejected = getLocalDate(preAcceptCase.getDateRejected());
            if (dateRejected == null) {
                errors.add("Rejected date is missing or invalid");
                return errors;
            }
            if (dateRejected.isBefore(receiptDate)) {
                errors.add(REJECTED_DATE_SHOULD_NOT_BE_EARLIER_THAN_THE_CASE_RECEIVED_DATE);
            }
        }

        return errors;
    }

    private static LocalDate getLocalDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            return null;
        }
    }
}
