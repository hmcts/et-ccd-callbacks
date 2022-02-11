package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class HearingsToJudgmentsCcdDataSource implements HearingsToJudgmentsDataSource {
    private final String authToken;
    private final CcdClient ccdClient;

    @Override
    public List<HearingsToJudgmentsSubmitEvent> getData(String caseTypeId, String managingOffice,
                                                        String listingDateFrom, String listingDateTo) {

        try {
            var query = HearingsToJudgmentsElasticSearchQuery.create(managingOffice, listingDateFrom, listingDateTo);
            var searchResult = ccdClient.runElasticSearch(authToken, caseTypeId, query,
                    HearingsToJudgmentsSearchResult.class);

            if (searchResult != null && CollectionUtils.isNotEmpty(searchResult.getCases())) {
                return searchResult.getCases();
            }

            return new ArrayList<>();
        } catch (Exception e) {
            throw new ReportException(String.format(
                    "Failed to get Hearings To Judgments search results for case type id %s", caseTypeId), e);
        }
    }
}
