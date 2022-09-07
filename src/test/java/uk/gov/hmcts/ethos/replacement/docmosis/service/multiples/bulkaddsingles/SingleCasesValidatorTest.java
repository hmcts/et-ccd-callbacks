package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;

@SuppressWarnings({"PMD.LawOfDemeter", "PMD.TooManyMethods"})
class SingleCasesValidatorTest {
    private static final String AUTH_TOKEN = "some-token";
    private List<SubmitEvent> submitEvents;
    private List<String> caseIds;
    private SingleCasesValidator singleCasesValidator;
    private MultipleDetails multipleDetails;

    @BeforeEach
    void setup() throws IOException {
        var ccdClient = mock(CcdClient.class);
        caseIds = new ArrayList<>();
        submitEvents = new ArrayList<>();
        when(ccdClient.retrieveCasesElasticSearchForCreation(AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, caseIds,
                MANUALLY_CREATED_POSITION)).thenReturn(submitEvents);
        when(ccdClient.retrieveCasesElasticSearchForCreation(AUTH_TOKEN, SCOTLAND_CASE_TYPE_ID, caseIds,
                MANUALLY_CREATED_POSITION)).thenReturn(submitEvents);
        singleCasesValidator = new SingleCasesValidator(ccdClient);
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);

        var multipleData = new MultipleData();
        multipleData.setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        multipleDetails.setCaseData(multipleData);
    }

    @Test
     void shouldSetSubmittedCaseAsInvalid() throws IOException {
        var ethosReference = "case1";
        caseIds.add(ethosReference);
        submitEvents.add(createSubmitEvent(ethosReference, SINGLE_CASE_TYPE, SUBMITTED_STATE, null));

        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        assertEquals(1, validatedCases.size());
        assertFalse(validatedCases.get(0).isValid());
        assertEquals(ethosReference, validatedCases.get(0).getEthosReference());
        assertEquals("Case is in state " + SUBMITTED_STATE, validatedCases.get(0).getInvalidReason());
    }

    @Test
    void shouldSetCaseInOtherMultipleAsValid() throws IOException {
        var ethosReference = "case1";
        var otherMultipleReference = "other-multiple";
        caseIds.add(ethosReference);
        submitEvents.add(createSubmitEvent(ethosReference, MULTIPLE_CASE_TYPE, ACCEPTED_STATE, otherMultipleReference));

        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        verify(validatedCases, ethosReference, true, null);
    }

    @Test
    void shouldSetCaseAlreadyInMultipleAsValid() throws IOException {
        var ethosReference = "case1";
        var multipleReference = "multiple1";
        caseIds.add(ethosReference);
        submitEvents.add(createSubmitEvent(ethosReference, MULTIPLE_CASE_TYPE, ACCEPTED_STATE, multipleReference));

        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        verify(validatedCases, ethosReference, true, null);
    }

    @Test
    void shouldSetUnknownCaseAsInvalid() throws IOException {
        var ethosReference = "case1";
        caseIds.add(ethosReference);

        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        verify(validatedCases, ethosReference, false, "Case not found");
    }

    @Test
    void shouldSetSingleAcceptedCaseAsValid() throws IOException {
        var ethosReference = "case1";
        caseIds.add(ethosReference);
        submitEvents.add(createSubmitEvent(ethosReference, SINGLE_CASE_TYPE, ACCEPTED_STATE, null));

        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        verify(validatedCases, ethosReference, true, null);
    }

    /**
     * This unit test is to check the scenario where a single case still has a multiple reference assigned.
     * (which is actually a bug)
     * @throws IOException an exception
     */
    @Test
    void shouldSetSingleAcceptedCaseWithMultipleReferenceAsValid() throws IOException {
        var ethosReference = "case1";
        var otherMultipleReference = "multiple2";
        caseIds.add(ethosReference);
        submitEvents.add(createSubmitEvent(ethosReference, SINGLE_CASE_TYPE, ACCEPTED_STATE, otherMultipleReference));

        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        verify(validatedCases, ethosReference, true, null);
    }

    @Test
    void shouldHandleAllCases() throws IOException {
        caseIds.addAll(List.of("case1", "case2", "case3", "case4"));
        submitEvents.add(createSubmitEvent("case1", SINGLE_CASE_TYPE, SUBMITTED_STATE, null));
        submitEvents.add(createSubmitEvent("case2", SINGLE_CASE_TYPE, ACCEPTED_STATE, null));
        submitEvents.add(createSubmitEvent("case3", SINGLE_CASE_TYPE, ACCEPTED_STATE, null));

        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        assertEquals(4, validatedCases.size());
        verify(validatedCases.get(0), "case1", false, "Case is in state " + SUBMITTED_STATE);
        verify(validatedCases.get(1), "case2", true, null);
        verify(validatedCases.get(2), "case3", true, null);
        verify(validatedCases.get(3), "case4", false, "Case not found");
    }

    @Test
    void shouldReturnErrorForDifferentOffice() throws IOException {
        caseIds.addAll(List.of("case1", "case2"));
        submitEvents.add(createSubmitEvent("case1", SINGLE_CASE_TYPE, ACCEPTED_STATE, null));
        submitEvents.add(createSubmitEvent("case2", SINGLE_CASE_TYPE, ACCEPTED_STATE, null));
        submitEvents.get(0).getCaseData().setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        submitEvents.get(1).getCaseData().setManagingOffice(TribunalOffice.WALES.getOfficeName());
        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        assertEquals(2, validatedCases.size());
        verify(validatedCases.get(0), "case1", false, "Case is managed by " + TribunalOffice.LEEDS.getOfficeName());
        verify(validatedCases.get(1), "case2", false, "Case is managed by " + TribunalOffice.WALES.getOfficeName());
    }

    @Test
    void shouldNotReturnErrorForDifferentOfficeForScotland() throws IOException {
        multipleDetails.setCaseTypeId(SCOTLAND_BULK_CASE_TYPE_ID);
        multipleDetails.getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        caseIds.addAll(List.of("case1", "case2"));
        submitEvents.add(createSubmitEvent("case1", SINGLE_CASE_TYPE, ACCEPTED_STATE, null));
        submitEvents.add(createSubmitEvent("case2", SINGLE_CASE_TYPE, ACCEPTED_STATE, null));
        submitEvents.get(0).getCaseData().setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        submitEvents.get(1).getCaseData().setManagingOffice(TribunalOffice.DUNDEE.getOfficeName());
        var validatedCases = singleCasesValidator.getValidatedCases(caseIds, multipleDetails, AUTH_TOKEN);
        assertEquals(2, validatedCases.size());
        verify(validatedCases.get(0), "case1", true, null);
        verify(validatedCases.get(1), "case2", true, null);
    }

    private SubmitEvent createSubmitEvent(String ethosReference, String caseType, String state,
                                          String multipleReference) {
        var submitEvent = new SubmitEvent();
        submitEvent.setState(state);
        var caseData = new CaseData();
        caseData.setEthosCaseReference(ethosReference);
        caseData.setEcmCaseType(caseType);
        caseData.setMultipleReference(multipleReference);
        caseData.setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        submitEvent.setCaseData(caseData);

        return submitEvent;
    }

    private void verify(List<ValidatedSingleCase> validatedCases, String expectedEthosReference,
                        boolean expectedValid, String expectedInvalidReason) {
        assertEquals(1, validatedCases.size());
        verify(validatedCases.get(0), expectedEthosReference, expectedValid, expectedInvalidReason);
    }

    private void verify(ValidatedSingleCase validatedCase, String expectedEthosReference,
                        boolean expectedValid, String expectedInvalidReason) {
        assertEquals(expectedValid, validatedCase.isValid());
        assertEquals(expectedEthosReference, validatedCase.getEthosReference());
        assertEquals(expectedInvalidReason, validatedCase.getInvalidReason());
    }

}
