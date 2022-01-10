package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferOfficeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferDifferentCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferSameCountryService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@RestController
@RequestMapping("/caseTransferMultiples")
@Slf4j
public class CaseTransferMultiplesController {

    private static final String LOG_MESSAGE = "{} received notification request for case reference : {}";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final MultipleTransferSameCountryService multipleTransferSameCountryService;
    private final MultipleTransferDifferentCountryService multipleTransferDifferentCountryService;

    public CaseTransferMultiplesController(
            VerifyTokenService verifyTokenService,
            MultipleTransferSameCountryService multipleTransferSameCountryService,
            MultipleTransferDifferentCountryService multipleTransferDifferentCountryService) {
        this.verifyTokenService = verifyTokenService;
        this.multipleTransferSameCountryService = multipleTransferSameCountryService;
        this.multipleTransferDifferentCountryService = multipleTransferDifferentCountryService;
    }

    @PostMapping(value = "/initTransferToEnglandWales", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Initialise case for transfer to England/Wales")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> initTransferToEnglandWales(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var multipleData = multipleRequest.getCaseDetails().getCaseData();
        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(multipleData);

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .data(multipleData)
                .build());
    }

    @PostMapping(value = "/initTransferToScotland", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Initialise case for transfer to Scotland")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> initTransferToScotland(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var multipleData = multipleRequest.getCaseDetails().getCaseData();
        CaseTransferOfficeService.populateTransferToScotlandOfficeOptions(multipleData);

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .data(multipleData)
                .build());
    }

    @PostMapping(value = "/transferSameCountry", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transfer a multiple and its cases to another office within the same country")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = MultipleCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> transferSameCountry(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, "MULTIPLE TRANSFER SAME COUNTRY ---> ", multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var multipleDetails = multipleRequest.getCaseDetails();
        var errors = multipleTransferSameCountryService.transferMultiple(multipleDetails, userToken);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/transferDifferentCountry", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transfer a multiple and its cases to another office in a different country")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = MultipleCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> transferDifferentCountry(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, "MULTIPLE TRANSFER DIFFERENT COUNTRY ---> ",
                multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var multipleDetails = multipleRequest.getCaseDetails();
        var errors = multipleTransferDifferentCountryService.transferMultiple(multipleDetails, userToken);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }
}
