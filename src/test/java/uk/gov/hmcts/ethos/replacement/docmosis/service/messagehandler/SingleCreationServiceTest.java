package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MessageHandlerTestHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler.SingleCreationService.CREATE_CASE_EVENT_SUMMARY_TEMPLATE;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SingleCreationServiceTest {
    @InjectMocks
    private SingleCreationService singleCreationService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private FeatureToggleService featureToggleService;
    @Captor
    ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;
    @Captor
    ArgumentCaptor<CaseData> caseDataArgumentCaptor;
    private static final String USER_TOKEN = "accessToken";

    @Test
    @SuppressWarnings({"PMD.LawOfDemeter"})
    void caseTransferToScotlandCreateCase() throws IOException {
        var ethosCaseReference = "4150002/2020";
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var caseData = new CaseData();
        caseData.setEthosCaseReference(ethosCaseReference);
        caseData.setManagingOffice(managingOffice);
        caseData.setAllPartyFlags(createAllPartyFlags());
        var submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        CaseData newCaseData = new CaseData();
        SubmitEvent newCaseSubmitEvent = new SubmitEvent();
        newCaseSubmitEvent.setCaseData(newCaseData);

        CaseDetails returnedCaseDetails = new CaseDetails();
        returnedCaseDetails.setCaseData(newCaseData);
        CCDRequest returnedCCDRequest = new CCDRequest();
        returnedCCDRequest.setCaseDetails(returnedCaseDetails);
        when(ccdClient.startEventForCase(eq(USER_TOKEN), any(), any(), any(), eq("claimantTransferredCaseAccess")))
            .thenReturn(returnedCCDRequest);

        UpdateCaseMsg updateCaseMsg = MessageHandlerTestHelper.generateCreationSingleCaseMsg();
        ((CreationSingleDataModel)updateCaseMsg.getDataModelParent()).setOfficeCT(
            TribunalOffice.GLASGOW.getOfficeName());
        when(ccdClient.submitCaseCreation(eq(USER_TOKEN), any(), any(), any()))
            .thenReturn(newCaseSubmitEvent);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        CCDRequest updateCCDRequest = new CCDRequest();
        updateCCDRequest.setCaseDetails(caseDetails);
        when(ccdClient.startEventForCase(eq(USER_TOKEN), any(), any(), any()))
            .thenReturn(updateCCDRequest);
        when(ccdClient.retrieveCasesElasticSearch(eq(USER_TOKEN), any(), any()))
            .thenReturn(new ArrayList<>());
        when(ccdClient.startCaseCreationTransfer(eq(USER_TOKEN), any()))
            .thenReturn(updateCCDRequest);
        when(featureToggleService.isCaseFlagsV2Enabled(SCOTLAND_CASE_TYPE_ID)).thenReturn(false);

        singleCreationService.sendCreation(submitEvent, USER_TOKEN, updateCaseMsg);

        verify(ccdClient, times(1))
            .retrieveCasesElasticSearch(USER_TOKEN, SCOTLAND_CASE_TYPE_ID, List.of(ethosCaseReference));
        verify(ccdClient, times(1)).startCaseCreationTransfer(eq(USER_TOKEN), caseDetailsArgumentCaptor.capture());
        assertNull(caseDetailsArgumentCaptor.getValue().getCaseData().getAllPartyFlags());
        var expectedEventSummary = String.format(CREATE_CASE_EVENT_SUMMARY_TEMPLATE, managingOffice);
        verify(ccdClient, times(1)).submitCaseCreation(eq(USER_TOKEN), any(), any(),
                                                       eq(expectedEventSummary));
        verify(ccdClient, times(1)).startEventForCase(eq(USER_TOKEN), any(), any(), any());
        verify(ccdClient, times(1)).startEventForCase(eq(USER_TOKEN), any(), any(), any(), eq(
            "claimantTransferredCaseAccess"));

        verify(ccdClient, times(2)).submitEventForCase(eq(USER_TOKEN), any(), any(), any(),
                                                       any(), any());
        verify(ccdClient, times(0)).returnCaseCreationTransfer(eq(USER_TOKEN), any(), any(),
                                                               any());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    @SuppressWarnings({"PMD.LawOfDemeter"})
    void caseTransferToEnglandCreateCase() throws IOException {
        String ethosCaseReference = "4150002/2020";
        String managingOffice = TribunalOffice.DUNDEE.getOfficeName();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference(ethosCaseReference);
        caseData.setManagingOffice(managingOffice);
        caseData.setFeeGroupReference("1234123412341234");
        AllPartyFlags allPartyFlags = createAllPartyFlags();
        caseData.setAllPartyFlags(allPartyFlags);
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);

        UpdateCaseMsg updateCaseMsg = MessageHandlerTestHelper.generateCreationSingleCaseMsg();
        ((CreationSingleDataModel)updateCaseMsg.getDataModelParent()).setOfficeCT(
            TribunalOffice.NEWCASTLE.getOfficeName());
        when(featureToggleService.isCaseFlagsV2Enabled(ENGLANDWALES_CASE_TYPE_ID)).thenReturn(true);

        singleCreationService.sendCreation(submitEvent, USER_TOKEN, updateCaseMsg);

        verify(ccdClient).retrieveCasesElasticSearch(USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID,
                                                     List.of(ethosCaseReference));
        verify(ccdClient).startCaseCreationTransfer(eq(USER_TOKEN), caseDetailsArgumentCaptor.capture());
        assertNull(caseDetailsArgumentCaptor.getValue().getCaseData().getFeeGroupReference());
        assertSame(allPartyFlags, caseDetailsArgumentCaptor.getValue().getCaseData().getAllPartyFlags());
        var expectedEventSummary = String.format(CREATE_CASE_EVENT_SUMMARY_TEMPLATE, managingOffice);
        verify(ccdClient).submitCaseCreation(eq(USER_TOKEN), any(), any(), eq(expectedEventSummary));
        verify(ccdClient, times(0)).returnCaseCreationTransfer(eq(USER_TOKEN), any(), any(),
                                                               any());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    @SuppressWarnings({"PMD.LawOfDemeter"})
    void caseTransferToScotlandUpdateExisting() throws IOException {
        var ethosCaseReference = "4150002/2020";
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var caseData = new CaseData();
        caseData.setEthosCaseReference(ethosCaseReference);
        caseData.setManagingOffice(managingOffice);
        AllPartyFlags allPartyFlags = createAllPartyFlags();
        caseData.setAllPartyFlags(allPartyFlags);
        var submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        var caseId = 100;
        submitEvent.setCaseId(caseId);

        var updateCaseMsg = MessageHandlerTestHelper.generateCreationSingleCaseMsg();
        ((CreationSingleDataModel)updateCaseMsg.getDataModelParent()).setOfficeCT(
            TribunalOffice.GLASGOW.getOfficeName());
        when(featureToggleService.isCaseFlagsV2Enabled(SCOTLAND_CASE_TYPE_ID)).thenReturn(true);

        when(ccdClient.retrieveCasesElasticSearch(USER_TOKEN, SCOTLAND_CASE_TYPE_ID, List.of(ethosCaseReference)))
            .thenReturn(new ArrayList<>(Collections.singletonList(submitEvent)));

        singleCreationService.sendCreation(submitEvent, USER_TOKEN, updateCaseMsg);

        verify(ccdClient).retrieveCasesElasticSearch(USER_TOKEN, SCOTLAND_CASE_TYPE_ID, List.of(ethosCaseReference));
        verify(ccdClient).returnCaseCreationTransfer(USER_TOKEN, SCOTLAND_CASE_TYPE_ID, "EMPLOYMENT",
                                                     String.valueOf(caseId));
        verify(ccdClient).submitEventForCase(eq(USER_TOKEN), caseDataArgumentCaptor.capture(),
                                             eq(SCOTLAND_CASE_TYPE_ID), eq("EMPLOYMENT"), any(),
                                             eq(String.valueOf(caseId)));
        assertSame(allPartyFlags, caseDataArgumentCaptor.getValue().getAllPartyFlags());
        verify(ccdClient, times(0)).startCaseCreationTransfer(eq(USER_TOKEN), any());
        verifyNoMoreInteractions(ccdClient);
    }

    private AllPartyFlags createAllPartyFlags() {
        return AllPartyFlags.builder()
                .caseFlags(createCaseFlags("Case flags"))
                .claimantFlags(createCaseFlags("Claimant flags"))
                .claimantExternalFlags(createCaseFlags("Claimant external flags"))
                .respondentFlags(createCaseFlags("Respondent flags"))
                .respondentExternalFlags(createCaseFlags("Respondent external flags"))
                .respondent1Flags(createCaseFlags("Respondent 1 flags"))
                .respondent1ExternalFlags(createCaseFlags("Respondent 1 external flags"))
                .respondent2Flags(createCaseFlags("Respondent 2 flags"))
                .respondent2ExternalFlags(createCaseFlags("Respondent 2 external flags"))
                .respondent3Flags(createCaseFlags("Respondent 3 flags"))
                .respondent3ExternalFlags(createCaseFlags("Respondent 3 external flags"))
                .respondent4Flags(createCaseFlags("Respondent 4 flags"))
                .respondent4ExternalFlags(createCaseFlags("Respondent 4 external flags"))
                .respondent5Flags(createCaseFlags("Respondent 5 flags"))
                .respondent5ExternalFlags(createCaseFlags("Respondent 5 external flags"))
                .respondent6Flags(createCaseFlags("Respondent 6 flags"))
                .respondent6ExternalFlags(createCaseFlags("Respondent 6 external flags"))
                .respondent7Flags(createCaseFlags("Respondent 7 flags"))
                .respondent7ExternalFlags(createCaseFlags("Respondent 7 external flags"))
                .respondent8Flags(createCaseFlags("Respondent 8 flags"))
                .respondent8ExternalFlags(createCaseFlags("Respondent 8 external flags"))
                .respondent9Flags(createCaseFlags("Respondent 9 flags"))
                .respondent9ExternalFlags(createCaseFlags("Respondent 9 external flags"))
                .claimantRepresentativeFlags(createCaseFlags("Claimant representative flags"))
                .claimantRepresentativeExternalFlags(createCaseFlags("Claimant representative external flags"))
                .representativeFlags(createCaseFlags("Representative flags"))
                .representativeExternalFlags(createCaseFlags("Representative external flags"))
                .representative1Flags(createCaseFlags("Representative 1 flags"))
                .representative1ExternalFlags(createCaseFlags("Representative 1 external flags"))
                .representative2Flags(createCaseFlags("Representative 2 flags"))
                .representative2ExternalFlags(createCaseFlags("Representative 2 external flags"))
                .representative3Flags(createCaseFlags("Representative 3 flags"))
                .representative3ExternalFlags(createCaseFlags("Representative 3 external flags"))
                .representative4Flags(createCaseFlags("Representative 4 flags"))
                .representative4ExternalFlags(createCaseFlags("Representative 4 external flags"))
                .representative5Flags(createCaseFlags("Representative 5 flags"))
                .representative5ExternalFlags(createCaseFlags("Representative 5 external flags"))
                .representative6Flags(createCaseFlags("Representative 6 flags"))
                .representative6ExternalFlags(createCaseFlags("Representative 6 external flags"))
                .representative7Flags(createCaseFlags("Representative 7 flags"))
                .representative7ExternalFlags(createCaseFlags("Representative 7 external flags"))
                .representative8Flags(createCaseFlags("Representative 8 flags"))
                .representative8ExternalFlags(createCaseFlags("Representative 8 external flags"))
                .representative9Flags(createCaseFlags("Representative 9 flags"))
                .representative9ExternalFlags(createCaseFlags("Representative 9 external flags"))
                .build();
    }

    private CaseFlagsType createCaseFlags(String sectionName) {
        FlagDetailType flagDetail = FlagDetailType.builder()
                .name(sectionName + " detail")
                .flagCode("CF-001")
                .status("Active")
                .build();
        return CaseFlagsType.builder()
                .partyName(sectionName + " party")
                .roleOnCase(sectionName + " role")
                .groupId(sectionName + " group")
                .visibility(sectionName.contains("external") ? "External" : "Internal")
                .details(ListTypeItem.from(flagDetail))
                .build();
    }

}
