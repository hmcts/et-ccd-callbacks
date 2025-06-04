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
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ET3DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3NotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ServingService;

import java.io.IOException;
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

    /**
     * Handles the CCD "about to start" callback for ET3 notification processing.
     * This endpoint is triggered by the case management system when the event is initiated,
     * and performs validation to ensure that at least one respondent in the case has a selected response status.
     * If no such respondent is found, an error message is returned to prevent the user from proceeding.
     *
     * @param  userToken        Used for authorisation
     *
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds ET1 case data
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/aboutToStart", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for bundles respondent")
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
        List<String> errors = new ArrayList<>();
        if (ET3DocumentHelper.containsNoRespondentWithResponseStatus(caseData.getRespondentCollection())) {
            errors.add("At least one respondent must have a selected response status.");
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }

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
            errors.add("Upload at least one document. All uploaded documents must be accepted or rejected.");

        }

        if (!ET3DocumentHelper.areET3DocumentsConsistentWithRespondentResponses(caseData.getRespondentCollection(),
                caseData.getEt3NotificationDocCollection())) {
            errors.add("Please upload the appropriate ET3 document for each respondent’s response status.");
        }
        caseData.setEt3OtherTypeDocumentName(
            servingService.generateOtherTypeDocumentLink(caseData.getEt3NotificationDocCollection()));
        caseData.setEt3EmailLinkToAcas(servingService.generateEmailLinkToAcas(caseData, true));
        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Handles the CCD "about to submit" callback for the respondent bundle ET3 process.
     * <p>
     * This endpoint is triggered just before the case data is submitted. It performs the following actions:
     * <ul>
     *     <li>Adds or removes ET3 documents based on respondent responses</li>
     *     <li>Sets ET3 notification accepted dates</li>
     *     <li>Clones and updates ET3 notification documents into the main document collection</li>
     *     <li>Performs a case update through the {@code caseUpdateForCaseWorkerService}</li>
     * </ul>
     * <p>
     * If an exception occurs (e.g., during document cloning), the error is captured and returned as part of the
     * callback response.
     *
     * @param ccdRequest the CCD callback request containing case details and data
     * @param userToken  the authorization token of the user triggering the event
     * @return a {@link ResponseEntity} containing a {@link CCDCallbackResponse}, which includes:
     *         <ul>
     *             <li>Validation errors, if any occurred during processing</li>
     *             <li>Updated case data, if processing was successful</li>
     *         </ul>
     */
    @PostMapping(value = "/aboutToSubmit", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for bundles respondent")
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
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        ET3DocumentHelper.addOrRemoveET3Documents(caseData);
        Et3ResponseHelper.setEt3NotificationAcceptedDates(caseData);
        try {
            ET3DocumentHelper.addET3NotificationDocumentsToDocumentCollection(caseData);
        } catch (IOException e) {
            errors.add(e.getMessage());
        }
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
        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(ccdRequest.getCaseDetails().getCaseData())
            .confirmation_header(String.format(SUBMITTED_HEADER, NotificationHelper.getParties(caseData)))
            .confirmation_body("<span></span>")
            .build());
    }

}
