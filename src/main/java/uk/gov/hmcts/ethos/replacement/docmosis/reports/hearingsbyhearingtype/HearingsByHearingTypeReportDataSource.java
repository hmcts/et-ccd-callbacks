package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype;

import uk.gov.hmcts.et.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;
import java.util.List;

public interface HearingsByHearingTypeReportDataSource {
    List<HearingsByHearingTypeSubmitEvent> getData(ReportParams reportParams);
}
