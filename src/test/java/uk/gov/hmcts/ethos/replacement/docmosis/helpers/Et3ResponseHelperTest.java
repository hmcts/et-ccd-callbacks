package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@SuppressWarnings({"PMD.UseProperClassLoader", "PMD.LinguisticNaming", "PMD.TooManyMethods"})
class Et3ResponseHelperTest {

    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withRespondent("test", NO, null, false)
                .withEt3RepresentingRespondent("test")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseData = caseDetails.getCaseData();

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
        caseData.setEt3ResponseHearingRespondent(List.of("Phone hearings"));
        caseData.setEt3ResponseHearingRepresentative(List.of("Video hearings"));
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

        Address repAddress = CaseDataBuilder.builder().createAddress(
                "r1", "r2", "r3", "rTown", "rCounty",
                "rPostcode", "rCountry"
        );

        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId("id");
        representedTypeRItem.setValue(RepresentedTypeR.builder()
                .respRepName("test")
                .representativeAddress(repAddress)
                .representativePhoneNumber("phone")
                .build());
        caseData.setRepCollection(List.of(representedTypeRItem));
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
        caseData.setEt3ResponseEmploymentEndDate(null);

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
        caseData.setEt3ResponseEmploymentEndDate(null);

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
        // UTF-8 is required here for special characters to resolve on Windows correctly
        String expected = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource("et3ResponseDocument.json")).toURI())), UTF_8);

        String actual = Et3ResponseHelper.getDocumentRequest(caseData, "any");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getDocumentRequest_buildsCorrectData_withoutRespondentRepAddress() throws IOException, URISyntaxException {
        caseData.getRepCollection().get(0).getValue().setRepresentativeAddress(null);

        // UTF-8 is required here for special characters to resolve on Windows correctly
        String expected = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource("et3ResponseDocument.json")).toURI())), UTF_8);

        JSONObject json = new JSONObject(expected);
        JSONObject data = (JSONObject)json.get("data");

        data.remove("repAddressLine1");
        data.remove("repAddressLine2");
        data.remove("repTown");
        data.remove("repCounty");
        data.remove("repPostcode");

        String test = json.toString();
        String actual = Et3ResponseHelper.getDocumentRequest(caseData, "any");
        JSONAssert.assertEquals(test, actual, false);
    }

    @Test
    void createDynamicListSelection() {
        Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(caseData.getEt3RepresentingRespondent(), hasSize(1));
    }

    @Test
    void validateRespondents_noErrors() {
        List<String> errors = Et3ResponseHelper.validateRespondents(caseData);
        assertThat(errors.isEmpty());
    }

    @Test
    void validateRespondents_noOption() {
        caseData.setEt3RepresentingRespondent(new ArrayList<>());
        List<String> errors = Et3ResponseHelper.validateRespondents(caseData);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0)).isEqualTo("No respondents found");
    }

    @Test
    void addEt3DataToRespondent() {
        caseData.setEt3ResponseIsClaimantNameCorrect(YES);
        caseData.setEt3ResponsePhone("1234");
        Et3ResponseHelper.addEt3DataToRespondent(caseData);
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        assertThat(respondentSumType.getEt3ResponseIsClaimantNameCorrect()).isEqualTo(YES);
        assertThat(respondentSumType.getResponseReceived()).isEqualTo(YES);
        assertThat(respondentSumType.getResponseReceivedDate()).isEqualTo(LocalDate.now().toString());
    }

    @Test
    void createDynamicListSelection_noRespondents() {
        caseData.setRespondentCollection(null);
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0)).isEqualTo("No respondents found");
    }

    @Test
    void createDynamicListSelection_noExtensionGranted() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setResponseReceived(YES);
        respondentSumType.setExtensionRequested(NO);
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors, hasSize(0));
        assertThat(caseData.getEt3RepresentingRespondent().get(0).getValue().getDynamicList().getListItems(),
            hasSize(0));
    }

    @Test
    void createDynamicListSelection_extensionDateBefore() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setResponseReceived(YES);
        respondentSumType.setExtensionRequested(YES);
        respondentSumType.setExtensionGranted(YES);
        respondentSumType.setExtensionDate("2000-12-31");
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors, hasSize(0));
        assertThat(caseData.getEt3RepresentingRespondent().get(0).getValue().getDynamicList().getListItems(),
            hasSize(0));
    }

    @Test
    void createDynamicListSelection_extensionDateAfter() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setResponseReceived(YES);
        respondentSumType.setExtensionRequested(YES);
        respondentSumType.setExtensionGranted(YES);
        respondentSumType.setExtensionDate("2999-12-31");
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors, hasSize(0));
        assertThat(caseData.getEt3RepresentingRespondent().get(0).getValue().getDynamicList().getListItems(),
            hasSize(1));
    }
}
