package uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.reports.eccreport.EccReportSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class EccReportCcdDataSource implements EccReportDataSource {
    private final String authToken;
    private final CcdClient ccdClient;

    @Override
    public List<EccReportSubmitEvent> getData(ReportParams reportParams) {
        try {
            var query = EccReportElasticSearchQuery.create(reportParams.getManagingOffice(), reportParams.getDateFrom(),
                    reportParams.getDateTo());
            return ccdClient.eccReportSearch(authToken, reportParams.getCaseTypeId(), query);
        } catch (Exception e) {
            throw new ReportException(String.format(
                    "Failed to get ECC Report search results for case type %s and office %s",
                    reportParams.getCaseTypeId(), reportParams.getManagingOffice()), e);
        }
    }
}