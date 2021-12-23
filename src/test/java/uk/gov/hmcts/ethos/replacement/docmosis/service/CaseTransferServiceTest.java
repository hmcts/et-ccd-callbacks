package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ecm.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.ecm.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@RunWith(SpringJUnit4ClassRunner.class)
public class CaseTransferServiceTest {

    @InjectMocks
    private CaseTransferService caseTransferService;
    @Mock
    private CcdClient ccdClient;
    private CCDRequest ccdRequest;
    private SubmitEvent submitEvent;
    private String authToken;

    @Mock
    private PersistentQHelperService persistentQHelperService;

    @Before
    public void setUp() {
        ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        CaseData caseData = MultipleUtil.getCaseData("2123456/2020");
        caseData.setCaseRefNumberCount("2");
        caseData.setPositionTypeCT("PositionTypeCT");
        DynamicFixedListType officeCT = new DynamicFixedListType();
        DynamicValueType valueType = new DynamicValueType();
        valueType.setCode(TribunalOffice.LEEDS.getOfficeName());
        officeCT.setValue(valueType);
        caseData.setOfficeCT(officeCT);
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setJurisdiction("Employment");
        caseDetails.setState(ACCEPTED_STATE);
        ccdRequest.setCaseDetails(caseDetails);
        submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setCaseId(12345);
        authToken = "authToken";
    }

    @Test
    public void createCaseTransfer() {
        caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);
        assertEquals("PositionTypeCT", ccdRequest.getCaseDetails().getCaseData().getPositionType());
        assertEquals("Transferred to " + TribunalOffice.LEEDS.getOfficeName(), ccdRequest.getCaseDetails().getCaseData().getLinkedCaseCT());
    }

    @Test(expected = CaseCreationException.class)
    public void testCreateCaseTransferOriginalCaseException() throws IOException {
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenThrow(new IOException());
        var caseData = new CaseData();
        caseData.setCounterClaim("1000/2021");
        caseData.setOfficeCT(DynamicFixedListType.of(DynamicValueType.create("Test Office", "Test Office")));
        var caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseData(caseData);

        caseTransferService.createCaseTransfer(caseDetails, authToken);

        fail("CaseCreationException expected to be thrown by createCaseTransfer");
    }

    @Test(expected = CaseCreationException.class)
    public void testCreateCaseTransferAllCasesToBeTransferredException() throws IOException {
        var eccCaseData = new CaseData();
        var eccCounterClaimTypeItem = new EccCounterClaimTypeItem();
        var eccCounterClaimType = new EccCounterClaimType();
        eccCounterClaimType.setCounterClaim("counter-claim");
        eccCounterClaimTypeItem.setValue(eccCounterClaimType);
        eccCaseData.setEccCases(List.of(eccCounterClaimTypeItem));
        var submitEvent = new SubmitEvent();
        submitEvent.setCaseData(eccCaseData);

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
        var caseData = new CaseData();
        caseData.setCounterClaim("1000/2021");
        caseData.setOfficeCT(DynamicFixedListType.of(DynamicValueType.create("Test Office", "Test Office")));
        var caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseData(caseData);

        caseTransferService.createCaseTransfer(caseDetails, authToken);

        fail("CaseCreationException expected to be thrown by createCaseTransfer");
    }

    @Test
    public void InterCountryCaseTransfer() {
        ccdRequest.getCaseDetails().setCaseTypeId(SCOTLAND_CASE_TYPE_ID);
        ccdRequest.getCaseDetails().getCaseData().setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);
        assertEquals("PositionTypeCT", ccdRequest.getCaseDetails().getCaseData().getPositionType());
        assertEquals("Transferred to " + TribunalOffice.LEEDS.getOfficeName(), ccdRequest.getCaseDetails().getCaseData().getLinkedCaseCT());
    }

    @Test
    public void IntraCountryCaseTransfer() {
        ccdRequest.getCaseDetails().getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);
        assertEquals("PositionTypeCT", ccdRequest.getCaseDetails().getCaseData().getPositionType());
        assertEquals("Transferred to " + TribunalOffice.LEEDS.getOfficeName(), ccdRequest.getCaseDetails().getCaseData().getLinkedCaseCT());
        assertEquals(TribunalOffice.LEEDS.getOfficeName(), ccdRequest.getCaseDetails().getCaseData().getManagingOffice());
    }

    @Test
    public void createCaseTransferECC() throws IOException {
        CaseData caseData = MultipleUtil.getCaseData("3434232323");
        caseData.setCaseRefNumberCount("2");
        DynamicFixedListType officeCT = new DynamicFixedListType();
        DynamicValueType valueType = new DynamicValueType();
        valueType.setCode(ENGLANDWALES_CASE_TYPE_ID);
        officeCT.setValue(valueType);
        caseData.setOfficeCT(officeCT);
        SubmitEvent submitEvent1 = new SubmitEvent();
        submitEvent1.setCaseId(12345);
        submitEvent.getCaseData().setReasonForCT("New Reason");
        List<SubmitEvent> submitEventList = new ArrayList<>(Collections.singletonList(submitEvent));
        List<SubmitEvent> submitEventList1 = new ArrayList<>(Collections.singletonList(submitEvent1));
        ccdRequest.getCaseDetails().getCaseData().setCounterClaim("3434232323");
        EccCounterClaimTypeItem item = new EccCounterClaimTypeItem();
        EccCounterClaimType type = new EccCounterClaimType();
        type.setCounterClaim("2123456/2020");
        item.setId(UUID.randomUUID().toString());
        item.setValue(type);
        caseData.setEccCases(List.of(item));
        submitEvent1.setCaseData(caseData);
        when(ccdClient.retrieveCasesElasticSearch(authToken,ccdRequest.getCaseDetails().getCaseTypeId(), List.of("3434232323"))).thenReturn(submitEventList1);
        when(ccdClient.retrieveCasesElasticSearch(authToken,ccdRequest.getCaseDetails().getCaseTypeId(), List.of("2123456/2020"))).thenReturn(submitEventList);
        when(ccdClient.startEventForCase(authToken, "Manchester", "Employment", "12345")).thenReturn(ccdRequest);
        caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);
        assertEquals("PositionTypeCT", submitEvent.getCaseData().getPositionType());
        assertEquals("Transferred to " + TribunalOffice.LEEDS.getOfficeName(), submitEvent.getCaseData().getLinkedCaseCT());
        assertEquals("PositionTypeCT", submitEvent1.getCaseData().getPositionType());
        assertEquals("Transferred to " + TribunalOffice.LEEDS.getOfficeName(), submitEvent1.getCaseData().getLinkedCaseCT());
    }

    @Test
    public void createCaseTransferMultiples() {
        ccdRequest.getCaseDetails().getCaseData().setStateAPI(MULTIPLE);
        caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);
        assertEquals("PositionTypeCT", ccdRequest.getCaseDetails().getCaseData().getPositionType());
        assertEquals("Transferred to " + TribunalOffice.LEEDS.getOfficeName(), ccdRequest.getCaseDetails().getCaseData().getLinkedCaseCT());
    }

    @Test
    public void createCaseTransferBfNotCleared() {
        ccdRequest.getCaseDetails().getCaseData().setBfActions(BFHelperTest.generateBFActionTypeItems());
        ccdRequest.getCaseDetails().getCaseData().getBfActions().get(0).getValue().setCleared(null);
        var errors = caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);
        assertEquals(1, errors.size());
        var expectedBfActionsError = String.format(CaseTransferService.BF_ACTIONS_ERROR_MSG,
                ccdRequest.getCaseDetails().getCaseData().getEthosCaseReference());
        assertEquals(expectedBfActionsError, errors.get(0));
    }

    @Test
    public void createCaseTransferHearingListed() {
        ccdRequest.getCaseDetails().getCaseData().setHearingCollection(getHearingTypeCollection(HEARING_STATUS_LISTED));
        var errors = caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);
        assertEquals(1, errors.size());
        var expectedHearingsError = String.format(CaseTransferService.HEARINGS_ERROR_MSG,
                ccdRequest.getCaseDetails().getCaseData().getEthosCaseReference());
        assertEquals(expectedHearingsError, errors.get(0));
    }

    @Test
    public void createCaseTransferHearingListedAndBfNotCleared() {
        ccdRequest.getCaseDetails().getCaseData().setBfActions(BFHelperTest.generateBFActionTypeItems());
        ccdRequest.getCaseDetails().getCaseData().getBfActions().get(0).getValue().setCleared(null);
        ccdRequest.getCaseDetails().getCaseData().setHearingCollection(getHearingTypeCollection(HEARING_STATUS_LISTED));

        List<String> errors = caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);

        assertEquals(2, errors.size());
        var expectedBfActionsError = String.format(CaseTransferService.BF_ACTIONS_ERROR_MSG,
                ccdRequest.getCaseDetails().getCaseData().getEthosCaseReference());
        assertEquals(expectedBfActionsError, errors.get(0));
        var expectedHearingsError = String.format(CaseTransferService.HEARINGS_ERROR_MSG,
                ccdRequest.getCaseDetails().getCaseData().getEthosCaseReference());
        assertEquals(expectedHearingsError, errors.get(1));
    }

    @Test
    public void createCaseTransferBfClearedAndNotHearingListed() {
        ccdRequest.getCaseDetails().getCaseData().setBfActions(BFHelperTest.generateBFActionTypeItems());
        ccdRequest.getCaseDetails().getCaseData().setHearingCollection(getHearingTypeCollection(HEARING_STATUS_HEARD));
        List<String> errors = caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), authToken);
        assertEquals(0, errors.size());
    }

    @Test
    public void testPopulateCaseTransferOfficesIgnoresMissingManagingOffice() {
        var caseData = new CaseData();
        var tribunalOffices = List.of(TribunalOffice.LEEDS, TribunalOffice.MANCHESTER);
        var caseTransferOffices = createOfficeList(tribunalOffices);
        caseData.setOfficeCT(caseTransferOffices);
        var missingManagingOfficeValues = new String[] { null, "", " "};
        for (String managingOffice : missingManagingOfficeValues) {
            caseData.setManagingOffice(managingOffice);
            caseTransferService.populateCaseTransferOffices(caseData);
            verifyTribunalOffices(tribunalOffices, caseData.getOfficeCT().getListItems());
        }
    }

    @Test
    public void testPopulateCaseTransferOfficesScotland() {
        var caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        var tribunalOffices = List.of(TribunalOffice.LEEDS, TribunalOffice.MANCHESTER);
        var caseTransferOffices = createOfficeList(tribunalOffices);
        caseData.setOfficeCT(caseTransferOffices);
        caseTransferService.populateCaseTransferOffices(caseData);
        assertTrue(caseData.getOfficeCT().getListItems().isEmpty());
        assertNull(caseData.getOfficeCT().getValue());
    }

    @Test
    public void testPopulateCaseTransferOfficesEnglandWales() {
        var caseData = new CaseData();
        var tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        tribunalOffices.remove(TribunalOffice.MANCHESTER);
        var caseTransferOffices = createOfficeList(tribunalOffices);
        caseData.setManagingOffice(managingOffice);
        caseData.setOfficeCT(caseTransferOffices);

        caseTransferService.populateCaseTransferOffices(caseData);

        verifyTribunalOffices(tribunalOffices, caseData.getOfficeCT().getListItems());
    }

    private List<HearingTypeItem> getHearingTypeCollection(String hearingState){
        HearingType hearingType = new HearingType();
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingStatus(hearingState);
        dateListedTypeItem.setId("123");
        dateListedTypeItem.setValue(dateListedType);
        hearingType.setHearingDateCollection(new ArrayList<>(Collections.singleton(dateListedTypeItem)));

        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId("1234");
        hearingTypeItem.setValue(hearingType);
        return new ArrayList<>(Collections.singletonList(hearingTypeItem));
    }

    private DynamicFixedListType createOfficeList(List<TribunalOffice> tribunalOffices) {
        var listItems = new ArrayList<DynamicValueType>();
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            listItems.add(DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()));
        }

        return DynamicFixedListType.from(listItems);
    }

    private void verifyTribunalOffices(List<TribunalOffice> expected, List<DynamicValueType> listItems) {
        assertEquals(expected.size(), listItems.size());
        Iterator<TribunalOffice> expectedItr = expected.listIterator();
        Iterator<DynamicValueType> listItemsItr = listItems.listIterator();
        while (expectedItr.hasNext() && listItemsItr.hasNext()) {
            var tribunalOffice = expectedItr.next();
            var dynamicValueType = listItemsItr.next();
            assertEquals(tribunalOffice.getOfficeName(), dynamicValueType.getCode());
            assertEquals(tribunalOffice.getOfficeName(), dynamicValueType.getLabel());
        }
    }
}
