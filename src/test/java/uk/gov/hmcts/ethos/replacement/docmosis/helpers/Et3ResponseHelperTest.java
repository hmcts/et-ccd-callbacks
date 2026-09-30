package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ET3_RESPONSE_STATUS_ACCEPTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ET3_RESPONSE_STATUS_NOT_ACCEPTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ET3_RESPONSE_STATUS_NOT_RECEIVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ET3_RESPONSE_STATUS_REJECTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ALL_RESPONDENTS_INCOMPLETE_SECTIONS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.CONTEST_CLAIM_REASON_REQUIRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.EMPLOYER_CLAIM_DETAILS_REQUIRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ET3_RESPONSE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ET3_RESPONSE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ET3_RESPONSE_EMPLOYMENT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.NO_RESPONDENTS_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.RESPONDENT_POSTCODE_REQUIRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.addEt3DataToRespondent;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.findRepresentativeFromCaseData;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.generateEventHyperlinks;

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
        assertThat(errors.getFirst(), is(END_DATE_MUST_BE_AFTER_THE_START_DATE));
    }

    @Test
    void givenStartDateInTheFuture_shouldReturnAnError() {
        caseData.setEt3ResponseEmploymentStartDate("2099-02-02");

        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        assertThat(errors.size(), is(1));
        assertThat(errors.getFirst(), is(START_DATE_MUST_BE_IN_THE_PAST));
    }

    @Test
    void givenStartDateInTheFutureAndEndDateBeforeStartDate_shouldReturnErrors() {
        caseData.setEt3ResponseEmploymentStartDate("2099-02-02");
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        assertThat(errors.size(), is(2));
        assertThat(errors.getFirst(), is(START_DATE_MUST_BE_IN_THE_PAST));
        assertThat(errors.get(1), is(END_DATE_MUST_BE_AFTER_THE_START_DATE));
    }

    @Test
    void createDynamicListSelection() {
        Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(caseData.getEt3RepresentingRespondent(), hasSize(1));
    }

    @Test
    void validateRespondents_noErrors() {
        List<String> errors = Et3ResponseHelper.validateRespondents(caseData, ET3_RESPONSE_DETAILS);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateRespondents_noOption() {
        caseData.setEt3RepresentingRespondent(new ArrayList<>());
        List<String> errors = Et3ResponseHelper.validateRespondents(caseData, ET3_RESPONSE_DETAILS);
        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst()).isEqualTo(NO_RESPONDENTS_FOUND);
    }

    @Test
    void addEt3DataToRespondent_allSections() {
        answerRespondentDetailsMandatoryQuestions();
        answerResponseDetailsMandatoryQuestions();
        caseData.setEt3ResponsePhone("1234");
        caseData.setEt3ResponseAcasAgree(YES);
        caseData.setEt3ResponseEmploymentCount("10");
        caseData.setEt3ResponseContactPreference("Post");
        caseData.setEt3ResponseReference("REF1234");
        addEt3DataToRespondent(caseData, ET3_RESPONSE);
        addEt3DataToRespondent(caseData, ET3_RESPONSE_EMPLOYMENT_DETAILS);
        addEt3DataToRespondent(caseData, ET3_RESPONSE_DETAILS);
        RespondentSumType respondentSumType = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondentSumType.getEt3ResponseIsClaimantNameCorrect()).isEqualTo(YES);
        RepresentedTypeR representative = findRepresentativeFromCaseData(caseData);
        assumeTrue(ObjectUtils.isNotEmpty(representative));
        assertThat(representative.getRepresentativePhoneNumber()).isEqualTo("1234");
        assertThat(representative.getRepresentativePreference()).isEqualTo("Post");
        assertThat(representative.getRepresentativeReference()).isEqualTo("REF1234");
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
        assertThat(errors.getFirst()).isEqualTo(NO_RESPONDENTS_FOUND);
    }

    @Test
    void createDynamicListSelection_responseContinueNo_returnsNoRespondentsRequireEt3() {
        caseData.getRespondentCollection().getFirst().getValue().setResponseContinue(NO);
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst()).isEqualTo("There are no respondents that require an ET3");
    }

    @Test
    void createDynamicListSelection_responseContinueYes_allowsSubmissionChoice() {
        caseData.getRespondentCollection().getFirst().getValue().setResponseContinue(YES);
        caseData.getRespondentCollection().getFirst().getValue().setResponseReceived(NO);
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors).isEmpty();
        assertThat(caseData.getEt3RepresentingRespondent(), hasSize(1));
    }

    @Test
    void createEt3SubmitRespondents_allSectionsCompleted() {
        caseData.getRespondentCollection().getFirst().getValue().setPersonalDetailsSection(YES);
        caseData.getRespondentCollection().getFirst().getValue().setClaimDetailsSection(YES);
        caseData.getRespondentCollection().getFirst().getValue().setEmploymentDetailsSection(YES);
        List<String> errors = Et3ResponseHelper.et3SubmitRespondents(caseData);
        assertThat(errors).isEmpty();
        assertThat(caseData.getSubmitEt3Respondent()).isNotNull();
    }

    @Test
    void et3SubmitRespondents_allSectionsCompleted_butResponseContinueNo_returnsError() {
        caseData.getRespondentCollection().getFirst().getValue().setPersonalDetailsSection(YES);
        caseData.getRespondentCollection().getFirst().getValue().setClaimDetailsSection(YES);
        caseData.getRespondentCollection().getFirst().getValue().setEmploymentDetailsSection(YES);
        caseData.getRespondentCollection().getFirst().getValue().setResponseContinue(NO);
        List<String> errors = Et3ResponseHelper.et3SubmitRespondents(caseData);
        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst()).isEqualTo(ALL_RESPONDENTS_INCOMPLETE_SECTIONS);
    }

    @Test
    void createEt3SubmitRespondents_twoSectionsCompleted() {
        caseData.getRespondentCollection().getFirst().getValue().setPersonalDetailsSection(YES);
        caseData.getRespondentCollection().getFirst().getValue().setClaimDetailsSection(YES);
        caseData.getRespondentCollection().getFirst().getValue().setEmploymentDetailsSection(NO);
        List<String> errors = Et3ResponseHelper.et3SubmitRespondents(caseData);
        assertThat(errors, hasSize(1));
        assertThat(errors.getFirst()).isEqualTo(ALL_RESPONDENTS_INCOMPLETE_SECTIONS);
    }

    @Test
    void shouldAddPersonalDetailsToRespondentForEt3Response() {
        answerRespondentDetailsMandatoryQuestions();
        addEt3DataToRespondent(caseData, ET3_RESPONSE);
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondent.getEt3ResponseIsClaimantNameCorrect()).isEqualTo("Yes");
        assertThat(respondent.getPersonalDetailsSection()).isEqualTo("Yes");
    }

    @Test
    void shouldAddClaimDetailsToRespondentForEt3ResponseDetails() {
        answerResponseDetailsMandatoryQuestions();
        caseData.setEt3ResponseAcasAgree("Yes");
        addEt3DataToRespondent(caseData, ET3_RESPONSE_DETAILS);
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondent.getEt3ResponseAcasAgree()).isEqualTo("Yes");
        assertThat(respondent.getClaimDetailsSection()).isEqualTo("Yes");
    }

    @Test
    void shouldAddEmploymentDetailsToRespondentForEt3ResponseEmploymentDetails() {
        caseData.setEt3ResponseEmploymentCount("5");
        addEt3DataToRespondent(caseData, ET3_RESPONSE_EMPLOYMENT_DETAILS);
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondent.getEt3ResponseEmploymentCount()).isEqualTo("5");
        assertThat(respondent.getEmploymentDetailsSection()).isEqualTo("Yes");
    }

    @ParameterizedTest
    @MethodSource
    void respondentDetailsSection_incompleteWhenMandatoryQuestionUnanswered(String isClaimantNameCorrect,
                                                                           String respondentName,
                                                                           String postCode) {
        caseData.setEt3ResponseIsClaimantNameCorrect(isClaimantNameCorrect);
        caseData.setEt3ResponseRespondentLegalName(respondentName);
        caseData.setEt3RespondentAddress(postCode == null ? null : createAddress(postCode));
        addEt3DataToRespondent(caseData, ET3_RESPONSE);
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondent.getPersonalDetailsSection()).isEqualTo(NO);
    }

    private static Stream<Arguments> respondentDetailsSection_incompleteWhenMandatoryQuestionUnanswered() {
        return Stream.of(
                Arguments.of(null, "Respondent Ltd", "AB1 2CD"),
                Arguments.of(YES, null, "AB1 2CD"),
                Arguments.of(YES, " ", "AB1 2CD"),
                Arguments.of(YES, "Respondent Ltd", null),
                Arguments.of(YES, "Respondent Ltd", "")
        );
    }

    @Test
    void responseDetailsSection_completeWithDocumentsInsteadOfDetails() {
        caseData.setEt3ResponseRespondentContestClaim(YES);
        caseData.setEt3ResponseContestClaimDocument(List.of(DocumentTypeItem.builder().build()));
        caseData.setEt3ResponseEmployerClaim(YES);
        caseData.setEt3ResponseEmployerClaimDocument(UploadedDocumentType.builder().documentFilename("a.pdf").build());
        addEt3DataToRespondent(caseData, ET3_RESPONSE_DETAILS);
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondent.getClaimDetailsSection()).isEqualTo(YES);
    }

    @Test
    void responseDetailsSection_completeWhenNotContestingAndNoEmployerClaim() {
        caseData.setEt3ResponseRespondentContestClaim(NO);
        caseData.setEt3ResponseEmployerClaim(NO);
        addEt3DataToRespondent(caseData, ET3_RESPONSE_DETAILS);
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondent.getClaimDetailsSection()).isEqualTo(YES);
    }

    @ParameterizedTest
    @MethodSource
    void responseDetailsSection_incompleteWhenMandatoryQuestionUnanswered(String contestClaim,
                                                                         String contestClaimDetails,
                                                                         String employerClaim,
                                                                         String employerClaimDetails) {
        caseData.setEt3ResponseRespondentContestClaim(contestClaim);
        caseData.setEt3ResponseContestClaimDetails(contestClaimDetails);
        caseData.setEt3ResponseEmployerClaim(employerClaim);
        caseData.setEt3ResponseEmployerClaimDetails(employerClaimDetails);
        addEt3DataToRespondent(caseData, ET3_RESPONSE_DETAILS);
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondent.getClaimDetailsSection()).isEqualTo(NO);
    }

    private static Stream<Arguments> responseDetailsSection_incompleteWhenMandatoryQuestionUnanswered() {
        return Stream.of(
                Arguments.of(null, null, NO, null),
                Arguments.of(YES, null, NO, null),
                Arguments.of(YES, " ", NO, null),
                Arguments.of(NO, null, null, null),
                Arguments.of(NO, null, YES, null),
                Arguments.of(NO, null, YES, "")
        );
    }

    @Test
    void responseDetailsSection_resubmittedIncomplete_marksSectionIncomplete() {
        answerResponseDetailsMandatoryQuestions();
        addEt3DataToRespondent(caseData, ET3_RESPONSE_DETAILS);
        caseData.setEt3ResponseContestClaimDetails(null);
        addEt3DataToRespondent(caseData, ET3_RESPONSE_DETAILS);
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        assertThat(respondent.getClaimDetailsSection()).isEqualTo(NO);
    }

    @ParameterizedTest
    @MethodSource
    void validateRespondentAddress(String postCode, List<String> expectedErrors) {
        caseData.setEt3RespondentAddress(postCode == null ? null : createAddress(postCode));
        assertThat(Et3ResponseHelper.validateRespondentAddress(caseData)).isEqualTo(expectedErrors);
    }

    private static Stream<Arguments> validateRespondentAddress() {
        return Stream.of(
                Arguments.of("AB1 2CD", List.of()),
                Arguments.of(null, List.of(RESPONDENT_POSTCODE_REQUIRED)),
                Arguments.of(" ", List.of(RESPONDENT_POSTCODE_REQUIRED))
        );
    }

    @ParameterizedTest
    @MethodSource
    void validateContestClaimReason(String contestClaim, String details, List<DocumentTypeItem> documents,
                                    List<String> expectedErrors) {
        caseData.setEt3ResponseRespondentContestClaim(contestClaim);
        caseData.setEt3ResponseContestClaimDetails(details);
        caseData.setEt3ResponseContestClaimDocument(documents);
        assertThat(Et3ResponseHelper.validateContestClaimReason(caseData)).isEqualTo(expectedErrors);
    }

    private static Stream<Arguments> validateContestClaimReason() {
        return Stream.of(
                Arguments.of(YES, "Reasons", null, List.of()),
                Arguments.of(YES, null, List.of(DocumentTypeItem.builder().build()), List.of()),
                Arguments.of(NO, null, null, List.of()),
                Arguments.of(YES, null, null, List.of(CONTEST_CLAIM_REASON_REQUIRED)),
                Arguments.of(YES, " ", List.of(), List.of(CONTEST_CLAIM_REASON_REQUIRED))
        );
    }

    @ParameterizedTest
    @MethodSource
    void validateEmployerClaimDetails(String employerClaim, String details, UploadedDocumentType document,
                                      List<String> expectedErrors) {
        caseData.setEt3ResponseEmployerClaim(employerClaim);
        caseData.setEt3ResponseEmployerClaimDetails(details);
        caseData.setEt3ResponseEmployerClaimDocument(document);
        assertThat(Et3ResponseHelper.validateEmployerClaimDetails(caseData)).isEqualTo(expectedErrors);
    }

    private static Stream<Arguments> validateEmployerClaimDetails() {
        return Stream.of(
                Arguments.of(YES, "Details", null, List.of()),
                Arguments.of(YES, null, UploadedDocumentType.builder().documentFilename("a.pdf").build(), List.of()),
                Arguments.of(NO, null, null, List.of()),
                Arguments.of(YES, null, null, List.of(EMPLOYER_CLAIM_DETAILS_REQUIRED)),
                Arguments.of(YES, "", null, List.of(EMPLOYER_CLAIM_DETAILS_REQUIRED))
        );
    }

    private void answerRespondentDetailsMandatoryQuestions() {
        caseData.setEt3ResponseIsClaimantNameCorrect(YES);
        caseData.setEt3ResponseRespondentLegalName("Respondent Ltd");
        caseData.setEt3RespondentAddress(createAddress("AB1 2CD"));
    }

    private void answerResponseDetailsMandatoryQuestions() {
        caseData.setEt3ResponseRespondentContestClaim(YES);
        caseData.setEt3ResponseContestClaimDetails("Reasons for contesting the claim");
        caseData.setEt3ResponseEmployerClaim(YES);
        caseData.setEt3ResponseEmployerClaimDetails("Details of the employer's contract claim");
    }

    private static Address createAddress(String postCode) {
        Address address = new Address();
        address.setAddressLine1("1 Street");
        address.setPostCode(postCode);
        return address;
    }

    @Test
    void shouldThrowExceptionForInvalidEventId() {
        String invalidEventId = "invalid";
        assertThrows(IllegalArgumentException.class, () ->
                addEt3DataToRespondent(caseData, invalidEventId)
        );
    }

    @ParameterizedTest
    @MethodSource("createDynamicListSelectionExtension")
    void createDynamicListSelection_extensionRequested(String responseReceived, String extensionRequested,
                                                       String extensionGranted, String extensionDate,
                                                       String extensionResubmitted, int count, int errorsSize) {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().getFirst().getValue();
        respondentSumType.setResponseReceived(responseReceived);
        respondentSumType.setExtensionRequested(extensionRequested);
        respondentSumType.setExtensionGranted(extensionGranted);
        respondentSumType.setExtensionDate(extensionDate);
        respondentSumType.setExtensionResubmitted(extensionResubmitted);
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);
        assertThat(errors, hasSize(errorsSize));
        assertThat(caseData.getEt3RepresentingRespondent().getFirst().getValue().getDynamicList().getListItems(),
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

    @ParameterizedTest
    @MethodSource("createDynamicListSelectionWithResponseStatus")
    void createDynamicListSelection_responseReceivedYes_statusBased(String responseStatus, boolean shouldAllowSubmit) {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().getFirst().getValue();
        respondentSumType.setResponseContinue(YES);
        respondentSumType.setResponseReceived(YES);
        respondentSumType.setResponseStatus(responseStatus);

        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);

        if (shouldAllowSubmit) {
            assertThat(errors).isEmpty();
            assertThat(caseData.getEt3RepresentingRespondent().getFirst().getValue().getDynamicList().getListItems(),
                    hasSize(1));
        } else {
            assertThat(errors, hasSize(1));
            assertThat(errors.getFirst()).isEqualTo("There are no respondents that require an ET3");
        }
    }

    private static Stream<Arguments> createDynamicListSelectionWithResponseStatus() {
        return Stream.of(
            Arguments.of(null, false),
            Arguments.of("", false),
            Arguments.of(ET3_RESPONSE_STATUS_ACCEPTED, false),
            Arguments.of(ET3_RESPONSE_STATUS_NOT_ACCEPTED, true),
            Arguments.of(ET3_RESPONSE_STATUS_NOT_RECEIVED, true),
            Arguments.of(ET3_RESPONSE_STATUS_REJECTED, true)
        );
    }

    @Test
    void et3SubmitRespondents_allSectionsCompleted_butResponseReceivedAccepted_returnsError() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().getFirst().getValue();
        respondentSumType.setPersonalDetailsSection(YES);
        respondentSumType.setClaimDetailsSection(YES);
        respondentSumType.setEmploymentDetailsSection(YES);
        respondentSumType.setResponseReceived(YES);
        respondentSumType.setResponseStatus(ET3_RESPONSE_STATUS_ACCEPTED);

        List<String> errors = Et3ResponseHelper.et3SubmitRespondents(caseData);

        assertThat(errors, hasSize(1));
    }

    @Test
    void setEt3NotificationAcceptedDates() {
        RespondentSumTypeItem respondentSumTypeItemResponseNotAccepted = caseData.getRespondentCollection().getFirst();
        respondentSumTypeItemResponseNotAccepted.getValue().setResponseStatus("Not Accepted");
        Et3ResponseHelper.setEt3NotificationAcceptedDates(caseData);
        assertThat(respondentSumTypeItemResponseNotAccepted.getValue().getEt3NotificationAcceptedDate()).isNull();

        RespondentSumTypeItem respondentSumTypeItemResponseNotificationDocCollectionEmpty =
                caseData.getRespondentCollection().getFirst();
        respondentSumTypeItemResponseNotificationDocCollectionEmpty
                .getValue().setResponseStatus(ACCEPTED_STATE);
        Et3ResponseHelper.setEt3NotificationAcceptedDates(caseData);
        assertThat(respondentSumTypeItemResponseNotificationDocCollectionEmpty
                .getValue().getEt3NotificationAcceptedDate()).isNull();

        RespondentSumTypeItem respondentSumTypeItemResponseNotificationDocCollectionOfNotAccepted =
                caseData.getRespondentCollection().getFirst();
        respondentSumTypeItemResponseNotificationDocCollectionOfNotAccepted
                .getValue().setResponseStatus(ACCEPTED_STATE);
        caseData.setEt3NotificationDocCollection(List.of(DocumentTypeItem.builder().value(
                DocumentType.builder().typeOfDocument("2.12").build()).build()));
        Et3ResponseHelper.setEt3NotificationAcceptedDates(caseData);
        assertThat(respondentSumTypeItemResponseNotificationDocCollectionOfNotAccepted
                .getValue().getEt3NotificationAcceptedDate()).isNull();

        RespondentSumTypeItem respondentSumTypeItemResponseNotificationDocCollectionOfAccepted =
                caseData.getRespondentCollection().getFirst();
        respondentSumTypeItemResponseNotificationDocCollectionOfAccepted
                .getValue().setResponseStatus(ACCEPTED_STATE);
        caseData.setEt3NotificationDocCollection(List.of(DocumentTypeItem.builder().value(
                DocumentType.builder().typeOfDocument("2.11").build()).build()));
        Et3ResponseHelper.setEt3NotificationAcceptedDates(caseData);
        assertThat(respondentSumTypeItemResponseNotificationDocCollectionOfAccepted
                .getValue().getEt3NotificationAcceptedDate()).contains(LocalDate.now().toString());

        RespondentSumTypeItem respondentSumTypeItemValueNull = caseData.getRespondentCollection().getFirst();
        respondentSumTypeItemValueNull.setValue(null);
        assertDoesNotThrow(() -> Et3ResponseHelper.setEt3NotificationAcceptedDates(caseData));
    }

    @Test
    void generateEventHyperlinks_noRespondents_throws() {
        caseData.setRespondentCollection(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generateEventHyperlinks(caseData, "1234123412341234"));
        assertThat(exception.getMessage()).isEqualTo(NO_RESPONDENTS_FOUND);
    }

    @Test
    void generateEventHyperlinks_noEligibleRespondents_excludesSubmitButton() {
        String ccdId = "1234123412341234";
        String expected = ET3ResponseConstants.SECTION_COMPLETE_BODY.formatted(ccdId, ccdId, ccdId, ccdId, "");

        String actual = generateEventHyperlinks(caseData, ccdId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generateEventHyperlinks_eligibleRespondent_includesSubmitButton() {
        String ccdId = "1234123412341234";
        RespondentSumType respondent = caseData.getRespondentCollection().getFirst().getValue();
        respondent.setPersonalDetailsSection(YES);
        respondent.setEmploymentDetailsSection(YES);
        respondent.setClaimDetailsSection(YES);
        respondent.setResponseContinue(YES);
        respondent.setResponseReceived(NO);

        String expected = ET3ResponseConstants.SECTION_COMPLETE_BODY.formatted(
                ccdId,
                ccdId,
                ccdId,
                ccdId,
                ET3ResponseConstants.SUBMIT_ET3_BUTTON.formatted(ccdId)
        );

        String actual = generateEventHyperlinks(caseData, ccdId);

        assertThat(actual).isEqualTo(expected);
    }
}
