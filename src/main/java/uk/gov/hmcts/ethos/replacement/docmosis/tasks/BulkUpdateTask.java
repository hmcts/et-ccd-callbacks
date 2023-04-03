package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.BulkDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

import java.io.IOException;

@Slf4j
public class BulkUpdateTask implements Runnable {

    private final BulkDetails bulkDetails;
    private final SubmitEvent submitEvent;
    private final String authToken;
    private final CcdClient ccdClient;

    public BulkUpdateTask(BulkDetails bulkDetails, SubmitEvent submitEvent, String authToken, CcdClient ccdClient) {
        this.bulkDetails = bulkDetails;
        this.submitEvent = submitEvent;
        this.authToken = authToken;
        this.ccdClient = ccdClient;
    }

    @Override
    public void run() {

        log.info("Waiting: " + Thread.currentThread().getName());
        String caseId = String.valueOf(submitEvent.getCaseId());
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(authToken,
                    UtilHelper.getCaseTypeId(bulkDetails.getCaseTypeId()),
                    bulkDetails.getJurisdiction(), caseId);
            ccdClient.submitEventForCase(authToken, submitEvent.getCaseData(),
                    UtilHelper.getCaseTypeId(bulkDetails.getCaseTypeId()),
                    bulkDetails.getJurisdiction(), returnedRequest, caseId);
        } catch (IOException e) {
            log.error("Error processing bulk update threads:" + e.getMessage(), e);
        }
    }
}
