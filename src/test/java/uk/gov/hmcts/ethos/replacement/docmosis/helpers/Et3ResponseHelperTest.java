package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

class Et3ResponseHelperTest {

    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseData = caseDetails.getCaseData();
    }

    @Test
    void givenClaimant_shouldFormatToTable() {
        caseData.setClaimant("Test Person");

        String expected = "<pre> ET1 claimant name&#09&#09&#09&#09 Test Person</pre><hr>";
        assertThat(Et3ResponseHelper.formatClaimantNameForHtml(caseData), is(expected));
    }

    @Test
    void givenValidStartDateAndEndDate_shouldReturnNoErrors() {
        caseData.setEt3ResponseEmploymentStartDate("2022-02-02");
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        assertThat(Et3ResponseHelper.validateEmploymentDates(caseData).size(), is(0));
    }

    @Test
    void givenNoStartDateAndEndDate_shouldNotValidateDates() {
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        assertThat(Et3ResponseHelper.validateEmploymentDates(caseData).size(), is(0));
    }

    @Test
    void givenStartDateAndNoEndDate_shouldNotValidateDates() {
        caseData.setEt3ResponseEmploymentStartDate("2022-02-02");

        assertThat(Et3ResponseHelper.validateEmploymentDates(caseData).size(), is(0));
    }

    @Test
    void givenNoStartDateAndNoEndDate_shouldNotValidateDates() {
        assertThat(Et3ResponseHelper.validateEmploymentDates(caseData).size(), is(0));
    }

    @Test
    void givenInvalidStartDateAndEndDate_shouldReturnAnError() {
        caseData.setEt3ResponseEmploymentStartDate("2022-02-03");
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(END_DATE_MUST_BE_AFTER_THE_START_DATE));
    }

    @Test
    void givenStartDateInTheFuture_shouldReturnAnError() {
        caseData.setEt3ResponseEmploymentStartDate("2099-02-02");

        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(START_DATE_MUST_BE_IN_THE_PAST));
    }

    @Test
    void givenStartDateInTheFutureAndEndDateBeforeStartDate_shouldReturnErrors() {
        caseData.setEt3ResponseEmploymentStartDate("2099-02-02");
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        assertThat(errors.size(), is(2));
        assertThat(errors.get(0), is(START_DATE_MUST_BE_IN_THE_PAST));
        assertThat(errors.get(1), is(END_DATE_MUST_BE_AFTER_THE_START_DATE));
    }

    @Test
    void something() throws URISyntaxException, IOException {
        CaseData caseData = new CaseData();
        Address address = new Address();
        address.setAddressLine1("111 Road");
        address.setPostCode("DA8 2DE");

        caseData.setEt3ResponseClaimantName("et3ResponseClaimantName");
        caseData.setEt3ResponseIsClaimantNameCorrect("et3ResponseIsClaimantNameCorrect");
        caseData.setEt3ResponseClaimantNameCorrection("et3ResponseClaimantNameCorrection");
        caseData.setEt3ResponseNameShowInset("et3ResponseNameShowInset");
        caseData.setEt3ResponseRespondentLegalName("et3ResponseRespondentLegalName");
        caseData.setEt3ResponseRespondentCompanyNumber("et3ResponseRespondentCompanyNumber");
        caseData.setEt3ResponseRespondentEmployerType("et3ResponseRespondentEmployerType");
        caseData.setEt3ResponseRespondentPreferredTitle("et3ResponseRespondentPreferredTitle");
        caseData.setEt3ResponseRespondentContactName("et3ResponseRespondentContactName");
        caseData.setEt3RespondentAddress(address);
        caseData.setEt3ResponseDXAddress("et3ResponseDXAddress");
        caseData.setEt3ResponsePhone("et3ResponsePhone");
        caseData.setEt3ResponseContactPreference("et3ResponseContactPreference");
        caseData.setEt3ResponseContactReason("et3ResponseContactReason");
        caseData.setEt3ResponseHearingRepresentative(List.of("Phone hearing"));
        caseData.setEt3ResponseHearingRespondent(List.of("Video hearing"));
        caseData.setEt3ResponseEmploymentCount("et3ResponseEmploymentCount");
        caseData.setEt3ResponseMultipleSites("et3ResponseMultipleSites");
        caseData.setEt3ResponseSiteEmploymentCount("et3ResponseSiteEmploymentCount");
        caseData.setEt3ResponseAcasAgree("et3ResponseAcasAgree");
        caseData.setEt3ResponseAcasAgreeReason("et3ResponseAcasAgreeReason");
        caseData.setEt3ResponseAreDatesCorrect("et3ResponseAreDatesCorrect");
        caseData.setEt3ResponseEmploymentStartDate("et3ResponseEmploymentStartDate");
        caseData.setEt3ResponseEmploymentEndDate("et3ResponseEmploymentEndDate");
        caseData.setEt3ResponseEmploymentInformation("et3ResponseEmploymentInformation");
        caseData.setEt3ResponseContinuingEmployment("et3ResponseContinuingEmployment");
        caseData.setEt3ResponseIsJobTitleCorrect("et3ResponseIsJobTitleCorrect");
        caseData.setEt3ResponseCorrectJobTitle("et3ResponseCorrectJobTitle");
        caseData.setEt3ResponseClaimantWeeklyHours("et3ResponseClaimantWeeklyHours");
        caseData.setEt3ResponseClaimantCorrectHours("et3ResponseClaimantCorrectHours");
        caseData.setEt3ResponseEarningDetailsCorrect("et3ResponseEarningDetailsCorrect");
        caseData.setEt3ResponsePayFrequency("et3ResponsePayFrequency");
        caseData.setEt3ResponsePayBeforeTax("et3ResponsePayBeforeTax");
        caseData.setEt3ResponsePayTakehome("et3ResponsePayTakehome");
        caseData.setEt3ResponseIsNoticeCorrect("et3ResponseIsNoticeCorrect");
        caseData.setEt3ResponseCorrectNoticeDetails("et3ResponseCorrectNoticeDetails");
        caseData.setEt3ResponseIsPensionCorrect("et3ResponseIsPensionCorrect");
        caseData.setEt3ResponsePensionCorrectDetails("et3ResponsePensionCorrectDetails");
        caseData.setEt3ResponseRespondentContestClaim("et3ResponseRespondentContestClaim");
        caseData.setEt3ResponseContestClaimDetails("et3ResponseContestClaimDetails");
        caseData.setEt3ResponseEmployerClaim("et3ResponseEmployerClaim");
        caseData.setEt3ResponseEmployerClaimDetails("et3ResponseEmployerClaimDetails");
        caseData.setEt3ResponseRespondentSupportNeeded("et3ResponseRespondentSupportNeeded");
        caseData.setEt3ResponseRespondentSupportDetails("et3ResponseRespondentSupportDetails");

        var actual = Et3ResponseHelper.buildEt3FormDocument(caseData, "", "et3ResponseForm");

        String expected = getExpectedResult("exampleEt3ResponseForm.txt");

        assertThat(actual.toString(), is(expected));
    }

    private String getExpectedResult(String resourceFileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
            .getResource(resourceFileName)).toURI())));
    }
}
