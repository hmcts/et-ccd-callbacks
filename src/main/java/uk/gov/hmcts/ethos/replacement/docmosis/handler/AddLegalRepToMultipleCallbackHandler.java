package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples.AddLegalRepToMultipleController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class AddLegalRepToMultipleCallbackHandler extends CallbackHandlerBase {

    private final AddLegalRepToMultipleController addLegalRepToMultipleController;

    @Autowired
    public AddLegalRepToMultipleCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        AddLegalRepToMultipleController addLegalRepToMultipleController
    ) {
        super(caseDetailsConverter);
        this.addLegalRepToMultipleController = addLegalRepToMultipleController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("addLegalRepToMultiple");
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
    @SneakyThrows
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toCallbackResponse(
                addLegalRepToMultipleController.submitAddLegalRepToMultiple(
                    authorizationToken,
                    toCcdRequest(caseDetails)
                )
            );
    }

    @Override
    @SneakyThrows
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(
            addLegalRepToMultipleController.completeAddLegalRepToMultiple(
                authorizationToken,
                toCcdRequest(caseDetails)
            )
        );
    }
}
