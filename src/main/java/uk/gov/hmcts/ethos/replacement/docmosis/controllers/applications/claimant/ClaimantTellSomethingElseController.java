package uk.gov.hmcts.ethos.replacement.docmosis.controllers.applications.claimant;

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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.applications.TseViewApplicationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.claimant.ClaimantTellSomethingElseService;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/claimantTSE")
public class ClaimantTellSomethingElseController {

    private final ClaimantTellSomethingElseService claimantTseService;
    private final TseService tseService;

    /**
     * Callback endpoint to be called when the event ClaimantTSE is about to start.
     *
     * @param ccdRequest the request
     * @return Callback response entity
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStartClaimantTSE(
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (!Helper.isRespondentSystemUser(caseData)) {
            caseData.setClaimantTseRespNotAvailable(YES);
        } else {
            caseData.setClaimantTseRespNotAvailable(NO);
        }
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * This service is for validate Give Details are not all blank.
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds ET1 case data
     *
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/validateGiveDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Claimant Tell Something Else About To Start Event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateGiveDetails(
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = claimantTseService.validateGiveDetails(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitClaimantTSE(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();

        claimantTseService.populateClaimantTse(caseData);
        tseService.createApplication(caseData, CLAIMANT_REP_TITLE);
        claimantTseService.generateAndAddApplicationPdf(caseData, userToken, caseDetails.getCaseTypeId());

        // send email notifications
        if (Helper.isRespondentSystemUser(caseData)) {
            claimantTseService.sendRespondentsEmail(caseDetails);
        }
        claimantTseService.sendAcknowledgementEmail(caseDetails, userToken);
        claimantTseService.sendAdminEmail(caseDetails);

        // clear application data
        tseService.clearApplicationData(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called after submitting a create application event.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeApplication", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the reply to referral event flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> completeApplication(
            @RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        String body = claimantTseService.buildApplicationCompleteResponse(caseData);

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }

    @PostMapping(value = "/displayTable", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> displayClaimantApplicationsTable(
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setClaimantTseTableMarkUp(claimantTseService.generateClaimantApplicationTableMarkdown(caseData));

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Resets the dynamic list for select an application to view either an open or closed application.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/viewApplicationsAboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Resets the dynamic list for select an application to to view")
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
            @RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setTseViewApplicationSelect(null);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Populates the dynamic list of the applications open or closed on a case.
     * Called after 'view an application' is clicked and open or closed has been selected.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */

    @PostMapping(value = "/midPopulateChooseApplication", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the  dynamic list of the open or closed applications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> populateChooseApplication(

            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setTseViewApplicationSelect(
                TseViewApplicationHelper.populateOpenOrClosedApplications(caseData, true));
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/midPopulateSelectedApplicationData", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates data for the selected application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> populateSelectedApplicationData(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setTseApplicationSummaryAndResponsesMarkup(
                tseService.formatViewApplication(caseData, userToken, false)
        );
        return getCallbackRespEntityNoErrors(caseData);
    }
}
