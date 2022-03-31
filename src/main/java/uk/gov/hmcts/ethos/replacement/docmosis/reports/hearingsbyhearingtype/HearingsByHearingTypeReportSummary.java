package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class HearingsByHearingTypeReportSummary {
    private String date;
    private ReportFields fields;
}
