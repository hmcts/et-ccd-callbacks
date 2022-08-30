package uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport;

import uk.gov.hmcts.et.common.model.reports.eccreport.EccReportSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.util.List;

public interface EccReportDataSource {
    List<EccReportSubmitEvent> getData(ReportParams reportParams);
}
