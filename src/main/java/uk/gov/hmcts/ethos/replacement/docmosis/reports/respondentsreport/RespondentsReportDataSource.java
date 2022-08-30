package uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport;

import uk.gov.hmcts.et.common.model.reports.respondentsreport.RespondentsReportSubmitEvent;
import java.util.List;

public interface RespondentsReportDataSource {
    List<RespondentsReportSubmitEvent> getData(String caseTypeId, String managingOffice, String listingDateFrom,
                                               String listingDateTo);
}
