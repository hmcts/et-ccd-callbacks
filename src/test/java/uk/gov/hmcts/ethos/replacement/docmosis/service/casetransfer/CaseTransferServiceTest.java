package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferService.BF_ACTIONS_ERROR_MSG;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferService.HEARINGS_ERROR_MSG;

@RunWith(SpringJUnit4ClassRunner.class)
public class CaseTransferServiceTest {

    @InjectMocks
    private CaseTransferService caseTransferService;

    @Mock
    private CcdClient ccdClient;

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
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), Collections.emptyList(),
                officeCT, null);

        var errors = caseTransferService.caseTransferSameCountry(caseDetails, userToken);

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
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), Collections.emptyList(),
                officeCT, HEARING_STATUS_HEARD);
        addBfAction(caseDetails.getCaseData(), YES);

        var errors = caseTransferService.caseTransferSameCountry(caseDetails, userToken);

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
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), Collections.emptyList(),
                officeCT, null);
        addBfAction(caseDetails.getCaseData(), null);
        var expectedError = String.format(BF_ACTIONS_ERROR_MSG, claimantEthosCaseReference);

        var errors = caseTransferService.caseTransferSameCountry(caseDetails, userToken);

        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(), caseDetails.getCaseData().getManagingOffice());
        assertEquals(officeCT, caseDetails.getCaseData().getOfficeCT().getSelectedCode());
    }

    @Test
    public void caseTransferSameCountryWithHearingListed() {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), Collections.emptyList(),
                officeCT, HEARING_STATUS_LISTED);
        var expectedError = String.format(HEARINGS_ERROR_MSG, claimantEthosCaseReference);

        var errors = caseTransferService.caseTransferSameCountry(caseDetails, userToken);

        assertEquals(1, errors.size());
        assertEquals(expectedError, errors.get(0));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(), caseDetails.getCaseData().getManagingOffice());
        assertEquals(officeCT, caseDetails.getCaseData().getOfficeCT().getSelectedCode());
    }

    @Test
    public void caseTransferSameCountryWithBfActionAndHearingListed() {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), Collections.emptyList(),
                officeCT, HEARING_STATUS_LISTED);
        addBfAction(caseDetails.getCaseData(), null);
        var expectedErrors = List.of(String.format(BF_ACTIONS_ERROR_MSG, claimantEthosCaseReference),
                String.format(HEARINGS_ERROR_MSG, claimantEthosCaseReference));

        var errors = caseTransferService.caseTransferSameCountry(caseDetails, userToken);

        assertEquals(2, errors.size());
        assertEquals(expectedErrors.get(0), errors.get(0));
        assertEquals(expectedErrors.get(1), errors.get(1));
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(), caseDetails.getCaseData().getManagingOffice());
        assertEquals(officeCT, caseDetails.getCaseData().getOfficeCT().getSelectedCode());
    }

    @Test
    public void caseTransferSameCountryWithEccCase() throws IOException {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCaseReference = "120002/2021";
        var eccCases = List.of(eccCaseReference);
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), eccCases, officeCT, null);

        mockEccCaseSearch(eccCases);

        var errors = caseTransferService.caseTransferSameCountry(caseDetails, userToken);

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
    public void caseTransferSameCountryWithEccCases() throws IOException {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCases = List.of("120002/2021", "120003/2021");
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), eccCases, officeCT, null);

        mockEccCaseSearch(eccCases);

        var errors = caseTransferService.caseTransferSameCountry(caseDetails, userToken);

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
    public void caseTransferSameCountryWithEccCaseAsSource() throws IOException {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCaseReference = "120009/2021";
        var eccCaseDetails = createEccCase(eccCaseReference, officeCT);
        mockEccCaseSearch(List.of(eccCaseReference));

        var errors = caseTransferService.caseTransferSameCountry(eccCaseDetails, userToken);

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
    public void caseTransferSameCountryWithEccCaseReturnsTransferError() throws IOException {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var eccCases = List.of("120002/2021");
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), eccCases, officeCT, null);

        mockEccCaseSearch(eccCases);
        var caseTransferError = "A transfer error";
        when(caseTransferEventService.transfer(isA(CaseTransferEventParams.class))).thenReturn(
                List.of(caseTransferError));

        var errors = caseTransferService.caseTransferSameCountry(caseDetails, userToken);

        assertEquals(1, errors.size());
        assertEquals(caseTransferError, errors.get(0));
        assertEquals(officeCT, caseDetails.getCaseData().getManagingOffice());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getPositionTypeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());
        verify(caseTransferEventService, times(1)).transfer(isA(CaseTransferEventParams.class));
    }

    @Test
    public void caseTransferSameCountryEccLinkedCase() throws IOException {
        var officeCT = TribunalOffice.NEWCASTLE.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.MANCHESTER.getOfficeName(), Collections.emptyList(),
                officeCT, null);
        var eccCases = List.of("120002/2021");
        mockEccCaseSearch(eccCases);

        var errors = caseTransferService.caseTransferSameCountryEccLinkedCase(caseDetails, userToken);

        assertTrue(errors.isEmpty());
        verify(caseTransferEventService, never()).transfer(isA(CaseTransferEventParams.class));

        assertEquals(officeCT, caseDetails.getCaseData().getManagingOffice());
        assertNull(caseDetails.getCaseData().getOfficeCT());
        assertNull(caseDetails.getCaseData().getPositionTypeCT());
        assertNull(caseDetails.getCaseData().getStateAPI());
    }

    @Test(expected = CaseCreationException.class)
    public void getOriginalCaseException() throws IOException {
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenThrow(new IOException());

        caseTransferService.caseTransferSameCountry(createEccCase("120009/2021",
                TribunalOffice.NEWCASTLE.getOfficeName()), userToken);

        fail("CaseCreationException expected to be thrown by caseTransferSameCountry");
    }

    @Test(expected = CaseCreationException.class)
    public void getAllCasesToBeTransferredException() throws IOException {
        var submitEvent = CaseDataBuilder.builder()
                .withEccCase("120009/2021")
                .buildAsSubmitEvent(ACCEPTED_STATE);
        var submitEvents = List.of(submitEvent);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenAnswer(new Answer<>() {
            int count = 0;
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (count == 0) {
                    count++;
                    return submitEvents;
                } else {
                    throw new IOException();
                }
            }
        });

        caseTransferService.caseTransferSameCountry(
                createEccCase("120009/2021", TribunalOffice.NEWCASTLE.getOfficeName()), userToken);

        fail("CaseCreationException expected to be thrown by caseTransferSameCountry");
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

    private CaseDetails createCaseDetails(String managingOffice, List<String> eccCases, String office,
                                          String hearingStatus) {
        CaseDataBuilder builder = CaseDataBuilder.builder()
                .withEthosCaseReference(claimantEthosCaseReference)
                .withManagingOffice(managingOffice)
                .withCaseTransfer(office, null, reasonCT);
        for (String eccCase : eccCases) {
            builder.withEccCase(eccCase);
        }

        if (hearingStatus != null) {
            builder.withHearing("1", null, null)
                    .withHearingSession(0, "1", "2021-12-25", hearingStatus, false);
        }

        return builder.buildAsCaseDetails(caseTypeId, jurisdiction);
    }

    private void addBfAction(CaseData caseData, String cleared) {
        var bfAction = new BFActionType();
        bfAction.setCleared(cleared);
        var bfActionItem = new BFActionTypeItem();
        bfActionItem.setValue(bfAction);

        caseData.setBfActions(List.of(bfActionItem));
    }

    private void mockEccCaseSearch(List<String> ethosCaseReferences) throws IOException {
        var builder = CaseDataBuilder.builder().withEthosCaseReference(claimantEthosCaseReference);
        for (String ethosCaseReference : ethosCaseReferences) {
            builder.withEccCase(ethosCaseReference);

            var submitEvent = CaseDataBuilder.builder()
                    .withEthosCaseReference(ethosCaseReference)
                    .buildAsSubmitEvent(ACCEPTED_STATE);
            when(ccdClient.retrieveCasesElasticSearch(userToken, caseTypeId, List.of(ethosCaseReference)))
                    .thenReturn(List.of(submitEvent));
        }

        var submitEvent = builder.buildAsSubmitEvent(ACCEPTED_STATE);
        when(ccdClient.retrieveCasesElasticSearch(userToken, caseTypeId, List.of(claimantEthosCaseReference)))
                .thenReturn(List.of(submitEvent));
    }

    private CaseDetails createEccCase(String ethosCaseReference, String office) {
        return CaseDataBuilder.builder()
                .withEthosCaseReference(ethosCaseReference)
                .withCounterClaim(claimantEthosCaseReference)
                .withCaseTransfer(office, null, reasonCT)
                .buildAsCaseDetails(caseTypeId, jurisdiction);
    }
}
