package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultiplesSendNotificationService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Slf4j
@RequestMapping("/multiples/sendNotification")
@RestController
@RequiredArgsConstructor
public class MultiplesSendNotificationController {
    private final MultiplesSendNotificationService multiplesSendNotificationService;

    /**
     * Send Notification about to start with hearing details from lead case.
     *
     * @param multipleRequest holds the request and case data
     * @param userToken user token
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToStart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToStart(@RequestBody MultipleRequest multipleRequest,
                                                                 @RequestHeader("Authorization") String userToken) {
        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        multiplesSendNotificationService.setHearingDetailsFromLeadCase(multipleRequest.getCaseDetails(), errors);
        multiplesSendNotificationService.setMultipleWithExcelFileData(multipleDetails, userToken, errors);
        return getMultipleCallbackRespEntity(errors, multipleRequest.getCaseDetails());
    }

    /**
     * Send Notification about to submit from the Multiple.
     *
     * @param multipleRequest holds the request and case data
     * @param userToken       used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToSubmit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmit(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {

        MultipleDetails caseDetails = multipleRequest.getCaseDetails();
        MultipleData caseData = caseDetails.getCaseData();
        List<String> errors = new ArrayList<>();
        multiplesSendNotificationService.sendNotificationToSingles(
                caseData,
                caseDetails,
                userToken,
                errors
        );

        multiplesSendNotificationService.setSendNotificationDocumentsToDocumentCollection(caseData);
        multiplesSendNotificationService.clearSendNotificationFields(caseData);
        return getMultipleCallbackRespEntity(errors, caseDetails);
    }

    /**
     * Returns data needed to populate the submitted page.
     *
     * @param multipleRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "submitted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> submitted(@RequestBody MultipleRequest multipleRequest) {

        String caseId = multipleRequest.getCaseDetails().getCaseId();
        String body = String.format("""
                ### What happens next
                The selected parties will receive the notification. </br>
                The notifications will be stored on the individual case, not on the Multiple. </br>
                Another notification can be sent <a href="/cases/case-details/%s/trigger/sendNotification/sendNotification1">using this link</a>
                """, caseId);

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }
}
