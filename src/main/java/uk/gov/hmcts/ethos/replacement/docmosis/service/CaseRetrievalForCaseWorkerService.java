package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.exceptions.CaseRetrievalException;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("caseRetrievalForCaseWorkerService")
public class CaseRetrievalForCaseWorkerService {

    private static final String MESSAGE = "Failed to retrieve case for : ";
    private static final String EMPTY_STRING = "";
    private final CcdClient ccdClient;

    @Autowired
    public CaseRetrievalForCaseWorkerService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    public SubmitEvent caseRetrievalRequest(String authToken, String caseTypeId, String jurisdiction, String caseId) {
        try {
            return ccdClient.retrieveCase(authToken, caseTypeId, jurisdiction, caseId);
        } catch (Exception ex) {
            throw (CaseRetrievalException)new CaseRetrievalException(
                    MESSAGE + caseId + ex.getMessage()).initCause(ex);

        }
    }

    public List<SubmitEvent> casesRetrievalRequest(CCDRequest ccdRequest, String authToken) {
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("EventId: " + ccdRequest.getEventId());
        try {
            return ccdClient.retrieveCases(authToken, caseDetails.getCaseTypeId(), caseDetails.getJurisdiction());
        } catch (Exception ex) {
            throw (CaseRetrievalException)new CaseRetrievalException(
                    MESSAGE + caseDetails.getCaseId() + ex.getMessage()).initCause(ex);
        }
    }

    public String caseRefRetrievalRequest(String authToken, String caseTypeId, String jurisdiction, String caseId) {
        try {
            log.info("In Case Retrieval Service - caseRefRetrievalRequest for case type: {} ",  caseTypeId);
            return ccdClient.retrieveTransferredCaseReference(authToken, caseTypeId, jurisdiction, caseId);
        } catch (Exception ex) {
            throw new CaseCreationException(MESSAGE + caseId + ex.getMessage());
        }
    }

    public List<SubmitEvent> casesRetrievalESRequest(String currentCaseId, String authToken, String caseTypeId,
                                                     List<String> caseIds) {
        try {
            return ccdClient.retrieveCasesElasticSearch(authToken, caseTypeId, caseIds);
        } catch (Exception ex) {
            throw (CaseRetrievalException)new CaseRetrievalException(
                    MESSAGE + currentCaseId + ex.getMessage()).initCause(ex);
        }
    }

    public Pair<String, List<SubmitEvent>> transferSourceCaseRetrievalESRequest(String currentCaseId, String authToken,
                                                                                List<String> caseTypeIdsToCheck) {
        try {
            for (String targetOffice : caseTypeIdsToCheck) {
                List<SubmitEvent> submitEvents = ccdClient.retrieveTransferredCaseElasticSearch(authToken,
                        targetOffice, currentCaseId);
                if (!submitEvents.isEmpty()) {
                    return Pair.of(targetOffice, submitEvents);
                }
            }

            return Pair.of(EMPTY_STRING, new ArrayList<>());
        } catch (Exception ex) {
            throw (CaseRetrievalException)new CaseRetrievalException(
                    MESSAGE + currentCaseId + ex.getMessage()).initCause(ex);
        }
    }
}
