package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.CallbackHandler;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.ListingRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public abstract class ListingCallbackHandlerBase implements CallbackHandler<CaseData> {

    private final CaseDetailsConverter caseDetailsConverter;

    protected ListingCallbackHandlerBase(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    public CallbackResponse<CaseData> aboutToSubmit(CallbackRequest data) {
        var caseDetails = data == null ? null : data.getCaseDetails();
        return aboutToSubmit(caseDetails);
    }

    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        return toCallbackResponse(aboutToSubmit(toListingRequest(caseDetails)));
    }

    Object aboutToSubmit(ListingRequest listingRequest) {
        throw new IllegalStateException("Handler does not support about-to-submit callbacks for events: "
            + getHandledEventIds());
    }

    @Override
    public SubmittedCallbackResponse submitted(CallbackRequest data) {
        var caseDetails = data == null ? null : data.getCaseDetails();
        return submitted(caseDetails);
    }

    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(submitted(toListingRequest(caseDetails)));
    }

    Object submitted(ListingRequest listingRequest) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }

    protected ListingRequest toListingRequest(CaseDetails caseDetails) {
        ListingDetails listingDetails = convertTo(caseDetails, ListingDetails.class);
        return new ListingRequest(listingDetails);
    }

    protected uk.gov.hmcts.et.common.model.ccd.CaseDetails toCaseDetails(CaseDetails caseDetails) {
        return caseDetailsConverter.convert(caseDetails);
    }

    protected CCDRequest toCcdRequest(CaseDetails caseDetails) {
        return new CCDRequest(toCaseDetails(caseDetails));
    }

    protected <T> T convertTo(CaseDetails caseDetails, Class<T> type) {
        return caseDetailsConverter.convert(caseDetails, type);
    }

    protected CallbackResponse<CaseData> toCallbackResponse(Object result) {
        return toCcdCallbackResponse(result);
    }

    protected SubmittedCallbackResponse toSubmittedCallbackResponse(Object result) {
        return toCcdCallbackResponse(result);
    }

    protected CCDCallbackResponse emptyResponse() {
        return CCDCallbackResponse.builder().build();
    }

    private CCDCallbackResponse toCcdCallbackResponse(Object result) {
        Object responseBody = result;
        if (result instanceof ResponseEntity<?> responseEntity) {
            responseBody = responseEntity.getBody();
        }

        if (responseBody == null) {
            return emptyResponse();
        }

        if (responseBody instanceof CCDCallbackResponse callbackResponse) {
            return callbackResponse;
        }

        return caseDetailsConverter.getObjectMapper().convertValue(responseBody, CCDCallbackResponse.class);
    }
}
