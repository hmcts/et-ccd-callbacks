package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CreateReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the Create Referral event pages, formats data appropriately for rendering on the front end.
 */
@Slf4j
@RequestMapping("/createReferral")
@RestController
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement", "PMD.ExcessiveImports"})
public class CreateReferralController {

    private final String referralTemplateId;
    private final VerifyTokenService verifyTokenService;
    private final EmailService emailService;
    private final UserService userService;
    private final CreateReferralService createReferralService;
    private final DocumentManagementService documentManagementService;

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String CREATE_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>Your referral has been sent. Replies and instructions will appear in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";

    public CreateReferralController(@Value("${referral.template.id}") String referralTemplateId,
                                    VerifyTokenService verifyTokenService,
                                    EmailService emailService, UserService userService,
                                    CreateReferralService createReferralService,
                                    DocumentManagementService documentManagementService) {
        this.referralTemplateId = referralTemplateId;
        this.emailService = emailService;
        this.verifyTokenService = verifyTokenService;
        this.userService = userService;
        this.createReferralService = createReferralService;
        this.documentManagementService = documentManagementService;
    }

    /**
     * Called for the first page of the Create Referral event.
     * Populates the Referral hearing detail's section on the page.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> initReferralHearingDetails(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setReferralHearingDetails(ReferralHelper.populateHearingDetails(caseData));
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called for the email validation of the Create Referral event.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/validateReferentEmail", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> validateReferentEmail(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = ReferralHelper.validateEmail(caseData.getReferentEmail());

        if (CollectionUtils.isNotEmpty(caseData.getReferralDocument())) {
            ReferralHelper.addDocumentUploadErrors(caseData.getReferralDocument(), errors);
        }

        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Called at the end of creating a referral, takes the information saved in case data and stores it in the
     * referral collection.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitReferralDetails(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if ("Party not responded/compiled".equals(caseData.getReferralSubject())) {
            caseData.setReferralSubject("Party not responded/complied");
        }
        UserDetails userDetails = userService.getUserDetails(userToken);
        String referralNumber = String.valueOf(ReferralHelper.getNextReferralNumber(caseData));
        emailService.sendEmail(
            referralTemplateId,
            caseData.getReferentEmail(),
            ReferralHelper.buildPersonalisation(
                ccdRequest.getCaseDetails(),
                referralNumber,
                true,
                userDetails.getName()
            )
        );

        log.info("Event: Referral Email sent. "
            + ". EventId: " + ccdRequest.getEventId()
            + ". Referral number: " + referralNumber
            + ". Emailed at: " + DateTime.now());

        caseData.setReferredBy(String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()));
        DocumentInfo documentInfo = createReferralService.generateCRDocument(caseData,
            userToken, ccdRequest.getCaseDetails().getCaseTypeId());

        ReferralHelper.createReferral(
            caseData,
            String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()),
            this.documentManagementService.addDocumentToDocumentField(documentInfo));

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called after submitting a create referral event.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeCreateReferral", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> completeCreateReferral(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String body = String.format(CREATE_REFERRAL_BODY,
            ccdRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .confirmation_body(body)
            .build());
    }
}