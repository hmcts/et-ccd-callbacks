package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.CaseTransferController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class AssignCaseCallbackHandler extends CallbackHandlerBase {

    private final CaseTransferController aboutController;

    @Autowired
    public AssignCaseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseTransferController aboutController
    ) {
        super(caseDetailsConverter);
        this.aboutController = aboutController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("assignCase");
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
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toCallbackResponse(
            aboutController.assignCase(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }
}
