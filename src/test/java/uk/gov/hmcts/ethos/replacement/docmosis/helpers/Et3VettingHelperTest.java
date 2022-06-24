package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.ET3_TABLE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.NO_CLAIM_SERVED_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.NO_ET3_RESPONSE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.NO_RESPONDENTS_FOUND_ERROR;

class Et3VettingHelperTest {

    private List<String> errors;
    @Test
    void givenET3Received_datesShouldShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withChooseEt3Respondent("Jack")
                .withRespondent("Jack", YES, "2022-03-01", false)
                .withClaimServedDate("2022-01-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "30 January 2022", "None",  "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenET3NotReceived_et3ShouldNotShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withChooseEt3Respondent("Jack")
                .withRespondent("Jack", NO, "2022-03-01", false)
                .withClaimServedDate("2022-01-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "30 January 2022", "None", NO);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenET1isNull_et1ShouldShowError() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withChooseEt3Respondent("Jack")
                .withRespondent("Jack", YES, "2022-03-01", false)
                .withClaimServedDate(null)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date", "Cannot find ET3 Due Date", "None", "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenAnExtensionDate_extensionDateShouldShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withChooseEt3Respondent("Jack")
                .withRespondent("Jack", YES, "2022-03-01", true)
                .withClaimServedDate("2022-01-01")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "30 January 2022", "1 March 2022", "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenAllFieldsEmpty() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withChooseEt3Respondent("Jack")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date", "Cannot find ET3 Due Date", "None", "No");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void ifThereIsAnEt3Response() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withChooseEt3Respondent("Jack")
                .withRespondent("Jack", YES, "2022-03-01", false)
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
                .withRespondent("Jack", NO, "2022-03-01", false)
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
                .withRespondent("John", YES, "2022-01-01", false)
                .withRespondent("Terry", YES, "2022-03-01", false)
                .withRespondent("Jack", NO, "2022-03-01", false)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        errors = Et3VettingHelper.populateRespondentDynamicList(caseDetails.getCaseData());
        assertThat(errors.size(), is(0));
        assertThat(caseDetails.getCaseData().getEt3ChooseRespondent().getListItems().size(), is(3));
    }

    @Test
    void populateRespondentDynamicList_noRespondentsError() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("123456789/1234")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        errors = Et3VettingHelper.populateRespondentDynamicList(caseDetails.getCaseData());
        assertThat(errors.get(0), is(String.format(NO_RESPONDENTS_FOUND_ERROR, "123456789/1234")));
    }

    @Test
    void givenTheEt3IsOnTime_ResponseInTimeShouldBeYes() {
        CaseData caseData = CaseDataBuilder.builder()
                .withChooseEt3Respondent("John")
                .withRespondent("John", YES, "2022-01-06", false)
                .withRespondent("Jack", YES, "2022-02-02", false)
                .withClaimServedDate("2022-01-01")
                .build();
        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(0));
        assertThat(caseData.getEt3ResponseInTime(), is(YES));
    }

    @Test
    void givenTheEt3IsLate_ResponseInTimeShouldBeNo() {
        CaseData caseData = CaseDataBuilder.builder()
                .withChooseEt3Respondent("John")
                .withRespondent("John", YES, "2022-02-06", false)
                .withRespondent("Jack", YES, "2022-02-02", false)
                .withClaimServedDate("2022-01-01")
                .build();
        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(0));
        assertThat(caseData.getEt3ResponseInTime(), is(NO));
    }

    @Test
    void givenThatResponseIsBeforeExtension_responseInTimeShouldBeYes() {
        CaseData caseData = CaseDataBuilder.builder()
                .withChooseEt3Respondent("John")
                .withRespondent("John", YES, "2022-02-05", true)
                .withClaimServedDate("2022-01-01")
                .build();
        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(0));
        assertThat(caseData.getEt3ResponseInTime(), is(YES));
    }

    @Test
    void givenThatResponseIsAfterExtension_responseInTimeShouldBeNo() {
        CaseData caseData = CaseDataBuilder.builder()
                .withChooseEt3Respondent("John")
                .withRespondent("John", YES, "2022-03-02", true)
                .withClaimServedDate("2022-01-01")
                .build();

        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(0));
        assertThat(caseData.getEt3ResponseInTime(), is(NO));
    }

    @Test
    void givenClaimServedDateIsMissing_shouldReturnError() {
        CaseData caseData = CaseDataBuilder.builder()
                .withChooseEt3Respondent("John")
                .withRespondent("John", YES, "2022-03-02", false)
                .build();
        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(NO_CLAIM_SERVED_DATE));
    }


    @Test
    void givenNoEt3Response_shouldReturnError() {
        CaseData caseData = CaseDataBuilder.builder()
                .withChooseEt3Respondent("John")
                .withRespondent("John", NO, "2022-03-02", false)
                .withClaimServedDate("2022-01-01")
                .build();

        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(NO_ET3_RESPONSE));
    }

    @Test
    void givenNoEt3ResponseAndNoClaimServedDate_shouldReturnMultipleErrors() {
        CaseData caseData = CaseDataBuilder.builder()
                .withChooseEt3Respondent("John")
                .withRespondent("John", NO, "2022-03-02", false)
                .build();

        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(2));
        assertTrue(errors.contains(NO_ET3_RESPONSE));
        assertTrue(errors.contains(NO_CLAIM_SERVED_DATE));
    }

    private String generateEt3Dates(String et1ServedDate, String et3DueDate, String extensionDate,String et3ReceivedDate) {
        return String.format(ET3_TABLE_DATA, et1ServedDate, et3DueDate, extensionDate, et3ReceivedDate);
    }
}
