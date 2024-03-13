package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralDataFromCaseData;

/**
 * REST controller for the Create Referral event pages, formats data appropriately for rendering on the front end.
 */
@Slf4j
@RequestMapping("/multiples/createReferral")
@RequiredArgsConstructor
@RestController
public class CreateReferralMultiplesController {

    private final VerifyTokenService verifyTokenService;
    private final ReferralService referralService;
    private final UserIdamService userIdamService;
    private final DocumentManagementService documentManagementService;

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    private static final String CREATE_REFERRAL_BODY = "<hr>"
            + "<h3>What happens next</h3>"
            + "<p>Your referral has been sent. Replies and instructions will appear in the "
            + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";

    /**
     * Called for the first page of the Create Multiples Referral event.
     * Populates the Referral hearing detail's section on the page.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for referral create")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                        @Content(mediaType = "application/json",
                                schema = @Schema(implementation = MultipleCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> initReferralHearingDetails(
            @RequestBody MultipleRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("ABOUT TO START CREATE REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleData multipleData = ccdRequest.getCaseDetails().getCaseData();
        CaseData caseData = new ObjectMapper().convertValue(ccdRequest.getCaseDetails().getCaseData(), CaseData.class);
        caseData.setReferralHearingDetails(ReferralHelper.populateHearingDetails(caseData));
        return multipleResponse(multipleData, null);
    }

    /**
     * Called for the email validation of the Create Multiples Referral event.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/validateReferentEmail", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for referral create")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                        @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MultipleCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> validateReferentEmail(
            @RequestBody MultipleRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("VALIDATE REFERENT EMAIL CREATE REFERRAL ---> " + LOG_MESSAGE
                + ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        MultipleDetails caseDetails = ccdRequest.getCaseDetails();
        MultipleData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();

        if (StringUtils.isNotEmpty(caseData.getReferentEmail())) {
            errors = ReferralHelper.validateEmail(caseData.getReferentEmail());

            if (CollectionUtils.isNotEmpty(caseData.getReferralDocument())) {
                ReferralHelper.addDocumentUploadErrors(caseData.getReferralDocument(), errors);
            }
        }

        return getMultipleCallbackRespEntity(errors, caseDetails);
    }

    /**
     * Called at the end of creating a multiples referral, takes the information saved in case data and stores it in the
     * referral collection.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
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
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmitReferralDetails(
            @RequestBody MultipleRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("ABOUT TO SUBMIT CREATE REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = new ObjectMapper().convertValue(ccdRequest.getCaseDetails().getCaseData(), CaseData.class);
        if ("Party not responded/compiled".equals(caseData.getReferralSubject())) {
            caseData.setReferralSubject("Party not responded/complied");
        }
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        String referralNumber = String.valueOf(ReferralHelper.getNextReferralNumber(caseData.getReferralCollection()));

        caseData.setReferredBy(String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()));
        DocumentInfo documentInfo = referralService.generateCRDocument(caseData,
                userToken, ccdRequest.getCaseDetails().getCaseTypeId());

        ReferralHelper.createReferral(
                caseData,
                String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()),
                this.documentManagementService.addDocumentToDocumentField(documentInfo));

        log.info("Event: Referral Email sent. "
                    + ". EventId: " + ccdRequest.getEventId()
                    + ". Referral number: " + referralNumber
                    + ". Emailed at: " + DateTime.now());

        clearReferralDataFromCaseData(caseData);

        MultipleData multipleData = new ObjectMapper().convertValue(
                ccdRequest.getCaseDetails().getCaseData(), MultipleData.class);
        multipleData.setReferralCollection(caseData.getReferralCollection());
        return ResponseEntity.ok(MultipleCallbackResponse.builder().data(multipleData).build());

    }

    /**
     * Called after submitting a create multiples referral event.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeCreateReferral", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the reply to referral event flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = MultipleCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<MultipleCallbackResponse> completeCreateReferral(
            @RequestBody MultipleRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("COMPLETE CREATE REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String body = String.format(CREATE_REFERRAL_BODY,
                ccdRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }
}
