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
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BundlingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bundle")
public class BundlingController {

    @Autowired
    private BundlingService bundlingService;
    @Autowired
    private VerifyTokenService verifyTokenService;

    private static final String INVALID_TOKEN = "Invalid Token {}";

    @PostMapping(path = "/selectBundle", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
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

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        // Convert doc type from legacy to new before bundling
        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseDetails.getCaseData());
        UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseDetails.getCaseData());
        caseDetails.getCaseData().setCaseBundles(null);
        caseDetails.getCaseData().setCaseBundles(bundlingService.createBundleRequest(caseDetails, userToken));
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(path = "/aboutToSubmit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Stitch bundle")
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

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setCaseBundles(bundlingService.stitchBundle(ccdRequest.getCaseDetails(), userToken));
        BundlingHelper.addBundleToDocumentCollection(caseData);
        caseData.setCaseBundles(null);
        return getCallbackRespEntityNoErrors(caseData);
    }
}
