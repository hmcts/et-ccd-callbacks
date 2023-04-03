package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils.BF_ACTIONS_ERROR_MSG;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils.HEARINGS_ERROR_MSG;

@ExtendWith(SpringExtension.class)
class CaseTransferDifferentCountryServiceTest {

    @InjectMocks
    CaseTransferDifferentCountryService caseTransferDifferentCountryService;

    @Mock
    CaseTransferUtils caseTransferUtils;

    @Mock
    CaseTransferEventService caseTransferEventService;

    @Captor
    private ArgumentCaptor<CaseTransferEventParams> caseTransferEventParamsArgumentCaptor;

    private static final String CLAIMANT_ETHOS_CASE_REFERENCE = "120001/2021";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String USER_TOKEN = "my-test-token";
    private static final String REASON_CT = "Just a test";
    private final String caseTypeId = ENGLANDWALES_CASE_TYPE_ID;
    private final String expectedPositionType = CaseTransferDifferentCountryService.CASE_TRANSFERRED_POSITION_TYPE;

    @Test
    void testCaseTransfer() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        CaseDetails caseDetails = createCaseDetails(managingOffice, officeCT, null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData()));

        List<String> errors = caseTransferDifferentCountryService.transferCase(caseDetails, USER_TOKEN);

        assertTrue(errors.isEmpty());
        verify(caseTransferEventService, times(1)).transfer(
            caseTransferEventParamsArgumentCaptor.capture());

        verifyCaseTransferEventParams(CLAIMANT_ETHOS_CASE_REFERENCE, CLAIMANT_ETHOS_CASE_REFERENCE, officeCT,
                caseTransferEventParamsArgumentCaptor.getValue());

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferSuccessWithBfActionClearedAndHearingHeard() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        CaseDetails caseDetails = createCaseDetails(managingOffice, officeCT, HEARING_STATUS_HEARD);
        addBfAction(caseDetails.getCaseData(), YES);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData()));

        List<String> errors = caseTransferDifferentCountryService.transferCase(caseDetails, USER_TOKEN);

        assertTrue(errors.isEmpty());
        verify(caseTransferEventService, times(1)).transfer(
            caseTransferEventParamsArgumentCaptor.capture());
        verifyCaseTransferEventParams(CLAIMANT_ETHOS_CASE_REFERENCE, CLAIMANT_ETHOS_CASE_REFERENCE, officeCT,
                caseTransferEventParamsArgumentCaptor.getValue());

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferFailsWithBfAction() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        CaseDetails caseDetails = createCaseDetails(managingOffice, officeCT, null);
        addBfAction(caseDetails.getCaseData(), null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData()));
        String expectedError = String.format(BF_ACTIONS_ERROR_MSG, CLAIMANT_ETHOS_CASE_REFERENCE);
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(List.of(expectedError));

        List<String> errors = caseTransferDifferentCountryService.transferCase(caseDetails, USER_TOKEN);

        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        verifyCaseDataAfterTransferFails(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferFailsWithHearingListed() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        CaseDetails caseDetails = createCaseDetails(managingOffice, officeCT, HEARING_STATUS_LISTED);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData()));
        String expectedError = String.format(HEARINGS_ERROR_MSG, CLAIMANT_ETHOS_CASE_REFERENCE);
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(List.of(expectedError));

        List<String> errors = caseTransferDifferentCountryService.transferCase(caseDetails, USER_TOKEN);

        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        verifyCaseDataAfterTransferFails(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferFailsWithBfActionAndHearingListed() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        CaseDetails caseDetails = createCaseDetails(managingOffice, officeCT, HEARING_STATUS_LISTED);
        addBfAction(caseDetails.getCaseData(), null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData()));
        List<String> expectedErrors = List.of(String.format(BF_ACTIONS_ERROR_MSG, CLAIMANT_ETHOS_CASE_REFERENCE),
                String.format(HEARINGS_ERROR_MSG, CLAIMANT_ETHOS_CASE_REFERENCE));
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(expectedErrors);

        List<String> errors = caseTransferDifferentCountryService.transferCase(caseDetails, USER_TOKEN);

        assertEquals(2, errors.size());
        assertEquals(expectedErrors.get(0), errors.get(0));
        assertEquals(expectedErrors.get(1), errors.get(1));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        verifyCaseDataAfterTransferFails(caseDetails, managingOffice, officeCT);
    }

    @Test
    void caseTransferWithEccCase() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        String eccCaseReference = "120002/2021";
        List<String> eccCases = List.of(eccCaseReference);
        CaseDetails caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        CaseData eccCaseData = createEccCaseSearchResult(eccCaseReference, managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData));

        List<String> errors = caseTransferDifferentCountryService.transferCase(caseDetails, USER_TOKEN);

        assertTrue(errors.isEmpty());

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);

        verify(caseTransferEventService, times(2))
            .transfer(caseTransferEventParamsArgumentCaptor.capture());
        List<CaseTransferEventParams> params = caseTransferEventParamsArgumentCaptor.getAllValues();
        verifyCaseTransferEventParams(CLAIMANT_ETHOS_CASE_REFERENCE, CLAIMANT_ETHOS_CASE_REFERENCE, officeCT,
            params.get(0));
        verifyCaseTransferEventParams(eccCaseReference, CLAIMANT_ETHOS_CASE_REFERENCE, officeCT, params.get(1));
    }

    @Test
    void caseTransferWithEccCases() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        List<String> eccCases = List.of("120002/2021", "120003/2021");
        CaseDetails caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        CaseData eccCaseData1 = createEccCaseSearchResult(eccCases.get(0), managingOffice);
        CaseData eccCaseData2 = createEccCaseSearchResult(eccCases.get(1), managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData1, eccCaseData2));

        List<String> errors = caseTransferDifferentCountryService.transferCase(caseDetails, USER_TOKEN);

        assertTrue(errors.isEmpty());

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);

        verify(caseTransferEventService, times(3))
            .transfer(caseTransferEventParamsArgumentCaptor.capture());
        List<CaseTransferEventParams> params = caseTransferEventParamsArgumentCaptor.getAllValues();
        verifyCaseTransferEventParams(CLAIMANT_ETHOS_CASE_REFERENCE, CLAIMANT_ETHOS_CASE_REFERENCE, officeCT,
            params.get(0));
        verifyCaseTransferEventParams(eccCases.get(0), CLAIMANT_ETHOS_CASE_REFERENCE, officeCT, params.get(1));
        verifyCaseTransferEventParams(eccCases.get(1), CLAIMANT_ETHOS_CASE_REFERENCE, officeCT, params.get(2));
    }

    @Test
    void caseTransferWithEccCaseAsSource() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        String eccCaseReference = "120009/2021";
        CaseDetails eccCaseDetails = createEccCaseDetails(eccCaseReference, managingOffice, officeCT);
        CaseData claimantCaseDeta = createCaseDetails(managingOffice, List.of(eccCaseReference), null,
            null).getCaseData();
        when(caseTransferUtils.getAllCasesToBeTransferred(eccCaseDetails, USER_TOKEN))
                .thenReturn(List.of(claimantCaseDeta, eccCaseDetails.getCaseData()));

        List<String> errors = caseTransferDifferentCountryService.transferCase(eccCaseDetails, USER_TOKEN);

        assertTrue(errors.isEmpty());

        verifyCaseDataAfterTransfer(eccCaseDetails, managingOffice, officeCT);

        verify(caseTransferEventService, times(2)).transfer(
            caseTransferEventParamsArgumentCaptor.capture());
        List<CaseTransferEventParams> params = caseTransferEventParamsArgumentCaptor.getAllValues();
        verifyCaseTransferEventParams(CLAIMANT_ETHOS_CASE_REFERENCE, eccCaseReference, officeCT, params.get(0));
        verifyCaseTransferEventParams(eccCaseReference, eccCaseReference, officeCT, params.get(1));
    }

    @Test
    void caseTransferWithEccCaseReturnsTransferError() {
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        List<String> eccCases = List.of("120002/2021");
        CaseDetails caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        CaseData eccCaseData = createEccCaseSearchResult(eccCases.get(0), managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData));

        String caseTransferError = "A transfer error";
        when(caseTransferEventService.transfer(isA(CaseTransferEventParams.class))).thenReturn(
                List.of(caseTransferError));

        List<String> errors = caseTransferDifferentCountryService.transferCase(caseDetails, USER_TOKEN);

        assertEquals(2, errors.size());
        assertEquals(caseTransferError, errors.get(0));
        assertEquals(caseTransferError, errors.get(1));

        verifyCaseDataAfterTransfer(caseDetails, managingOffice, officeCT);

        verify(caseTransferEventService, times(2)).transfer(isA(CaseTransferEventParams.class));
    }

    @Test
    void transferCaseNoCasesFoundThrowsException() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference(CLAIMANT_ETHOS_CASE_REFERENCE)
                .buildAsCaseDetails(caseTypeId, JURISDICTION);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, USER_TOKEN)).thenReturn(Collections.emptyList());

        Assertions.assertThrows(IllegalStateException.class, () -> caseTransferDifferentCountryService.transferCase(
                caseDetails, USER_TOKEN));

        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));
    }

    private void verifyCaseTransferEventParams(String expectedEthosCaseReference,
                                               String expectedSourceEthosCaseReference, String expectedManagingOffice,
                                               CaseTransferEventParams params) {
        assertEquals(USER_TOKEN, params.getUserToken());
        assertEquals(caseTypeId, params.getCaseTypeId());
        assertEquals(JURISDICTION, params.getJurisdiction());
        assertEquals(List.of(expectedEthosCaseReference), params.getEthosCaseReferences());
        assertEquals(expectedSourceEthosCaseReference, params.getSourceEthosCaseReference());
        assertEquals(expectedManagingOffice, params.getNewManagingOffice());
        assertEquals(expectedPositionType, params.getPositionType());
        assertEquals(REASON_CT, params.getReason());
        assertEquals(SINGLE_CASE_TYPE, params.getMultipleReference());
        assertFalse(params.isConfirmationRequired());
        assertFalse(params.isTransferSameCountry());
    }

    private CaseDetails createCaseDetails(String managingOffice, String officeCT, String hearingStatus) {
        return createCaseDetails(managingOffice, Collections.emptyList(), officeCT, hearingStatus);
    }

    private CaseDetails createCaseDetails(String managingOffice, List<String> eccCases, String officeCT,
                                          String hearingStatus) {
        CaseDataBuilder builder = CaseDataBuilder.builder()
                .withEthosCaseReference(CLAIMANT_ETHOS_CASE_REFERENCE)
                .withManagingOffice(managingOffice)
                .withCaseTransfer(officeCT, REASON_CT);
        for (String eccCase : eccCases) {
            builder.withEccCase(eccCase);
        }

        if (hearingStatus != null) {
            builder.withHearing("1", null, null, null, null,
                    null, null)
                    .withHearingSession(0, "1", "2021-12-25", hearingStatus, false);
        }

        return builder.buildAsCaseDetails(caseTypeId, JURISDICTION);
    }

    private CaseData createEccCaseSearchResult(String ethosCaseReference, String managingOffice) {
        return CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withManagingOffice(managingOffice)
                .withCounterClaim(CLAIMANT_ETHOS_CASE_REFERENCE)
                .build();
    }

    private void addBfAction(CaseData caseData, String cleared) {
        BFActionType bfAction = new BFActionType();
        bfAction.setCleared(cleared);
        BFActionTypeItem bfActionItem = new BFActionTypeItem();
        bfActionItem.setValue(bfAction);

        caseData.setBfActions(List.of(bfActionItem));
    }

    private CaseDetails createEccCaseDetails(String ethosCaseReference, String managingOffice, String officeCT) {
        return CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withManagingOffice(managingOffice)
                .withCounterClaim(CLAIMANT_ETHOS_CASE_REFERENCE)
                .withCaseTransfer(officeCT, REASON_CT)
                .buildAsCaseDetails(caseTypeId, JURISDICTION);
    }

    private void verifyCaseDataAfterTransfer(CaseDetails caseDetails, String managingOffice, String officeCT) {
        assertEquals(managingOffice, caseDetails.getCaseData().getManagingOffice());
        assertEquals("Transferred to " + officeCT, caseDetails.getCaseData().getLinkedCaseCT());
        assertEquals(expectedPositionType, caseDetails.getCaseData().getPositionType());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());
    }

    private void verifyCaseDataAfterTransferFails(CaseDetails caseDetails, String managingOffice, String officeCT) {
        assertEquals(managingOffice, caseDetails.getCaseData().getManagingOffice());
        assertEquals(officeCT, caseDetails.getCaseData().getOfficeCT().getSelectedCode());
        assertNull(caseDetails.getCaseData().getPositionType());
    }
}
