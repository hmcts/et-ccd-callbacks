package uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;

final class EccReportElasticSearchQuery {

    private EccReportElasticSearchQuery() {
        // Access through static methods
    }

    static String create(String managingOffice, String dateToSearchFrom, String dateToSearchTo) {
        BoolQueryBuilder boolQueryBuilder = boolQuery();
        if (StringUtils.isNotBlank(managingOffice)) {
            boolQueryBuilder.must(new MatchQueryBuilder("data.managingOffice", managingOffice));
        }

        boolQueryBuilder
                .must(new ExistsQueryBuilder("data.eccCases"))
                .must(new ExistsQueryBuilder("data.respondentCollection"))
                .filter(new RangeQueryBuilder("data.receiptDate").gte(dateToSearchFrom).lte(dateToSearchTo));

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }
}
