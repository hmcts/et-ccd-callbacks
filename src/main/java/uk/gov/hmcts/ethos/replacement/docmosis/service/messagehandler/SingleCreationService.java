package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper.generateMarkUp;

/**
 * Service for creating new cases during case transfer.
 * Migrated from et-message-handler.
 */
@Slf4j
@Service
public class SingleCreationService {

    public static final String CREATE_CASE_EVENT_SUMMARY_TEMPLATE = "Case created by transfer from %s";

    private final CcdClient ccdClient;
    private String ccdGatewayBaseUrl;

    private static class CaseTransferContext {
        private final String caseId;
        private final String ccdGatewayBaseUrl;
        private final String positionTypeCT;
        private final String jurisdiction;
        private final String state;
        private final String owningOfficeCT;
        private final String reasonForCT;

        CaseTransferContext(String caseId, String ccdGatewayBaseUrl, String positionTypeCT,
                            String jurisdiction, String state, String owningOfficeCT, String reasonForCT) {
            this.caseId = caseId;
            this.ccdGatewayBaseUrl = ccdGatewayBaseUrl;
            this.positionTypeCT = positionTypeCT;
            this.jurisdiction = jurisdiction;
            this.state = state;
            this.owningOfficeCT = owningOfficeCT;
            this.reasonForCT = reasonForCT;
        }
    }

    @Autowired
    public SingleCreationService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    public void sendCreation(SubmitEvent oldSubmitEvent, String accessToken, UpdateCaseMsg updateCaseMsg)
        throws IOException {

        CreationSingleDataModel creationSingleDataModel = (CreationSingleDataModel) updateCaseMsg.getDataModelParent();
        String caseTypeId = TribunalOffice.getCaseTypeId(creationSingleDataModel.getOfficeCT());
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String ethosCaseReference = oldSubmitEvent.getCaseData().getEthosCaseReference();
        SubmitEvent caseDestinationOffice = existCaseDestinationOffice(accessToken, ethosCaseReference, caseTypeId);

        if (caseDestinationOffice == null) {
            log.info("Creating new case for transfer to {} {}", caseTypeId, ethosCaseReference);
            transferNewCase(oldSubmitEvent, jurisdiction, accessToken, updateCaseMsg);
        } else {
            log.info("Case exists for transfer to {} {}", caseTypeId, ethosCaseReference);
            updateExistingCase(caseDestinationOffice, oldSubmitEvent, jurisdiction, accessToken, updateCaseMsg);
        }
    }

    private void updateExistingCase(SubmitEvent caseDestinationOffice, SubmitEvent oldSubmitEvent,
                                    String jurisdiction, String accessToken, UpdateCaseMsg updateCaseMsg)
        throws IOException {
        CreationSingleDataModel creationSingleDataModel = (CreationSingleDataModel) updateCaseMsg.getDataModelParent();
        String caseTypeId = TribunalOffice.getCaseTypeId(creationSingleDataModel.getOfficeCT());
        String caseId = String.valueOf(oldSubmitEvent.getCaseId());
        String destinationCaseId = String.valueOf(caseDestinationOffice.getCaseId());
        ccdGatewayBaseUrl = creationSingleDataModel.getCcdGatewayBaseUrl();
        CCDRequest returnedRequest = ccdClient.returnCaseCreationTransfer(accessToken, caseTypeId, jurisdiction,
                                                                          destinationCaseId);

        CaseTransferContext context = new CaseTransferContext(
            caseId,
            ccdGatewayBaseUrl,
            creationSingleDataModel.getPositionTypeCT(),
            jurisdiction,
            oldSubmitEvent.getState(),
            creationSingleDataModel.getOfficeCT(),
            creationSingleDataModel.getReasonForCT()
        );

        ccdClient.submitEventForCase(accessToken,
            generateCaseDataCaseTransfer(caseDestinationOffice.getCaseData(),
                                         oldSubmitEvent.getCaseData(),
                                         context
            ),
            caseTypeId,
            jurisdiction,
            returnedRequest,
            destinationCaseId
        );
    }

    private void transferNewCase(SubmitEvent oldSubmitEvent, String jurisdiction, String accessToken,
                                 UpdateCaseMsg updateCaseMsg) throws IOException {
        CreationSingleDataModel creationSingleDataModel = (CreationSingleDataModel) updateCaseMsg.getDataModelParent();
        ccdGatewayBaseUrl = creationSingleDataModel.getCcdGatewayBaseUrl();
        String sourceCaseTypeId = updateCaseMsg.getCaseTypeId();
        String caseId = String.valueOf(oldSubmitEvent.getCaseId());
        String caseTypeId = TribunalOffice.getCaseTypeId(creationSingleDataModel.getOfficeCT());

        CaseTransferContext context = new CaseTransferContext(
            caseId,
            ccdGatewayBaseUrl,
            creationSingleDataModel.getPositionTypeCT(),
            jurisdiction,
            oldSubmitEvent.getState(),
            creationSingleDataModel.getOfficeCT(),
            creationSingleDataModel.getReasonForCT()
        );

        CaseDetails newCaseDetailsCT = createCaseDetailsCaseTransfer(oldSubmitEvent.getCaseData(),
                                                                     caseTypeId,
                                                                     context);
        CCDRequest returnedRequest = ccdClient.startCaseCreationTransfer(accessToken, newCaseDetailsCT);
        String eventSummary = String.format(CREATE_CASE_EVENT_SUMMARY_TEMPLATE,
                                         oldSubmitEvent.getCaseData().getManagingOffice());
        SubmitEvent newCase = ccdClient.submitCaseCreation(accessToken, newCaseDetailsCT, returnedRequest,
                                                           eventSummary);
        if (newCase != null) {
            // On the old case, add a link to the new case
            String transferredCaseLink = generateMarkUp(ccdGatewayBaseUrl,
                                                                   String.valueOf(newCase.getCaseId()),
                                                                   newCase.getCaseData().getEthosCaseReference());
            CCDRequest updateCCDRequest = ccdClient.startEventForCase(accessToken, sourceCaseTypeId, jurisdiction,
                                                                      caseId);

            updateCCDRequest.getCaseDetails().getCaseData().setTransferredCaseLink(transferredCaseLink);
            ccdClient.submitEventForCase(accessToken, updateCCDRequest.getCaseDetails().getCaseData(), sourceCaseTypeId,
                                         jurisdiction, updateCCDRequest, caseId);

            // On the new case, give access to the user who created the old case
            CCDRequest ccdRequest = ccdClient.startEventForCase(
                accessToken, caseTypeId, jurisdiction, String.valueOf(newCase.getCaseId()),
                "claimantTransferredCaseAccess");
            ccdClient.submitEventForCase(accessToken, ccdRequest.getCaseDetails().getCaseData(),
                                         caseTypeId, jurisdiction, ccdRequest, String.valueOf(newCase.getCaseId()));

        }
    }

    private SubmitEvent existCaseDestinationOffice(String accessToken, String ethosCaseReference,
                                                   String destinationCaseTypeId) throws IOException {
        List<SubmitEvent> submitEvents = retrieveDestinationCase(accessToken, ethosCaseReference,
                                                                 destinationCaseTypeId);
        return submitEvents.isEmpty() ? null : submitEvents.getFirst();
    }

    private List<SubmitEvent> retrieveDestinationCase(String authToken, String ethosCaseReference,
                                                      String destinationCaseTypeId) throws IOException {
        return ccdClient.retrieveCasesElasticSearch(authToken, destinationCaseTypeId,
                                                    new ArrayList<>(Collections.singletonList(ethosCaseReference)));
    }

    private CaseDetails createCaseDetailsCaseTransfer(CaseData oldCaseData, String caseTypeId,
                                                      CaseTransferContext context) {
        CaseDetails newCaseTransferCaseDetails = new CaseDetails();
        newCaseTransferCaseDetails.setCaseTypeId(caseTypeId);
        newCaseTransferCaseDetails.setJurisdiction(context.jurisdiction);

        CaseData newCaseData = generateNewCaseDataCaseTransfer(oldCaseData,
                                                               context);
        newCaseData.setReasonForCT(context.reasonForCT);
        newCaseTransferCaseDetails.setCaseData(newCaseData);
        return newCaseTransferCaseDetails;
    }

    private CaseData generateNewCaseDataCaseTransfer(CaseData oldCaseData, CaseTransferContext context) {
        return copyCaseData(oldCaseData, new CaseData(), context);
    }

    private CaseData generateCaseDataCaseTransfer(CaseData newCaseData, CaseData oldCaseData,
                                                  CaseTransferContext context) {

        return copyCaseData(oldCaseData, newCaseData, context);
    }

    @SuppressWarnings("PMD.NcssCount")
    private CaseData copyCaseData(CaseData oldCaseData, CaseData newCaseData, CaseTransferContext context) {
        newCaseData.setEthosCaseReference(oldCaseData.getEthosCaseReference());
        newCaseData.setEcmCaseType(oldCaseData.getEcmCaseType());
        newCaseData.setClaimantTypeOfClaimant(oldCaseData.getClaimantTypeOfClaimant());
        newCaseData.setClaimantCompany(oldCaseData.getClaimantCompany());
        newCaseData.setClaimantIndType(oldCaseData.getClaimantIndType());
        newCaseData.setClaimantType(oldCaseData.getClaimantType());
        newCaseData.setClaimantOtherType(oldCaseData.getClaimantOtherType());
        newCaseData.setPreAcceptCase(oldCaseData.getPreAcceptCase());
        newCaseData.setReceiptDate(oldCaseData.getReceiptDate());
        newCaseData.setClaimantWorkAddressQuestion(oldCaseData.getClaimantWorkAddressQuestion());
        newCaseData.setClaimantWorkAddressQRespondent(oldCaseData.getClaimantWorkAddressQRespondent());
        newCaseData.setRepresentativeClaimantType(oldCaseData.getRepresentativeClaimantType());
        newCaseData.setRespondentCollection(oldCaseData.getRespondentCollection());
        newCaseData.setRepCollection(oldCaseData.getRepCollection());
        newCaseData.setPositionType(oldCaseData.getPositionTypeCT());
        newCaseData.setDateToPosition(oldCaseData.getDateToPosition());
        newCaseData.setCurrentPosition(oldCaseData.getCurrentPosition());
        newCaseData.setDepositCollection(oldCaseData.getDepositCollection());
        newCaseData.setJudgementCollection(oldCaseData.getJudgementCollection());
        newCaseData.setJurCodesCollection(oldCaseData.getJurCodesCollection());
        newCaseData.setBfActions(oldCaseData.getBfActions());
        newCaseData.setUserLocation(oldCaseData.getUserLocation());
        newCaseData.setDocumentCollection(oldCaseData.getDocumentCollection());
        newCaseData.setAdditionalCaseInfoType(oldCaseData.getAdditionalCaseInfoType());
        newCaseData.setCaseNotes(oldCaseData.getCaseNotes());
        newCaseData.setClaimantWorkAddress(oldCaseData.getClaimantWorkAddress());
        newCaseData.setClaimantRepresentedQuestion(oldCaseData.getClaimantRepresentedQuestion());
        newCaseData.setCaseSource(oldCaseData.getCaseSource());
        newCaseData.setConciliationTrack(oldCaseData.getConciliationTrack());
        newCaseData.setCounterClaim(oldCaseData.getCounterClaim());
        newCaseData.setEccCases(oldCaseData.getEccCases());
        newCaseData.setRestrictedReporting(oldCaseData.getRestrictedReporting());
        newCaseData.setRespondent(oldCaseData.getRespondent());
        newCaseData.setClaimant(oldCaseData.getClaimant());
        newCaseData.setCaseRefECC(oldCaseData.getCaseRefECC());
        newCaseData.setCcdID(oldCaseData.getCcdID());
        newCaseData.setFlagsImageAltText(oldCaseData.getFlagsImageAltText());
        newCaseData.setCompanyPremises(oldCaseData.getCompanyPremises());
        if (context.state != null && !context.state.equals(CLOSED_STATE)) {
            newCaseData.setPositionType(context.positionTypeCT);
        }
        newCaseData.setManagingOffice(context.owningOfficeCT);
        newCaseData.setMultipleReference(oldCaseData.getMultipleReference());
        log.info("setLeadClaimant is set to " + oldCaseData.getLeadClaimant());
        newCaseData.setLeadClaimant(oldCaseData.getLeadClaimant());

        newCaseData.setReasonForCT(context.reasonForCT);
        newCaseData.setLinkedCaseCT(generateMarkUp(context.ccdGatewayBaseUrl, context.caseId,
                                                   oldCaseData.getEthosCaseReference()));
        newCaseData.setTypesOfClaim(oldCaseData.getTypesOfClaim());
        newCaseData.setClaimServedDate(oldCaseData.getClaimServedDate());
        newCaseData.setNoticeOfChangeAnswers0(oldCaseData.getNoticeOfChangeAnswers0());
        newCaseData.setNoticeOfChangeAnswers1(oldCaseData.getNoticeOfChangeAnswers1());
        newCaseData.setNoticeOfChangeAnswers2(oldCaseData.getNoticeOfChangeAnswers2());
        newCaseData.setNoticeOfChangeAnswers3(oldCaseData.getNoticeOfChangeAnswers3());
        newCaseData.setNoticeOfChangeAnswers4(oldCaseData.getNoticeOfChangeAnswers4());
        newCaseData.setNoticeOfChangeAnswers5(oldCaseData.getNoticeOfChangeAnswers5());
        newCaseData.setNoticeOfChangeAnswers6(oldCaseData.getNoticeOfChangeAnswers6());
        newCaseData.setNoticeOfChangeAnswers7(oldCaseData.getNoticeOfChangeAnswers7());
        newCaseData.setNoticeOfChangeAnswers8(oldCaseData.getNoticeOfChangeAnswers8());
        newCaseData.setNoticeOfChangeAnswers9(oldCaseData.getNoticeOfChangeAnswers9());
        newCaseData.setRespondentOrganisationPolicy0(oldCaseData.getRespondentOrganisationPolicy0());
        newCaseData.setRespondentOrganisationPolicy1(oldCaseData.getRespondentOrganisationPolicy1());
        newCaseData.setRespondentOrganisationPolicy2(oldCaseData.getRespondentOrganisationPolicy2());
        newCaseData.setRespondentOrganisationPolicy3(oldCaseData.getRespondentOrganisationPolicy3());
        newCaseData.setRespondentOrganisationPolicy4(oldCaseData.getRespondentOrganisationPolicy4());
        newCaseData.setRespondentOrganisationPolicy5(oldCaseData.getRespondentOrganisationPolicy5());
        newCaseData.setRespondentOrganisationPolicy6(oldCaseData.getRespondentOrganisationPolicy6());
        newCaseData.setRespondentOrganisationPolicy7(oldCaseData.getRespondentOrganisationPolicy7());
        newCaseData.setRespondentOrganisationPolicy8(oldCaseData.getRespondentOrganisationPolicy8());
        newCaseData.setRespondentOrganisationPolicy9(oldCaseData.getRespondentOrganisationPolicy9());

        return newCaseData;
    }
}
