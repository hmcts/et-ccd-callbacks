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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@SuppressWarnings({"PMD.UseProperClassLoader", "PMD.LinguisticNaming"})
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
    void getDocumentRequest_buildsCorrectData() throws IOException, URISyntaxException {
        Address et3Address = CaseDataBuilder.builder().createAddress("line1", "line2", "line3", "town", "county",
            "postcode", "country");

        caseData.setEt3RespondentAddress(et3Address);
        caseData.setClaimant("claimant name");
        caseData.setEt3ResponseRespondentLegalName("legal name");
        caseData.setEt3ResponseRespondentCompanyNumber("1234");
        caseData.setEt3ResponseRespondentEmployerType("employer type");
        caseData.setEt3ResponseRespondentContactName("contact name");
        caseData.setEt3ResponseEmploymentCount("100");
        caseData.setEt3ResponseSiteEmploymentCount("100");
        caseData.setEt3ResponseAcasAgreeReason("acas agree reason");
        caseData.setEt3ResponseEmploymentStartDate("2022-01-01");
        caseData.setEt3ResponseEmploymentEndDate("2022-01-02");
        caseData.setEt3ResponseEmploymentInformation("employment information");
        caseData.setEt3ResponseCorrectJobTitle("fall guy");
        caseData.setEt3ResponseClaimantCorrectHours("168");
        caseData.setEt3ResponsePayBeforeTax("69000");
        caseData.setEt3ResponsePayTakehome("25000");
        caseData.setEt3ResponseCorrectNoticeDetails("notice details");
        caseData.setEt3ResponseContestClaimDetails("contest claim");
        caseData.setEt3ResponseEmployerClaimDetails("ecc");
        caseData.setEt3ResponseRespondentSupportDetails("support details");
        caseData.setEt3ResponsePensionCorrectDetails("pension details");
        caseData.setEt3ResponseHearingRespondent(Arrays.asList("Phone hearings"));
        caseData.setEt3ResponseHearingRepresentative(Arrays.asList("Video hearings"));
        caseData.setEt3ResponseRespondentPreferredTitle("Mr");
        caseData.setEt3ResponseMultipleSites("Yes");
        caseData.setEt3ResponseAcasAgree("No");
        caseData.setEt3ResponseAreDatesCorrect("No");
        caseData.setEt3ResponseIsJobTitleCorrect("No");
        caseData.setEt3ResponseClaimantWeeklyHours("No");
        caseData.setEt3ResponseEarningDetailsCorrect("No");
        caseData.setEt3ResponseIsNoticeCorrect("Not applicable");
        caseData.setEt3ResponseRespondentContestClaim("Yes");
        caseData.setEt3ResponseRespondentSupportNeeded("Yes");
        caseData.setEt3ResponseContinuingEmployment("No");
        caseData.setEt3ResponseIsPensionCorrect("No");
        caseData.setEt3ResponseContactPreference("Post");
        caseData.setEt3ResponsePayFrequency("Weekly");

        String expected = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
            .getResource("et3ResponseDocument.json")).toURI())));

        String actual = Et3ResponseHelper.getDocumentRequest(caseData, "any");
        assertThat(actual).isEqualTo(expected);
    }
}
