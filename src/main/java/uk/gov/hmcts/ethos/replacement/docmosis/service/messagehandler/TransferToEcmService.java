package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.TransferToEcmDataModel;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.List;

/**
 * Service for transferring cases to ECM (Employment Case Management).
 */
@Slf4j
@Service
public class TransferToEcmService {
    
    private final CcdClient ccdClient;
    private final AdminUserService adminUserService;
    private final CreateEcmSingleService createEcmSingleService;

    @Autowired
    public TransferToEcmService(CcdClient ccdClient, AdminUserService adminUserService,
                                CreateEcmSingleService createEcmSingleService) {
        this.ccdClient = ccdClient;
        this.adminUserService = adminUserService;
        this.createEcmSingleService = createEcmSingleService;
    }

    public void transferToEcm(CreateUpdatesMsg createUpdatesMsg) throws IOException {
        if (createUpdatesMsg.getDataModelParent() instanceof TransferToEcmDataModel) {
            log.info("Searching for cases {} to transfer to ECM", createUpdatesMsg.getEthosCaseRefCollection());
        } else {
            log.warn("Invalid model state for messageID {}", createUpdatesMsg.getMsgId());
            return;
        }

        String accessToken = adminUserService.getAdminUserToken();

        List<SubmitEvent> submitEvents = ccdClient.retrieveCasesElasticSearch(
            accessToken, createUpdatesMsg.getCaseTypeId(), createUpdatesMsg.getEthosCaseRefCollection());

        if (submitEvents.isEmpty()) {
            log.warn("No cases found for messageID {} and case references {}", createUpdatesMsg.getMsgId(),
                     createUpdatesMsg.getEthosCaseRefCollection());
        } else {
            log.info("Transferring cases {} to ECM", createUpdatesMsg.getEthosCaseRefCollection());
            createEcmSingleService.sendCreation(submitEvents.getFirst(), accessToken, createUpdatesMsg);
        }
    }
}
