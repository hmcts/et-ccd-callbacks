package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.TransferToEcmDataModel;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

import java.io.IOException;

/**
 * Service for creating ECM cases during transfer.
 * Migrated from et-message-handler.
 * Note: This is a simplified version - full TransferToEcmCaseDataHelper functionality to be migrated as needed.
 */
@Slf4j
@Service
public class CreateEcmSingleService {

    private final CcdClient ccdClient;
    
    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;
    
    private final String officeName = TribunalOffice.SCOTLAND.getOfficeName();

    @Autowired
    public CreateEcmSingleService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    public void sendCreation(SubmitEvent oldSubmitEvent, String accessToken, CreateUpdatesMsg createUpdatesMsg)
        throws IOException {
        transferNewCase(oldSubmitEvent, createUpdatesMsg, accessToken);
    }

    private void transferNewCase(SubmitEvent oldSubmitEvent, CreateUpdatesMsg createUpdatesMsg, String accessToken)
        throws IOException {
        CaseDetails newEcmCaseDetailsCt = createCaseDetailsCaseTransfer(oldSubmitEvent, createUpdatesMsg);

        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest returnedEcmCcdRequest =
            ccdClient.startEcmCaseCreationTransfer(accessToken, newEcmCaseDetailsCt);

        TransferToEcmDataModel transferToEcmDataModel = (TransferToEcmDataModel) createUpdatesMsg.getDataModelParent();
        String officeCT = transferToEcmDataModel.getOfficeCT();
        String caseId = String.valueOf(oldSubmitEvent.getCaseId());
        log.info("Creating case in {} for ET case {}", officeCT, caseId);
        
        uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent newCase = ccdClient.submitEcmCaseCreation(accessToken,
                                                                                                newEcmCaseDetailsCt,
                                                                                                 returnedEcmCcdRequest);
        if (newCase != null) {
            String transferredCaseLink = getTransferredCaseLink(
                ccdGatewayBaseUrl,
                String.valueOf(newCase.getCaseId()),
                newCase.getCaseData().getEthosCaseReference());
            
            uk.gov.hmcts.et.common.model.ccd.CCDRequest updateCCDRequest =
                ccdClient.startEventForCase(accessToken, transferToEcmDataModel.getOfficeCT(),
                                            returnedEcmCcdRequest.getCaseDetails().getJurisdiction(), caseId);
            updateCCDRequest.getCaseDetails().getCaseData().setTransferredCaseLink(transferredCaseLink);
            ccdClient.submitEventForCase(accessToken, updateCCDRequest.getCaseDetails().getCaseData(),
                                         transferToEcmDataModel.getOfficeCT(),
                                         returnedEcmCcdRequest.getCaseDetails().getJurisdiction(),
                                         updateCCDRequest, caseId);
        }
    }

    private CaseDetails createCaseDetailsCaseTransfer(SubmitEvent oldSubmitEvent, CreateUpdatesMsg createUpdatesMsg) {
        TransferToEcmDataModel transferToEcmDataModel = (TransferToEcmDataModel) createUpdatesMsg.getDataModelParent();
        String originalCaseTypeId = createUpdatesMsg.getCaseTypeId();
        CaseData newEcmCaseData = generateNewCaseDataForCaseTransfer(oldSubmitEvent, originalCaseTypeId);
        newEcmCaseData.setReasonForCT(transferToEcmDataModel.getReasonForCT());
        
        CaseDetails newEcmCaseDetails = new CaseDetails();
        newEcmCaseDetails.setCaseData(newEcmCaseData);
        
        String officeCT = transferToEcmDataModel.getOfficeCT();
        String caseTypeId =  TribunalOffice.isScotlandOffice(officeCT) ? officeName : getCorrectedOfficeName(officeCT);
        newEcmCaseDetails.setCaseTypeId(caseTypeId);
        newEcmCaseDetails.setJurisdiction(createUpdatesMsg.getJurisdiction());
        return newEcmCaseDetails;
    }

    private CaseData generateNewCaseDataForCaseTransfer(SubmitEvent oldSubmitEvent, String caseTypeId) {
        uk.gov.hmcts.et.common.model.ccd.CaseData caseData = oldSubmitEvent.getCaseData();
        String state = oldSubmitEvent.getState();
        String caseId = String.valueOf(oldSubmitEvent.getCaseId());
        log.info("Copying case data for case {}", caseId);
        
        // TODO: Full implementation requires migrating TransferToEcmCaseDataHelper
        // For now, create a simple copy with essential fields
        CaseData newCaseData = new CaseData();
        newCaseData.setEthosCaseReference(caseData.getEthosCaseReference());
        newCaseData.setEcmCaseType(caseData.getEcmCaseType());
        newCaseData.setLinkedCaseCT(generateMarkUp(ccdGatewayBaseUrl, caseId, caseData.getEthosCaseReference()));
        
        log.warn("CreateEcmSingleService is using simplified case data copy - full implementation pending");
        return newCaseData;
    }

    private static String getCorrectedOfficeName(String oldCaseOfficeName) {
        if (StringUtils.hasLength(oldCaseOfficeName) && oldCaseOfficeName.contains(" ")) {
            return oldCaseOfficeName.replace(" ", "");
        }
        return oldCaseOfficeName;
    }
    
    private String generateMarkUp(String ccdGatewayBaseUrl, String caseId, String ethosCaseRef) {
        String url = ccdGatewayBaseUrl + "/cases/case-details/" + caseId;
        return "<a target=\"_blank\" href=\"" + url + "\">" + ethosCaseRef + "</a>";
    }
    
    private String getTransferredCaseLink(String ccdGatewayBaseUrl, String caseId, String ethosCaseReference) {
        return generateMarkUp(ccdGatewayBaseUrl, caseId, ethosCaseReference);
    }
}
