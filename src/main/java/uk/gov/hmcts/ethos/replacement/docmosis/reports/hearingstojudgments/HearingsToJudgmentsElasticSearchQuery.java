package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

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
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_JUDGMENT_COLLECTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_STATE_KEYWORD;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReport.VALID_CASE_STATES;
@SuppressWarnings({"PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal"})
class HearingsToJudgmentsElasticSearchQuery {

    private HearingsToJudgmentsElasticSearchQuery() {
        // Access through static methods
    }

    static String create(String managingOffice, String dateToSearchFrom, String dateToSearchTo) {
        var boolQueryBuilder = boolQuery()
                .must(new ExistsQueryBuilder(ELASTICSEARCH_FIELD_JUDGMENT_COLLECTION))
                .must(new ExistsQueryBuilder(ELASTICSEARCH_FIELD_HEARING_COLLECTION))
                .filter(new TermsQueryBuilder(ELASTICSEARCH_FIELD_STATE_KEYWORD, VALID_CASE_STATES))
                .filter(new RangeQueryBuilder(ELASTICSEARCH_FIELD_HEARING_LISTED_DATE)
                        .gte(dateToSearchFrom).lte(dateToSearchTo));

        if (StringUtils.isNotBlank(managingOffice) && TribunalOffice.isEnglandWalesOffice(managingOffice)) {
            boolQueryBuilder.filter(new TermsQueryBuilder(ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD, managingOffice));
        }

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(boolQueryBuilder).toString();
    }
}
