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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3ResponseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;


/**
 * REST controller for the ET3 Response pages, formats data appropriately for rendering on the front end.
 */
@Slf4j
@RequestMapping("/et3Response")
@RestController
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class Et3ResponseController {

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String PROCESSING_COMPLETE_HEADER = "<h1>ET3 application complete</h1>";
    private static final String PROCESSING_COMPLETE_BODY =
        "<h3>What happens next</h3>\r\n\r\nYou should receive confirmation from the tribunal office to process your"
            + " application within 5 working days. If you have not heard from them within 5 days, "
            + "contact the office directly.";
    private static final String SECTION_COMPLETE_BODY =
            "You may want to complete the rest of the ET3 Form using the links below"
            + "<br><a href=\"/cases/case-details/%s/trigger/et3Response/et3Response1\">ET3 - Respondent Details</a>"
            + "<br><a href=\"/cases/case-details/%s/trigger/et3ResponseEmploymentDetails/et3ResponseEmploymentDetails1"
            + "\">ET3 - Employment Details</a>"
            + "<br><a href=\"/cases/case-details/%s/trigger/et3ResponseClaimDetails/et3ResponseClaimDetails1\">ET3 - "
            + "Claim Details</a>";
    private final VerifyTokenService verifyTokenService;
    private final Et3ResponseService et3ResponseService;

    /**
     * Called at the start of the ET3 Response journey. 
     * Sets hidden inset fields to YES to enable inset text functionality in ExUI.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for et3 response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initEt3Response(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setEt3ResponseShowInset(YES);
        caseData.setEt3ResponseNameShowInset(YES);
        caseData.setEt3ResponseClaimantName(Et3ResponseHelper.formatClaimantNameForHtml(caseData));
        List<String> errors = Et3ResponseHelper.createDynamicListSelection(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/validateRespondent", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "validate dates are correct for employment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateRespondent(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = Et3ResponseHelper.validateRespondents(caseData);
        Et3ResponseHelper.reloadDataOntoEt3(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }


    /**
     * Called when trying to submit on the ET3 Employment Dates page.
     * Validates start and end dates.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/midEmploymentDates", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "validate dates are correct for employment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midEmploymentDates(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/submitSection", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Save answers to the given specific respondent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitSection(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et3ResponseHelper.addEt3DataToRespondent(caseData, ccdRequest.getEventId());
        Et3ResponseHelper.resetEt3FormFields(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/sectionComplete", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "display the next steps after ET3 response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> sectionComplete(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String ccdId = ccdRequest.getCaseDetails().getCaseId();
        String body = String.format(SECTION_COMPLETE_BODY, ccdId, ccdId, ccdId);
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_body(body)
                .build());
    }


    /**
     * Generates ET3 Response document and add the ET3 Fields to each respondent.
     * @param ccdRequest generic request from CCD
     * @param userToken authentication token to verify the user
     * @return Callback response entity with case data attached.
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
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DocumentInfo documentInfo = et3ResponseService.generateEt3ResponseDocument(caseData, userToken,
            ccdRequest.getCaseDetails().getCaseTypeId());
        et3ResponseService.saveEt3ResponseDocument(caseData, documentInfo);
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        FlagsImageHelper.buildFlagsImageFileName(ccdRequest.getCaseDetails().getCaseTypeId(), caseData);
        Et3ResponseHelper.resetEt3FormFields(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Generates the confirmation page for the ET3 response journey, with instructions on what to do next.
     * @param ccdRequest generic request from CCD
     * @param userToken authentication token to verify the user
     * @return this will return and display a message to the user on the next steps.
     */
    @PostMapping(value = "/processingComplete", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "display the next steps after ET3 response")
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

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(ccdRequest.getCaseDetails().getCaseData())
            .confirmation_header(PROCESSING_COMPLETE_HEADER)
            .confirmation_body(PROCESSING_COMPLETE_BODY)
            .build());
    }

    @PostMapping(value = "/startSubmitEt3", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "display the next steps after ET3 response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> startSubmitEt3(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = Et3ResponseHelper.et3SubmitRespondents(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/reloadSubmitData", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "display the next steps after ET3 response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> reloadSubmitData(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et3ResponseHelper.reloadSubmitOntoEt3(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }
}