package uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD;

class RespondentReportElasticSearchQueryTest {

    private static Stream<Arguments> queryShouldNotIncludeManagingOffice() {
        return Stream.of(
                Arguments.of(TribunalOffice.ABERDEEN.getOfficeName()),
                Arguments.of(TribunalOffice.DUNDEE.getOfficeName()),
                Arguments.of(TribunalOffice.EDINBURGH.getOfficeName()),
                Arguments.of(TribunalOffice.GLASGOW.getOfficeName()),
                Arguments.of("")
        );
    }


    @ParameterizedTest
    @MethodSource
    void queryShouldNotIncludeManagingOffice(String managingOffice){
        var dateToSearchFrom = "2020-02-02";
        var dateToSearchTo = "2020-02-20";
        var elasticSearchQuery = RespondentsReportElasticSearchQuery.create(managingOffice, dateToSearchFrom, dateToSearchTo);
        assertFalse(elasticSearchQuery.contains(ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD));
    }

    private static Stream<Arguments> queryShouldIncludeManagingOffice() {
        return Stream.of(
                Arguments.of(TribunalOffice.BRISTOL.getOfficeName()),
                Arguments.of(TribunalOffice.LEEDS.getOfficeName()),
                Arguments.of(TribunalOffice.LONDON_CENTRAL.getOfficeName()),
                Arguments.of(TribunalOffice.LONDON_EAST.getOfficeName()),
                Arguments.of(TribunalOffice.LONDON_CENTRAL.getOfficeName()),
                Arguments.of(TribunalOffice.LONDON_SOUTH.getOfficeName()),
                Arguments.of(TribunalOffice.MANCHESTER.getOfficeName()),
                Arguments.of(TribunalOffice.MIDLANDS_EAST.getOfficeName()),
                Arguments.of(TribunalOffice.MIDLANDS_WEST.getOfficeName()),
                Arguments.of(TribunalOffice.NEWCASTLE.getOfficeName()),
                Arguments.of(TribunalOffice.WALES.getOfficeName()),
                Arguments.of(TribunalOffice.WATFORD.getOfficeName())
        );
    }

    @ParameterizedTest
    @MethodSource
    void queryShouldIncludeManagingOffice(String managingOffice) {
        var dateToSearchFrom = "2020-02-02";
        var dateToSearchTo = "2020-02-20";
        var elasticSearchQuery = RespondentsReportElasticSearchQuery.create(managingOffice, dateToSearchFrom, dateToSearchTo);
        assertTrue(elasticSearchQuery.contains(ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD));
    }

}
