package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.ET3_TABLE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.NO_RESPONDENTS_FOUND_ERROR;


class Et3VettingHelperTest {

    private List<String> errors;
    @Test
    void givenET3Received_datesShouldShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withChooseEt3Respondent("Jack")
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
                .withChooseEt3Respondent("Jack")
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
                .withChooseEt3Respondent("Jack")
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
                .withChooseEt3Respondent("Jack")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date", "Cannot find ET3 Due Date", "No");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void ifThereIsAnEt3Response() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withChooseEt3Respondent("Jack")
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
                .withChooseEt3Respondent("Jack")
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

    @Test
    void populateRespondentDynamicList() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withRespondent("John", YES, "2022-01-01")
                .withRespondent("Terry", YES, "2022-03-01")
                .withRespondent("Jack", NO, "2022-03-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        errors = Et3VettingHelper.populateRespondentDynamicList(caseDetails.getCaseData());
        assertEquals(0, errors.size());
        assertEquals(3, caseDetails.getCaseData().getEt3ChooseRespondent().getListItems().size());
    }

    @Test
    void populateRespondentDynamicList_noRespondentsError() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("123456789/1234")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        errors = Et3VettingHelper.populateRespondentDynamicList(caseDetails.getCaseData());
        assertEquals(String.format(NO_RESPONDENTS_FOUND_ERROR, "123456789/1234"), errors.get(0));
    }

    private String generateEt3Dates(String et1ServedDate, String et3DueDate, String et3ReceivedDate) {
        return String.format(ET3_TABLE_DATA, et1ServedDate, et3DueDate, et3ReceivedDate);
    }
}
