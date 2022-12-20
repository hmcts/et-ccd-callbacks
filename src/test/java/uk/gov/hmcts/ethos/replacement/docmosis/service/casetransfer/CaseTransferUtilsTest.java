package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils.BF_ACTIONS_ERROR_MSG;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils.HEARINGS_ERROR_MSG;

@ExtendWith(SpringExtension.class)
class CaseTransferUtilsTest {

    @InjectMocks
    CaseTransferUtils caseTransferUtils;

    @Mock
    CcdClient ccdClient;

    private static final String USER_TOKEN = "my-test-token";

    @Test
    void testGetsClaimantCase() {
        String ethosCaseReference = "claimant-case-ref";
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        List<CaseData> cases = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN);
        assertEquals(1, cases.size());
        assertEquals(ethosCaseReference, cases.get(0).getEthosCaseReference());
    }

    @Test
    void testGetsEccCase() throws IOException {
        String ethosCaseReference = "claimant-case-ref";
        String eccCaseReference = "ecc-case";
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withEccCase(eccCaseReference)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        SubmitEvent submitEvent = CaseDataBuilder.builder()
                .withEthosCaseReference(eccCaseReference)
                .withCounterClaim(ethosCaseReference)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID, List.of(eccCaseReference)))
                .thenReturn(List.of(submitEvent));

        List<CaseData> cases = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN);
        assertEquals(2, cases.size());
        assertEquals(ethosCaseReference, cases.get(0).getEthosCaseReference());
        assertEquals(eccCaseReference, cases.get(1).getEthosCaseReference());
    }

    @Test
    void testGetsEccCases() throws IOException {
        String ethosCaseReference = "claimant-case-ref";
        List<String> eccCaseReferences = List.of("ecc-case1", "ecc-case2");
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withEccCase(eccCaseReferences.get(0))
                .withEccCase(eccCaseReferences.get(1))
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        SubmitEvent eccCase1SubmitEvent = CaseDataBuilder.builder()
                .withEthosCaseReference(eccCaseReferences.get(0))
                .withCounterClaim(ethosCaseReference)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID,
                List.of(eccCaseReferences.get(0)))).thenReturn(List.of(eccCase1SubmitEvent));
        SubmitEvent eccCase2SubmitEvent = CaseDataBuilder.builder()
                .withEthosCaseReference(eccCaseReferences.get(1))
                .withCounterClaim(ethosCaseReference)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID,
                List.of(eccCaseReferences.get(1)))).thenReturn(List.of(eccCase2SubmitEvent));

        List<CaseData> cases = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN);
        assertEquals(3, cases.size());
        assertEquals(ethosCaseReference, cases.get(0).getEthosCaseReference());
        assertEquals(eccCaseReferences.get(0), cases.get(1).getEthosCaseReference());
        assertEquals(eccCaseReferences.get(1), cases.get(2).getEthosCaseReference());
    }

    @Test
    void testGetsCounterClaim() throws IOException {
        String ethosCaseReference = "ecc-case";
        String counterClaim = "claimant-case";
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withCounterClaim(counterClaim)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        SubmitEvent submitEvent = CaseDataBuilder.builder()
                .withEthosCaseReference(counterClaim)
                .withEccCase(ethosCaseReference)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID, List.of(counterClaim)))
                .thenReturn(List.of(submitEvent));
        SubmitEvent eccSubmitEvent = CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withCounterClaim(counterClaim)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID, List.of(ethosCaseReference)))
                .thenReturn(List.of(eccSubmitEvent));

        List<CaseData> cases = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN);
        assertEquals(2, cases.size());
        assertEquals(counterClaim, cases.get(0).getEthosCaseReference());
        assertEquals(ethosCaseReference, cases.get(1).getEthosCaseReference());
    }

    @Test
    void testCounterClaimSearchException() throws IOException {
        String counterClaim = "120001/2021";
        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID, List.of(counterClaim)))
                .thenThrow(new IOException());

        CaseDetails eccCase = CaseDataBuilder.builder()
                .withEthosCaseReference("120002/2021")
                .withCounterClaim(counterClaim)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        assertThrows(CaseCreationException.class, () -> caseTransferUtils.getAllCasesToBeTransferred(
                eccCase, USER_TOKEN));
    }

    @Test
    void testEccCaseSearchException() throws IOException {
        String counterClaim = "120001/2021";
        String eccCaseRef = "120009/2021";
        SubmitEvent submitEvent = CaseDataBuilder.builder()
                .withEthosCaseReference(counterClaim)
                .withEccCase(eccCaseRef)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID, List.of(counterClaim)))
                .thenReturn(List.of(submitEvent));

        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID, List.of(eccCaseRef)))
                .thenThrow(new IOException());

        CaseDetails eccCase = CaseDataBuilder.builder()
                .withEthosCaseReference(eccCaseRef)
                .withCounterClaim(counterClaim)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        assertThrows(CaseCreationException.class, () -> caseTransferUtils.getAllCasesToBeTransferred(
                eccCase, USER_TOKEN));
    }

    @Test
    void testValidateCaseReturnsNoErrors() {
        CaseData caseData = CaseDataBuilder.builder()
                .withBfAction(YES)
                .withHearing("1", "HearingType", "Judge", null, null, null, null)
                .withHearingSession(0, "1", "2021-12-25", HEARING_STATUS_HEARD, true)
                .build();

        List<String> errors = caseTransferUtils.validateCase(caseData);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateCaseWithBfActionNotClearedReturnsError() {
        String ethosCaseReference = "case-ref";
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withBfAction(null)
                .build();

        List<String> errors = caseTransferUtils.validateCase(caseData);

        assertEquals(1, errors.size());
        String expectedError = String.format(BF_ACTIONS_ERROR_MSG, ethosCaseReference);
        assertEquals(expectedError, errors.get(0));
    }

    @Test
    void testValidateCaseWithHearingStillListedReturnsError() {
        String ethosCaseReference = "case-ref";
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withHearing("1", "HearingType", "Judge", null, null, null, null)
                .withHearingSession(0, "1", "2021-12-25", HEARING_STATUS_LISTED, true)
                .build();

        List<String> errors = caseTransferUtils.validateCase(caseData);

        assertEquals(1, errors.size());
        String expectedError = String.format(HEARINGS_ERROR_MSG, ethosCaseReference);
        assertEquals(expectedError, errors.get(0));
    }

    @Test
    void testValidateCaseWithBfActionAndHearingErrorsReturnsErrors() {
        String ethosCaseReference = "case-ref";
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withBfAction(null)
                .withHearing("1", "HearingType", "Judge", null, null, null, null)
                .withHearingSession(0, "1", "2021-12-25", HEARING_STATUS_LISTED, true)
                .build();

        List<String> errors = caseTransferUtils.validateCase(caseData);

        assertEquals(2, errors.size());
        String expectedError1 = String.format(BF_ACTIONS_ERROR_MSG, ethosCaseReference);
        assertEquals(expectedError1, errors.get(0));
        String expectedError2 = String.format(HEARINGS_ERROR_MSG, ethosCaseReference);
        assertEquals(expectedError2, errors.get(1));
    }
}
