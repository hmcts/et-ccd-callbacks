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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DigitalCaseFileService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dcf")
public class DigitalCaseFileController {

    private final DigitalCaseFileService digitalCaseFileService;

    @PostMapping(path = "/asyncAboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(description = "Submit DCF asynchronously")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = APPLICATION_JSON_VALUE, 
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitAsync(@RequestBody CCDRequest ccdRequest,
                                                                  @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                  String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        digitalCaseFileService.createUploadRemoveDcf(userToken, ccdRequest.getCaseDetails());
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(path = "/asyncCompleteAboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(description = "Submit DCF asynchronously")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> asyncCompleteAboutToSubmit(
            @RequestBody CCDRequest ccdRequest, @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DigitalCaseFileHelper.addDcfToDocumentCollection(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

}
