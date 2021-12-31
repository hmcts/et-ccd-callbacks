package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

@RunWith(SpringJUnit4ClassRunner.class)
public class CaseTransferSameCountryServiceTest {

    @InjectMocks
    private CaseTransferSameCountryService caseTransferSameCountryService;

    @Mock
    private CaseTransferUtils caseTransferUtils;

    @Mock
    private CaseTransferEventService caseTransferEventService;

    @Captor
    private ArgumentCaptor<CaseTransferEventParams> caseTransferEventParamsArgumentCaptor;

    private final String claimantEthosCaseReference = "120001/2021";
    private final String caseTypeId = ENGLANDWALES_CASE_TYPE_ID;
    private final String jurisdiction = "EMPLOYMENT";
    private final String userToken = "my-test-token";
    private final String reasonCT = "Just a test";

    @Test
    public void caseTransferSameCountry() {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), officeCT, null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));

        var errors = caseTransferSameCountryService.transferCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(officeCT, caseDetails.getCaseData().getManagingOffice());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getPositionTypeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());
    }

    @Test
    public void caseTransferSameCountryWithBfActionClearedAndHearingHeard() {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), officeCT, HEARING_STATUS_HEARD);
        addBfAction(caseDetails.getCaseData(), YES);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));

        var errors = caseTransferSameCountryService.transferCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(officeCT, caseDetails.getCaseData().getManagingOffice());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getPositionTypeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());
    }

    @Test
    public void caseTransferSameCountryWithBfAction() {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), officeCT, null);
        addBfAction(caseDetails.getCaseData(), null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));
        var expectedError = String.format(BF_ACTIONS_ERROR_MSG, claimantEthosCaseReference);
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(List.of(expectedError));

        var errors = caseTransferSameCountryService.transferCase(caseDetails, userToken);

        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(), caseDetails.getCaseData().getManagingOffice());
        assertEquals(officeCT, caseDetails.getCaseData().getOfficeCT().getSelectedCode());
    }

    @Test
    public void caseTransferSameCountryWithHearingListed() {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), officeCT, HEARING_STATUS_LISTED);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));
        var expectedError = String.format(HEARINGS_ERROR_MSG, claimantEthosCaseReference);
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(List.of(expectedError));

        var errors = caseTransferSameCountryService.transferCase(caseDetails, userToken);

        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(), caseDetails.getCaseData().getManagingOffice());
        assertEquals(officeCT, caseDetails.getCaseData().getOfficeCT().getSelectedCode());
    }

    @Test
    public void caseTransferSameCountryWithBfActionAndHearingListed() {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), officeCT, HEARING_STATUS_LISTED);
        addBfAction(caseDetails.getCaseData(), null);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData()));
        var expectedErrors = List.of(String.format(BF_ACTIONS_ERROR_MSG, claimantEthosCaseReference),
                String.format(HEARINGS_ERROR_MSG, claimantEthosCaseReference));
        when(caseTransferUtils.validateCase(caseDetails.getCaseData())).thenReturn(expectedErrors);

        var errors = caseTransferSameCountryService.transferCase(caseDetails, userToken);

        assertEquals(2, errors.size());
        assertEquals(expectedErrors.get(0), errors.get(0));
        assertEquals(expectedErrors.get(1), errors.get(1));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(), caseDetails.getCaseData().getManagingOffice());
        assertEquals(officeCT, caseDetails.getCaseData().getOfficeCT().getSelectedCode());
    }

    @Test
    public void caseTransferSameCountryWithEccCase() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCaseReference = "120002/2021";
        var eccCases = List.of(eccCaseReference);
        var caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        var eccCaseData = createEccCaseSearchResult(eccCaseReference, managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData));

        var errors = caseTransferSameCountryService.transferCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());

        assertEquals(officeCT, caseDetails.getCaseData().getManagingOffice());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getPositionTypeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());

        verify(caseTransferEventService, times(1)).transfer(caseTransferEventParamsArgumentCaptor.capture());
        var params = caseTransferEventParamsArgumentCaptor.getValue();
        verifyCaseTransferEventParams(eccCaseReference,claimantEthosCaseReference, officeCT, params);
    }

    @Test
    public void caseTransferSameCountryWithEccCases() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCases = List.of("120002/2021", "120003/2021");
        var caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        var eccCaseData1 = createEccCaseSearchResult(eccCases.get(0), managingOffice);
        var eccCaseData2 = createEccCaseSearchResult(eccCases.get(1), managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData1, eccCaseData2));

        var errors = caseTransferSameCountryService.transferCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());

        assertEquals(officeCT, caseDetails.getCaseData().getManagingOffice());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getPositionTypeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());

        verify(caseTransferEventService, times(2)).transfer(caseTransferEventParamsArgumentCaptor.capture());
        var allParams = caseTransferEventParamsArgumentCaptor.getAllValues();
        verifyCaseTransferEventParams(eccCases.get(0), claimantEthosCaseReference, officeCT, allParams.get(0));
        verifyCaseTransferEventParams(eccCases.get(1), claimantEthosCaseReference, officeCT, allParams.get(1));
    }

    @Test
    public void caseTransferSameCountryWithEccCaseAsSource() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCaseReference = "120009/2021";
        var eccCaseDetails = createEccCaseDetails(eccCaseReference, managingOffice, officeCT);
        var claimantCaseDeta = createCaseDetails(managingOffice, List.of(eccCaseReference), null, null).getCaseData();
        when(caseTransferUtils.getAllCasesToBeTransferred(eccCaseDetails, userToken))
                .thenReturn(List.of(claimantCaseDeta, eccCaseDetails.getCaseData()));

        var errors = caseTransferSameCountryService.transferCase(eccCaseDetails, userToken);

        assertTrue(errors.isEmpty());
        assertEquals(officeCT, eccCaseDetails.getCaseData().getManagingOffice());
        assertNull(eccCaseDetails.getCaseData().getOfficeCT());
        assertNull(eccCaseDetails.getCaseData().getPositionTypeCT());
        assertNull(eccCaseDetails.getCaseData().getStateAPI());
        verify(caseTransferEventService, times(1)).transfer(caseTransferEventParamsArgumentCaptor.capture());
        var params = caseTransferEventParamsArgumentCaptor.getValue();
        verifyCaseTransferEventParams(claimantEthosCaseReference, eccCaseReference, officeCT, params);
    }

    @Test
    public void caseTransferSameCountryWithEccCaseReturnsTransferError() {
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCases = List.of("120002/2021");
        var caseDetails = createCaseDetails(managingOffice, eccCases, officeCT, null);
        var eccCaseData = createEccCaseSearchResult(eccCases.get(0), managingOffice);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken))
                .thenReturn(List.of(caseDetails.getCaseData(), eccCaseData));

        var caseTransferError = "A transfer error";
        when(caseTransferEventService.transfer(isA(CaseTransferEventParams.class))).thenReturn(
                List.of(caseTransferError));

        var errors = caseTransferSameCountryService.transferCase(caseDetails, userToken);

        assertEquals(1, errors.size());
        assertEquals(caseTransferError, errors.get(0));
        assertEquals(officeCT, caseDetails.getCaseData().getManagingOffice());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getPositionTypeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());
        verify(caseTransferEventService, times(1)).transfer(isA(CaseTransferEventParams.class));
    }

    @Test
    public void caseTransferSameCountryEccLinkedCase() {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), Collections.emptyList(),
                officeCT, null);

        var errors = caseTransferSameCountryService.updateEccLinkedCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());
        verify(caseTransferUtils, never()).getAllCasesToBeTransferred(caseDetails, userToken);
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(officeCT, caseDetails.getCaseData().getManagingOffice());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getPositionTypeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());
    }

    private void verifyCaseTransferEventParams(String expectedEthosCaseReference,
                                               String expectedSourceEthosCaseReference, String expectedManagingOffice,
                                               CaseTransferEventParams params) {
        assertEquals(userToken, params.getUserToken());
        assertEquals(caseTypeId, params.getCaseTypeId());
        assertEquals(jurisdiction, params.getJurisdiction());
        assertEquals(expectedEthosCaseReference, params.getEthosCaseReference());
        assertEquals(expectedSourceEthosCaseReference, params.getSourceEthosCaseReference());
        assertEquals(expectedManagingOffice, params.getNewManagingOffice());
        assertNull(params.getPositionType());
        assertEquals(reasonCT, params.getReason());
        assertEquals(SINGLE_CASE_TYPE, params.getEcmCaseType());
        assertTrue(params.isTransferSameCountry());
    }

    private CaseDetails createCaseDetails(String managingOffice, String officeCT, String hearingStatus) {
        return createCaseDetails(managingOffice, Collections.emptyList(), officeCT, hearingStatus);
    }

    private CaseDetails createCaseDetails(String managingOffice, List<String> eccCases, String officeCT,
                                          String hearingStatus) {
        CaseDataBuilder builder = CaseDataBuilder.builder()
                .withEthosCaseReference(claimantEthosCaseReference)
                .withManagingOffice(managingOffice)
                .withCaseTransfer(officeCT, null, reasonCT);
        for (String eccCase : eccCases) {
            builder.withEccCase(eccCase);
        }

        if (hearingStatus != null) {
            builder.withHearing("1", null, null)
                    .withHearingSession(0, "1", "2021-12-25", hearingStatus, false);
        }

        return builder.buildAsCaseDetails(caseTypeId, jurisdiction);
    }

    private CaseData createEccCaseSearchResult(String ethosCaseReference, String managingOffice) {
        return CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withManagingOffice(managingOffice)
                .withCounterClaim(claimantEthosCaseReference)
                .build();
    }

    private void addBfAction(CaseData caseData, String cleared) {
        var bfAction = new BFActionType();
        bfAction.setCleared(cleared);
        var bfActionItem = new BFActionTypeItem();
        bfActionItem.setValue(bfAction);

        caseData.setBfActions(List.of(bfActionItem));
    }

    private CaseDetails createEccCaseDetails(String ethosCaseReference, String managingOffice, String officeCT) {
        return CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withManagingOffice(managingOffice)
                .withCounterClaim(claimantEthosCaseReference)
                .withCaseTransfer(officeCT, null, reasonCT)
                .buildAsCaseDetails(caseTypeId, jurisdiction);
    }
}
