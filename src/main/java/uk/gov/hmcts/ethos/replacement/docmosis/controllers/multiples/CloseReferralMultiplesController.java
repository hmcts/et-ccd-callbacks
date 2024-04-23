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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;

import java.io.IOException;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralDataFromCaseData;

@Slf4j
@RequestMapping("/multiples/closeReferral")
@RestController
@RequiredArgsConstructor
public class CloseReferralMultiplesController {
    private final CaseLookupService caseLookupService;
    private static final String CLOSE_REFERRAL_BODY = "<hr>"
            + "<h3>What happens next</h3>"
            + "<p>We have closed this referral. You can still view it in the "
            + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";

    /**
     * Called for the first page of the Close Referral event.
     * Populates the Referral select dropdown.
     *
     * @param multipleRequest holds the request and case data
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for referral reply")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MultipleCallbackResponse.class))
                }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToStart(
            @RequestBody MultipleRequest multipleRequest) {

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        clearReferralDataFromCaseData(multipleData);
        multipleData.setSelectReferral(
                ReferralHelper.populateSelectReferralDropdown(multipleData.getReferralCollection())
        );
        return multipleResponse(multipleData, null);
    }

    /**
     * Called for the second page of the Close Referral event.
     * Populates the Referral hearing and reply details section on the page.
     *
     * @param multipleRequest holds the request and case data
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/initHearingAndReferralDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for close referral event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> initHearingDetailsForCloseReferral(
            @RequestBody MultipleRequest multipleRequest) throws IOException {
        MultipleDetails details = multipleRequest.getCaseDetails();
        MultipleData multipleData = details.getCaseData();
        CaseData leadCase = caseLookupService.getLeadCaseFromMultipleAsAdmin(details);
        multipleData.setCloseReferralHearingDetails(
                ReferralHelper.populateHearingReferralDetails(multipleData, leadCase)
        );

        return multipleResponse(multipleData, null);
    }

    /**
     * Called at the end of Close Referral event, it sets the referral status to Closed.
     *
     * @param multipleRequest holds the request and case data
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmitCloseReferral(
            @RequestBody MultipleRequest multipleRequest) {

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        ReferralHelper.addReferralDocumentToDocumentCollection(multipleData);
        ReferralHelper.setReferralStatusToClosed(multipleData);
        ReferralHelper.clearCloseReferralDataFromCaseData(multipleData);
        return multipleResponse(multipleData, null);
    }

    /**
     * Called after submitting a close referral event.
     *
     * @param multipleRequest holds the request and case data
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeCloseReferral", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the close referral event flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MultipleCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<MultipleCallbackResponse> completeInitialConsideration(
            @RequestBody CCDRequest multipleRequest) {

        String body = String.format(CLOSE_REFERRAL_BODY,
                multipleRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }
}
