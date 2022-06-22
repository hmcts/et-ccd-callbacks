package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.ET3_TABLE_DATA;


class Et3VettingHelperTest {
    @Test
    void givenET3Received_datesShouldShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withRespondent("Jack", YES, "2022-03-01")
                .withClaimServedDate("2022-01-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "30 January 2022", "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenET3NotReceived_et3ShouldNotShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withRespondent("Jack", NO, "2022-03-01")
                .withClaimServedDate("2022-01-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "30 January 2022", NO);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenET1isNull_et1ShouldShowError() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withRespondent("Jack", YES, "2022-03-01")
                .withClaimServedDate(null)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date", "Cannot find ET3 Due Date", "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenAllFieldsEmpty() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date", "Cannot find ET3 Due Date", "No");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void ifThereIsAnEt3Response() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withRespondent("Jack", YES, "2022-03-01")
                .withClaimServedDate("2022-01-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        boolean actual = Et3VettingHelper.isThereAnEt3Response(caseDetails.getCaseData());
        boolean expected = true;
        assertEquals(expected, actual);
    }

    @Test
    void ifThereIsNotAnEt3Response() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withRespondent("Jack", NO, "2022-03-01")
                .withClaimServedDate("2022-01-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        boolean actual = Et3VettingHelper.isThereAnEt3Response(caseDetails.getCaseData());
        boolean expected = false;
        assertEquals(expected, actual);
    }

    @Test
    void ifThereIsNoRespondentCollection() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withClaimServedDate("2022-01-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        boolean actual = Et3VettingHelper.isThereAnEt3Response(caseDetails.getCaseData());
        boolean expected = false;
        assertEquals(expected, actual);
    }
    private String generateEt3Dates(String et1ServedDate, String et3DueDate, String et3ReceivedDate) {
        return String.format(ET3_TABLE_DATA, et1ServedDate, et3DueDate, et3ReceivedDate);
    }
}
