package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.reports.hearingstojudgments.HearingsToJudgmentsSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class HearingsToJudgmentsCcdReportDataSource implements HearingsToJudgmentsReportDataSource {
    private final String authToken;
    private final CcdClient ccdClient;

    @Override
    public List<HearingsToJudgmentsSubmitEvent> getData(String caseTypeId, String managingOffice,
                                                        String listingDateFrom, String listingDateTo) {
        try {
            var query = HearingsToJudgmentsElasticSearchQuery.create(managingOffice, listingDateFrom, listingDateTo);
            return ccdClient.hearingsToJudgementsSearch(authToken, caseTypeId, query);
        } catch (Exception e) {
            throw new ReportException(String.format(
                    "Failed to get Hearings To Judgments search results for case type id %s", caseTypeId), e);
        }
    }
}
