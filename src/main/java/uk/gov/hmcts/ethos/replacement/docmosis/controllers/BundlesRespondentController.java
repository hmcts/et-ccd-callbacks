package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesRespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/bundlesRespondent")
@RestController
@RequiredArgsConstructor
public class BundlesRespondentController {
    public static final String BUNDLES_LOG = "Bundles feature flag is {}";
    public static final String BUNDLES_FEATURE_IS_NOT_AVAILABLE = "Bundles feature is not available";

    private final VerifyTokenService verifyTokenService;
    private final BundlesRespondentService bundlesRespondentService;
    private final FeatureToggleService featureToggleService;

    private static final String INVALID_TOKEN = "Invalid Token {}";

    /**
     * Called at the start of Bundles Respondent Prepare Doc for Hearing journey.
     * Sets hidden inset fields to YES to enable inset text functionality in ExUI.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for bundles respondent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        boolean bundlesToggle = featureToggleService.isBundlesEnabled();
        log.info(BUNDLES_LOG, bundlesToggle);
        if (bundlesToggle) {
            if (!verifyTokenService.verifyTokenSignature(userToken)) {
                log.error(INVALID_TOKEN, userToken);
                return ResponseEntity.status(FORBIDDEN.value()).build();
            }
            CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
            caseData.setBundlesRespondentPrepareDocNotesShow(YES);
            return getCallbackRespEntityNoErrors(caseData);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, BUNDLES_FEATURE_IS_NOT_AVAILABLE);
    }

    /**
     * About to Submit for Bundles Respondent Prepare Doc for Hearing journey.
     * @param ccdRequest generic request from CCD
     * @param userToken authentication token to verify the user
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "About to Submit for bundles respondent")
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
            @RequestHeader("Authorization") String userToken) {

        boolean bundlesToggle = featureToggleService.isBundlesEnabled();
        log.info(BUNDLES_LOG, bundlesToggle);
        if (bundlesToggle) {
            if (!verifyTokenService.verifyTokenSignature(userToken)) {
                log.error(INVALID_TOKEN, userToken);
                return ResponseEntity.status(FORBIDDEN.value()).build();
            }

            CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
            bundlesRespondentService.addToBundlesCollection(caseData);
            bundlesRespondentService.clearInputData(caseData);
            return getCallbackRespEntityNoErrors(caseData);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, BUNDLES_FEATURE_IS_NOT_AVAILABLE);
    }

    /**
     * Populates the hearing list on page 3.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/midPopulateHearings", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the hearing list on page 3.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midPopulateHearings(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        boolean bundlesToggle = featureToggleService.isBundlesEnabled();
        log.info(BUNDLES_LOG, bundlesToggle);
        if (bundlesToggle) {
            if (!verifyTokenService.verifyTokenSignature(userToken)) {
                log.error(INVALID_TOKEN, userToken);
                return ResponseEntity.status(FORBIDDEN.value()).build();
            }
            CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
            bundlesRespondentService.populateSelectHearings(caseData);
            return getCallbackRespEntityNoErrors(caseData);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, BUNDLES_FEATURE_IS_NOT_AVAILABLE);
    }

    /**
     * Validates the uploaded file is a PDF.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/midValidateUpload", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Validates the uploaded file is a PDF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midValidateUpload(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        boolean bundlesToggle = featureToggleService.isBundlesEnabled();
        log.info(BUNDLES_LOG, bundlesToggle);
        List<String> errors = new ArrayList<>();
        if (bundlesToggle) {
            if (!verifyTokenService.verifyTokenSignature(userToken)) {
                log.error(INVALID_TOKEN, userToken);
                return ResponseEntity.status(FORBIDDEN.value()).build();
            }
            CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
            bundlesRespondentService.validateFileUpload(caseData);
            return getCallbackRespEntityErrors(errors, caseData);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, BUNDLES_FEATURE_IS_NOT_AVAILABLE);
    }

    /**
     * Renders data for the submitted page.
     */
    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Renders data for the submitted page.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        boolean bundlesToggle = featureToggleService.isBundlesEnabled();
        log.info(BUNDLES_LOG, bundlesToggle);
        if (bundlesToggle) {
            if (!verifyTokenService.verifyTokenSignature(userToken)) {
                log.error(INVALID_TOKEN, userToken);
                return ResponseEntity.status(FORBIDDEN.value()).build();
            }
            CCDCallbackResponse response = CCDCallbackResponse.builder()
                    .data(ccdRequest.getCaseDetails().getCaseData())
                    .build();
            String header = "<h1>You have sent your hearing documents to the tribunal</h1>";
            String body = """
                    <h2>What happens next</h2>
                        The tribunal will let you know
                        if they have any questions about the hearing documents you have submitted.
                    """;
            response.setConfirmation_header(header);
            response.setConfirmation_body(body);
            return ResponseEntity.ok(response);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, BUNDLES_FEATURE_IS_NOT_AVAILABLE);
    }
}