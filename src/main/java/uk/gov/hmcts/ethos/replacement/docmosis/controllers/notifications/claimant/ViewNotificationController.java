package uk.gov.hmcts.ethos.replacement.docmosis.controllers.notifications.claimant;

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
import uk.gov.hmcts.ethos.replacement.docmosis.service.ProvideSomethingElseViewService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/claimantViewNotification")
@RestController
@RequiredArgsConstructor
public class ViewNotificationController {

    private final ProvideSomethingElseViewService provideSomethingElseViewService;

    @PostMapping(value = "/all/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Generates the notifications table for the claimant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> allNotificationsAboutToStart(
            @RequestBody
            CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setPseViewNotifications(
                provideSomethingElseViewService.generateViewNotificationsMarkdown(caseData, CLAIMANT_TITLE));
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/all/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Generates the notifications table for the claimant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> allNotificationsAboutToSubmit(
            @RequestBody
            CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        // Clear the notifications table to reduce the size of the payload
        caseData.setPseViewNotifications(null);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the dynamic list for Select a judgment, order or notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setClaimantSelectNotification(provideSomethingElseViewService.populateSelectDropdownView(caseData,
                CLAIMANT_TITLE));

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/midDetailsTable", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Mid Event for initial Request/Order details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midDetailsTable(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setClaimantNotificationTableMarkdown(
                provideSomethingElseViewService.initialOrdReqDetailsTableMarkUp(caseData, CLAIMANT_TITLE));
        return getCallbackRespEntityNoErrors(caseData);
    }
}
