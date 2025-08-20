package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseNotesService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;

@Slf4j
@RequestMapping("/caseNotes")
@RestController
@RequiredArgsConstructor
public class CaseNotesController {
    private final CaseNotesService caseNotesService;

    /**
     * Saves note to collection on multiples case.
     *
     * @param multipleRequest holds the request and case data
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/multiples/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToSubmit")
    @ApiResponse(responseCode = "200", description = "Accessed successfully",
        content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MultipleCallbackResponse.class))
        })
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmitMultiplesCaseNotes(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        caseNotesService.addCaseNote(multipleData, userToken);
        return multipleResponse(multipleData, null);
    }

    /**
     * Saves note to collection on singles case.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/singles/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToSubmit")
    @ApiResponse(responseCode = "200", description = "Accessed successfully",
        content = {
            @Content(mediaType = "application/json",
                schema = @Schema(implementation = CCDCallbackResponse.class))
        })
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitSinglesCaseNotes(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseNotesService.addCaseNote(caseData, userToken);
        return getCallbackRespEntityNoErrors(caseData);
    }
}
