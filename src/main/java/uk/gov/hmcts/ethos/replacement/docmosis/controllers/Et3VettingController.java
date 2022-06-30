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
 * Rest controller for the ET3 Vetting pages, provides access to the state of the ET3 Response
 * and formats data appropriately for rendering on the front end.
 */
@Slf4j
@RequestMapping("/et3Vetting")
@RestController
public class Et3VettingController {
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;

    public Et3VettingController(VerifyTokenService verifyTokenService) {
        this.verifyTokenService = verifyTokenService;
    }

    /**
     * Method calls when the Is there an ET3 Response?" page is loaded, will generate a table
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
     * Creates a DynamicList containing a list of all the respondents which the user will be able to select as part of
     * the ET3 Vetting Process.
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

}