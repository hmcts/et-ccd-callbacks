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
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferOfficeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@RequestMapping("/caseTransfer")
@Slf4j
public class CaseTransferController {

    private static final String LOG_MESSAGE = "{} received notification request for case reference : {}";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final CaseTransferService caseTransferService;

    public CaseTransferController(VerifyTokenService verifyTokenService, CaseTransferService caseTransferService) {
        this.verifyTokenService = verifyTokenService;
        this.caseTransferService = caseTransferService;
    }

    @PostMapping(value = "/dynamicListOffices", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "populates all offices except the current one in dynamic lists.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> dynamicListOffices(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        CaseTransferOfficeService.populateOfficeOptions(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/transferSameCountry", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transfer a case to another office within the same country")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> transferSameCountry(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, "CASE TRANSFER SAME COUNTRY ---> ", ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferService.caseTransferSameCountry(ccdRequest.getCaseDetails(), userToken);

        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/transferSameCountryEccLinkedCase", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transfer a ECC linked case to another office within the same country")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> transferSameCountryEccLinkedCase(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, "CASE TRANSFER SAME COUNTRY ECC LINKED CASE ---> ",
                ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var errors = caseTransferService.caseTransferSameCountryEccLinkedCase(ccdRequest.getCaseDetails(), userToken);

        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/transferDifferentCountry", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transfer a case to another office in a different country")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> transferDifferentCountry(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, "CASE TRANSFER DIFFERENT COUNTRY ---> ", ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferService.caseTransferDifferentCountry(ccdRequest.getCaseDetails(), userToken);

        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

}
