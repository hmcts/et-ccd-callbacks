package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import java.util.List;

public interface HearingsToJudgmentsDataSource {
    List<HearingsToJudgmentsSubmitEvent> getData(String caseTypeId, String managingOffice, String listingDateFrom,
                                                 String listingDateTo);
}