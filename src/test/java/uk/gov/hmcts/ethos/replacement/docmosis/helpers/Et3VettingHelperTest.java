package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentBuilder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MSL_HEARING_FORMAT_VIDEO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.ET3_TABLE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.NO_CLAIM_SERVED_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.NO_ET3_RESPONSE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper.NO_RESPONDENTS_FOUND_ERROR;

class Et3VettingHelperTest {
    private static final String CASE_NOT_LISTED = "<h2>Hearing details</h2>The case has not been listed<hr>";
    private List<String> errors;

    @Test
    void givenET3Received_datesShouldShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(YES, "2022-03-01")
                .build()
            )
            .withClaimServedDate("2022-01-01")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "29 January 2022", "None",  "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenET3NotReceived_et3ShouldNotShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(NO, "2022-03-01")
                .build()
            )
            .withClaimServedDate("2022-01-01")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "29 January 2022", "None", NO);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenET1isNull_et1ShouldShowError() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(YES, "2022-03-01")
                .build()
            )
            .withClaimServedDate(null)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date", "Cannot find ET3 Due Date",
            "None", "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenAnExtensionDate_extensionDateShouldShow() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(YES, "2022-03-01")
                .withExtension()
                .build()
            )
            .withClaimServedDate("2022-01-01")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "29 January 2022",
            "1 March 2022", "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenAllFieldsEmpty() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        String actualResult = Et3VettingHelper.getEt3DatesInMarkdown(caseDetails.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date",
            "Cannot find ET3 Due Date", "None", "No");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void ifThereIsAnEt3Response() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withChooseEt3Respondent("Jack")
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(YES, "2022-03-01")
                .build()
            )
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
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(NO, "2022-03-01")
                .build()
            )
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
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-01-01")
                .build()
            )
            .withRespondent(RespondentBuilder.builder()
                .withName("Terry")
                .withReceived(YES, "2022-03-01")
                .build()
            )
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(NO, "2022-03-01")
                .build()
            )
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
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-01-06")
                .build()
            )
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(YES, "2022-02-02")
                .build()
            )
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
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-02-06")
                .build()
            )
            .withRespondent(RespondentBuilder.builder()
                .withName("Jack")
                .withReceived(YES, "2022-02-02")
                .build()
            )
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
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-02-05")
                .withExtension()
                .build()
            )
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
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-03-02")
                .build()
            )
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
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-03-02")
                .build()
            )
            .build();
        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(NO_CLAIM_SERVED_DATE));
    }

    @Test
    void givenNoEt3Response_shouldReturnError() {
        CaseData caseData = CaseDataBuilder.builder()
            .withChooseEt3Respondent("John")
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(NO, "2022-03-02")
                .build()
            )
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
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(NO, "2022-03-02")
                .build()
            )
            .build();

        errors = Et3VettingHelper.calculateResponseTime(caseData);
        assertThat(errors.size(), is(2));
        assertTrue(errors.contains(NO_ET3_RESPONSE));
        assertTrue(errors.contains(NO_CLAIM_SERVED_DATE));
    }

    private String generateEt3Dates(String servedDate, String dueDate, String extensionDate, String et3ReceivedDate) {
        return String.format(ET3_TABLE_DATA, servedDate, dueDate, extensionDate, et3ReceivedDate);
    }

    @Test
    void givenNoRespondents_shouldReturnNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .withChooseEt3Respondent("John")
            .withClaimServedDate("2022-01-01")
            .build();

        Et3VettingHelper.getRespondentNameAndAddress(caseData);

        assertNull(caseData.getEt3NameAddressRespondent());
    }

    @Test
    void givenNameAndAddress_shouldReturnMarkupWithNameAndAddress() {
        CaseData caseData = CaseDataBuilder.builder()
            .withChooseEt3Respondent("John")
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withET3ResponseRespondentName("John")
                .withReceived(YES, "2022-02-05")
                .withExtension()
                .withET3ResponseRespondentAddress("32 Bridge Road", "Erith", "", "", "DA8 2DE")
                .withAddress("47 Bridge Road", "Erith", "", "", "DA8 2DE")
                .build()
            )
            .withClaimServedDate("2022-01-01")
            .build();

        Et3VettingHelper.getRespondentNameAndAddress(caseData);
        String expected = "<h2>Respondent</h2><pre>Name &#09&#09&#09&#09&#09&#09&nbsp; John<br><br>Contact "
            + "address &#09&#09 32 Bridge Road<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09Erith<br>&#09&#09&#09&#09&#09"
            + "&#09&#09&#09&#09<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09DA8 2DE</pre><hr>";

        assertThat(caseData.getEt3NameAddressRespondent(), is(expected));
    }

    @Test
    void givenNameButNoAddress_shouldReturnMarkupWithName() {
        CaseData caseData = CaseDataBuilder.builder()
            .withChooseEt3Respondent("John")
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withET3ResponseRespondentName("John")
                .withReceived(YES, "2022-02-05")
                .withExtension()
                .build()
            )
            .withClaimServedDate("2022-01-01")
            .build();

        Et3VettingHelper.getRespondentNameAndAddress(caseData);
        String expected = "<h2>Respondent</h2><pre>Name &#09&#09&#09&#09&#09&#09&nbsp; John<br><br>Contact "
            + "address &#09&#09 None Given</pre><hr>";

        assertThat(caseData.getEt3NameAddressRespondent(), is(expected));
    }

    @Test
    void givenNoNameAndAddress_shouldReturnMarkupWithNoNameAndAddress() {
        CaseData caseData = CaseDataBuilder.builder()
            .withChooseEt3Respondent("John")
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-02-05")
                .withExtension()
                .withET3ResponseRespondentAddress("32 Bridge Road", "Erith", "", "", "DA8 2DE")
                .withAddress("47 Bridge Road", "Erith", "", "", "DA8 2DE")
                .build()
            )
            .withClaimServedDate("2022-01-01")
            .build();

        Et3VettingHelper.getRespondentNameAndAddress(caseData);
        String expected = "<h2>Respondent</h2><pre>Name &#09&#09&#09&#09&#09&#09&nbsp; None Given<br><br>"
            + "Contact address &#09&#09 32 Bridge Road<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09Erith<br>&#09&#09&#09"
            + "&#09&#09&#09&#09&#09&#09<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09DA8 2DE</pre><hr>";

        assertThat(caseData.getEt3NameAddressRespondent(), is(expected));
    }

    @Test
    void givenAddressWithLine3_shouldReturnMarkupWithNoNameAndAddress() {
        CaseData caseData = CaseDataBuilder.builder()
            .withChooseEt3Respondent("John")
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-02-05")
                .withExtension()
                .withET3ResponseRespondentName("John")
                .withET3ResponseRespondentAddress("32 Bridge Road", "Erith", "Erith", "Erith", "DA8 2DE")
                .build()
            )
            .withClaimServedDate("2022-01-01")
            .build();

        Et3VettingHelper.getRespondentNameAndAddress(caseData);
        String expected = "<h2>Respondent</h2><pre>Name &#09&#09&#09&#09&#09&#09&nbsp; John<br><br>Contact "
            + "address &#09&#09 32 Bridge Road<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09Erith<br>&#09&#09&#09&#09&#09"
            + "&#09&#09&#09&#09Erith<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09Erith<br>&#09&#09&#09&#09&#09&#09&#09&#09"
            + "&#09DA8 2DE</pre><hr>";

        assertThat(caseData.getEt3NameAddressRespondent(), is(expected));
    }

    @Test
    void givenAddressWithNoLine2_shouldReturnMarkupWithNoNameAndAddress() {
        CaseData caseData = CaseDataBuilder.builder()
            .withChooseEt3Respondent("John")
            .withRespondent(RespondentBuilder.builder()
                .withName("John")
                .withReceived(YES, "2022-02-05")
                .withExtension()
                .withET3ResponseRespondentName("John")
                .withET3ResponseRespondentAddress("32 Bridge Road", "", "Erith", "", "DA8 2DE")
                .build()
            )
            .withClaimServedDate("2022-01-01")
            .build();

        Et3VettingHelper.getRespondentNameAndAddress(caseData);

        String expected = "<h2>Respondent</h2><pre>Name &#09&#09&#09&#09&#09&#09&nbsp; John<br><br>Contact"
            + " address &#09&#09 32 Bridge Road<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09Erith<br>&#09&#09&#09&#09&#09"
            + "&#09&#09&#09&#09<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09DA8 2DE</pre><hr>";

        assertThat(caseData.getEt3NameAddressRespondent(), is(expected));
    }

    @Test
    void givenHearingIsListed_SetCaseAsListed() {
        CaseData caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", List.of(MSL_HEARING_FORMAT_VIDEO), "2", "Days", "Sit Alone")
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000", HEARING_STATUS_LISTED, false)
            .withConciliationTrack("Test track")
            .build();
        Et3VettingHelper.setHearingListedForExUi(caseData);

        assertThat(caseData.getEt3HearingDetails(), is(
                "| <h2>Hearing details</h2>| | \r\n"
                    + "|--|--|\r\n"
                    + "|Date| Saturday 25 December 2021|\r\n"
                    + "|Hearing Type| test|\r\n"
                    + "|Hearing Length| 2 Days|\r\n"
                    + "|Hearing Format| Video|\r\n"
                    + "|Sit Alone/Full Panel| Sit Alone|\r\n"
                    + "|Track| Test track|"
            )
        );
        assertThat(caseData.getEt3IsCaseListedForHearing(), is(YES));
    }

    @Test
    void givenHearingIsNotListed_SetCaseAsNotListed() {
        CaseData caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", List.of(MSL_HEARING_FORMAT_VIDEO), "2", "Days", "Sit Alone")
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000", HEARING_STATUS_POSTPONED, false)
            .withConciliationTrack("Test track")
            .build();
        Et3VettingHelper.setHearingListedForExUi(caseData);

        assertThat(caseData.getEt3HearingDetails(), is(CASE_NOT_LISTED));
        assertThat(caseData.getEt3IsCaseListedForHearing(), is(NO));
    }

    @Test
    void givenHearingIsListedButNoTrack_SetCaseAsListedNoTrackFound() {
        CaseData caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", List.of(MSL_HEARING_FORMAT_VIDEO), "2", "Days", "Sit Alone")
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000", HEARING_STATUS_LISTED, false)
            .build();
        Et3VettingHelper.setHearingListedForExUi(caseData);

        assertThat(caseData.getEt3HearingDetails(), is(
                "| <h2>Hearing details</h2>| | \r\n"
                    + "|--|--|\r\n"
                    + "|Date| Saturday 25 December 2021|\r\n"
                    + "|Hearing Type| test|\r\n"
                    + "|Hearing Length| 2 Days|\r\n"
                    + "|Hearing Format| Video|\r\n"
                    + "|Sit Alone/Full Panel| Sit Alone|\r\n"
                    + "|Track| Track could not be found|"
            )
        );
        assertThat(caseData.getEt3IsCaseListedForHearing(), is(YES));
    }

    @Test
    void givenNoHearings_SetCaseAsNotListed() {
        CaseData caseData = CaseDataBuilder.builder().build();
        Et3VettingHelper.setHearingListedForExUi(caseData);

        assertThat(caseData.getEt3HearingDetails(), is(CASE_NOT_LISTED));
        assertThat(caseData.getEt3IsCaseListedForHearing(), is(NO));
    }

    @Test
    void givenManagingOfficeEnglandWales_returnExpectedTable() {
        CaseData caseData = CaseDataBuilder.builder()
            .withManagingOffice(TribunalOffice.MANCHESTER.getOfficeName())
            .build();
        Et3VettingHelper.transferApplication(caseData);

        assertThat(caseData.getEt3TribunalLocation(), is(
            "| <h2>Tribunal location</h2>| | \r\n"
                + "|--|--|\r\n"
                + "|Tribunal| England & Wales|\r\n"
                + "|Office| Manchester|"
        ));
    }

    @Test
    void givenManagingOfficeScotland_returnExpectedTable() {
        CaseData caseData = CaseDataBuilder.builder()
            .withManagingOffice(TribunalOffice.GLASGOW.getOfficeName())
            .build();
        Et3VettingHelper.transferApplication(caseData);

        assertThat(caseData.getEt3TribunalLocation(), is(
            "| <h2>Tribunal location</h2>| | \r\n"
                + "|--|--|\r\n"
                + "|Tribunal| Scotland|\r\n"
                + "|Office| Glasgow|"
        ));
    }

    @Test
    void givenNoManagingOffice_returnIllegalArgumentException() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThrows(IllegalArgumentException.class, () -> Et3VettingHelper.transferApplication(caseData));
    }
}