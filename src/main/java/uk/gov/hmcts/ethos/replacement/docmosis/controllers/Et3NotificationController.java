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
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ET3DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseUpdateForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3NotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ServingService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/et3Notification")
public class Et3NotificationController {

    private static final String SUBMITTED_HEADER =
        "<h1>Documents submitted</h1>\r\n\r\n<h5>We have notified the following parties:</h5>\r\n\r\n<h3>%s</h3>";

    private final ServingService servingService;
    private final Et3NotificationService et3NotificationService;
    private final CaseUpdateForCaseWorkerService caseUpdateForCaseWorkerService;

    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has caseData as an object.
     * @param  userToken        Used for authorisation
     *
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds ET1 case data
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/midUploadDocuments", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "return serving document other type names")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> et3Notification(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        if (ET3DocumentHelper.hasInconsistentAcceptanceStatus(caseData.getEt3NotificationDocCollection())) {
            errors.add("Upload at least one document. All uploaded documents must be accepted or not accepted.");
        }
        if (ET3DocumentHelper.containsNoRespondentWithResponseStatus(caseData.getRespondentCollection())) {
            errors.add("At least one respondent must have a selected response status.");
        }
        caseData.setEt3OtherTypeDocumentName(
            servingService.generateOtherTypeDocumentLink(caseData.getEt3NotificationDocCollection()));
        caseData.setEt3EmailLinkToAcas(servingService.generateEmailLinkToAcas(caseData, true));
        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Builds the confirmation screen and sends email notifications.
     */
    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "display final screen and send notifications to relevant parties")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> et3NotificationSubmitted(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        et3NotificationService.sendNotifications(ccdRequest.getCaseDetails());
        ET3DocumentHelper.addOrRemoveET3Documents(caseData);
        Et3ResponseHelper.setEt3NotificationAcceptedDates(caseData);
        SubmitEvent submitEvent = caseUpdateForCaseWorkerService.caseUpdateRequest(ccdRequest, userToken);
        log.info("Case updated correctly with id: " + submitEvent.getCaseId());
        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(ccdRequest.getCaseDetails().getCaseData())
            .confirmation_header(String.format(SUBMITTED_HEADER, NotificationHelper.getParties(caseData)))
            .confirmation_body("<span></span>")
            .build());
    }

}
