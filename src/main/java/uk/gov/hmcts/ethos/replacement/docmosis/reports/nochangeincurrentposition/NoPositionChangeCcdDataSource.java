package uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.multiples.MultipleCaseSearchResult;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;

@RequiredArgsConstructor
@Slf4j
public class NoPositionChangeCcdDataSource implements NoPositionChangeDataSource {

    private final String authToken;
    private final CcdClient ccdClient;

    @Override
    public List<NoPositionChangeSubmitEvent> getData(String caseTypeId, String reportDate, String managingOffice) {
        try {
            String reportDate3MonthsAgo = LocalDate.parse(reportDate, OLD_DATE_TIME_PATTERN2)
                .minusMonths(3).format(OLD_DATE_TIME_PATTERN2);
            String query = NoPositionChangeElasticSearchQuery.create(reportDate3MonthsAgo, managingOffice);
            List<NoPositionChangeSubmitEvent> submitEvents = new ArrayList<>();
            NoPositionChangeSearchResult searchResult = ccdClient.runElasticSearch(authToken, caseTypeId, query,
                    NoPositionChangeSearchResult.class);

            if (searchResult != null && CollectionUtils.isNotEmpty(searchResult.getCases())) {
                submitEvents.addAll(searchResult.getCases());
            }

            return submitEvents;
        } catch (Exception e) {
            throw new ReportException(String.format(
                    "Failed to get No Change In Current Position search results for case type id %s", caseTypeId), e);
        }
    }

    @Override
    public List<SubmitMultipleEvent> getMultiplesData(String caseTypeId, List<String> multipleRefsList) {
        try {
            String query = NoPositionChangeMultiplesElasticSearchQuery.create(multipleRefsList);
            List<SubmitMultipleEvent> submitEvents = new ArrayList<>();
            MultipleCaseSearchResult searchResult = ccdClient.runElasticSearch(authToken, caseTypeId, query,
                MultipleCaseSearchResult.class);

            if (searchResult != null && CollectionUtils.isNotEmpty(searchResult.getCases())) {
                submitEvents.addAll(searchResult.getCases());
            }

            return submitEvents;
        } catch (Exception e) {
            throw new ReportException(String.format(
                    "Failed to get No Change In Current Position multiples search results for case type id %s",
                    caseTypeId), e);
        }
    }
}
