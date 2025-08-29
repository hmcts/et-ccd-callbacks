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
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/notificationDocument")
@RestController
@RequiredArgsConstructor
public class NotificationDocumentController {

    private final SendNotificationService sendNotificationService;
    private final RespondNotificationService respondNotificationService;

    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populate the notification dynamic list for document generation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        respondNotificationService.populateSendNotificationSelection(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit the request to generate a document for a notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DocumentInfo documentInfo = sendNotificationService.createNotificationSummary(caseData, userToken,
                ccdRequest.getCaseDetails().getCaseTypeId());
        caseData.setDocMarkUp(documentInfo.getMarkUp());
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Show the generated document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_body("Download the notification summary from this link: "
                                   + ccdRequest.getCaseDetails().getCaseData().getDocMarkUp())
                .build());
    }
}
