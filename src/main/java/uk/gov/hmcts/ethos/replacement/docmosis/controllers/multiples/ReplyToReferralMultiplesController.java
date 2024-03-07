package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralReplyDataFromCaseData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for the ReplyToReferral event.
 */
@Slf4j
@RequestMapping("/multiples/replyReferral")
@RestController
@RequiredArgsConstructor
public class ReplyToReferralMultiplesController {
    private final VerifyTokenService verifyTokenService;
    private final UserIdamService userIdamService;
    private final ReferralService referralService;
    private final DocumentManagementService documentManagementService;
    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;
    private final CaseLookupService caseLookupService;

    @Value("${template.referral}")
    private String referralTemplateId;

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String REPLY_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>We have recorded your reply. You can view it in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";


    /**
     * Called for the first page of the Reply to Referral event.
     * Populates the Referral select dropdown.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
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
        @RequestBody MultipleRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {
        log.info("ABOUT TO START REPLY TO REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setIsJudge(ReferralHelper.isJudge(userIdamService.getUserDetails(userToken).getRoles()));
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection()));
        return multipleResponse(caseData, null);
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
                    schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> initHearingDetailsForReplyToReferral(
        @RequestBody MultipleRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) throws IOException {
        log.info("INIT HEARING AND REFERRAL DETAILS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var details = ccdRequest.getCaseDetails();
        MultipleData caseData = details.getCaseData();
        var leadCase = caseLookupService.getCaseDataAsAdmin(details.getCaseTypeId().replace(MULTIPLE, ""), caseData.getLeadCaseId());
        caseData.setHearingAndReferralDetails(ReferralHelper.populateHearingReferralDetails(caseData, leadCase));
        return multipleResponse(caseData, null);
    }

    /**
     * Called for the email validation of the Reply Referral event.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/validateReplyToEmail", consumes = APPLICATION_JSON_VALUE)
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
    public ResponseEntity<MultipleCallbackResponse> validateReplyToEmail(
        @RequestBody MultipleRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {
        log.info("VALIDATE REPLY TO EMAIL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        MultipleData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();

        if (StringUtils.isNotEmpty(caseData.getReplyToEmailAddress())) {
            errors = ReferralHelper.validateEmail(caseData.getReplyToEmailAddress());

            if (CollectionUtils.isNotEmpty(caseData.getReplyDocument())) {
                ReferralHelper.addDocumentUploadErrors(caseData.getReplyDocument(), errors);
            }
        }

        return multipleResponse(caseData, errors);
    }

    /**
     * Called at the end of Reply Referral event, takes the information saved in case data and stores it in the
     * referral reply collection.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     * @throws IOException 
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
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmitReferralReply(
        @RequestBody MultipleRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) throws IOException {
        log.info("ABOUT TO SUBMIT REPLY TO REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails caseDetails = ccdRequest.getCaseDetails();
        MultipleData caseData = caseDetails.getCaseData();
        UserDetails userDetails = userIdamService.getUserDetails(userToken);

        String referralCode = caseData.getSelectReferral().getValue().getCode();

        String name = String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName());
        ReferralHelper.createReferralReply(caseData, name, featureToggleService.isWorkAllocationEnabled());

        CaseData leadCase = caseLookupService.getCaseDataAsAdmin(caseDetails.getCaseTypeId().replace(MULTIPLE, ""), caseData.getLeadCaseId());
        DocumentInfo documentInfo = referralService.generateDocument(caseData, leadCase, userToken, caseDetails.getCaseTypeId());

        ReferralType referral = ReferralHelper.getSelectedReferral(caseData);

        referral.setReferralSummaryPdf(this.documentManagementService.addDocumentToDocumentField(documentInfo));
        String caseLink = emailService.getExuiCaseLink(caseDetails.getCaseId());

        if (StringUtils.isNotEmpty(caseData.getReplyToEmailAddress())) {
            emailService.sendEmail(
                    referralTemplateId,
                    caseData.getReplyToEmailAddress(),
                    ReferralHelper.buildPersonalisation(caseData, leadCase, referralCode, false, userDetails.getName(), caseLink)
            );

            log.info("Event: Referral Reply Email sent. "
                    + ". EventId: " + ccdRequest.getEventId()
                    + ". Referral code: " + referralCode
                    + ". Emailed at: " + DateTime.now());

        }

        clearReferralReplyDataFromCaseData(caseData);

        return multipleResponse(caseData, null);
    }

    /**
     * Called after submitting a reply to referral event.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeReplyToReferral", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the reply to referral event flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = MultipleCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<MultipleCallbackResponse> completeReplyToReferral(
        @RequestBody MultipleRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {
        log.info("COMPLETE REPLY TO REFERRAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String body = String.format(REPLY_REFERRAL_BODY,
            ccdRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
            .confirmation_body(body)
            .build());
    }
}