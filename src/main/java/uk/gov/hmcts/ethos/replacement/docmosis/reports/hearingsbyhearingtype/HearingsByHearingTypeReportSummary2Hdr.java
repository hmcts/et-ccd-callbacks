package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class HearingsByHearingTypeReportSummary2Hdr {

    HearingsByHearingTypeReportSummary2Hdr(String subSplit) {
        this.subSplit = subSplit;
    }
    private ReportFields fields;
    private String subSplit;
}
