package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseAdminHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseAdminService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the Response to an Application event.
 */
@Slf4j
@RequestMapping("/tseAdmin")
@RestController
@RequiredArgsConstructor
public class TseAdminController {

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;
    private final TseAdminService tseAdminService;

    /**
    * Populates the dynamic list for select an application to respond to.
    *
    * @param ccdRequest holds the request and case data
    * @param userToken  used for authorization
    * @return Callback response entity with case data attached.
    */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the dynamic list for select an application to respond to")
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

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setTseAdminSelectApplication(TseAdminHelper.populateSelectApplicationAdminDropdown(caseData));
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Record a Decision and send notify emails to claimant and/or respondent.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        tseAdminService.saveTseAdminDataFromCaseData(caseData);
        tseAdminService.sendRecordADecisionEmails(ccdRequest.getCaseDetails().getCaseId(), caseData);
        tseAdminService.clearTseAdminDataFromCaseData(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Middle Event for initial Application & Response details.
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds case data
     * @param  userToken        Used for authorisation
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/midDetailsTable", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Mid Event for initial Application & Response details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midDetailsTable(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        tseAdminService.initialTseAdminTableMarkUp(caseData, userToken);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Returns data needed to populate the submitted page.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates data for the submitted page")
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

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String body = String.format("### What happens next\r\n\r\nYou can view the decision in the <a " + "href"
            + "=\"/cases/case-details/%s#Applications\" target=\"_blank\">Applications tab (opens in new tab)</a>",
            ccdRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .confirmation_body(body)
            .build());
    }

    /**
     * Display the selected application details for close application event.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/displayCloseApplicationTable", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "To display the selected application details for close application event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> displayCloseApplicationTable(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setTseAdminCloseApplicationTable(
            tseAdminService
                .generateCloseApplicationDetailsMarkdown(caseData, userToken));
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * About to Submit Close Application.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmitCloseApplication", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "About to Submit Close Application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitCloseApplication(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        tseAdminService.aboutToSubmitCloseApplication(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Returns data needed to populate the Close Application submitted page.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/submittedCloseApplication", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates data for the submitted page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submittedCloseApplication(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String body = String.format("### What happens next\r\n\r\nYou can still view the application in the"
                        + " <a href=\"/cases/case-details/%s#Applications\""
                        + " target=\"_blank\">Applications tab (opens in a new tab)</a>",
                ccdRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }
}
