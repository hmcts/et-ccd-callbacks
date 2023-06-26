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
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class ReferenceDataController {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    private final VerifyTokenService verifyTokenService;
    private final ReferenceService referenceService;

    @PostMapping(value = "/hearingVenueReferenceData", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> hearingVenueReferenceData(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("HEARING VENUE REFERENCE DATA ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error("Invalid Token {}", userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = referenceService.fetchHearingVenueRefData(ccdRequest.getCaseDetails(), userToken);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/dateListedReferenceData", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> dateListedReferenceData(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("DATE LISTED REFERENCE DATA ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error("Invalid Token {}", userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = referenceService.fetchDateListedRefData(ccdRequest.getCaseDetails(), userToken);

        return getCallbackRespEntityNoErrors(caseData);
    }

}