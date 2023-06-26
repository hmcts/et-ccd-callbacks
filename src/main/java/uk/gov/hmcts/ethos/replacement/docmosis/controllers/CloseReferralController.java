package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/closeReferral")
@RestController
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class CloseReferralController {
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;
    private static final String CLOSE_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>We have closed this referral. You can still view it in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";

    /**
     * Called for the first page of the Close Referral event.
     * Populates the Referral select dropdown.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData));
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called for the second page of the Close Referral event.
     * Populates the Referral hearing and reply details section on the page.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/initHearingAndReferralDetails", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> initHearingDetailsForCloseReferral(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setCloseReferralHearingDetails(ReferralHelper.populateHearingReferralDetails(caseData));
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called at the end of Close Referral event, it sets the referral status to Closed.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitReferralReply(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        ReferralHelper.setReferralStatusToClosed(caseData);
        ReferralHelper.clearCloseReferralDataFromCaseData(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called after submitting a close referral event.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeCloseReferral", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> completeInitialConsideration(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String body = String.format(CLOSE_REFERRAL_BODY,
                ccdRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .confirmation_body(body)
            .build());
    }

}
