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
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.AllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandAllocateHearingService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class AllocateHearingCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final AllocateHearingService allocateHearingService;
    private final ScotlandAllocateHearingService scotlandAllocateHearingService;

    @Autowired
    public AllocateHearingCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        AllocateHearingService allocateHearingService,
        ScotlandAllocateHearingService scotlandAllocateHearingService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.allocateHearingService = allocateHearingService;
        this.scotlandAllocateHearingService = scotlandAllocateHearingService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("allocateHearing");
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
        return toCallbackResponse(aboutToSubmitAllocateHearing(toCcdRequest(caseDetails), authorizationToken));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitAllocateHearing(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        String caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            allocateHearingService.updateCase(caseData);
        } else if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            scotlandAllocateHearingService.updateCase(caseData);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }
}
