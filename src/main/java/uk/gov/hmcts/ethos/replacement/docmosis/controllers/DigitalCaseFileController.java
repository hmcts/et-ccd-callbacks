package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DigitalCaseFileService;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dcf")
public class DigitalCaseFileController {

    private final DigitalCaseFileService digitalCaseFileService;

    @PostMapping(path = "/selectDcf", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Select DCF configuration and documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> selectDcf(@RequestBody CCDRequest ccdRequest,
                                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION)
                                                            String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        // Convert doc type from legacy to new before dcf
        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
        UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
        caseData.setCaseBundles(digitalCaseFileService.createCaseFileRequest(caseData));
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(path = "/aboutToSubmit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Stitch DCF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(@RequestBody CCDRequest ccdRequest,
                                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION)
                                                            String userToken) {

        List<String> errors = new ArrayList<>();
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        digitalCaseFileService.stitchCaseFile(caseDetails, userToken, errors);

        return getCallbackRespEntityErrors(errors, caseDetails.getCaseData());
    }
}
