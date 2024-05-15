package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

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
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultiplesDigitalCaseFileService;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;

@Slf4j
@RequestMapping("/multiples/dcf")
@RestController
@RequiredArgsConstructor
public class MultiplesDigitalCaseFileController {
    private final MultiplesDigitalCaseFileService multiplesDigitalCaseFileService;

    @PostMapping(path = "/selectDcf", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Select DCF configuration and documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> selectDcf(@RequestBody MultipleRequest multipleRequest,
                                                              @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                              String userToken) {

        log.info("About to start DCF for " + multipleRequest.getCaseDetails().getCaseId());

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        // Convert doc type from legacy to new before dcf
        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(multipleData);
        UploadDocumentHelper.setDocumentTypeForDocumentCollection(multipleData);
        multipleData.setCaseBundles(multiplesDigitalCaseFileService.createCaseFileRequest(multipleData));
        return multipleResponse(multipleData, null);
    }

    @PostMapping(path = "/aboutToSubmit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Stitch DCF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmit(@RequestBody MultipleRequest multipleRequest,
                                                                  @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                  String userToken) {

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        multipleData.setCaseBundles(multiplesDigitalCaseFileService.stitchCaseFile(
                multipleRequest.getCaseDetails(),
                userToken));
        log.info(" case bundles{}", multipleData.getCaseBundles().toString());
        DigitalCaseFileHelper.addDcfToDocumentCollection(multipleData);
        multipleData.setCaseBundles(null);
        return multipleResponse(multipleData, null);
    }

}
