package uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_HEARING_COLLECTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_HEARING_LISTED_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD;

public final class SessionDaysElasticSearchQuery {
    private SessionDaysElasticSearchQuery() {
        // Access through static methods
    }

    static String create(String dateToSearchFrom, String dateToSearchTo, String managingOffice) {
        var boolQueryBuilder = boolQuery()
                .must(new ExistsQueryBuilder(ELASTICSEARCH_FIELD_HEARING_COLLECTION))
                .filter(new RangeQueryBuilder(ELASTICSEARCH_FIELD_HEARING_LISTED_DATE
                ).gte(dateToSearchFrom).lte(dateToSearchTo));

        if (StringUtils.isNotBlank(managingOffice) && TribunalOffice.isEnglandWalesOffice(managingOffice)) {
            boolQueryBuilder.filter(new TermsQueryBuilder(ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD, managingOffice));
        }

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }
}
