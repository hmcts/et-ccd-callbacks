package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.refdatafixes;

import org.elasticsearch.index.query.ExistsQueryBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import static uk.gov.hmcts.et.common.model.helper.Constants.MAX_ES_SIZE;

public class RefDataFixesElasticSearchQuery {
    private RefDataFixesElasticSearchQuery() {
        // Access through static methods
    }

    static String create(String dateToSearchFrom, String dateToSearchTo) {
        var boolQueryBuilder = boolQuery()
                .must(new ExistsQueryBuilder("data.hearingCollection"))
                .filter(new RangeQueryBuilder(
                        "data.hearingCollection.value.hearingDateCollection.value.listedDate"
                ).gte(dateToSearchFrom).lte(dateToSearchTo));

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }
}
