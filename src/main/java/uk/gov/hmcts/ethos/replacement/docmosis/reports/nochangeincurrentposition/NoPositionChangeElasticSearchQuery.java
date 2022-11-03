package uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReport.VALID_CASE_STATES;

final class NoPositionChangeElasticSearchQuery {
    private NoPositionChangeElasticSearchQuery() {
        // Access through static methods
    }

    static String create(String reportDateLimit, String managingOffice) {
        var boolQueryBuilder = boolQuery()
                .must(new TermsQueryBuilder("state.keyword", VALID_CASE_STATES))
                .filter(new RangeQueryBuilder("data.dateToPosition").lte(reportDateLimit).includeUpper(false));

        if (StringUtils.isNotBlank(managingOffice) && TribunalOffice.isEnglandWalesOffice(managingOffice)) {
            boolQueryBuilder.filter(new TermsQueryBuilder(ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD, managingOffice));
        }

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }
}
