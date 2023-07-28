package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ALL_RESPONDENTS_INCOMPLETE_SECTIONS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ET3_RESPONSE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ET3_RESPONSE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ET3_RESPONSE_EMPLOYMENT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.NO_RESPONDENTS_FOUND;

class Et3ResponseHelperTest {

    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withRespondent("test", NO, null, false)
                .withEt3RepresentingRespondent("test")
                .withSubmitEt3Respondent("test")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseData = caseDetails.getCaseData();

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
        caseData.setEthosCaseReference("1800001/2021");
        caseData.setClaimant("claimant name");
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        addEt3RespondentData(respondentSumType);

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
        caseData.setSubmitEt3Respondent(DynamicFixedListType.of(DynamicValueType.create("test", "test")));

        // UTF-8 is required here for special characters to resolve on Windows correctly
        String expected = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource("et3ResponseDocument.json")).toURI())), UTF_8);

        String actual = Et3ResponseHelper.getDocumentRequest(caseData, "any");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getDocumentRequest_buildsCorrectData_withoutRespondentRepAddress() throws IOException, URISyntaxException {
        caseData.getRepCollection().get(0).getValue().setRepresentativeAddress(null);
        caseData.setEthosCaseReference("1800001/2021");
        caseData.setClaimant("claimant name");
        addEt3RespondentData(caseData.getRespondentCollection().get(0).getValue());
        caseData.setSubmitEt3Respondent(DynamicFixedListType.of(DynamicValueType.create("test", "test")));

        // UTF-8 is required here for special characters to resolve on Windows correctly
        String expected = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource("et3ResponseDocument.json")).toURI())), UTF_8);

        JSONObject json = new JSONObject(expected);
        JSONObject data = (JSONObject) json.get("data");

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
        List<String> errors = Et3ResponseHelper.validateRespondents(caseData, ET3_RESPONSE_DETAILS);
        assertThat(errors.isEmpty());
    }

    @Test
    void validateRespondents_noOption() {
        caseData.setEt3RepresentingRespondent(new ArrayList<>());
        List<String> errors = Et3ResponseHelper.validateRespondents(caseData, ET3_RESPONSE_DETAILS);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0)).isEqualTo(NO_RESPONDENTS_FOUND);
    }

    @Test
    void addEt3DataToRespondent_allSections() {
        caseData.setEt3ResponseIsClaimantNameCorrect(YES);
        caseData.setEt3ResponsePhone("1234");
        caseData.setEt3ResponseAcasAgree(YES);
        caseData.setEt3ResponseEmploymentCount("10");
        Et3ResponseHelper.addEt3DataToRespondent(caseData, ET3_RESPONSE);
        Et3ResponseHelper.addEt3DataToRespondent(caseData, ET3_RESPONSE_EMPLOYMENT_DETAILS);
        Et3ResponseHelper.addEt3DataToRespondent(caseData, ET3_RESPONSE_DETAILS);
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        assertThat(respondentSumType.getEt3ResponseIsClaimantNameCorrect()).isEqualTo(YES);
        assertThat(respondentSumType.getResponseRespondentPhone1()).isEqualTo("1234");
        assertThat(respondentSumType.getEt3ResponseAcasAgree()).isEqualTo(YES);
        assertThat(respondentSumType.getEt3ResponseEmploymentCount()).isEqualTo("10");
        assertThat(respondentSumType.getPersonalDetailsSection()).isEqualTo(YES);
        assertThat(respondentSumType.getClaimDetailsSection()).isEqualTo(YES);
        assertThat(respondentSumType.getEmploymentDetailsSection()).isEqualTo(YES);

    }

    @Test
    void createDynamicListSelection_noRespondents() {
        caseData.setRespondentCollection(null);
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0)).isEqualTo(NO_RESPONDENTS_FOUND);
    }

    @Test
    void createEt3SubmitRespondents_allSectionsCompleted() {
        caseData.getRespondentCollection().get(0).getValue().setPersonalDetailsSection(YES);
        caseData.getRespondentCollection().get(0).getValue().setClaimDetailsSection(YES);
        caseData.getRespondentCollection().get(0).getValue().setEmploymentDetailsSection(YES);
        List<String> errors = Et3ResponseHelper.et3SubmitRespondents(caseData);
        assertThat(errors.isEmpty());
        assertThat(caseData.getSubmitEt3Respondent()).isNotNull();
    }

    @Test
    void createEt3SubmitRespondents_twoSectionsCompleted() {
        caseData.getRespondentCollection().get(0).getValue().setPersonalDetailsSection(YES);
        caseData.getRespondentCollection().get(0).getValue().setClaimDetailsSection(YES);
        caseData.getRespondentCollection().get(0).getValue().setEmploymentDetailsSection(NO);
        List<String> errors = Et3ResponseHelper.et3SubmitRespondents(caseData);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0)).isEqualTo(ALL_RESPONDENTS_INCOMPLETE_SECTIONS);
    }

    private static void addEt3RespondentData(RespondentSumType respondentSumType) {
        Address et3Address = CaseDataBuilder.builder().createAddress("line1", "line2", "line3", "town", "county",
                "postcode", "country");

        respondentSumType.setResponseRespondentAddress(et3Address);
        respondentSumType.setResponseRespondentName("legal name");
        respondentSumType.setEt3ResponseRespondentCompanyNumber("1234");
        respondentSumType.setEt3ResponseRespondentEmployerType("employer type");
        respondentSumType.setEt3ResponseRespondentContactName("contact name");
        respondentSumType.setEt3ResponseEmploymentCount("100");
        respondentSumType.setEt3ResponseSiteEmploymentCount("100");
        respondentSumType.setEt3ResponseAcasAgreeReason("acas agree reason");
        respondentSumType.setEt3ResponseEmploymentStartDate("2022-01-01");
        respondentSumType.setEt3ResponseEmploymentEndDate("2022-01-02");
        respondentSumType.setEt3ResponseEmploymentInformation("employment information");
        respondentSumType.setEt3ResponseCorrectJobTitle("fall guy");
        respondentSumType.setEt3ResponseClaimantCorrectHours("168");
        respondentSumType.setEt3ResponsePayBeforeTax("69000");
        respondentSumType.setEt3ResponsePayTakehome("25000");
        respondentSumType.setEt3ResponseCorrectNoticeDetails("notice details");
        respondentSumType.setEt3ResponseContestClaimDetails("contest claim");
        respondentSumType.setEt3ResponseEmployerClaimDetails("ecc");
        respondentSumType.setEt3ResponseRespondentSupportDetails("support details");
        respondentSumType.setEt3ResponsePensionCorrectDetails("pension details");
        respondentSumType.setEt3ResponseHearingRespondent(List.of("Phone hearings"));
        respondentSumType.setEt3ResponseHearingRepresentative(List.of("Video hearings"));
        respondentSumType.setEt3ResponseRespondentPreferredTitle("Mr");
        respondentSumType.setEt3ResponseMultipleSites("Yes");
        respondentSumType.setEt3ResponseAcasAgree("No");
        respondentSumType.setEt3ResponseAreDatesCorrect("No");
        respondentSumType.setEt3ResponseIsJobTitleCorrect("No");
        respondentSumType.setEt3ResponseClaimantWeeklyHours("No");
        respondentSumType.setEt3ResponseEarningDetailsCorrect("No");
        respondentSumType.setEt3ResponseIsNoticeCorrect("Not applicable");
        respondentSumType.setEt3ResponseRespondentContestClaim("Yes");
        respondentSumType.setEt3ResponseRespondentSupportNeeded("Yes");
        respondentSumType.setEt3ResponseContinuingEmployment("No");
        respondentSumType.setEt3ResponseIsPensionCorrect("No");
        respondentSumType.setResponseRespondentContactPreference("Post");
        respondentSumType.setEt3ResponsePayFrequency("Weekly");
    }

    @ParameterizedTest
    @MethodSource("createDynamicListSelectionExtension")
    void createDynamicListSelection_extensionRequested(String responseReceived, String extensionRequested,
                                                       String extensionGranted, String extensionDate,
                                                       String extensionResubmitted, int count, int errorsSize) {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setResponseReceived(responseReceived);
        respondentSumType.setExtensionRequested(extensionRequested);
        respondentSumType.setExtensionGranted(extensionGranted);
        respondentSumType.setExtensionDate(extensionDate);
        respondentSumType.setExtensionResubmitted(extensionResubmitted);
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors, hasSize(errorsSize));
        assertThat(caseData.getEt3RepresentingRespondent().get(0).getValue().getDynamicList().getListItems(),
            hasSize(count));
    }

    private static Stream<Arguments> createDynamicListSelectionExtension() {
        return Stream.of(
            Arguments.of(NO, null, null, null, null, 1, 0),
            Arguments.of(YES, YES, YES, "2000-12-31", null, 1, 1),
            Arguments.of(YES, YES, YES, "2999-12-31", null, 1, 0),
            Arguments.of(YES, YES, YES, "2999-12-31", YES, 1, 1)
        );
    }

}
