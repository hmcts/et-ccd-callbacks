package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import uk.gov.hmcts.et.common.model.reports.hearingstojudgments.HearingsToJudgmentsSubmitEvent;

import java.util.List;

public interface HearingsToJudgmentsReportDataSource {
    List<HearingsToJudgmentsSubmitEvent> getData(String caseTypeId, String managingOffice, String listingDateFrom,
                                                 String listingDateTo);
}