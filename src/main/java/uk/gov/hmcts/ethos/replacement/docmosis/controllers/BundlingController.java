package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlingService;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bundle")
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class BundlingController {

    @Autowired
    private BundlingService bundlingService;

    @PostMapping(path = "/createBundle", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Create bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> createBundle(@RequestBody CCDRequest ccdRequest,
                                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION)
                                                             String userToken) {
        ccdRequest.getCaseDetails().getCaseData().setCaseBundles(
                bundlingService.createBundleRequest(ccdRequest.getCaseDetails(), userToken));
        log.info(String.valueOf(ccdRequest));
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(path = "/stitchBundle", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Create bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> stitchBundle(@RequestBody CCDRequest ccdRequest,
                                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION)
                                                            String userToken) {
        ccdRequest.getCaseDetails().getCaseData().setCaseBundles(
                bundlingService.stitchBundle(ccdRequest.getCaseDetails(), userToken));
        log.info(String.valueOf(ccdRequest));
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

}