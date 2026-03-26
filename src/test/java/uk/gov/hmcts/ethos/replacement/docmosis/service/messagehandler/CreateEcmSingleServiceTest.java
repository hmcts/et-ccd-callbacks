package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MessageHandlerTestHelper;
import java.io.IOException;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;

@ExtendWith(SpringExtension.class)
class CreateEcmSingleServiceTest {
    @Mock
    private CcdClient ccdClient;
    @InjectMocks
    private CreateEcmSingleService createEcmSingleService;

    private static final String TEST_AUTH_TOKEN = "test auth token";

    @Test
    @SuppressWarnings({"PMD.LawOfDemeter"})
    void transferToEcm() throws IOException {
        ReflectionTestUtils.setField(createEcmSingleService, "ccdGatewayBaseUrl", "http://ccd-gateway");
        
        String ethosCaseReference = "4150001/2020";
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference(ethosCaseReference);
        caseData.setManagingOffice(managingOffice);
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setState(ACCEPTED_STATE);
        submitEvent.setCaseData(caseData);
        var caseDetails = new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId("Leeds");
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);

        var returnedEcmCcdRequest = new uk.gov.hmcts.ecm.common.model.ccd.CCDRequest();
        var ecmCaseDetails = new CaseDetails();
        returnedEcmCcdRequest.setCaseDetails(ecmCaseDetails);
        when(ccdClient.startEcmCaseCreationTransfer(eq(TEST_AUTH_TOKEN),
                                                    any(CaseDetails.class)))
            .thenReturn(returnedEcmCcdRequest);

        var ecmCaseData = new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        ecmCaseData.setEthosCaseReference(ethosCaseReference);
        ecmCaseData.setManagingOffice(managingOffice);
        ecmCaseData.setEthosCaseReference("18850001/2020");
        var ecmSubmitEvent = new uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent();
        ecmSubmitEvent.setCaseData(ecmCaseData);
        when(ccdClient.submitEcmCaseCreation(eq(TEST_AUTH_TOKEN),
                                             any(CaseDetails.class),
                                             any(uk.gov.hmcts.ecm.common.model.ccd.CCDRequest.class)))
            .thenReturn(ecmSubmitEvent);
        when(ccdClient.startEventForCase(eq(TEST_AUTH_TOKEN), any(), any(), any())).thenReturn(ccdRequest);

        CreateUpdatesMsg createUpdateMsg = MessageHandlerTestHelper.transferToEcmMessage();

        createEcmSingleService.sendCreation(submitEvent, TEST_AUTH_TOKEN, createUpdateMsg);

        verify(ccdClient, times(1)).startEcmCaseCreationTransfer(eq(TEST_AUTH_TOKEN),
                                          any(CaseDetails.class));
        verify(ccdClient, times(1)).startEventForCase(eq(TEST_AUTH_TOKEN), any(), any(), any());
        verify(ccdClient, times(1)).submitEventForCase(eq(TEST_AUTH_TOKEN), any(), any(), any(),
                                                       any(), any());
    }

    @Test
    @SuppressWarnings({"PMD.LawOfDemeter"})
    void transferToEcmForOfficeNameWithWhiteSpace() throws IOException {
        ReflectionTestUtils.setField(createEcmSingleService, "ccdGatewayBaseUrl", "http://ccd-gateway");
        
        String ethosCaseReference = "3600001/2021";
        String managingOffice = TribunalOffice.LONDON_EAST.getOfficeName();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference(ethosCaseReference);
        caseData.setManagingOffice(managingOffice);
        caseData.setDocumentCollection(new ArrayList<>());
        caseData.setAddressLabelCollection(new ArrayList<>());
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        CreateUpdatesMsg createUpdateMsg = MessageHandlerTestHelper.transferToEcmMessageForLondonEast();
        createEcmSingleService.sendCreation(submitEvent, TEST_AUTH_TOKEN, createUpdateMsg);

        var ecmCaseDetails = new CaseDetails();
        ecmCaseDetails.setCaseTypeId(managingOffice.replace(" ", ""));
        var ecmCaseData = new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        ecmCaseDetails.setCaseData(ecmCaseData);

        ArgumentCaptor<CaseDetails> ccdRequestCaptor = ArgumentCaptor.forClass(CaseDetails.class);
        verify(ccdClient, times(1))
            .submitEcmCaseCreation(eq(TEST_AUTH_TOKEN), ccdRequestCaptor.capture(), any());
        assertEquals(ecmCaseDetails.getCaseTypeId(), ccdRequestCaptor.getValue().getCaseTypeId());
    }

    @Test
    @SuppressWarnings({"PMD.LawOfDemeter"})
    void transferToEcmShouldClearOfficeSpecificFields() throws IOException {
        ReflectionTestUtils.setField(createEcmSingleService, "ccdGatewayBaseUrl", "http://ccd-gateway");

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("4150001/2020");
        caseData.setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        caseData.setFileLocation(DynamicFixedListType.of(DynamicValueType.create("Case Work Team",
                                                                                  "Case Work Team")));
        caseData.setClerkResponsible(DynamicFixedListType.of(DynamicValueType.create("Case Work Team",
                                                                                      "Case Work Team")));
        caseData.setFileLocationGlasgow(DynamicFixedListType.of(DynamicValueType.create("Glasgow Team",
                                                                                         "Glasgow Team")));
        caseData.setFileLocationAberdeen(DynamicFixedListType.of(DynamicValueType.create("Aberdeen Team",
                                                                                          "Aberdeen Team")));
        caseData.setFileLocationDundee(DynamicFixedListType.of(DynamicValueType.create("Dundee Team",
                                                                                        "Dundee Team")));
        caseData.setFileLocationEdinburgh(DynamicFixedListType.of(DynamicValueType.create("Edinburgh Team",
                                                                                           "Edinburgh Team")));

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setState(ACCEPTED_STATE);
        submitEvent.setCaseData(caseData);

        var returnedEcmCcdRequest = new uk.gov.hmcts.ecm.common.model.ccd.CCDRequest();
        returnedEcmCcdRequest.setCaseDetails(new CaseDetails());
        when(ccdClient.startEcmCaseCreationTransfer(eq(TEST_AUTH_TOKEN), any(CaseDetails.class)))
            .thenReturn(returnedEcmCcdRequest);

        var ecmCaseData = new uk.gov.hmcts.ecm.common.model.ccd.CaseData();
        ecmCaseData.setEthosCaseReference("18850001/2020");
        var ecmSubmitEvent = new uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent();
        ecmSubmitEvent.setCaseData(ecmCaseData);
        when(ccdClient.submitEcmCaseCreation(eq(TEST_AUTH_TOKEN),
            any(CaseDetails.class), any(uk.gov.hmcts.ecm.common.model.ccd.CCDRequest.class)))
            .thenReturn(ecmSubmitEvent);

        var ccdRequest = new CCDRequest();
        var requestCaseDetails = new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        requestCaseDetails.setCaseData(new uk.gov.hmcts.et.common.model.ccd.CaseData());
        ccdRequest.setCaseDetails(requestCaseDetails);
        when(ccdClient.startEventForCase(eq(TEST_AUTH_TOKEN), any(), any(), any())).thenReturn(ccdRequest);

        createEcmSingleService.sendCreation(
            submitEvent,
            TEST_AUTH_TOKEN,
            MessageHandlerTestHelper.transferToEcmMessage()
        );

        ArgumentCaptor<CaseDetails> detailsCaptor = ArgumentCaptor.forClass(CaseDetails.class);
        verify(ccdClient).submitEcmCaseCreation(eq(TEST_AUTH_TOKEN), detailsCaptor.capture(), any());
        uk.gov.hmcts.ecm.common.model.ccd.CaseData sentCaseData = detailsCaptor.getValue().getCaseData();
        assertNull(sentCaseData.getClerkResponsible());
        assertNull(sentCaseData.getFileLocation());
        assertNull(sentCaseData.getFileLocationGlasgow());
        assertNull(sentCaseData.getFileLocationAberdeen());
        assertNull(sentCaseData.getFileLocationDundee());
        assertNull(sentCaseData.getFileLocationEdinburgh());
    }
}
