package uk.gov.hmcts.ethos.replacement.docmosis.controllers.et1repped;

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
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/et1Repped/createCase")
public class CreateCaseController {
    private final VerifyTokenService verifyTokenService;
    private final Et1ReppedService et1ReppedService;
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String TRIAGE_ERROR_MESSAGE = """
            The postcode you entered is not included under the early adopter sites yet. Please use the ET1 claim form
            linked on this page or copy the following into your browser:
            https://www.claim-employment-tribunals.service.gov.uk/
            """;

    /**
     * Callback to handle postcode validation for the ET1 Repped journey.
     * @param ccdRequest the request
     * @param userToken the user token
     * @return the response entity
     * @throws InvalidPostcodeException if the postcode is invalid
     */
    @PostMapping(value = "/validatePostcode", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "About to start callback handler for ET1 repped journey")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validatePostcode(@RequestBody CCDRequest ccdRequest,
                                                            @RequestHeader("Authorization") String userToken)
            throws InvalidPostcodeException {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setEt1ReppedTriageYesNo(et1ReppedService.validatePostcode(caseData));
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/officeError", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Error page callback handler for ET1 repped journey")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> officeError(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = List.of(TRIAGE_ERROR_MESSAGE);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "about to submit callback handler for ET1 repped journey")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et1ReppedHelper.setCreateDraftData(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

}
