package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.NoticeOfChangeController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class ApplyNocDecisionCallbackHandler extends CallbackHandlerBase {

    private final NoticeOfChangeController noticeOfChangeController;

    @Autowired
    public ApplyNocDecisionCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        NoticeOfChangeController noticeOfChangeController
    ) {
        super(caseDetailsConverter);
        this.noticeOfChangeController = noticeOfChangeController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("applyNocDecision");
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
        try {
            return toCallbackResponse(
                noticeOfChangeController.handleAboutToSubmit(
                    authorizationToken,
                    toCallbackRequest(caseDetails)
                )
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to delegate callback to controller", exception);
        }
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(
            noticeOfChangeController.nocSubmitted(
                authorizationToken,
                toCallbackRequest(caseDetails)
            )
        );
    }
}
