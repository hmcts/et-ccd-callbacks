package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ServingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement", "PMD.LawOfDemeter", "PDM.FieldNamingConventions",
    "PMD.SignatureDeclareThrowsException"})
public class ET1ServingController {

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String SUBMITTED_HEADER =
        "<h1>Documents submitted</h1>\r\n\r\n<h5>We have notified the following parties:</h5>\r\n\r\n<h3>%s</h3>";

    private final VerifyTokenService verifyTokenService;
    private final ServingService servingService;

    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has caseData as an object.
     * @param  userToken        Used for authorisation
     *
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds ET1 case data
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/midServingDocumentOtherTypeNames", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> midServingDocumentOtherTypeNames(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setOtherTypeDocumentName(
                servingService.generateOtherTypeDocumentLink(caseData.getServingDocumentCollection()));
        caseData.setClaimantAndRespondentAddresses(servingService.generateClaimantAndRespondentAddress(caseData));
        caseData.setEmailLinkToAcas(servingService.generateEmailLinkToAcas(caseData, false));

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * About to submit the ET1 Serving journey.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/et1Serving/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) throws Exception {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        servingService.addServingDocToDocumentCollection(caseData);

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * Called after the ET1 Serving journey is complete. Sends an email to involved parties
     * and formats data for the success screen.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/et1Serving/submitted", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> submitted(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        servingService.sendNotifications(ccdRequest.getCaseDetails());

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(ccdRequest.getCaseDetails().getCaseData())
            .confirmation_header(String.format(SUBMITTED_HEADER, NotificationHelper.getParties(caseData)))
            .confirmation_body("<span></span>")
            .build());
    }
}
