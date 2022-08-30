package uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays;

import uk.gov.hmcts.et.common.model.reports.sessiondays.SessionDaysSubmitEvent;

import java.util.List;

public interface SessionDaysReportDataSource {
    List<SessionDaysSubmitEvent> getData(String caseTypeId, String managingOffice,
                                         String listingDateFrom, String listingDateTo);
}
