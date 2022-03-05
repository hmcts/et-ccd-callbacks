package uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SessionDaysReportParams {
    private final String caseTypeId;
    private final String managingOffice;
    private final String dateFrom;
    private final String dateTo;
}
