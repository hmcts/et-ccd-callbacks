package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.et.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD;

public class HearingsByHearingTypeElasticSearchQuery {
    private HearingsByHearingTypeElasticSearchQuery() {
        // Access through static methods
    }

    static String create(String managingOffice, String dateToSearchFrom, String dateToSearchTo) {
        var boolQueryBuilder = boolQuery()
                .must(new ExistsQueryBuilder("data.hearingCollection"))
                .filter(new RangeQueryBuilder(
                        "data.hearingCollection.value.hearingDateCollection.value.listedDate"
                ).gte(dateToSearchFrom).lte(dateToSearchTo));

        if (StringUtils.isNotBlank(managingOffice) && TribunalOffice.isEnglandWalesOffice(managingOffice)) {
            boolQueryBuilder.filter(new TermsQueryBuilder(ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD, managingOffice));
        }

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }
}
