package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.generic.GenericCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Component
public class ApplyNocDecisionCallbackHandler extends CallbackHandlerBase {

    private static final String APPLY_NOC_DECISION = "applyNocDecision";

    private final VerifyTokenService verifyTokenService;
    private final NocNotificationService nocNotificationService;
    private final NocRepresentativeService noCRepresentativeService;
    private final CcdCaseAssignment ccdCaseAssignment;

    @Autowired
    public ApplyNocDecisionCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        NocNotificationService nocNotificationService,
        NocRepresentativeService noCRepresentativeService,
        CcdCaseAssignment ccdCaseAssignment
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.nocNotificationService = nocNotificationService;
        this.noCRepresentativeService = noCRepresentativeService;
        this.ccdCaseAssignment = ccdCaseAssignment;
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
        CallbackRequest callbackRequest = toCallbackRequest(caseDetails);

        try {
            if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
                return toCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
            }

            CaseData updatedCaseData = noCRepresentativeService.updateRepresentation(
                callbackRequest.getCaseDetails(),
                authorizationToken
            );
            callbackRequest.getCaseDetails().setCaseData(updatedCaseData);
            return toCallbackResponse(
                ResponseEntity.ok(ccdCaseAssignment.applyNoc(callbackRequest, authorizationToken))
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to process about-to-submit NoC decision", exception);
        }
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        CallbackRequest callbackRequest = toCallbackRequest(caseDetails);
        GenericCallbackResponse callbackResponse = new GenericCallbackResponse();

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toSubmittedCallbackResponse(callbackResponse);
        }

        if (APPLY_NOC_DECISION.equals(callbackRequest.getEventId())) {
            var details = callbackRequest.getCaseDetails();
            CaseData data = details.getCaseData();

            try {
                nocNotificationService.sendNotificationOfChangeEmails(
                    callbackRequest.getCaseDetailsBefore(),
                    details,
                    callbackRequest.getCaseDetailsBefore().getCaseData().getChangeOrganisationRequestField()
                );
            } catch (Exception ignored) {
                // Preserve existing behavior: email errors do not fail callback.
            }

            callbackResponse.setConfirmation_header(
                "# You're now representing a client on case " + data.getEthosCaseReference()
            );
        }

        return toSubmittedCallbackResponse(callbackResponse);
    }
}
