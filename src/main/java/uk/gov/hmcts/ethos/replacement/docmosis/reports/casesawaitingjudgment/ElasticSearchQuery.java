package uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReport.VALID_POSITION_TYPES;

final class ElasticSearchQuery {

    private ElasticSearchQuery() {
        // Access through static methods
    }

    static String create(String managingOffice) {
        var boolQueryBuilder = boolQuery()
                .mustNot(new MatchQueryBuilder("state.keyword", "Closed"))
                .mustNot(new ExistsQueryBuilder("data.judgementCollection"))
                .must(new ExistsQueryBuilder("data.hearingCollection"))
                .filter(new TermsQueryBuilder("data.positionType.keyword", VALID_POSITION_TYPES));

        if (StringUtils.isNotBlank(managingOffice) && TribunalOffice.isEnglandWalesOffice(managingOffice)) {
            boolQueryBuilder.filter(new TermsQueryBuilder(ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD, managingOffice));
        }

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }
}
