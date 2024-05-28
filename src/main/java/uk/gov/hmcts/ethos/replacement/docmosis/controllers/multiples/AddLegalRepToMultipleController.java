package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the Add Legal Rep to Multiple pages.
 * Event is triggered by Legal Reps to add themselves to the Multiple their case is a subcase of.
 */
@Slf4j
@RequestMapping("/multiples/addLegalRepToMultiple")
@RequiredArgsConstructor
@RestController
public class AddLegalRepToMultipleController {

    private final VerifyTokenService verifyTokenService;
    private final MultipleReferenceService multipleReferenceService;
    private final UserIdamService userService;

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String ADD_USER_COMPLETE =
            "<h1>You have been added to the Multiple for the case: %s</h1>";

    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "start the Add Legal Rep to Multiple flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> startAddLegalRepToMultiple(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
        @RequestBody CCDRequest ccdRequest) {

        log.info("START OF ADD LEGAL REP TO MULTIPLE ---> {}", ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        List<String> errors = multipleReferenceService.validateSubcaseIsOfMultiple(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * About to Submit callback.
     * @param ccdRequest Holds the request and case data
     * @param userToken Used for authorisation
     * @return caseData in ccdRequest
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles Add Legal Rep to Multiple Submission")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> submitAddLegalRepToMultiple(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
        @RequestBody CCDRequest ccdRequest) throws IOException {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        String caseId = caseDetails.getCaseId();
        log.info("ISSUE ADD LEGAL REP TO MULTIPLE ABOUT TO SUBMIT ---> {}", caseId);

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String userToAddId = userService.getUserDetails(userToken).getUid();
        multipleReferenceService.addLegalRepToMultiple(caseDetails, userToAddId);

        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(value = "/completed", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Completes the Add Legal Rep to Multiple flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> completeAddLegalRepToMultiple(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
            @RequestBody CCDRequest ccdRequest) {
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        log.info("Issue Add Legal Rep to Multiple complete requested for case reference ---> {}",
                caseDetails.getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(CCDCallbackResponse
                .builder()
                .confirmation_header(
                        String.format(ADD_USER_COMPLETE, caseDetails.getCaseData().getEthosCaseReference()))
                .build());
    }
}
