package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the ET3 Vetting pages, provides access to the state of the ET3 Response
 * and formats data appropriately for rendering on the front end.
 */
@Slf4j
@RequestMapping("/et3Vetting")
@RestController
public class Et3VettingController {
    private static final String INVALID_TOKEN = "Invalid Token {}";
    public static final String PROCESSING_COMPLETE_HEADER = "<h2>Do this next</h2>You must:"
            + "<ul><li>accept or reject the ET3 response or refer the response</li>"
            + "<li>add any changed or new information to case details</li></ul>";
    private final VerifyTokenService verifyTokenService;

    public Et3VettingController(VerifyTokenService verifyTokenService) {
        this.verifyTokenService = verifyTokenService;
    }

    /**
     * Method calls when the "Is there an ET3 Response?" page is loaded, will generate a table
     * for displaying the state of the ET3 response and set if the response has been received.
     */
    @PostMapping(value = "/populateEt3Dates", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populate dates for ET3 vetting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> et3VettingStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setEt3Date(Et3VettingHelper.getEt3DatesInMarkdown(caseData));
        caseData.setEt3IsThereAnEt3Response(Et3VettingHelper.isThereAnEt3Response(caseData)
            ? YES
            : NO
        );
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * Called for the ET3 "Select Respondent" page. Creates a DynamicList containing a list of all the respondents
     * which the user will be able to select as part of the ET3 Vetting Process.
     * @param ccdRequest holds the request and case data
     * @param userToken used for authorization
     * @return this will call the response entity but will also display any error messages which occur.
     */
    @PostMapping(value = "/initEt3RespondentList", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for et3 vetting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initEt3RespondentList(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = Et3VettingHelper.populateRespondentDynamicList(caseData);
        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * Called for the ET3 "Did we receive the ET3 response in time?" page. Calculates whether the response is in time
     * by checking if the received date is before 28 days from the served date or the extension time.
     * @param ccdRequest holds the request and case data
     * @param userToken used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/calculateResponseInTime", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "calculate if the response was received in time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> calculateResponseInTime(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        if (YES.equals(caseData.getEt3IsThereAnEt3Response())) {
            errors = Et3VettingHelper.calculateResponseTime(caseData);
        }
        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * Called for the ET3 "Do the respondents name and address match" pages. Looks up the selected respondents name
     * and address and formats them for ExUI to display. Will show "None given" where this information is not available.
     * @param ccdRequest holds the request and case data
     * @param userToken used for authorization
     * @return Callback response entity with case data.
     */
    @PostMapping(value = "/initRespondentDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the details of the respondent for the respondent name and address pages")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initRespondentDetails(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et3VettingHelper.getRespondentNameAndAddress(caseData);

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * This method is used to display a message to the user once the submit button has been pressed. This will show the
     * user what the next steps are.
     * @param ccdRequest generic request from CCD
     * @param userToken authentication token to verify the user
     * @return this will return and display a message to the user on the next steps.
     */
    @PostMapping(value = "/processingComplete", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "display the next steps after ET3 Vetting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> processingComplete(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        // TODO refactor the PROCESSING_COMPLETE_HEADER variable. This will need to be refactored to include a
        //  hyperlink as part of the text. See RET-2020 for what the links should be once they have been added
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_body(PROCESSING_COMPLETE_HEADER)
                .build());
    }
}