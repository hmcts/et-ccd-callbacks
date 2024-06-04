package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.BulkCasesPayload;
import uk.gov.hmcts.ecm.common.model.helper.BulkRequestPayload;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.BulkData;
import uk.gov.hmcts.et.common.model.bulk.BulkDetails;
import uk.gov.hmcts.et.common.model.bulk.BulkRequest;
import uk.gov.hmcts.et.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.bulk.items.MultipleTypeItem;
import uk.gov.hmcts.et.common.model.bulk.items.SearchTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.CaseType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.bulk.types.MultipleType;
import uk.gov.hmcts.et.common.model.bulk.types.SearchType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class BulkUpdateServiceTest {

    @InjectMocks
    private BulkUpdateService bulkUpdateService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private CreateUpdatesBusSender createUpdatesBusSender;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    private FeatureToggleService featureToggleService;

    private CCDRequest ccdRequest;
    private BulkRequest bulkRequest;
    private SubmitEvent submitEvent;
    private SearchTypeItem searchTypeItem;
    private BulkDetails bulkDetails;
    private SubmitBulkEvent submitBulkEvent;
    private BulkRequestPayload bulkRequestPayload;

    @BeforeEach
    public void setUp() {
        ccdRequest = new CCDRequest();
        BulkData bulkData = new BulkData();
        bulkData.setMultipleReference("1111");
        bulkData.setJurCodesDynamicList(getJurCodesDynamicList());
        bulkData.setJurCodesCollection(getJurCodesCollection());
        MultipleType multipleType = new MultipleType();
        multipleType.setEthosCaseReferenceM("2222");
        multipleType.setRespondentRepM("JuanPedro");
        multipleType.setSubMultipleM(" ");
        MultipleTypeItem multipleTypeItem = new MultipleTypeItem();
        multipleTypeItem.setValue(multipleType);
        multipleTypeItem.setId("2222");
        bulkData.setMultipleCollection(new ArrayList<>(Collections.singletonList(multipleTypeItem)));
        bulkDetails = new BulkDetails();
        bulkDetails.setJurisdiction("TRIBUNALS");
        bulkDetails.setCaseData(bulkData);
        bulkDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        bulkDetails.setCaseId("2300001/2019");
        bulkRequest = new BulkRequest();
        bulkRequest.setCaseDetails(bulkDetails);
        CaseData caseData = new CaseData();
        caseData.setMultipleReference("2222");
        caseData.setEcmCaseType(MULTIPLE_CASE_TYPE);
        caseData.setEthosCaseReference("111");
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantLastName("JuanPedro");
        caseData.setClaimantIndType(claimantIndType);
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Mike");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(respondentSumTypeItem)));
        RepresentedTypeR representedTypeR = RepresentedTypeR.builder()
                .nameOfRepresentative("Juan").build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setValue(representedTypeR);
        caseData.setRepCollection(new ArrayList<>(Collections.singletonList(representedTypeRItem)));
        caseData.setJurCodesCollection(getJurCodesCollection());
        submitEvent = new SubmitEvent();
        submitEvent.setCaseId(1111);
        submitEvent.setCaseData(caseData);
        submitEvent.setState("Submitted");
        searchTypeItem = new SearchTypeItem();
        searchTypeItem.setId("11111");
        submitBulkEvent = new SubmitBulkEvent();
        submitBulkEvent.setCaseId(1111);
        submitBulkEvent.setCaseData(bulkData);
        bulkUpdateService = new BulkUpdateService(ccdClient, userIdamService,
                createUpdatesBusSender, caseManagementLocationService, featureToggleService);
        bulkRequestPayload = new BulkRequestPayload();
        bulkRequestPayload.setBulkDetails(bulkDetails);
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
    }

    @Test
    void caseUpdateFieldsRequestException() throws IOException {
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), searchTypeItem.getId()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(ccdClient.startEventForCase(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(),
                anyString())).thenReturn(submitEvent);

        assertThrows(Exception.class, () -> bulkUpdateService.caseUpdateFieldsRequest(bulkRequest.getCaseDetails(),
                searchTypeItem, "authToken", submitBulkEvent)
        );
    }

    @Test
    void caseUpdateFieldsRequest() throws IOException {
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), searchTypeItem.getId())).thenReturn(submitEvent);
        when(ccdClient.startEventForCase(anyString(), anyString(), anyString(), anyString())).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString()))
                .thenReturn(submitEvent);

        assertDoesNotThrow(() -> bulkUpdateService.caseUpdateFieldsRequest(bulkRequest.getCaseDetails(), searchTypeItem,
            "authToken", submitBulkEvent));
    }

    @Test
    void caseUpdateFieldsWithNewValuesRequest() throws IOException {
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), searchTypeItem.getId())).thenReturn(submitEvent);
        when(ccdClient.startEventForCase(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(),
                anyString())).thenReturn(submitEvent);

        assertDoesNotThrow(() -> bulkUpdateService.caseUpdateFieldsRequest(getBulkDetailsWithValues(), searchTypeItem,
            "authToken", submitBulkEvent));
    }

    @Test
    void bulkUpdateLogic() throws IOException {
        SubmitBulkEvent submitBulkEvent = new SubmitBulkEvent();
        BulkData bulkData = new BulkData();
        bulkData.setMultipleReference("1111");
        submitBulkEvent.setCaseData(bulkData);
        List<SubmitBulkEvent> submitBulkEventList = new ArrayList<>(Collections.singletonList(submitBulkEvent));

        when(ccdClient.retrieveBulkCases("authToken", ENGLANDWALES_BULK_CASE_TYPE_ID,
                bulkDetails.getJurisdiction())).thenReturn(submitBulkEventList);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), searchTypeItem.getId())).thenReturn(submitEvent);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), null)).thenReturn(submitEvent);
        when(ccdClient.retrieveBulkCasesElasticSearch("authToken", ENGLANDWALES_BULK_CASE_TYPE_ID,
                bulkData.getMultipleReference())).thenReturn(submitBulkEventList);

        new BulkCasesPayload().setSubmitEvents(new ArrayList<>(Collections.singleton(submitEvent)));

        assertNotNull(bulkUpdateService.bulkUpdateLogic(getBulkDetailsCompleteWithValues(getBulkDetailsWithValues()),
                "authToken").getBulkDetails());
    }

    @Test
    void bulkUpdateLogicSingleCasesUpdated() throws IOException {
        List<SubmitBulkEvent> submitBulkEventList = new ArrayList<>(Collections.singletonList(submitBulkEvent));

        when(ccdClient.retrieveBulkCases("authToken", ENGLANDWALES_BULK_CASE_TYPE_ID,
            bulkDetails.getJurisdiction())).thenReturn(submitBulkEventList);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
            bulkDetails.getJurisdiction(), searchTypeItem.getId())).thenReturn(submitEvent);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
            bulkDetails.getJurisdiction(), "1234")).thenReturn(submitEvent);

        SubmitBulkEvent submitBulkEvent = new SubmitBulkEvent();
        BulkData bulkData = new BulkData();
        bulkData.setMultipleReference("1111");
        submitBulkEvent.setCaseData(bulkData);
        BulkDetails bulkDetails = getBulkDetailsCompleteWithValues(getBulkDetailsWithValues());
        bulkDetails.getCaseData().setMultipleReferenceV2(null);
        MultipleType multipleTypeLead = new MultipleType();
        multipleTypeLead.setEthosCaseReferenceM("11112");
        multipleTypeLead.setStateM(ACCEPTED_STATE);
        multipleTypeLead.setCaseIDM("1234");
        MultipleTypeItem multipleTypeItem = new MultipleTypeItem();
        multipleTypeItem.setId("1112");
        multipleTypeItem.setValue(multipleTypeLead);
        bulkDetails.getCaseData().getMultipleCollection().add(0, multipleTypeItem);

        new BulkCasesPayload().setSubmitEvents(new ArrayList<>(Collections.singleton(submitEvent)));

        assertNotNull(bulkUpdateService.bulkUpdateLogic(bulkDetails, "authToken").getBulkDetails());
    }

    @Test
    void bulkUpdateLogicException() throws IOException {
        SubmitBulkEvent submitBulkEvent = new SubmitBulkEvent();
        BulkData bulkData = new BulkData();
        bulkData.setMultipleReference("1111");
        submitBulkEvent.setCaseData(bulkData);
        List<SubmitBulkEvent> submitBulkEventList = new ArrayList<>(Collections.singletonList(submitBulkEvent));
        BulkCasesPayload bulkCasesPayload = new BulkCasesPayload();
        bulkCasesPayload.setSubmitEvents(new ArrayList<>(Collections.singleton(submitEvent)));
        when(ccdClient.retrieveBulkCases("authToken", ENGLANDWALES_BULK_CASE_TYPE_ID,
                bulkDetails.getJurisdiction())).thenReturn(submitBulkEventList);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), searchTypeItem.getId())).thenReturn(submitEvent);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), null)).thenReturn(submitEvent);
        when(ccdClient.retrieveBulkCasesElasticSearch("authToken", ENGLANDWALES_BULK_CASE_TYPE_ID,
                bulkData.getMultipleReference())).thenThrow(new InternalException(ERROR_MESSAGE));

        assertThrows(Exception.class, () ->
                bulkUpdateService.bulkUpdateLogic(
                        getBulkDetailsCompleteWithValues(getBulkDetailsWithValues()), "authToken")
        );
    }

    @Test
    void bulkUpdateLogicWithErrors() throws IOException {
        List<SubmitBulkEvent> submitBulkEventList = new ArrayList<>(Collections.singletonList(submitBulkEvent));
        when(ccdClient.retrieveBulkCases("authToken", ENGLANDWALES_BULK_CASE_TYPE_ID,
            bulkDetails.getJurisdiction())).thenReturn(submitBulkEventList);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
            bulkDetails.getJurisdiction(), searchTypeItem.getId())).thenReturn(submitEvent);

        BulkData bulkData = new BulkData();
        bulkData.setMultipleReference("1111");
        new SubmitBulkEvent().setCaseData(bulkData);

        assertThat(bulkUpdateService.bulkUpdateLogic(getBulkDetailsWithValues(), "authToken").getErrors())
            .isNotEmpty();
    }

    @Test
    void bulkUpdateLogicAsyncErrors() throws IOException {
        SubmitBulkEvent submitBulkEvent = new SubmitBulkEvent();
        BulkData bulkData = new BulkData();
        bulkData.setMultipleReference("1111");
        submitBulkEvent.setCaseData(bulkData);
        List<SubmitBulkEvent> submitBulkEventList = new ArrayList<>(Collections.singletonList(submitBulkEvent));
        BulkCasesPayload bulkCasesPayload = new BulkCasesPayload();
        bulkCasesPayload.setSubmitEvents(new ArrayList<>(Collections.singleton(submitEvent)));
        when(ccdClient.startEventForCase(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(ccdClient.retrieveBulkCases("authToken", ENGLANDWALES_BULK_CASE_TYPE_ID,
                bulkDetails.getJurisdiction())).thenReturn(submitBulkEventList);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), searchTypeItem.getId())).thenReturn(submitEvent);
        when(ccdClient.retrieveCase("authToken", ENGLANDWALES_CASE_TYPE_ID,
                bulkDetails.getJurisdiction(), null)).thenReturn(submitEvent);
        assertEquals("[Multiple reference does not exist or it is the same as the current multiple case]",
                bulkUpdateService.bulkUpdateLogic(getBulkDetailsCompleteWithValues(getBulkDetailsWithValues()),
                        "authToken").getErrors().toString());
    }

    @Test
    void clearUpFields() {
        BulkData bulkData = bulkUpdateService.clearUpFields(bulkRequestPayload).getBulkDetails().getCaseData();
        assertNull(bulkData.getClaimantRepV2());
        assertNull(bulkData.getClerkResponsibleV2());
        assertNull(bulkData.getMultipleReferenceV2());
        assertNull(bulkData.getFileLocationV2());
        assertNull(bulkData.getFeeGroupReferenceV2());
        assertNull(bulkData.getFlag1Update());
        assertNull(bulkData.getFlag2Update());
        assertNull(bulkData.getEqpUpdate());
    }

    private BulkDetails getBulkDetailsWithValues() {
        BulkData bulkData = new BulkData();
        bulkData.setFileLocationV2(new DynamicFixedListType("Glasgow"));
        bulkData.setRespondentSurnameV2("Respondent");
        bulkData.setClaimantSurnameV2("Claimant");
        bulkData.setMultipleReferenceV2("1111");
        bulkData.setClerkResponsibleV2(new DynamicFixedListType("Juan"));
        bulkData.setPositionTypeV2("Awaiting");
        bulkData.setClaimantRepV2("ClaimantRep");
        bulkData.setRespondentRepV2("RespondentRep");
        bulkData.setFlag1Update("Flag1");
        bulkData.setFlag2Update("Flag2");
        bulkData.setEqpUpdate("EQP");
        bulkData.setFileLocationAberdeen(new DynamicFixedListType("Aberdeen"));
        bulkData.setFileLocationDundee(new DynamicFixedListType("Dundee"));
        bulkData.setFileLocationEdinburgh(new DynamicFixedListType("Edinburgh"));
        bulkData.setFileLocationGlasgow(new DynamicFixedListType("Glasgow"));
        bulkData.setJurCodesDynamicList(getJurCodesDynamicList());
        bulkData.setOutcomeUpdate("OutcomeNew");
        bulkData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        BulkDetails bulkDetails = new BulkDetails();
        bulkDetails.setCaseData(bulkData);
        bulkDetails.setJurisdiction("TRIBUNALS");
        bulkDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        return bulkDetails;
    }

    private DynamicFixedListType getJurCodesDynamicList() {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel("COM");
        dynamicValueType.setCode("COM");
        dynamicFixedListType.setValue(dynamicValueType);
        List<DynamicValueType> listItems = new ArrayList<>(Collections.singleton(dynamicValueType));
        dynamicFixedListType.setListItems(listItems);
        return dynamicFixedListType;
    }

    private List<JurCodesTypeItem> getJurCodesCollection() {
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("COM");
        jurCodesTypeItem.setId("COM");
        jurCodesTypeItem.setValue(jurCodesType);
        return new ArrayList<>(Collections.singleton(jurCodesTypeItem));
    }

    private BulkDetails getBulkDetailsCompleteWithValues(BulkDetails bulkDetails) {
        SearchTypeItem searchTypeItem1 = new SearchTypeItem();
        SearchType searchType = new SearchType();
        searchType.setEthosCaseReferenceS("22222");
        searchTypeItem1.setId("11111");
        searchTypeItem1.setValue(searchType);
        BulkData bulkData = bulkDetails.getCaseData();
        bulkData.setSearchCollection(new ArrayList<>(Collections.singletonList(searchTypeItem1)));
        CaseType caseType = new CaseType();
        caseType.setEthosCaseReference("2221");
        CaseIdTypeItem caseIdTypeItem = new CaseIdTypeItem();
        caseIdTypeItem.setId("2221");
        caseIdTypeItem.setValue(caseType);
        bulkData.setCaseIdCollection(new ArrayList<>(Collections.singletonList(caseIdTypeItem)));
        MultipleType multipleType = new MultipleType();
        multipleType.setSubMultipleM("12");
        multipleType.setEthosCaseReferenceM("11111");
        multipleType.setStateM(SUBMITTED_STATE);
        MultipleTypeItem multipleTypeItem = new MultipleTypeItem();
        multipleTypeItem.setId("1111");
        multipleTypeItem.setValue(multipleType);
        bulkData.setMultipleCollection(new ArrayList<>(Collections.singleton(multipleTypeItem)));
        bulkDetails.setCaseData(bulkData);
        return bulkDetails;
    }

    @Test
    void bulkPreAcceptLogicEmptyCases() {
        List<String> errors = bulkUpdateService.bulkPreAcceptLogic(
                bulkRequest.getCaseDetails(), new ArrayList<>(), "authToken", false).getErrors();
        assertEquals("[No cases on the multiple case: 2300001/2019]", errors.toString());
    }

    @Test
    void bulkPreAcceptLogic() {
        List<SubmitEvent> submitEvents = new ArrayList<>(Collections.singleton(submitEvent));
        bulkRequest.setCaseDetails(getBulkDetailsCompleteWithValues(bulkRequest.getCaseDetails()));
        BulkRequestPayload bulkRequestPayload = bulkUpdateService.bulkPreAcceptLogic(
                bulkRequest.getCaseDetails(), submitEvents, "authToken", false);
        String multipleCollection = "[MultipleTypeItem(id=1111, value=MultipleType("
                + "caseIDM=null, ethosCaseReferenceM=11111, leadClaimantM=null, "
                + "multipleReferenceM=null, clerkRespM=null, claimantSurnameM=null, respondentSurnameM=null, "
                + "claimantRepM=null, respondentRepM=null, " + "fileLocM=null, receiptDateM=null, positionTypeM=null,"
                + " feeGroupReferenceM=null, jurCodesCollectionM=null, stateM=Accepted, " + "subMultipleM=12, "
                + "subMultipleTitleM=null, currentPositionM=null, claimantAddressLine1M=null, "
                + "claimantPostCodeM=null, "
                + "respondentAddressLine1M=null, respondentPostCodeM=null, "
                + "flag1M=null, flag2M=null, eqpm=null,"
                + " respondentRepOrgM=null, claimantRepOrgM=null))]";
        assertEquals(multipleCollection,
                bulkRequestPayload.getBulkDetails().getCaseData().getMultipleCollection().toString());
    }

    @Test
    void bulkPreAcceptPQLogic() {
        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        List<SubmitEvent> submitEvents = new ArrayList<>(Collections.singleton(submitEvent));
        bulkRequest.setCaseDetails(getBulkDetailsCompleteWithValues(bulkRequest.getCaseDetails()));
        BulkRequestPayload bulkRequestPayload = bulkUpdateService.bulkPreAcceptLogic(
                bulkRequest.getCaseDetails(), submitEvents, "authToken", true);
        String multipleCollection = "[MultipleTypeItem(id=1111, value=MultipleType(caseIDM=null, "
                + "ethosCaseReferenceM=11111, leadClaimantM=null, " + "multipleReferenceM=null, clerkRespM=null,"
                + " claimantSurnameM=null, respondentSurnameM=null, claimantRepM=null, respondentRepM=null, "
                + "fileLocM=null, receiptDateM=null, positionTypeM=null, feeGroupReferenceM=null, "
                + "jurCodesCollectionM=null,"
                + " stateM=Accepted, " + "subMultipleM=12, subMultipleTitleM=null, currentPositionM=null, "
                + "claimantAddressLine1M=null, claimantPostCodeM=null, " + "respondentAddressLine1M=null, "
                + "respondentPostCodeM=null, flag1M=null, flag2M=null, eqpm=null, respondentRepOrgM=null, "
                + "claimantRepOrgM=null))]";
        assertEquals(multipleCollection, bulkRequestPayload.getBulkDetails().getCaseData()
                .getMultipleCollection().toString());
    }

    @Test
    void bulkPreAcceptPQLogicEmptyCases() {
        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        List<SubmitEvent> submitEvents = new ArrayList<>(Collections.singleton(submitEvent));
        bulkRequest.setCaseDetails(getBulkDetailsCompleteWithValues(bulkRequest.getCaseDetails()));
        bulkRequest.getCaseDetails().getCaseData().setCaseIdCollection(new ArrayList<>());
        BulkRequestPayload bulkRequestPayload = bulkUpdateService.bulkPreAcceptLogic(
                bulkRequest.getCaseDetails(), submitEvents, "authToken", true);
        String multipleCollection = "[MultipleTypeItem(id=1111, value=MultipleType(caseIDM=null, "
                + "ethosCaseReferenceM=11111, leadClaimantM=null, " + "multipleReferenceM=null,"
                + " clerkRespM=null, "
                + "claimantSurnameM=null, respondentSurnameM=null, claimantRepM=null, respondentRepM=null, "
                + "fileLocM=null, receiptDateM=null, positionTypeM=null, feeGroupReferenceM=null, "
                + "jurCodesCollectionM=null,"
                + " stateM=Accepted, " + "subMultipleM=12, subMultipleTitleM=null, currentPositionM=null, "
                + "claimantAddressLine1M=null, claimantPostCodeM=null, " + "respondentAddressLine1M=null, "
                + "respondentPostCodeM=null, flag1M=null, flag2M=null, eqpm=null, "
                + "respondentRepOrgM=null, claimantRepOrgM=null))]";
        assertEquals(multipleCollection,
                bulkRequestPayload.getBulkDetails().getCaseData().getMultipleCollection().toString());
    }

}