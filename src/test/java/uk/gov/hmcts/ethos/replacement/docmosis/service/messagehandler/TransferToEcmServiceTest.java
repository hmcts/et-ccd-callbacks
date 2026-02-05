package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.TransferToEcmDataModel;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TransferToEcmServiceTest {
    @Mock
    private CcdClient ccdClient;

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private TransferToEcmService transferToEcmService;

    @Mock
    private CreateEcmSingleService createEcmSingleService;

    private static final String USER_TOKEN = "Bearer some-auth-token";
    private CreateUpdatesMsg createUpdatesMsg;
    private List<SubmitEvent> submitEventList;

    @BeforeEach
    @SuppressWarnings({"PMD.LawOfDemeter"})
    public void setUp() {
        createUpdatesMsg = generateTransferToEcmMessage();
        var caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        caseData.setEthosCaseReference("6000000/2022");
        var submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEventList = List.of(submitEvent);
    }

    @Test
    void transferToEcm() throws IOException {
        when(adminUserService.getAdminUserToken())
            .thenReturn(USER_TOKEN);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList()))
            .thenReturn(submitEventList);

        transferToEcmService.transferToEcm(createUpdatesMsg);
        
        verify(ccdClient, times(1))
            .retrieveCasesElasticSearch(USER_TOKEN, createUpdatesMsg.getCaseTypeId(),
                                        createUpdatesMsg.getEthosCaseRefCollection());
        verify(createEcmSingleService, times(1))
            .sendCreation(submitEventList.get(0), USER_TOKEN, createUpdatesMsg);
    }

    @Test
    void transferToEcm_noCasesFound() throws IOException {
        when(adminUserService.getAdminUserToken())
            .thenReturn(USER_TOKEN);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList()))
            .thenReturn(Collections.emptyList());

        transferToEcmService.transferToEcm(createUpdatesMsg);
        
        verify(ccdClient, times(1))
            .retrieveCasesElasticSearch(eq(USER_TOKEN), eq(createUpdatesMsg.getCaseTypeId()),
                                        eq(createUpdatesMsg.getEthosCaseRefCollection()));
        verify(createEcmSingleService, never())
            .sendCreation(any(SubmitEvent.class), anyString(), eq(createUpdatesMsg));
    }

    @Test
    void transferToEcm_invalidDataModel() throws IOException {
        createUpdatesMsg.setDataModelParent(null); // Not a TransferToEcmDataModel

        transferToEcmService.transferToEcm(createUpdatesMsg);
        
        verify(ccdClient, never())
            .retrieveCasesElasticSearch(anyString(), anyString(), anyList());
        verify(createEcmSingleService, never())
            .sendCreation(any(SubmitEvent.class), anyString(), eq(createUpdatesMsg));
    }

    private CreateUpdatesMsg generateTransferToEcmMessage() {
        CreateUpdatesMsg msg = new CreateUpdatesMsg();
        msg.setMsgId(UUID.randomUUID().toString());
        msg.setJurisdiction("EMPLOYMENT");
        msg.setCaseTypeId("ET_Scotland");
        msg.setMultipleRef("6000001");
        msg.setEthosCaseRefCollection(List.of("6000000/2022"));
        msg.setTotalCases("1");
        msg.setUsername("test@test.com");
        msg.setDataModelParent(TransferToEcmDataModel.builder().build());
        return msg;
    }
}
