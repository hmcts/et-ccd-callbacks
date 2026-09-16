package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_REVIEW_SUPPORT_TASKS;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTaskEventService {
    private final CcdClient ccdClient;
    private final AdminUserService adminUserService;

    public void triggerCloseReviewSupportTasks(CaseDetails caseDetails) {
        String caseId = caseDetails.getCaseId();
        try {
            String adminUserToken = adminUserService.getAdminUserToken();
            CCDRequest ccdRequest = ccdClient.startEventForCase(
                    adminUserToken,
                    caseDetails.getCaseTypeId(),
                    EMPLOYMENT,
                    caseId,
                    EVENT_CLOSE_REVIEW_SUPPORT_TASKS);
            CaseDetails latestCaseDetails = ccdRequest.getCaseDetails();
            ccdClient.submitEventForCase(
                    adminUserToken,
                    latestCaseDetails.getCaseData(),
                    latestCaseDetails.getCaseTypeId(),
                    latestCaseDetails.getJurisdiction(),
                    ccdRequest,
                    caseId);
            log.info("Triggered review support task closure event for case {}", caseId);
        } catch (Exception exception) {
            log.error("Unable to trigger review support task closure event for case {}", caseId, exception);
            throw new GenericRuntimeException(exception);
        }
    }
}
