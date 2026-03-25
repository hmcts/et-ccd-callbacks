package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class AddLegalRepToMultipleCallbackHandler extends CallbackHandlerBase {

    private static final String ADD_USER_COMPLETE = "<h1>You have been added to the Multiple for the case: %s</h1>";
    private final MultipleReferenceService multipleReferenceService;
    private final UserIdamService userService;

    @Autowired
    public AddLegalRepToMultipleCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        MultipleReferenceService multipleReferenceService,
        UserIdamService userService
    ) {
        super(caseDetailsConverter);
        this.multipleReferenceService = multipleReferenceService;
        this.userService = userService;
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
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        try {
            return toCallbackResponse(submitAddLegalRepToMultiple(
                    authorizationToken,
                    toCcdRequest(caseDetails)
                ));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to add legal representative to multiple", exception);
        }
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(completeAddLegalRepToMultiple(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<CCDCallbackResponse> submitAddLegalRepToMultiple(
        String userToken,
        CCDRequest ccdRequest
    ) throws IOException {
        var details = ccdRequest.getCaseDetails();
        String userToAddId = userService.getUserDetails(userToken).getUid();
        multipleReferenceService.addLegalRepToMultiple(details, userToAddId);
        return getCallbackRespEntityNoErrors(details.getCaseData());
    }

    private ResponseEntity<CCDCallbackResponse> completeAddLegalRepToMultiple(CCDRequest ccdRequest) {
        var details = ccdRequest.getCaseDetails();
        return ResponseEntity.ok(
            CCDCallbackResponse.builder()
                .confirmation_header(String.format(ADD_USER_COMPLETE, details.getCaseData().getEthosCaseReference()))
                .build()
        );
    }
}
