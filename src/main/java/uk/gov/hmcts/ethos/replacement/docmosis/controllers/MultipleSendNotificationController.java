package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

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
import uk.gov.hmcts.ecm.common.model.servicebus.SendNotificationsDto;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.PersistentQHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Slf4j
@RequestMapping("/multipleSendNotification")
@RestController
@RequiredArgsConstructor
public class MultipleSendNotificationController {
    private final SendNotificationService sendNotificationService;
    private final CreateUpdatesBusSender createUpdatesBusSender;

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;

    /**
     * send Notification about to start.
     *
     * @param multipleRequest holds the request and case data
     * @param userToken       used for authorization
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
    public ResponseEntity<MultipleCallbackResponse> aboutToStart(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        List<String> errors = new ArrayList<>();
        // TODO: Get hearing details from lead case

        log.warn("About to start");

        return getMultipleCallbackRespEntity(errors, multipleRequest.getCaseDetails());
    }

    /**
     * Send Notification about to submit.
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
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmit(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails caseDetails = multipleRequest.getCaseDetails();
        MultipleData caseData = caseDetails.getCaseData();

        SendNotificationType sendNotificationType = new SendNotificationType();
        log.warn("About to submit" + sendNotificationType.getSendNotificationTitle());
        NotificationHelper.setSendNotificationValues(caseData, sendNotificationType);
        sendNotificationService.clearSendNotificationFields(caseData);
        List<String> ethosCaseRefCollection = new ArrayList<>();
        caseData.getCaseIdCollection().forEach(caseId -> ethosCaseRefCollection.add(caseId.getValue().toString()));
        SendNotificationsDto sendNotificationsDto =
                SendNotificationsDto.builder()
                        .caseTypeId(caseDetails.getCaseTypeId())
                        .jurisdiction(caseDetails.getJurisdiction())
                        .multipleRef(caseData.getMultipleReference())
                        .ethosCaseRefCollection(ethosCaseRefCollection)
                        .sendNotification(sendNotificationType).build();

        DataModelParent dataModelParent = PersistentQHelper.getDetachDataModel();

        List<String> errors = new ArrayList<>();
        createUpdatesBusSender.sendNotificationUpdatesToQueue(sendNotificationsDto,
                dataModelParent,
                errors,
                String.valueOf(ethosCaseRefCollection.size()));
        return getMultipleCallbackRespEntity(errors, multipleRequest.getCaseDetails());
    }

    /**
     * Returns data needed to populate the submitted page.
     *
     * @param multipleRequest holds the request and case data
     * @param userToken       used for authorization
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
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        String caseId = multipleRequest.getCaseDetails().getCaseId();
        String body = String.format("""
                ### What happens next
                The selected parties will receive the notification. </br>
                The notifications will be stored on the individual case, not on the Multiple. </br>
                Another notification can be sent <a href="/cases/case-details/%s/trigger/sendNotification/sendNotification1">using this link</a>
                """, caseId);

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }
}
