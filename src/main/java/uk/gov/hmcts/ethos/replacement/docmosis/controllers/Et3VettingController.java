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
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.Et3VettingType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3VettingService;
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
@RequiredArgsConstructor
public class Et3VettingController {

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String PROCESSING_COMPLETE_HEADER = "<h1>ET3 Processing complete</h1>";
    public static final String PROCESSING_COMPLETE_BODY = "<h2>Do this next</h2>You must:"
            + "<ul><li>accept or reject the ET3 response or refer the response</li>"
            + "<li>add any changed or new information to case details</li></ul>";
    private final VerifyTokenService verifyTokenService;
    private final Et3VettingService et3VettingService;

    /**
     * Called for the ET3 "Select Respondent" page. Creates a DynamicList containing a list of all the respondents
     * which the user will be able to select as part of the ET3 Vetting Process.
     * @param ccdRequest holds the request and case data
     * @param userToken used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
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
        @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        et3VettingService.updateValuesOnObject(caseData, new Et3VettingType());
        List<String> errors = Et3VettingHelper.populateRespondentDynamicList(caseData);
        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * Called after respondent is selected on the ET3 vetting page, populates data needed for
     * ET3 vetting a particular respondent, will recover information if the respondent has
     * previously been vetted.
     * @param ccdRequest holds the request and case data
     * @param userToken used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/midPopulateRespondentEt3Response", consumes = APPLICATION_JSON_VALUE)
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
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        et3VettingService.restoreEt3VettingFromRespondentOntoCaseData(caseData);

        caseData.setEt3Date(Et3VettingHelper.getEt3DatesInMarkdown(caseData));
        caseData.setEt3IsThereAnEt3Response(Et3VettingHelper.isThereAnEt3Response(caseData)
            ? YES
            : NO
        );

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * Called for the ET3 "Did we receive the ET3 response in time?" page. Calculates whether the response is in time
     * by checking if the received date is before 28 days from the served date or the extension time.
     * @param ccdRequest holds the request and case data
     * @param userToken used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/midCalculateResponseInTime", consumes = APPLICATION_JSON_VALUE)
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
            @RequestHeader("Authorization") String userToken) {

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
    @PostMapping(value = "/midRespondentNameAndAddressTable", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a table with the respondent's name and address")
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
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et3VettingHelper.getRespondentNameAndAddress(caseData);

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * Finds listed hearings for a case and sets the hearing details for ExUI. Will display a table with the earliest
     * hearing date and track type or static text saying that there are no listings for the case.
     * @param ccdRequest holds the request and case data
     * @param userToken used for authorization
     * @return this will call the response entity.
     */
    @PostMapping(value = "/midHearingListedTable", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "check to see if a hearing has been listed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> checkHearingListed(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et3VettingHelper.setHearingListedForExUi(caseData);
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * Creates a table for ExUI representing the case's current tribunal and office.
     * @param ccdRequest holds the request and case data.
     * @param userToken used for authorization
     * @return this will call the response entity.
     */
    @PostMapping(value = "/midTransferApplicationTable", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Request a transfer of tribunal office")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> transferApplication(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et3VettingHelper.transferApplication(caseData);
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    /**
     * During processing the ET3 journey data is stored onto case data, this method saves that information onto the
     * respondent selected at the start of vetting. The leftover information on case data will be deleted. This method
     * will also generate a document for the Vetting process and save it onto a respondent
     * @param ccdRequest generic request from CCD
     * @param userToken authentication token to verify the user
     * @return this will return and display a message to the user on the next steps.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Save answers to the given specific respondent")
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

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DocumentInfo documentInfo = et3VettingService.generateEt3ProcessingDocument(caseData, userToken,
                ccdRequest.getCaseDetails().getCaseTypeId());
        et3VettingService.saveEt3VettingToRespondent(caseData, documentInfo);

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Generates the confirmation page for the ET3 vetting journey, with instructions on what to do next.
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
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        // TODO refactor the PROCESSING_COMPLETE_HEADER variable. This will need to be refactored to include a
        //  hyperlink as part of the text. See RET-2020 for what the links should be once they have been added
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_header(PROCESSING_COMPLETE_HEADER)
                .confirmation_body(PROCESSING_COMPLETE_BODY)
                .build());
    }
}