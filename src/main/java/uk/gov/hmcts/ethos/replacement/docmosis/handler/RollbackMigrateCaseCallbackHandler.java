package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EcmMigrationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class RollbackMigrateCaseCallbackHandler extends CallbackHandlerBase {

    private final EcmMigrationService ecmMigrationService;

    @Autowired
    public RollbackMigrateCaseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        EcmMigrationService ecmMigrationService
    ) {
        super(caseDetailsConverter);
        this.ecmMigrationService = ecmMigrationService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("rollbackMigrateCase");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return false;
    }

    @Override
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        try {
            var ccdRequest = toCcdRequest(caseDetails);
            CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
            ecmMigrationService.rollbackEcmMigration(ccdRequest.getCaseDetails());
            return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to rollback migrated case", exception);
        }
    }
}
