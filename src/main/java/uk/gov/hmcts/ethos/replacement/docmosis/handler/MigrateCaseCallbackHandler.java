package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.GlobalSearchDataMigrationController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class MigrateCaseCallbackHandler extends CallbackHandlerBase {

    private final GlobalSearchDataMigrationController globalSearchDataMigrationController;

    @Autowired
    public MigrateCaseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        GlobalSearchDataMigrationController globalSearchDataMigrationController
    ) {
        super(caseDetailsConverter);
        this.globalSearchDataMigrationController = globalSearchDataMigrationController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("migrateCase");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return true;
    }

    @Override
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toCallbackResponse(
            globalSearchDataMigrationController.addGlobalSearchFieldsInCaseData(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        try {
            return toSubmittedCallbackResponse(
                globalSearchDataMigrationController.addServiceIdForGlobalSearchInCaseData(
                    toCcdRequest(caseDetails),
                    authorizationToken
                )
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to delegate callback to controller", exception);
        }
    }
}
