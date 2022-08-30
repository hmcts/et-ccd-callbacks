package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.client.CcdClient;
import uk.gov.hmcts.et.common.exceptions.CaseCreationException;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

@Slf4j
@RequiredArgsConstructor
@Service("caseCreationForCaseWorkerService")
public class CaseCreationForCaseWorkerService {

    private static final String MESSAGE = "Failed to create new case for case id : ";
    private final CcdClient ccdClient;

    public SubmitEvent caseCreationRequest(CCDRequest ccdRequest, String userToken) {
        var caseDetails = ccdRequest.getCaseDetails();
        log.info("EventId: " + ccdRequest.getEventId());
        try {
            return ccdClient.submitCaseCreation(userToken, caseDetails,
                   ccdClient.startCaseCreation(userToken, caseDetails));
        } catch (Exception ex) {
            throw new CaseCreationException(MESSAGE + caseDetails.getCaseId() + ex.getMessage());
        }
    }
}