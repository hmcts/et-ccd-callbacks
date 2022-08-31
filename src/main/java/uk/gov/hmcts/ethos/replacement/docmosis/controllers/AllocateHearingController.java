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
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.AllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandAllocateHearingService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@RequestMapping("/allocatehearing")
@Slf4j
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.UnnecessaryAnnotationValueElement", "PMD.LawOfDemeter"})
public class AllocateHearingController {
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final AllocateHearingService allocateHearingService;
    private final ScotlandAllocateHearingService scotlandAllocateHearingService;

    public AllocateHearingController(VerifyTokenService verifyTokenService,
                                     AllocateHearingService allocateHearingService,
                                     ScotlandAllocateHearingService scotlandAllocateHearingService) {
        this.verifyTokenService = verifyTokenService;
        this.allocateHearingService = allocateHearingService;
        this.scotlandAllocateHearingService = scotlandAllocateHearingService;
    }

    @PostMapping(value = "/initialiseHearings", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise hearings selection list")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initialiseHearingDynamicList(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        allocateHearingService.initialiseAllocateHearing(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/handleListingSelected", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update case data when a listing has been selected")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> handleListingSelected(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        var caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            allocateHearingService.handleListingSelected(caseData);
        } else if (Constants.SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            scotlandAllocateHearingService.handleListingSelected(caseData);
        } else {
            log.error("Unexpected case type id " + caseTypeId);
            return ResponseEntity.badRequest().build();
        }

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/handleManagingOfficeSelected", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update case data when a managing office has been selected")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> handleManagingOfficeSelected(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        if (!Constants.SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            log.error("Unexpected case type id {}", caseTypeId);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        scotlandAllocateHearingService.handleManagingOfficeSelected(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/populateRooms", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populate rooms selection list in case data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> populateRoomDynamicList(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        var caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            allocateHearingService.populateRooms(caseData);
        } else if (Constants.SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            scotlandAllocateHearingService.populateRooms(caseData);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update case data input in Allocate Hearing event")
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

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        var caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            allocateHearingService.updateCase(caseData);
        } else if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            scotlandAllocateHearingService.updateCase(caseData);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }
}
