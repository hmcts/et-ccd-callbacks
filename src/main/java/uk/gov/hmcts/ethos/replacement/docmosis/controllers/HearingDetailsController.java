package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.hearingdetails.HearingDetailsService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@RequestMapping("/hearingdetails")
@Slf4j
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class HearingDetailsController {
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final HearingDetailsService hearingDetailsService;

    public HearingDetailsController(VerifyTokenService verifyTokenService,
                                    HearingDetailsService hearingDetailsService) {
        this.verifyTokenService = verifyTokenService;
        this.hearingDetailsService = hearingDetailsService;
    }

    @PostMapping(value = "/initialiseHearings", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> initialiseHearingDynamicList(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        hearingDetailsService.initialiseHearingDetails(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/handleListingSelected", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> handleListingSelected(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        hearingDetailsService.handleListingSelected(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/hearingMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> hearingMidEventValidation(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        hearingDetailsService.updateCase(ccdRequest.getCaseDetails());

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }
}
