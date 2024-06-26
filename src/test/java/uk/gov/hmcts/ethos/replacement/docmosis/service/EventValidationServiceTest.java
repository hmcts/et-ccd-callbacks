package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingRequest;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelperTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_CLOSED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSING_HEARD_CASE_WITH_NO_JUDGE_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSING_LISTED_CASE_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.DUPLICATED_JURISDICTION_CODES_JUDGEMENT_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.DUPLICATE_JURISDICTION_CODE_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EARLY_DATE_RETURNED_FROM_JUDGE_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPTY_HEARING_COLLECTION_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPTY_RESPONDENT_COLLECTION_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FUTURE_RECEIPT_DATE_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FUTURE_RESPONSE_RECEIVED_DATE_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_NUMBER_MISMATCH_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INVALID_LISTING_DATE_RANGE_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_CODES_DELETED_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_CODES_EXISTENCE_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_DISMISSED_ON_WITHDRAWAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_NOT_ALLOCATED_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MISSING_JUDGEMENT_JURISDICTION_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MISSING_JURISDICTION_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MISSING_JURISDICTION_OUTCOME_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RECEIPT_DATE_LATER_THAN_ACCEPTED_ERROR_MESSAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TARGET_HEARING_DATE_INCREMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseCloseValidator.CLOSING_CASE_WITH_BF_OPEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService.DISPOSAL_DATE_BEFORE_RECEIPT_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService.DISPOSAL_DATE_HEARING_DATE_MATCH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService.OPEN_REFERRAL_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService.RECEIPT_DATE_LATER_THAN_REJECTED_ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class EventValidationServiceTest {

    private static final LocalDate PAST_RECEIPT_DATE = LocalDate.now().minusDays(1);
    private static final LocalDate CURRENT_RECEIPT_DATE = LocalDate.now();
    private static final LocalDate FUTURE_RECEIPT_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate PAST_ACCEPTED_DATE = LocalDate.now().minusDays(1);

    private static final LocalDate PAST_TARGET_HEARING_DATE = PAST_RECEIPT_DATE.plusDays(TARGET_HEARING_DATE_INCREMENT);
    private static final LocalDate CURRENT_TARGET_HEARING_DATE =
            CURRENT_RECEIPT_DATE.plusDays(TARGET_HEARING_DATE_INCREMENT);

    private static final LocalDate PAST_RESPONSE_RECEIVED_DATE = LocalDate.now().minusDays(1);
    private static final LocalDate CURRENT_RESPONSE_RECEIVED_DATE = LocalDate.now();
    private static final LocalDate FUTURE_RESPONSE_RECEIVED_DATE = LocalDate.now().plusDays(1);
    private static final String DISPOSAL_DATE = "2022-05-01";
    private static final String DISPOSAL_DATE_NO_MATCH = "2022-05-15";
    private static final String HEARING_DATE = "2022-05-01T10:10:00.000";
    private static final String HEARING_DATE2 = "2022-06-30T10:10:00.000";
    private EventValidationService eventValidationService;

    private CaseDetails caseDetails1;
    private CaseDetails caseDetails2;
    private CaseDetails caseDetails3;
    private CaseDetails caseDetails4;
    private CaseDetails caseDetails5;
    private CaseDetails caseDetails23;
    private CaseDetails validHearingStatusCaseCloseEventCaseDetails;
    private CaseDetails invalidHearingStatusCaseCloseEventCaseDetails;
    private CaseDetails validJudgeAllocationCaseDetails;
    private CaseDetails invalidJudgeAllocationCaseDetails;
    private CaseDetails outcomeNotAllocatedCaseDetails;
    private CaseDetails caseDetails16;
    private CaseDetails caseDetails17;
    private CaseDetails caseDetails18;
    private ListingRequest listingRequestValidDateRange;
    private ListingRequest listingRequestInvalidDateRange;
    private ListingRequest listingRequest31DaysInvalidRange;
    private ListingRequest listingRequest30DaysValidRange;

    private CaseData caseData;
    private MultipleData multipleData;

    @BeforeEach
    public void setup() throws Exception {
        eventValidationService = new EventValidationService();

        caseDetails1 = generateCaseDetails("caseDetailsTest1.json");
        caseDetails2 = generateCaseDetails("caseDetailsTest2.json");
        caseDetails3 = generateCaseDetails("caseDetailsTest3.json");
        caseDetails4 = generateCaseDetails("caseDetailsTest4.json");
        caseDetails5 = generateCaseDetails("caseDetailsTest5.json");
        caseDetails23 = generateCaseDetails("caseDetailsTest23.json");
        validHearingStatusCaseCloseEventCaseDetails = generateCaseDetails(
                "CaseCloseEvent_ValidHearingStatusCaseDetails.json");
        invalidHearingStatusCaseCloseEventCaseDetails = generateCaseDetails(
                "CaseCloseEvent_InValidHearingStatusCaseDetails.json");
        validJudgeAllocationCaseDetails = generateCaseDetails(
                "CaseCloseEvent_ValidJudgeAllocationStatusCaseDetails.json");
        invalidJudgeAllocationCaseDetails = generateCaseDetails(
                "CaseCloseEvent_InValidJudgeAllocationStatusCaseDetails.json");
        outcomeNotAllocatedCaseDetails = generateCaseDetails(
                "CaseCloseEvent_JurisdictionOutcomeNotAllocated.json");
        caseDetails16 = generateCaseDetails("caseDetailsTest16.json");
        caseDetails17 = generateCaseDetails("caseDetailsTest17.json");
        caseDetails18 = generateCaseDetails("caseDetailsTest18.json");

        listingRequestValidDateRange = generateListingDetails("exampleListingV1.json");
        listingRequestInvalidDateRange = generateListingDetails("exampleListingV3.json");
        listingRequest31DaysInvalidRange = generateListingDetails("exampleListingV5.json");
        listingRequest30DaysValidRange = generateListingDetails("exampleListingV4.json");

        caseData = new CaseData();
        multipleData = new MultipleData();
    }

    @Test
    void shouldValidatePastReceiptDate() {
        caseData.setReceiptDate(PAST_RECEIPT_DATE.toString());
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        List<String> errors = eventValidationService.validateReceiptDate(caseDetails);

        assertEquals(0, errors.size());
        assertEquals(caseData.getTargetHearingDate(), PAST_TARGET_HEARING_DATE.toString());
    }

    @ParameterizedTest
    @CsvSource({"2023-01-01", "2019-01-01"})
    void shouldValidateRejectedDate(String rejectedDate) {
        caseData.setReceiptDate("2022-01-01");
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setDateRejected(rejectedDate);
        caseData.setPreAcceptCase(casePreAcceptType);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setState(REJECTED_STATE);
        caseDetails.setCaseData(caseData);
        List<String> errors = eventValidationService.validateReceiptDate(caseDetails);
        if ("2023-01-01".equals(rejectedDate)) {
            assertEquals(0, errors.size());
        }
        if ("2019-01-01".equals(rejectedDate)) {
            assertEquals(RECEIPT_DATE_LATER_THAN_REJECTED_ERROR_MESSAGE, errors.get(0));
        }
    }

    @Test
    void shouldValidateCurrentReceiptDate() {
        caseData.setReceiptDate(CURRENT_RECEIPT_DATE.toString());
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        List<String> errors = eventValidationService.validateReceiptDate(caseDetails);

        assertEquals(0, errors.size());
        assertEquals(caseData.getTargetHearingDate(), CURRENT_TARGET_HEARING_DATE.toString());
    }

    @Test
    void shouldValidateFutureReceiptDate() {
        caseData.setReceiptDate(FUTURE_RECEIPT_DATE.toString());
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        List<String> errors = eventValidationService.validateReceiptDate(caseDetails);

        assertEquals(1, errors.size());
        assertEquals(FUTURE_RECEIPT_DATE_ERROR_MESSAGE, errors.get(0));
    }

    @ParameterizedTest
    @CsvSource({
        MULTIPLE_CASE_TYPE + "," + SUBMITTED_STATE,
        MULTIPLE_CASE_TYPE + "," + ACCEPTED_STATE,
        SINGLE_CASE_TYPE + "," + SUBMITTED_STATE,
        SINGLE_CASE_TYPE + "," + ACCEPTED_STATE
    })
    void shouldValidateCaseState(String caseType, String caseState) {
        caseDetails1.getCaseData().setEcmCaseType(caseType);
        caseDetails1.setState(caseState);

        boolean validated = eventValidationService.validateCaseState(caseDetails1);

        if (Objects.equals(caseType, MULTIPLE_CASE_TYPE) && Objects.equals(caseState, SUBMITTED_STATE)) {
            assertFalse(validated);
        } else {
            assertTrue(validated);
        }
    }

    @Test
    void shouldValidateReceiptDateLaterThanAcceptedDate() {
        caseData.setReceiptDate(CURRENT_RECEIPT_DATE.toString());
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setDateAccepted(PAST_ACCEPTED_DATE.toString());
        caseData.setPreAcceptCase(casePreAcceptType);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setState(ACCEPTED_STATE);
        caseDetails.setCaseData(caseData);
        List<String> errors = eventValidationService.validateReceiptDate(caseDetails);

        assertEquals(1, errors.size());
        assertEquals(RECEIPT_DATE_LATER_THAN_ACCEPTED_ERROR_MESSAGE, errors.get(0));
    }

    @Test
    void shouldValidatePastReceiptDateMultiple() {
        multipleData.setReceiptDate(PAST_RECEIPT_DATE.toString());

        List<String> errors = eventValidationService.validateReceiptDateMultiple(multipleData);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateFutureReceiptDateMultiple() {
        multipleData.setReceiptDate(FUTURE_RECEIPT_DATE.toString());

        List<String> errors = eventValidationService.validateReceiptDateMultiple(multipleData);

        assertEquals(1, errors.size());
        assertEquals(FUTURE_RECEIPT_DATE_ERROR_MESSAGE, errors.get(0));
    }

    @Test
    void shouldValidateActiveRespondentsAllFound() {
        List<String> errors = eventValidationService.validateActiveRespondents(caseDetails1.getCaseData());

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateActiveRespondentsNoneFound() {
        List<String> errors = eventValidationService.validateActiveRespondents(caseDetails2.getCaseData());

        assertEquals(1, errors.size());
        assertEquals(EMPTY_RESPONDENT_COLLECTION_ERROR_MESSAGE, errors.get(0));
    }

    @Test
    void shouldValidateReturnedFromJudgeDateBeforeReferredToJudgeDate() {
        List<String> errors = eventValidationService.validateET3ResponseFields(caseDetails1.getCaseData());

        assertEquals(1, errors.size());
        assertEquals(EARLY_DATE_RETURNED_FROM_JUDGE_ERROR_MESSAGE
                + " for respondent 1 (Antonio Vazquez)", errors.get(0));
    }

    @Test
    void shouldValidateReturnedFromJudgeDateAndReferredToJudgeDateAreMissingDate() {
        List<String> errors = eventValidationService.validateET3ResponseFields(caseDetails3.getCaseData());

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateResponseReceivedDateIsFutureDate() {
        CaseData caseData = caseDetails1.getCaseData();

        caseData.getRespondentCollection().get(0).getValue()
                .setResponseReceivedDate(PAST_RESPONSE_RECEIVED_DATE.toString());
        caseData.getRespondentCollection().get(1).getValue()
                .setResponseReceivedDate(CURRENT_RESPONSE_RECEIVED_DATE.toString());
        caseData.getRespondentCollection().get(2).getValue()
                .setResponseReceivedDate(FUTURE_RESPONSE_RECEIVED_DATE.toString());

        List<String> errors = eventValidationService.validateET3ResponseFields(caseData);

        assertEquals(2, errors.size());
        assertEquals(FUTURE_RESPONSE_RECEIVED_DATE_ERROR_MESSAGE
                + " for respondent 3 (Mike Jordan)", errors.get(1));
    }

    @Test
    void shouldValidateResponseReceivedDateForMissingDate() {
        CaseData caseData = caseDetails3.getCaseData();

        List<String> errors = eventValidationService.validateET3ResponseFields(caseData);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateRespRepNamesWithEmptyRepCollection() {
        CaseData caseData = caseDetails1.getCaseData();

        List<String> errors = eventValidationService.validateRespRepNames(caseData);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateRespRepNamesWithMismatch() {
        CaseData caseData = caseDetails2.getCaseData();

        List<String> errors = eventValidationService.validateRespRepNames(caseData);

        assertEquals(1, errors.size());
    }

    @Test
    void shouldValidateRespRepNamesWithMatch() {
        CaseData caseData = caseDetails3.getCaseData();

        List<String> errors = eventValidationService.validateRespRepNames(caseData);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateRespRepNamesWithNullRepCollection() {
        CaseData caseData = caseDetails4.getCaseData();

        List<String> errors = eventValidationService.validateRespRepNames(caseData);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateRespRepNamesWithMatchResponseName() {
        CaseData caseData = caseDetails5.getCaseData();

        List<String> errors = eventValidationService.validateRespRepNames(caseData);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateHearingNumberMatching() {
        List<String> errors = eventValidationService.validateHearingNumber(caseDetails1.getCaseData(),
                caseDetails1.getCaseData().getCorrespondenceType(),
                caseDetails1.getCaseData().getCorrespondenceScotType());

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateHearingNumberMismatch() {
        List<String> errors = eventValidationService.validateHearingNumber(caseDetails2.getCaseData(),
                caseDetails2.getCaseData().getCorrespondenceType(),
                caseDetails2.getCaseData().getCorrespondenceScotType());

        assertEquals(1, errors.size());
        assertEquals(HEARING_NUMBER_MISMATCH_ERROR_MESSAGE, errors.get(0));
    }

    @Test
    void shouldValidateHearingNumberMissing() {
        List<String> errors = eventValidationService.validateHearingNumber(caseDetails3.getCaseData(),
                caseDetails3.getCaseData().getCorrespondenceType(),
                caseDetails3.getCaseData().getCorrespondenceScotType());

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateHearingNumberForEmptyHearings() {
        List<String> errors = eventValidationService.validateHearingNumber(caseDetails4.getCaseData(),
                caseDetails4.getCaseData().getCorrespondenceType(),
                caseDetails4.getCaseData().getCorrespondenceScotType());

        assertEquals(1, errors.size());
        assertEquals(EMPTY_HEARING_COLLECTION_ERROR_MESSAGE, errors.get(0));
    }

    @Test
    void shouldValidateJurisdictionCodesWithDuplicatesCodesAndExistenceJudgement() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(caseDetails1.getCaseData(), errors);

        assertEquals(2, errors.size());
        assertEquals(DUPLICATE_JURISDICTION_CODE_ERROR_MESSAGE + " \"COM\" in Jurisdiction 3 "
                + "- \"DOD\" in Jurisdiction 5 ", errors.get(0));
        assertEquals(JURISDICTION_CODES_DELETED_ERROR + "[CCP, ADG]", errors.get(1));
    }

    @Test
    void shouldValidateJurisdictionCodesForDisposedHearingWithSameDisposalDateOfJurisdiction() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(caseDetails23.getCaseData(), errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldHaveValidationErrorForJurisdictionCodesOfDisposedHearingWithDifferentDisposalDateOfJurisdiction() {
        List<String> errors = new ArrayList<>();
        caseDetails23.getCaseData().getJurCodesCollection().get(0).getValue().setDisposalDate("2024-02-26");
        eventValidationService.validateJurisdiction(caseDetails23.getCaseData(), errors);

        assertEquals(1, errors.size());
        assertEquals(String.format(DISPOSAL_DATE_HEARING_DATE_MATCH, "DDA"), errors.get(0));
    }

    @Test
    void shouldValidateJurisdictionCodesWithUniqueCodes() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(caseDetails2.getCaseData(), errors);

        assertEquals(0, errors.size());
    }

    @ParameterizedTest
    @CsvSource({SUBMITTED_STATE + ",false", ACCEPTED_STATE + ",false", REJECTED_STATE
            + ",false", CLOSED_STATE + ",true"})
    void validateCurrentPositionCaseClosed(String state, boolean expected) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setState(state);
        CaseData caseData = new CaseData();
        caseData.setPositionType(CASE_CLOSED_POSITION);
        caseDetails.setCaseData(caseData);
        boolean validated = eventValidationService.validateCurrentPosition(caseDetails);
        assertEquals(expected, validated);
    }

    @Test
    void shouldValidateJurisdictionCodesWithEmptyCodes() {
        List<String> errors = new ArrayList<>();
        caseDetails3.getCaseData().setJudgementCollection(new ArrayList<>());
        eventValidationService.validateJurisdiction(caseDetails3.getCaseData(), errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateDisposalDateInFuture() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(setCaseDataForDisposalDateTest(
                "2777-05-15", YES, JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING), errors);
        assertThat(errors.get(0))
                .isEqualTo(String.format(EventValidationService.DISPOSAL_DATE_IN_FUTURE, "blah blah"));

    }

    private HearingTypeItem setHearing(String hearingDate, String disposed) {
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId(UUID.randomUUID().toString());
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(hearingDate);
        dateListedType.setHearingCaseDisposed(disposed);
        dateListedTypeItem.setId(UUID.randomUUID().toString());
        dateListedTypeItem.setValue(dateListedType);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(Collections.singletonList(dateListedTypeItem));
        hearingTypeItem.setValue(hearingType);
        return hearingTypeItem;
    }

    private CaseData setCaseDataForDisposalDateTest(String disposalDate, String disposed, String outcome) {
        CaseData caseData = new CaseData();
        HearingTypeItem hearingTypeItem1 = setHearing(HEARING_DATE2, disposed);
        HearingTypeItem hearingTypeItem2 = setHearing(HEARING_DATE, disposed);
        caseData.setReceiptDate("2019-01-01");
        caseData.setHearingCollection(Arrays.asList(hearingTypeItem1, hearingTypeItem2));
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        jurCodesTypeItem.setId(UUID.randomUUID().toString());
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("blah blah");
        jurCodesType.setDisposalDate(disposalDate);
        jurCodesType.setJudgmentOutcome(outcome);
        jurCodesTypeItem.setValue(jurCodesType);
        caseData.setJurCodesCollection(Collections.singletonList(jurCodesTypeItem));
        return caseData;
    }

    @Test
    void disposalDateNonHearingOutcome() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(setCaseDataForDisposalDateTest(DISPOSAL_DATE_NO_MATCH, YES,
                JURISDICTION_OUTCOME_DISMISSED_ON_WITHDRAWAL),
            errors);
        assertThat(errors).isEmpty();
    }

    @Test
    void disposalDateNoMatchWithHearingDate() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(setCaseDataForDisposalDateTest(DISPOSAL_DATE_NO_MATCH, YES,
                JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING),
            errors);
        assertThat(errors.get(0))
            .isEqualTo(String.format(DISPOSAL_DATE_HEARING_DATE_MATCH, "blah blah"));
    }

    @Test
    void disposalDateMatchWithHearingDate() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(setCaseDataForDisposalDateTest(DISPOSAL_DATE, YES,
                JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING),
            errors);
        assertThat(errors).isEmpty();
    }

    @Test
    void disposalDateAfterReceiptDate() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(setCaseDataForDisposalDateTest(DISPOSAL_DATE, YES,
                        JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING),
                errors);
        assertThat(errors).isEmpty();

    }

    @Test
    void disposalDateBeforeReceiptDate() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(setCaseDataForDisposalDateTest("2018-02-02", YES,
                        JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING),
                errors);
        assertThat(errors.get(0))
                .isEqualTo(String.format(DISPOSAL_DATE_BEFORE_RECEIPT_DATE, "blah blah"));
    }

    @Test
    void disposalDateMatchWithHearingDateNoDisposal() {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdiction(setCaseDataForDisposalDateTest(DISPOSAL_DATE, NO,
                JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING),
            errors);
        assertThat(errors.get(0))
            .isEqualTo(String.format(DISPOSAL_DATE_HEARING_DATE_MATCH, "blah blah"));
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void shouldValidateJurisdictionOutcomePresentAndMissing(boolean isRejected, boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdictionOutcome(caseDetails1.getCaseData(), isRejected,
                partOfMultiple, errors);

        assertEquals(1, errors.size());
        if (partOfMultiple) {
            assertEquals(caseDetails1.getCaseData().getEthosCaseReference() + " - "
                    + MISSING_JURISDICTION_OUTCOME_ERROR_MESSAGE, errors.get(0));
        } else {
            assertEquals(MISSING_JURISDICTION_OUTCOME_ERROR_MESSAGE, errors.get(0));
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void shouldValidateWhenJurisdictionOutcomeSetToNotAllocated(boolean isRejected, boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdictionOutcome(outcomeNotAllocatedCaseDetails.getCaseData(),
                isRejected, partOfMultiple, errors);

        assertEquals(1, errors.size());
        if (partOfMultiple) {
            assertEquals(outcomeNotAllocatedCaseDetails.getCaseData().getEthosCaseReference() + " - "
                    + JURISDICTION_OUTCOME_NOT_ALLOCATED_ERROR_MESSAGE, errors.get(0));
        } else {
            assertEquals(JURISDICTION_OUTCOME_NOT_ALLOCATED_ERROR_MESSAGE, errors.get(0));
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void shouldValidateJurisdictionOutcomePresent(boolean isRejected, boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdictionOutcome(caseDetails2.getCaseData(),
                isRejected, partOfMultiple, errors);

        assertEquals(0, errors.size());
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void shouldValidateJurisdictionOutcomeMissing(boolean isRejected, boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJurisdictionOutcome(caseDetails3.getCaseData(),
                isRejected, partOfMultiple, errors);

        if (isRejected) {
            assertEquals(0, errors.size());
        } else {
            assertEquals(1, errors.size());

            if (partOfMultiple) {
                assertEquals(caseDetails1.getCaseData().getEthosCaseReference() + " - "
                        + MISSING_JURISDICTION_MESSAGE, errors.get(0));
            } else {
                assertEquals(MISSING_JURISDICTION_MESSAGE, errors.get(0));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldValidateJurisdictionCodeForJudgementPresentAndMissing(boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJudgementsHasJurisdiction(caseDetails18.getCaseData(), partOfMultiple, errors);

        assertEquals(1, errors.size());
        if (partOfMultiple) {
            assertEquals(caseDetails18.getCaseData().getEthosCaseReference() + " - "
                    + MISSING_JUDGEMENT_JURISDICTION_MESSAGE, errors.get(0));
        } else {
            assertEquals(MISSING_JUDGEMENT_JURISDICTION_MESSAGE, errors.get(0));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldValidateJurisdictionCodeForJudgementPresent(boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJudgementsHasJurisdiction(caseDetails17.getCaseData(), partOfMultiple, errors);

        assertEquals(0, errors.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldValidateJurisdictionCodeForJudgementMissing(boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateJudgementsHasJurisdiction(caseDetails16.getCaseData(), partOfMultiple, errors);

        assertEquals(1, errors.size());
        if (partOfMultiple) {
            assertEquals(caseDetails16.getCaseData().getEthosCaseReference() + " - "
                    + MISSING_JUDGEMENT_JURISDICTION_MESSAGE, errors.get(0));
        } else {
            assertEquals(MISSING_JUDGEMENT_JURISDICTION_MESSAGE, errors.get(0));
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void shouldValidateCaseBeforeCloseEventWithErrors(boolean isRejected, boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        caseDetails18.getCaseData().setBfActions(BFHelperTest.generateBFActionTypeItems());
        caseDetails18.getCaseData().getBfActions().get(0).getValue().setCleared(null);
        eventValidationService.validateCaseBeforeCloseEvent(caseDetails18.getCaseData(),
                isRejected, partOfMultiple, errors);

        assertEquals(4, errors.size());
        assertThat(errors).contains(String.format(CLOSING_CASE_WITH_BF_OPEN_ERROR,
                caseDetails18.getCaseData().getEthosCaseReference()));
        if (partOfMultiple) {
            assertThat(errors).contains(caseDetails18.getCaseData().getEthosCaseReference()
                    + " - " + MISSING_JUDGEMENT_JURISDICTION_MESSAGE);
            assertThat(errors).doesNotContain(caseDetails18.getCaseData().getEthosCaseReference()
                    + " - " + CLOSING_HEARD_CASE_WITH_NO_JUDGE_ERROR);
        } else {
            assertThat(errors).contains(MISSING_JURISDICTION_OUTCOME_ERROR_MESSAGE);
            assertThat(errors).doesNotContain(CLOSING_HEARD_CASE_WITH_NO_JUDGE_ERROR);
            assertThat(errors).contains(CLOSING_LISTED_CASE_ERROR);
        }
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void shouldValidateCaseBeforeCloseEventNoErrors(boolean isRejected, boolean partOfMultiple) {
        List<String> errors = new ArrayList<>();
        eventValidationService.validateCaseBeforeCloseEvent(caseDetails17.getCaseData(),
                isRejected, partOfMultiple, errors);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateJurisdictionCodesWithinJudgement() {
        List<String> errors = eventValidationService.validateJurisdictionCodesWithinJudgement(
                caseDetails1.getCaseData());

        assertEquals(2, errors.size());
        assertEquals(JURISDICTION_CODES_EXISTENCE_ERROR + "ADG, ADG, ADG, CCP, CCP", errors.get(0));
        assertEquals(DUPLICATED_JURISDICTION_CODES_JUDGEMENT_ERROR + "Case Management - [COM] & Reserved "
                + "- [CCP, ADG]", errors.get(1));
    }

    @Test
    void shouldCreateErrorMessageWithDatesInFutureWithinJudgement() {
        JudgementTypeItem judgementTypeItem = new JudgementTypeItem();
        JudgementType judgementType = new JudgementType();
        judgementTypeItem.setId(UUID.randomUUID().toString());
        judgementType.setDateJudgmentMade("2777-01-01");
        judgementType.setDateJudgmentSent("2777-01-01");
        judgementTypeItem.setValue(judgementType);

        CaseData caseData = new CaseData();
        caseData.setJudgementCollection(List.of(judgementTypeItem));
        List<String> errors = eventValidationService.validateJudgementDates(caseData);
        assertEquals("Date of Judgement Made can't be in future", errors.get(0));
        assertEquals("Date of Judgement Sent can't be in future", errors.get(1));
    }

    @Test
    void shouldNotCreateErrorMessageWithDatesBeforeTodayWithinJudgement() {
        JudgementTypeItem judgementTypeItem = new JudgementTypeItem();
        JudgementType judgementType = new JudgementType();
        judgementTypeItem.setId(UUID.randomUUID().toString());
        judgementType.setDateJudgmentMade("2020-01-01");
        judgementType.setDateJudgmentSent("2021-12-01");
        judgementTypeItem.setValue(judgementType);

        CaseData caseData = new CaseData();
        caseData.setJudgementCollection(List.of(judgementTypeItem));
        List<String> errors = eventValidationService.validateJudgementDates(caseData);
        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateJurisdictionCodesWithinJudgementEmptyJurCodesCollection() {
        List<String> errors = eventValidationService.validateJurisdictionCodesWithinJudgement(
                caseDetails3.getCaseData());

        assertEquals(1, errors.size());
        assertEquals(JURISDICTION_CODES_EXISTENCE_ERROR + "ADG, COM", errors.get(0));
    }

    @Test
    void shouldValidateReportDateRangeValidDates() {

        ListingData listingsCase = listingRequestValidDateRange.getCaseDetails().getCaseData();
        List<String> errors = eventValidationService.validateListingDateRange(
                listingsCase.getListingDateFrom(),
                listingsCase.getListingDateTo()
        );
        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateReportDateRangeValidDates_30Days() {

        ListingData listingsCase = listingRequest30DaysValidRange.getCaseDetails().getCaseData();
        List<String> errors = eventValidationService.validateListingDateRange(
                listingsCase.getListingDateFrom(),
                listingsCase.getListingDateTo()
        );
        assertEquals(0, errors.size());
    }

    @Test
    void shouldValidateReportDateRangeInvalidDates() {

        ListingData listingsCase = listingRequestInvalidDateRange.getCaseDetails().getCaseData();
        List<String> errors = eventValidationService.validateListingDateRange(
                listingsCase.getListingDateFrom(),
                listingsCase.getListingDateTo()
        );
        assertEquals(1, errors.size());
        assertEquals(INVALID_LISTING_DATE_RANGE_ERROR_MESSAGE, errors.get(0));
    }

    @Test
    void shouldValidateReportDateRangeInvalidDates_31Days() {

        ListingData listingsCase = listingRequest31DaysInvalidRange.getCaseDetails().getCaseData();
        List<String> errors = eventValidationService.validateListingDateRange(
                listingsCase.getListingDateFrom(),
                listingsCase.getListingDateTo()
        );
        assertEquals(1, errors.size());
        assertEquals(INVALID_LISTING_DATE_RANGE_ERROR_MESSAGE, errors.get(0));
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    private ListingRequest generateListingDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ListingRequest.class);
    }

    @Test
    void validateRestrictedBy() {
        eventValidationService.validateRestrictedReportingNames(caseDetails2.getCaseData());
        assertEquals("Claimant", caseDetails2.getCaseData().getRestrictedReporting().getRequestedBy());
        eventValidationService.validateRestrictedReportingNames(caseDetails1.getCaseData());
        assertEquals("Judge", caseDetails1.getCaseData().getRestrictedReporting().getRequestedBy());
        eventValidationService.validateRestrictedReportingNames(caseDetails3.getCaseData());
        assertEquals("Respondent", caseDetails3.getCaseData().getRestrictedReporting().getRequestedBy());
    }

    @Test
    void shouldReturnsNoErrorsForHearingHearingStatusValidationWithNoHearings() {
        List<String> errors = new ArrayList<>();
        CaseData caseWithNoHearings = validHearingStatusCaseCloseEventCaseDetails.getCaseData();
        caseWithNoHearings.getHearingCollection().clear();
        eventValidationService.validateHearingStatusForCaseCloseEvent(caseWithNoHearings, errors);
        assertEquals(0, errors.size());
    }

    @Test
    void shouldPassCaseCloseEventValidationCaseWithNoListedHearingStatus() {
        List<String> errors = new ArrayList<>();
        CaseData validCase = validHearingStatusCaseCloseEventCaseDetails.getCaseData();
        eventValidationService.validateHearingStatusForCaseCloseEvent(validCase, errors);
        assertEquals(0, errors.size());
    }

    @Test
    void shouldFailCaseCloseEventValidationCaseWithListedHearingStatus() {
        List<String> errors = new ArrayList<>();
        CaseData invalidCase = invalidHearingStatusCaseCloseEventCaseDetails.getCaseData();
        eventValidationService.validateHearingStatusForCaseCloseEvent(invalidCase, errors);
        assertEquals(1, errors.size());
        assertEquals(CLOSING_LISTED_CASE_ERROR, errors.get(0));
    }

    @Test
    void shouldPassHearingJudgeAllocationValidationForCaseCloseEventHearingWithJudge() {
        List<String> errors = new ArrayList<>();
        CaseData validCase = validJudgeAllocationCaseDetails.getCaseData();
        eventValidationService.validateHearingJudgeAllocationForCaseCloseEvent(validCase, errors);
        assertEquals(0, errors.size());
    }

    @Test
    void shouldFailHearingJudgeAllocationValidationForCaseCloseEventHearingWithNoJudge() {
        List<String> errors = new ArrayList<>();
        CaseData invalidCase = invalidJudgeAllocationCaseDetails.getCaseData();
        eventValidationService.validateHearingJudgeAllocationForCaseCloseEvent(invalidCase, errors);
        assertThat(errors)
                .hasSize(1);
        assertThat(errors.get(0))
                .isEqualTo(CLOSING_HEARD_CASE_WITH_NO_JUDGE_ERROR);
    }

    @Test
    void shouldReturnsNoErrorsForHearingJudgeAllocationValidationWithNoHearings() {
        List<String> errors = new ArrayList<>();
        CaseData caseWithNoHearings = invalidJudgeAllocationCaseDetails.getCaseData();
        caseWithNoHearings.getHearingCollection().clear();
        eventValidationService.validateHearingJudgeAllocationForCaseCloseEvent(caseWithNoHearings, errors);
        assertThat(errors)
                .isEmpty();
    }

    @Test
    void shouldReturnErrorWithOpenReferral() {
        ReferralType referralType = new ReferralType();
        referralType.setReferralStatus("Awaiting Instructions");
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setValue(referralType);
        caseData.setReferralCollection(List.of(referralTypeItem));
        List<String> errors = new ArrayList<>();

        eventValidationService.validateCaseBeforeCloseEvent(caseData, false, false, errors);

        assertTrue(errors.contains(OPEN_REFERRAL_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnErrorWithOpenAndClosedReferrals() {
        ReferralType referralType = new ReferralType();
        referralType.setReferralStatus("Awaiting Instructions");
        ReferralTypeItem openReferral = new ReferralTypeItem();
        openReferral.setValue(referralType);

        ReferralType referralType2 = new ReferralType();
        referralType2.setReferralStatus("Closed");
        ReferralTypeItem closedReferral = new ReferralTypeItem();
        closedReferral.setValue(referralType2);

        caseData.setReferralCollection(List.of(openReferral, closedReferral));
        List<String> errors = new ArrayList<>();

        eventValidationService.validateCaseBeforeCloseEvent(caseData, false, false, errors);
        assertTrue(errors.contains(OPEN_REFERRAL_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnNoErrorsWithClosedReferrals() {
        caseData = caseDetails17.getCaseData();
        ReferralType referralType = new ReferralType();
        referralType.setReferralStatus("Closed");
        ReferralTypeItem closedReferral = new ReferralTypeItem();
        closedReferral.setValue(referralType);
        caseData.setReferralCollection(List.of(closedReferral));
        List<String> errors = new ArrayList<>();
        eventValidationService.validateCaseBeforeCloseEvent(caseData, false, false, errors);
        assertTrue(errors.isEmpty());
    }
}
