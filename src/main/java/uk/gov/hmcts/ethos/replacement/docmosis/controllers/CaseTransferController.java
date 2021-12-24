package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseTransferService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@Slf4j
public class CaseTransferController {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
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
        log.info("DYNAMIC LIST OFFICES ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        caseTransferService.populateCaseTransferOffices(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/createCaseTransfer", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "create a new Case in a different office.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Accessed successfully", response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> createCaseTransfer(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("CREATE CASE TRANSFER ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferService.createCaseTransfer(ccdRequest.getCaseDetails(), userToken);

        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

}
