package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntity;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class Et1VettingController {

    private final VerifyTokenService verifyTokenService;
    private final Et1VettingService et1VettingService;

    /**
     * Initialise ET1 case vetting.
     * Gets userToken as a parameter for security validation and ccdRequest data which has caseData as an object.
     * @param userToken Used for authorisation
     * @param ccdRequest CaseData which is a generic data type for most of the methods which holds ET1 case data
     * @return caseData in ccdRequest
     */
    @PostMapping(value = "/initialiseEt1Vetting", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise case vetting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initialiseEt1Vetting(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("CASE VETTING ABOUT TO START ---> " + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        et1VettingService.initialBeforeYouStart(caseDetails);

        List<JurCodesTypeItem> jurCodesCollection = caseDetails.getCaseData().getJurCodesCollection();
        if (jurCodesCollection != null) {
            caseDetails.getCaseData()
                    .setExistingJurisdictionCodes(et1VettingService.generateJurisdictionCodesHtml(jurCodesCollection));
        }

        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    /**
     * Mid callback event in the Jurisdiction code page
     * Gets userToken as a parameter for security validation and ccdRequest data which has caseData as an object.
     * Validates the jurisdiction code that caseworker has added against the existing codes
     * to prevent duplicate entries.
     * Populates the track allocation html, which depends on the existing jurisdiction codes.
     * @param userToken Used for authorisation
     * @param ccdRequest CaseData which is a generic data type for most of the methods which holds ET1 case data
     * @return errors from the jurisdiction code validation (if there is any) and caseData in ccdRequest
     */
    @PostMapping(value = "/jurisdictionCodes", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Validate Jurisdiction Codes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> jurisdictionCodes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = et1VettingService.validateJurisdictionCodes(caseData);
        if (errors.isEmpty()) {
            et1VettingService.populateEt1TrackAllocationHtml(caseData);
        }

        return getCallbackRespEntity(errors, ccdRequest.getCaseDetails());
    }
}