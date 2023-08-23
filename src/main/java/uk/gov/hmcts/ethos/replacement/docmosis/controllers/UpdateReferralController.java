package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import java.util.ArrayList;
import java.util.List;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearUpdateReferralDataFromCaseData;

/**
 * REST controller for the Update Referral event pages, formats data appropriately for rendering on the front end.
 */
@Slf4j
@RequestMapping("/updateReferral")
@RestController
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement", "PMD.ExcessiveImports"})
public class UpdateReferralController {
    private final VerifyTokenService verifyTokenService;
    private final UserService userService;
    private final EmailService emailService;
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final ReferralService referralService;
    private final DocumentManagementService documentManagementService;
    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
    @Value("${template.referral}")
    private String referralTemplateId;

    /**
     * Called for the first page of the Update Referral event.
     * Populates the Referral hearing detail's section on the page.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for referral update")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> updateReferralAboutToStart(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {
        log.info("ABOUT TO START UPDATE REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setReferralHearingDetails(ReferralHelper.populateHearingDetails(caseData));
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData));
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called for the second page of the Reply Referral event.
     * Populates the Referral hearing and reply detail's section on the page.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/initHearingAndReferralDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for reply to referral event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                        @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initHearingDetailsForUpdateReferral(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("INIT HEARING AND UPDATE REFERRAL DETAILS ---> "
                + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        List<String> errors = new ArrayList<>();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (ReferralHelper.isValidReferralStatus(caseData)) {
            ReferralHelper.populateUpdateReferralDetails(caseData);
            caseData.setHearingAndReferralDetails(ReferralHelper.populateHearingReferralDetails(caseData));
        } else {
            errors.add("Only referrals with status awaiting instructions can be updated.");
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Called at the end of updating a referral, takes the information saved in case data and stores it in the
     * update referral collection.
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
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitUpdateReferralDetails(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {
        log.info("ABOUT TO SUBMIT UPDATE REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if ("Party not responded/compiled".equals(caseData.getReferralSubject())) {
            caseData.setUpdateReferralSubject("Party not responded/complied");
        }
        UserDetails userDetails = userService.getUserDetails(userToken);
        ReferralHelper.updateReferral(
                caseData,
                String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()));
        ReferralType referral = caseData.getReferralCollection()
                .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();
        String referralNumber = String.valueOf(ReferralHelper.getNextReferralNumber(
                referral.getUpdateReferralCollection()));

        DocumentInfo documentInfo = referralService.generateCRDocument(caseData,
                userToken, ccdRequest.getCaseDetails().getCaseTypeId());

        referral.setReferralSummaryPdf(this.documentManagementService.addDocumentToDocumentField(documentInfo));
        String caseLink = emailService.getExuiCaseLink(ccdRequest.getCaseDetails().getCaseId());
        emailService.sendEmail(
                referralTemplateId,
                caseData.getUpdateReferentEmail(),
                ReferralHelper.buildPersonalisationUpdateReferral(
                        ccdRequest.getCaseDetails(),
                        referralNumber,
                        userDetails.getName(),
                        caseLink
                )
        );
        log.info("Event: Update Referral Email sent. "
                + ". EventId: " + ccdRequest.getEventId()
                + ". Update Referral number: " + referralNumber
                + ". Emailed at: " + DateTime.now());
        clearUpdateReferralDataFromCaseData(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called after submitting an update referral event.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeUpdateReferral", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the reply to referral event flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> completeUpdateReferral(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {
        log.info("COMPLETE UPDATE REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .build());
    }
}