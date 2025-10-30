package uk.gov.hmcts.ethos.replacement.docmosis.controllers.notifications.admin;

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
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the SendNotification event.
 */
@Slf4j
@RequestMapping("/sendNotification")
@RestController
@RequiredArgsConstructor
public class SendNotificationController {

    private final SendNotificationService sendNotificationService;

    private static final String SUBMITTED_BODY_TEMPLATE = """
            ### What happens next
    
            The selected parties will receive the notification.
    
            You can view the notification in the <a href="/cases/case-details/%s#Notifications" target="_blank">
            Notifications tab (opens in a new tab)</a>
    
            Another notification can be sent <a href="/cases/case-details/%s/trigger/sendNotification/sendNotification1">
            using this link</a>
            """;

    /**
     * send Notification about to start.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToStart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        sendNotificationService.populateHearingSelection(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Send Notification about to submit.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToSubmit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        sendNotificationService.createSendNotification(caseData);
        sendNotificationService.sendNotifyEmails(caseDetails);
        sendNotificationService.clearSendNotificationFields(caseData);
        caseData.setDraftAndSignJudgement(null);
        return getCallbackRespEntityNoErrors(caseData);
    }


    /**
     * Returns data needed to populate the submitted page.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "submitted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        String caseId = ccdRequest.getCaseDetails().getCaseId();
        String body = String.format(SUBMITTED_BODY_TEMPLATE, caseId, caseId);

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }

}
