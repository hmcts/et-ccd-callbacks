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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BundlesCallbackHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesRespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/bundlesRespondent")
@RestController
@RequiredArgsConstructor
public class BundlesRespondentController {

    private final BundlesRespondentService bundlesRespondentService;
    private final SendNotificationService sendNotificationService;

    /**
     * Called at the start of Bundles Respondent Prepare Doc for Hearing journey.
     * Sets hidden inset fields to YES to enable inset text functionality in ExUI.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
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
    public ResponseEntity<CCDCallbackResponse> aboutToStart(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setBundlesRespondentPrepareDocNotesShow(YES);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * About to Submit for Bundles Respondent Prepare Doc for Hearing journey.
     *
     * @param ccdRequest generic request from CCD
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "About to Submit for bundles respondent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        bundlesRespondentService.addToBundlesCollection(caseData);
        bundlesRespondentService.clearInputData(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Populates the hearing list on page 3 and validates the length of text input.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/midPopulateHearings", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the hearing list on page 3.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midPopulateHearings(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        bundlesRespondentService.populateSelectHearings(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Validates the uploaded file is a PDF.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/midValidateUpload", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Validates the uploaded file is a PDF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midValidateUpload(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = bundlesRespondentService.validateFileUpload(caseData);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Renders data for the submitted page.
     */
    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Renders data for the submitted page.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitted(@RequestBody CCDRequest ccdRequest) {
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        sendNotificationService.notify(caseDetails);
        return BundlesCallbackHelper.buildSubmittedResponse(ccdRequest);
    }

    @PostMapping(value = "/removeHearingBundle", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove a hearing bundle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Removed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> removeHearingBundle(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        try {
            bundlesRespondentService.removeHearingBundles(caseData);
            bundlesRespondentService.clearInputData(caseData);
            return getCallbackRespEntityNoErrors(caseData);
        } catch (Exception e) {
            log.error("Error removing hearing bundle", e);
            return getCallbackRespEntityErrors(List.of(e.getMessage()), caseData);
        }
    }

    /**
     * Populates the hearing bundle list on page 2 based on the party selected.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/midPopulateRemoveHearingBundles", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the hearing bundle list on page 2")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midPopulateRemoveHearingBundles(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        bundlesRespondentService.populateSelectRemoveHearingBundle(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }
}
