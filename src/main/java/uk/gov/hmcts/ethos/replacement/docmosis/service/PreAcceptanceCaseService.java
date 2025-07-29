package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        CasePreAcceptType preAcceptCase = caseData.getPreAcceptCase();
        LocalDate dateOfReceipt = LocalDate.parse(caseData.getReceiptDate());
        if (YES.equals(preAcceptCase.getCaseAccepted())) {
            LocalDate dateAccepted = LocalDate.parse(preAcceptCase.getDateAccepted());
            if (dateAccepted.isBefore(dateOfReceipt)) {
                errors.add(ACCEPTED_DATE_SHOULD_NOT_BE_EARLIER_THAN_THE_CASE_RECEIVED_DATE);
            }
        } else if (NO.equals(preAcceptCase.getCaseAccepted())) {
            LocalDate dateRejected = LocalDate.parse(preAcceptCase.getDateRejected());
            if (dateRejected.isBefore(dateOfReceipt)) {
                errors.add(REJECTED_DATE_SHOULD_NOT_BE_EARLIER_THAN_THE_CASE_RECEIVED_DATE);
            }
        }
        return errors;
    }
}
