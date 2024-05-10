package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.CaseNotesService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;

@Slf4j
@RequestMapping("/multiples/caseNotes")
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
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToSubmit")
    @ApiResponse(responseCode = "200", description = "Accessed successfully",
        content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MultipleCallbackResponse.class))
        })
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmitCaseNotes(
            @RequestBody MultipleRequest multipleRequest) {

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        caseNotesService.addCaseNote(multipleData);
        return multipleResponse(multipleData, null);
    }
}
